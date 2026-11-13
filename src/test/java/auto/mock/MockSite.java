package auto.mock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地实习系统（龙财实习系统），完整实现三条流程：
 * 注册 → 登录 → 学生首页 → 申请实习（两步）→ 申请列表。
 *
 * <p>端口随机、进程内嵌、不依赖外网。用户表与申请记录存在内存里，
 * 同一个 JVM 内所有测试类共享一份，所以「注册的账号」能在后面的用例里登录。
 *
 * <p>业务规则按真实系统写，不做为了让用例变绿的特殊分支：
 * 用户名 3-30 位且唯一、密码至少 6 位且两次一致、真实姓名必填；
 * 登录校验用户名/密码/角色三者；申请校验公司与岗位、日期格式与先后顺序。
 */
public class MockSite {

    /** 可选实习公司。 */
    static final List<String> COMPANIES = List.of(
            "龙财科技有限公司",
            "哈尔滨数据服务有限公司",
            "黑龙江软件园发展有限公司",
            "龙财会计师事务所");

    /** 可选实习岗位。 */
    static final List<String> POSITIONS = List.of(
            "Java 开发实习生",
            "前端开发实习生",
            "数据分析实习生",
            "测试工程师实习生",
            "会计助理");

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static volatile MockSite instance;

    private final HttpServer server;
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final List<Apply> applies = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    private MockSite() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/login", this::login);
        server.createContext("/logout", this::logout);
        server.createContext("/register", this::register);
        server.createContext("/student/dashboard", this::dashboard);
        server.createContext("/student/apply-list", this::applyList);
        server.createContext("/student/apply/step2", this::applyStep2);
        server.createContext("/student/apply/submit", this::applySubmit);
        server.createContext("/student/apply", this::applyStep1);
        // 默认单线程处理所有请求，压测时会把排队时间算进响应时间。
        // 按 Tomcat 默认的 maxThreads=200 配线程池，测出来的才是接口本身的耗时。
        server.setExecutor(Executors.newFixedThreadPool(200));
        server.start();

        seed();
    }

    /** 全局单例：所有测试类共用一个站点和一份用户数据。 */
    public static MockSite getInstance() {
        if (instance == null) {
            synchronized (MockSite.class) {
                if (instance == null) {
                    try {
                        instance = new MockSite();
                    } catch (IOException e) {
                        throw new IllegalStateException("启动本地实习系统失败", e);
                    }
                }
            }
        }
        return instance;
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public String url(String path) {
        return baseUrl() + path;
    }

    public void stop() {
        server.stop(0);
        instance = null;
    }

    /** 预置演示账号，和参考前端页面上写的一致。 */
    private void seed() {
        users.put("admin", new User("admin", "123456", "系统管理员", "admin", "", ""));
        users.put("teacher01", new User("teacher01", "123456", "张建国", "teacher", "13900000001", ""));
        users.put("20230001", new User("20230001", "123456", "李明", "student", "13900000002", ""));
    }

    // ================= 登录 =================
    private void login(HttpExchange ex) throws IOException {
        if (isGet(ex)) {
            String registered = query(ex, "registered");
            if (registered == null || registered.isEmpty()) {
                send(ex, 200, MockPages.login("", "", ""));
            } else {
                // 注册成功后 302 回这里，提示 + 用户名预填
                send(ex, 200, MockPages.login("", MockPages.ok("注册成功，请登录"), registered));
            }
            return;
        }
        Map<String, String> f = form(ex);
        String username = trimmed(f, "username");
        String password = f.getOrDefault("password", "");
        String role = f.getOrDefault("role", "student");

        User user = users.get(username);
        if (user == null || !user.password().equals(password)) {
            send(ex, 200, MockPages.login(MockPages.err("用户名或密码错误"), "", username));
            return;
        }
        if (!user.role().equals(role)) {
            send(ex, 200, MockPages.login(MockPages.err("角色不匹配，请选择正确的身份"), "", username));
            return;
        }

        String sid = UUID.randomUUID().toString();
        sessions.put(sid, username);
        ex.getResponseHeaders().add("Set-Cookie", "SID=" + sid + "; Path=/");
        redirect(ex, "/student/dashboard");
    }

    private void logout(HttpExchange ex) throws IOException {
        String sid = cookie(ex, "SID");
        if (sid != null) {
            sessions.remove(sid);
        }
        redirect(ex, "/login");
    }

    // ================= 注册 =================
    private void register(HttpExchange ex) throws IOException {
        if (isGet(ex)) {
            send(ex, 200, MockPages.register("", Map.of()));
            return;
        }
        Map<String, String> f = form(ex);
        String username = trimmed(f, "username");
        String realName = trimmed(f, "realName");
        String password = f.getOrDefault("password", "");
        String confirm = f.getOrDefault("confirm", "");
        String role = f.getOrDefault("role", "student");

        String error = validateRegister(username, realName, password, confirm);
        if (error == null && users.containsKey(username)) {
            error = "用户名已存在";
        }
        if (error != null) {
            send(ex, 200, MockPages.register(MockPages.err(error), f));
            return;
        }

        users.put(username, new User(username, password, realName, role,
                trimmed(f, "phone"), trimmed(f, "email")));
        // 注册成功后真实跳转到登录页（302），URL 会变，和参考项目 router.push('/login') 行为一致
        redirect(ex, "/login?registered=" + URLEncoder.encode(username, StandardCharsets.UTF_8));
    }

    private static String validateRegister(String username, String realName, String password, String confirm) {
        if (username.isEmpty()) {
            return "请输入用户名";
        }
        if (username.length() < 3 || username.length() > 30) {
            return "用户名长度需在 3 到 30 个字符";
        }
        if (realName.isEmpty()) {
            return "请输入真实姓名";
        }
        if (password.length() < 6) {
            return "密码至少 6 位";
        }
        if (!password.equals(confirm)) {
            return "两次密码输入不一致";
        }
        return null;
    }

    // ================= 学生首页 =================
    private void dashboard(HttpExchange ex) throws IOException {
        User user = currentUser(ex);
        if (user == null) {
            redirect(ex, "/login");
            return;
        }
        List<Apply> mine = appliesOf(user.username());
        String latest = mine.isEmpty() ? "暂无" : mine.get(mine.size() - 1).status();
        send(ex, 200, MockPages.dashboard(user, mine.size(), latest));
    }

    // ================= 申请第一步 =================
    private void applyStep1(HttpExchange ex) throws IOException {
        User user = currentUser(ex);
        if (user == null) {
            redirect(ex, "/login");
            return;
        }
        send(ex, 200, MockPages.applyStep1(user, "", Map.of()));
    }

    // ================= 申请第二步 =================
    private void applyStep2(HttpExchange ex) throws IOException {
        User user = currentUser(ex);
        if (user == null) {
            redirect(ex, "/login");
            return;
        }
        Map<String, String> f = form(ex);
        String error = validateStep1(f);
        if (error != null) {
            send(ex, 200, MockPages.applyStep1(user, MockPages.err(error), f));
            return;
        }
        send(ex, 200, MockPages.applyStep2(user, "", f));
    }

    // ================= 提交申请 =================
    private void applySubmit(HttpExchange ex) throws IOException {
        User user = currentUser(ex);
        if (user == null) {
            redirect(ex, "/login");
            return;
        }
        Map<String, String> f = form(ex);

        String error = validateStep1(f);
        if (error == null && trimmed(f, "duty").isEmpty()) {
            error = "请填写主要工作内容";
        }
        if (error == null && trimmed(f, "intro").isEmpty()) {
            error = "请填写个人简介";
        }
        if (error != null) {
            send(ex, 200, MockPages.applyStep2(user, MockPages.err(error), f));
            return;
        }

        String no = "SQ" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%03d", seq.incrementAndGet());
        applies.add(new Apply(no, user.username(),
                trimmed(f, "company"), trimmed(f, "position"), trimmed(f, "dept"),
                trimmed(f, "startDate"), trimmed(f, "endDate"),
                trimmed(f, "mentorName"), trimmed(f, "mentorPhone"), trimmed(f, "location"),
                trimmed(f, "salary"), trimmed(f, "duty"), trimmed(f, "intro"),
                "待审批", LocalDateTime.now().format(TS)));
        redirect(ex, "/student/apply-list?no=" + no);
    }

    private static String validateStep1(Map<String, String> f) {
        if (trimmed(f, "company").isEmpty()) {
            return "请选择实习公司";
        }
        if (trimmed(f, "position").isEmpty()) {
            return "请选择实习岗位";
        }
        String start = trimmed(f, "startDate");
        String end = trimmed(f, "endDate");
        if (start.isEmpty() || end.isEmpty()) {
            return "请选择实习时间";
        }
        try {
            LocalDate s = LocalDate.parse(start, DAY);
            LocalDate e = LocalDate.parse(end, DAY);
            if (e.isBefore(s)) {
                return "结束时间不能早于开始时间";
            }
        } catch (DateTimeParseException ex) {
            return "日期格式应为 yyyy-MM-dd";
        }
        return null;
    }

    // ================= 申请列表 =================
    private void applyList(HttpExchange ex) throws IOException {
        User user = currentUser(ex);
        if (user == null) {
            redirect(ex, "/login");
            return;
        }
        send(ex, 200, MockPages.applyList(user, appliesOf(user.username()), query(ex, "no")));
    }

    // ================= 辅助 =================
    private List<Apply> appliesOf(String username) {
        List<Apply> mine = new ArrayList<>();
        for (Apply a : applies) {
            if (a.username().equals(username)) {
                mine.add(a);
            }
        }
        return mine;
    }

    private User currentUser(HttpExchange ex) {
        String sid = cookie(ex, "SID");
        if (sid == null) {
            return null;
        }
        String username = sessions.get(sid);
        return username == null ? null : users.get(username);
    }

    private static boolean isGet(HttpExchange ex) {
        return "GET".equalsIgnoreCase(ex.getRequestMethod());
    }

    private static String trimmed(Map<String, String> f, String key) {
        return f.getOrDefault(key, "").trim();
    }

    private static String cookie(HttpExchange ex, String name) {
        String raw = ex.getRequestHeaders().getFirst("Cookie");
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        for (String part : raw.split(";")) {
            String p = part.trim();
            if (p.startsWith(name + "=")) {
                return p.substring(name.length() + 1);
            }
        }
        return null;
    }

    private static String query(HttpExchange ex, String name) {
        String q = ex.getRequestURI().getQuery();
        if (q == null || q.isEmpty()) {
            return null;
        }
        for (String kv : q.split("&")) {
            String[] p = kv.split("=", 2);
            if (p[0].equals(name)) {
                return p.length > 1 ? decode(p[1]) : "";
            }
        }
        return null;
    }

    private static Map<String, String> form(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new LinkedHashMap<>();
        for (String kv : body.split("&")) {
            if (kv.isBlank()) {
                continue;
            }
            String[] p = kv.split("=", 2);
            map.put(decode(p[0]), p.length > 1 ? decode(p[1]) : "");
        }
        return map;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange ex, int code, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }
}

/** 用户账号。 */
record User(String username, String password, String realName, String role, String phone, String email) {

    String roleLabel() {
        return switch (role) {
            case "teacher" -> "指导教师";
            case "admin" -> "管理员";
            default -> "学生";
        };
    }
}

/** 实习申请记录。 */
record Apply(String no, String username, String company, String position, String dept,
             String start, String end, String mentor, String phone, String location,
             String salary, String duty, String intro, String status, String createdAt) {
}
