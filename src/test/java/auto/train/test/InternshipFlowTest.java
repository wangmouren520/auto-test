package auto.train.test;

import auto.AutoBaseTest;
import auto.Pace;
import auto.auth.LoginPage;
import auto.auth.RegisterPage;
import auto.train.InternshipApplyPage;
import auto.train.StudentHomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 实习流程测试：注册 → 用新账号登录 → 申请实习。
 *
 * <p>主链路用同一个浏览器会话贯穿三步，验证的是「注册的账号真的能登录，
 * 登录的学生真的能提交申请」这条完整业务，而不是三个孤立页面。
 */
public class InternshipFlowTest extends AutoBaseTest {

    /** 新注册账号用的密码。 */
    private static final String PASSWORD = "abc123456";
    /** 系统预置演示账号的密码。 */
    private static final String SEED_PASSWORD = "123456";

    @Test(description = "完整链路：注册新账号 → 用新账号登录 → 提交实习申请")
    public void test_register_login_apply() {
        String account = "stu" + (System.currentTimeMillis() % 1_000_000);
        String realName = "测试学生";
        String base = site.baseUrl();

        // ---------- ① 注册 ----------
        RegisterPage reg = new RegisterPage(driver);
        reg.open(base);
        reg.submit(account, realName, PASSWORD, PASSWORD, "13900001234", "stu@example.com");

        String msg = reg.message();
        Assert.assertTrue(msg.contains("注册成功"), "注册未成功，实际提示: " + msg);
        Assert.assertTrue(reg.currentUrl().contains("/login"), "注册后应回到登录页: " + reg.currentUrl());
        System.out.println("① 注册成功: " + account + " -> " + msg);

        // ---------- ② 用刚注册的账号登录 ----------
        LoginPage login = new LoginPage(driver);
        login.open(base);
        login.submit(account, PASSWORD);

        Assert.assertTrue(login.currentUrl().contains("/student/dashboard"),
                "登录后未进入学生首页，当前 URL: " + login.currentUrl());

        StudentHomePage home = new StudentHomePage(driver);
        Assert.assertEquals(home.welcome(), "登录成功", "首页未显示登录成功");
        Assert.assertTrue(home.userLine().contains(realName),
                "首页未显示注册时的真实姓名，实际: " + home.userLine());
        System.out.println("② 登录成功 -> " + login.currentUrl());

        // ---------- ③ 申请实习 ----------
        home.goApply();
        InternshipApplyPage apply = new InternshipApplyPage(driver);
        apply.fillStep1("龙财科技有限公司", "Java 开发实习生", "技术研发部", "2026-11-10", "2027-05-10");
        apply.next();
        apply.fillStep2("王建国", "技术总监", "0451-88886666", "哈尔滨市松北区学院路 1 号",
                "参与后端接口开发、联调与单元测试。",
                "本人熟悉 Java 与自动化测试，希望从事后端开发工作。");
        apply.submit();

        String no = apply.applyNo();
        Assert.assertNotNull(no, "未拿到申请编号");
        Assert.assertTrue(no.startsWith("SQ"), "申请编号格式异常: " + no);
        Assert.assertEquals(apply.applyStatus(), "待审批", "申请状态异常");
        System.out.println("③ 申请提交成功: " + no + " / " + apply.applyStatus());

        // ---------- ④ 回到首页应能看到申请数增加 ----------
        driver.get(site.url("/student/dashboard"));
        Pace.step();
        Assert.assertEquals(new StudentHomePage(driver).applyCount(), "1",
                "首页申请数应为 1");
    }

    @Test(description = "申请实习：未选公司直接下一步应被拦截")
    public void test_apply_requires_company() {
        loginAs("20230001");

        new StudentHomePage(driver).goApply();
        InternshipApplyPage apply = new InternshipApplyPage(driver);
        apply.fillStep1("", "Java 开发实习生", "", "2026-11-10", "2027-05-10");
        apply.next();

        String error = apply.error();
        Assert.assertTrue(error.contains("请选择实习公司"), "未拦截空公司，实际提示: " + error);
        System.out.println("[未选公司] 已拦截: " + error);
    }

    @Test(description = "申请实习：结束时间早于开始时间应被拦截")
    public void test_apply_date_reversed() {
        loginAs("20230001");

        new StudentHomePage(driver).goApply();
        InternshipApplyPage apply = new InternshipApplyPage(driver);
        apply.fillStep1("龙财科技有限公司", "Java 开发实习生", "技术研发部", "2027-05-10", "2026-11-10");
        apply.next();

        String error = apply.error();
        Assert.assertTrue(error.contains("结束时间"), "未拦截时间倒置，实际提示: " + error);
        System.out.println("[时间倒置] 已拦截: " + error);
    }

    @Test(description = "未登录直接访问申请页应跳转登录")
    public void test_apply_requires_login() {
        driver.get(site.url("/student/apply"));
        Pace.step();

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/login"), "未登录竟能进入申请页: " + url);
        System.out.println("[未登录] 已跳转: " + url);
    }

    private void loginAs(String username) {
        LoginPage login = new LoginPage(driver);
        login.open(site.baseUrl());
        login.submit(username, SEED_PASSWORD);
        Assert.assertTrue(login.currentUrl().contains("/student/dashboard"),
                "登录失败，当前 URL: " + login.currentUrl());
    }
}
