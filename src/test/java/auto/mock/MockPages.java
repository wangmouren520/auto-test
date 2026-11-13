package auto.mock;

import java.util.List;
import java.util.Map;

/**
 * 实习系统的页面模板。视觉参考 trainfrontback（Vue3 + Element Plus）：
 * 渐变背景 #667eea → #764ba2、白色卡片、主色 #409eff、圆角 4px / 10px。
 *
 * <p>元素 id 是对外契约，用例靠它定位，改样式不要改 id。
 */
final class MockPages {

    private MockPages() {
    }

    // ---------------- 登录 ----------------
    static String login(String error, String msg, String username) {
        String body = """
                <div class="auth-wrap">
                  <div class="auth-box">
                    <div class="auth-title">
                      <h1>龙财实习系统</h1>
                      <p>Longcai Internship Management System</p>
                    </div>
                    {{0}}{{1}}
                    <form method="post" action="/login">
                      <div class="field">
                        <input id="username" name="username" type="text" placeholder="请输入用户名/学号" value="{{2}}" autocomplete="off">
                      </div>
                      <div class="field">
                        <input id="password" name="password" type="password" placeholder="请输入密码">
                      </div>
                      <div class="field">
                        <div class="role-group">
                          <label><input type="radio" id="role-student" name="role" value="student" checked><span>学生</span></label>
                          <label><input type="radio" id="role-teacher" name="role" value="teacher"><span>指导教师</span></label>
                          <label><input type="radio" id="role-admin" name="role" value="admin"><span>管理员</span></label>
                        </div>
                      </div>
                      <button id="submit" class="btn" type="submit">登 录</button>
                    </form>
                    <div class="demo-tips">演示账号：admin / teacher01 / 20230001&nbsp;&nbsp;&nbsp;密码：123456</div>
                    <div class="auth-foot">还没有账号？<a id="to-register" href="/register">立即注册</a></div>
                  </div>
                </div>
                """;
        return shell("登录 - 龙财实习系统", fill(body, error, msg, esc(username)));
    }

    // ---------------- 注册 ----------------
    static String register(String error, Map<String, String> v) {
        String body = """
                <div class="auth-wrap">
                  <div class="auth-box wide">
                    <div class="auth-title">
                      <h1>注册新账号</h1>
                      <p>龙财实习管理系统</p>
                    </div>
                    {{0}}
                    <form method="post" action="/register">
                      <div class="field">
                        <label>角色</label>
                        <div class="role-group">
                          <label><input type="radio" id="role-student" name="role" value="student" checked><span>学生</span></label>
                          <label><input type="radio" id="role-teacher" name="role" value="teacher"><span>指导教师</span></label>
                        </div>
                      </div>
                      <div class="row">
                        <div class="field">
                          <label class="req">用户名</label>
                          <input id="username" name="username" type="text" placeholder="3-30 位字符" value="{{1}}" autocomplete="off">
                        </div>
                        <div class="field">
                          <label class="req">真实姓名</label>
                          <input id="realName" name="realName" type="text" placeholder="请输入真实姓名" value="{{2}}">
                        </div>
                      </div>
                      <div class="row">
                        <div class="field">
                          <label class="req">密码</label>
                          <input id="password" name="password" type="password" placeholder="至少 6 位">
                        </div>
                        <div class="field">
                          <label class="req">确认密码</label>
                          <input id="confirm" name="confirm" type="password" placeholder="再次输入密码">
                        </div>
                      </div>
                      <div class="row">
                        <div class="field">
                          <label>手机号</label>
                          <input id="phone" name="phone" type="text" placeholder="选填" value="{{3}}">
                        </div>
                        <div class="field">
                          <label>邮箱</label>
                          <input id="email" name="email" type="text" placeholder="选填" value="{{4}}">
                        </div>
                      </div>
                      <button id="submit" class="btn" type="submit">注 册</button>
                    </form>
                    <div class="auth-foot">已有账号？<a id="to-login" href="/login">返回登录</a></div>
                  </div>
                </div>
                """;
        return shell("注册 - 龙财实习系统", fill(body, error,
                esc(v.get("username")), esc(v.get("realName")),
                esc(v.get("phone")), esc(v.get("email"))));
    }

    // ---------------- 学生首页 ----------------
    static String dashboard(User user, int applyCount, String latestStatus) {
        String body = nav(user) + """
                <div class="page">
                  <h2 class="page-title" id="welcome">登录成功</h2>
                  <p class="page-sub" id="user-line">欢迎回来，{{0}}（{{1}}），当前身份：{{2}}</p>
                  <div class="stat-row">
                    <div class="stat">
                      <div class="stat-num" id="apply-count">{{3}}</div>
                      <div class="stat-label">已提交申请</div>
                    </div>
                    <div class="stat">
                      <div class="stat-num small" id="latest-status">{{4}}</div>
                      <div class="stat-label">最新申请状态</div>
                    </div>
                  </div>
                  <div class="card">
                    <h3 class="card-title">实习申请</h3>
                    <p class="card-text">实习申请需填写实习公司与岗位、实习时间、企业导师等信息，提交后由指导教师审批。</p>
                    <div class="actions left">
                      <a class="btn auto" id="to-apply" href="/student/apply">申请实习</a>
                      <a class="btn plain auto" id="to-list" href="/student/apply-list">我的申请</a>
                    </div>
                  </div>
                </div>
                """;
        return shell("首页 - 龙财实习系统", fill(body,
                esc(user.realName()), esc(user.username()), esc(user.roleLabel()),
                String.valueOf(applyCount), esc(latestStatus)));
    }

    // ---------------- 申请第一步 ----------------
    static String applyStep1(User user, String error, Map<String, String> v) {
        String body = nav(user) + """
                <div class="page">
                  <h2 class="page-title">申请实习</h2>
                  <p class="page-sub">请如实填写实习信息，提交后由指导教师审批</p>
                  <div class="card">
                    {{0}}
                    {{1}}
                    <form method="post" action="/student/apply/step2">
                      <div class="row">
                        <div class="field">
                          <label class="req">实习公司</label>
                          <select id="company" name="company">{{2}}</select>
                        </div>
                        <div class="field">
                          <label class="req">实习岗位</label>
                          <select id="position" name="position">{{3}}</select>
                        </div>
                      </div>
                      <div class="row">
                        <div class="field">
                          <label>所在部门</label>
                          <input id="dept" name="dept" type="text" placeholder="如：技术研发部" value="{{4}}">
                        </div>
                        <div class="field">
                          <label>实习补贴</label>
                          <input id="salary" name="salary" type="text" placeholder="如：3000元/月" value="{{5}}">
                        </div>
                      </div>
                      <div class="row">
                        <div class="field">
                          <label class="req">实习开始时间</label>
                          <input id="startDate" name="startDate" type="text" placeholder="如 2026-11-10" value="{{6}}">
                        </div>
                        <div class="field">
                          <label class="req">实习结束时间</label>
                          <input id="endDate" name="endDate" type="text" placeholder="如 2027-05-10" value="{{7}}">
                        </div>
                      </div>
                      <div class="actions">
                        <button id="next" class="btn auto" type="submit">下一步</button>
                      </div>
                    </form>
                  </div>
                </div>
                """;
        return shell("申请实习 - 龙财实习系统", fill(body,
                steps(1), error,
                options(MockSite.COMPANIES, v.get("company")),
                options(MockSite.POSITIONS, v.get("position")),
                esc(v.get("dept")), esc(v.get("salary")),
                esc(v.get("startDate")), esc(v.get("endDate"))));
    }

    // ---------------- 申请第二步 ----------------
    static String applyStep2(User user, String error, Map<String, String> v) {
        String body = nav(user) + """
                <div class="page">
                  <h2 class="page-title">申请实习</h2>
                  <p class="page-sub">第二步：填写实习详情</p>
                  <div class="card">
                    {{0}}
                    {{1}}
                    <table class="info">
                      <tr>
                        <th>实习公司</th><td id="sum-company">{{2}}</td>
                        <th>实习岗位</th><td id="sum-position">{{3}}</td>
                      </tr>
                      <tr>
                        <th>实习时间</th><td colspan="3" id="sum-date">{{4}} 至 {{5}}</td>
                      </tr>
                    </table>
                    <form method="post" action="/student/apply/submit">
                      <input type="hidden" name="company" value="{{6}}">
                      <input type="hidden" name="position" value="{{7}}">
                      <input type="hidden" name="dept" value="{{8}}">
                      <input type="hidden" name="salary" value="{{9}}">
                      <input type="hidden" name="startDate" value="{{10}}">
                      <input type="hidden" name="endDate" value="{{11}}">
                      <div class="row">
                        <div class="field">
                          <label>公司导师</label>
                          <input id="mentorName" name="mentorName" type="text" placeholder="实习单位指导教师">
                        </div>
                        <div class="field">
                          <label>导师职称</label>
                          <input id="mentorTitle" name="mentorTitle" type="text" placeholder="如：技术总监">
                        </div>
                      </div>
                      <div class="row">
                        <div class="field">
                          <label>导师电话</label>
                          <input id="mentorPhone" name="mentorPhone" type="text" placeholder="选填">
                        </div>
                        <div class="field">
                          <label>工作地点</label>
                          <input id="location" name="location" type="text" placeholder="实习具体地址">
                        </div>
                      </div>
                      <div class="field">
                        <label>福利待遇</label>
                        <div class="checks">
                          <label><input type="checkbox" id="insurance" name="insurance" value="1"> 五险一金</label>
                          <label><input type="checkbox" id="apartment" name="apartment" value="1"> 提供住宿</label>
                        </div>
                      </div>
                      <div class="field">
                        <label class="req">主要工作内容</label>
                        <textarea id="duty" name="duty" rows="3" placeholder="请描述实习期间的主要职责"></textarea>
                      </div>
                      <div class="field">
                        <label class="req">个人简介</label>
                        <textarea id="intro" name="intro" rows="4" placeholder="请简要介绍自己及实习意向"></textarea>
                      </div>
                      <div class="actions">
                        <a class="btn plain auto" href="/student/apply">上一步</a>
                        <button id="submit" class="btn success auto" type="submit">提交申请</button>
                      </div>
                    </form>
                  </div>
                </div>
                """;
        return shell("填写实习信息 - 龙财实习系统", fill(body,
                steps(2), error,
                esc(v.get("company")), esc(v.get("position")),
                esc(v.get("startDate")), esc(v.get("endDate")),
                esc(v.get("company")), esc(v.get("position")), esc(v.get("dept")),
                esc(v.get("salary")), esc(v.get("startDate")), esc(v.get("endDate"))));
    }

    // ---------------- 申请列表 ----------------
    static String applyList(User user, List<Apply> list, String justNo) {
        StringBuilder rows = new StringBuilder();
        if (list.isEmpty()) {
            rows.append("<tr><td colspan=\"6\" class=\"empty\">暂无申请记录</td></tr>");
        } else {
            for (int i = list.size() - 1; i >= 0; i--) {
                Apply a = list.get(i);
                boolean newest = i == list.size() - 1;
                rows.append("<tr>")
                        .append("<td").append(newest ? " id=\"apply-no\"" : "").append(">").append(esc(a.no())).append("</td>")
                        .append("<td>").append(esc(a.company())).append("</td>")
                        .append("<td>").append(esc(a.position())).append("</td>")
                        .append("<td>").append(esc(a.start())).append(" 至 ").append(esc(a.end())).append("</td>")
                        .append("<td").append(newest ? " id=\"apply-status\"" : "").append("><span class=\"tag\">")
                        .append(esc(a.status())).append("</span></td>")
                        .append("<td class=\"muted\">").append(esc(a.createdAt())).append("</td>")
                        .append("</tr>");
            }
        }
        String body = nav(user) + """
                <div class="page">
                  <h2 class="page-title">我的实习申请</h2>
                  <p class="page-sub">提交后可在此查看审批状态</p>
                  {{0}}
                  <div class="card">
                    <table class="info list">
                      <thead>
                        <tr><th>申请编号</th><th>公司</th><th>岗位</th><th>实习时间</th><th>状态</th><th>提交时间</th></tr>
                      </thead>
                      <tbody>{{1}}</tbody>
                    </table>
                  </div>
                  <div class="actions left">
                    <a class="btn plain auto" href="/student/dashboard">返回首页</a>
                  </div>
                </div>
                """;
        return shell("我的申请 - 龙财实习系统",
                fill(body, justNo == null ? "" : ok("实习申请已提交，编号：" + justNo), rows.toString()));
    }

    // ---------------- 片段 ----------------
    static String err(String text) {
        return "<div id=\"error\" class=\"alert error\">" + esc(text) + "</div>";
    }

    static String ok(String text) {
        return "<div id=\"msg\" class=\"alert ok\">" + esc(text) + "</div>";
    }

    private static String nav(User user) {
        String tpl = """
                <div class="nav">
                  <div class="logo"><span class="badge">龙</span>龙财实习系统</div>
                  <div class="right">
                    <span id="nav-user">{{0}}（{{1}}）</span>
                    <a id="logout" href="/logout">退出</a>
                  </div>
                </div>
                """;
        return fill(tpl, esc(user.realName()), esc(user.roleLabel()));
    }

    private static String steps(int active) {
        String tpl = """
                <div class="steps">
                  <div class="step {{0}}"><span class="num">1</span>选择公司与岗位</div>
                  <div class="step-line"></div>
                  <div class="step {{1}}"><span class="num">2</span>填写实习信息</div>
                  <div class="step-line"></div>
                  <div class="step"><span class="num">3</span>提交审批</div>
                </div>
                """;
        return fill(tpl, active > 1 ? "done" : "active", active > 1 ? "active" : "");
    }

    private static String options(List<String> items, String current) {
        StringBuilder sb = new StringBuilder("<option value=\"\">请选择</option>");
        for (String it : items) {
            sb.append("<option value=\"").append(esc(it)).append("\"")
                    .append(it.equals(current) ? " selected" : "")
                    .append(">").append(esc(it)).append("</option>");
        }
        return sb.toString();
    }

    // ---------------- 骨架 ----------------
    private static String shell(String title, String body) {
        return "<!doctype html>\n<html lang=\"zh-CN\">\n<head>\n"
                + "<meta charset=\"utf-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n"
                + "<title>" + esc(title) + "</title>\n"
                + "<style>" + CSS + "</style>\n"
                + "</head>\n<body>" + body + "</body>\n</html>";
    }

    /** 顺序填充 {@code {{0}}}、{@code {{1}}}… 占位符。 */
    static String fill(String template, Object... values) {
        String out = template;
        for (int i = 0; i < values.length; i++) {
            out = out.replace("{{" + i + "}}", String.valueOf(values[i]));
        }
        return out;
    }

    /** HTML 转义，防止用户输入破坏页面结构。 */
    static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final String CSS = """
            *{box-sizing:border-box;margin:0;padding:0}
            body{font-family:"Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei",Arial,sans-serif;
                 color:#303133;background:#f5f7fa;-webkit-font-smoothing:antialiased}
            a{color:#409eff;text-decoration:none}

            /* ---------- 登录 / 注册 ---------- */
            .auth-wrap{min-height:100vh;display:flex;align-items:center;justify-content:center;
                       background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:24px}
            .auth-box{width:420px;background:#fff;border-radius:10px;padding:40px;
                      box-shadow:0 10px 40px rgba(0,0,0,.15)}
            .auth-box.wide{width:520px}
            .auth-title{text-align:center;margin-bottom:30px}
            .auth-title h1{font-size:26px;color:#303133;margin-bottom:6px;font-weight:600;letter-spacing:1px}
            .auth-title p{font-size:12px;color:#909399}
            .demo-tips{text-align:center;font-size:12px;color:#909399;margin-top:14px}
            .auth-foot{text-align:center;margin-top:12px;font-size:13px;color:#606266}
            .auth-foot a:hover{text-decoration:underline}

            /* ---------- 表单 ---------- */
            .field{margin-bottom:18px;flex:1}
            .field label{display:block;font-size:13px;color:#606266;margin-bottom:6px}
            .field label.req:before{content:"*";color:#f56c6c;margin-right:4px}
            input[type=text],input[type=password],select,textarea{
              width:100%;height:40px;padding:0 15px;font-size:14px;color:#606266;background:#fff;
              border:1px solid #dcdfe6;border-radius:4px;outline:none;font-family:inherit;
              transition:border-color .2s}
            textarea{height:auto;padding:8px 15px;line-height:1.6;resize:vertical}
            input::placeholder,textarea::placeholder{color:#c0c4cc}
            input:focus,select:focus,textarea:focus{border-color:#409eff}
            select{cursor:pointer}
            .row{display:flex;gap:16px}
            .checks{display:flex;gap:24px;font-size:14px;color:#606266;padding-top:6px}
            .checks label{display:flex;align-items:center;gap:6px;cursor:pointer;margin:0}

            /* ---------- 按钮 ---------- */
            .btn{display:block;width:100%;height:40px;line-height:40px;text-align:center;
                 background:#409eff;color:#fff;border:0;border-radius:4px;font-size:14px;
                 font-family:inherit;cursor:pointer;transition:background .2s,color .2s,border-color .2s;
                 text-decoration:none}
            .btn:hover{background:#66b1ff}
            .btn.plain{background:#fff;color:#606266;border:1px solid #dcdfe6}
            .btn.plain:hover{color:#409eff;border-color:#c6e2ff;background:#ecf5ff}
            .btn.success{background:#67c23a}
            .btn.success:hover{background:#85ce61}
            .btn.auto{display:inline-block;width:auto;padding:0 26px}

            /* ---------- 角色单选 ---------- */
            .role-group{display:flex;width:100%}
            .role-group label{flex:1;position:relative;cursor:pointer}
            .role-group input{position:absolute;opacity:0;inset:0;cursor:pointer;margin:0}
            .role-group span{display:block;height:40px;line-height:40px;text-align:center;font-size:14px;
                             color:#606266;background:#fff;border:1px solid #dcdfe6;border-left:0}
            .role-group label:first-child span{border-left:1px solid #dcdfe6;border-radius:4px 0 0 4px}
            .role-group label:last-child span{border-radius:0 4px 4px 0}
            .role-group input:checked+span{background:#409eff;color:#fff;border-color:#409eff}

            /* ---------- 提示 ---------- */
            .alert{padding:10px 14px;border-radius:4px;font-size:13px;margin-bottom:18px;line-height:1.5}
            .alert.error{background:#fef0f0;color:#f56c6c;border:1px solid #fde2e2}
            .alert.ok{background:#f0f9eb;color:#67c23a;border:1px solid #e1f3d8}

            /* ---------- 顶栏 ---------- */
            .nav{height:60px;background:#fff;border-bottom:1px solid #e4e7ed;display:flex;
                 align-items:center;justify-content:space-between;padding:0 32px;
                 box-shadow:0 1px 4px rgba(0,0,0,.04)}
            .nav .logo{display:flex;align-items:center;gap:10px;font-size:17px;font-weight:600;color:#303133}
            .nav .badge{width:32px;height:32px;border-radius:8px;color:#fff;display:flex;
                        align-items:center;justify-content:center;font-size:15px;font-weight:700;
                        background:linear-gradient(135deg,#667eea,#764ba2)}
            .nav .right{display:flex;align-items:center;gap:18px;font-size:14px;color:#606266}
            .nav .right a{font-size:13px}

            /* ---------- 主体 ---------- */
            .page{max-width:960px;margin:24px auto 60px;padding:0 24px}
            .page-title{font-size:20px;color:#303133;margin-bottom:4px;font-weight:600}
            .page-sub{font-size:13px;color:#909399;margin-bottom:20px}
            .card{background:#fff;border-radius:8px;padding:28px 32px;border:1px solid #ebeef5;
                  box-shadow:0 1px 3px rgba(0,0,0,.03)}
            .card-title{font-size:16px;color:#303133;margin-bottom:8px;font-weight:600}
            .card-text{font-size:13px;color:#909399;line-height:1.7;margin-bottom:20px}
            .actions{margin-top:26px;display:flex;gap:12px;justify-content:center}
            .actions.left{justify-content:flex-start}

            /* ---------- 统计 ---------- */
            .stat-row{display:flex;gap:16px;margin-bottom:20px;align-items:stretch}
            .stat{flex:1;background:#fff;border:1px solid #ebeef5;border-radius:8px;padding:20px 24px;
                  box-shadow:0 1px 3px rgba(0,0,0,.03);display:flex;flex-direction:column;justify-content:center}
            .stat-num{font-size:28px;font-weight:600;color:#409eff;line-height:1.2}
            .stat-num.small{font-size:18px;color:#e6a23c}
            .stat-label{font-size:13px;color:#909399;margin-top:6px}

            /* ---------- 步骤条 ---------- */
            .steps{display:flex;align-items:center;margin-bottom:26px;padding-bottom:20px;
                   border-bottom:1px solid #f0f2f5}
            .step{display:flex;align-items:center;gap:8px;font-size:14px;color:#c0c4cc;white-space:nowrap}
            .step .num{width:24px;height:24px;border-radius:50%;border:1px solid #c0c4cc;
                       display:flex;align-items:center;justify-content:center;font-size:13px}
            .step.active{color:#409eff}
            .step.active .num{background:#409eff;border-color:#409eff;color:#fff}
            .step.done{color:#67c23a}
            .step.done .num{background:#67c23a;border-color:#67c23a;color:#fff}
            .step-line{flex:1;height:1px;background:#e4e7ed;margin:0 16px}

            /* ---------- 表格 ---------- */
            table.info{width:100%;border-collapse:collapse;font-size:13px;margin-bottom:20px}
            table.info th,table.info td{border:1px solid #ebeef5;padding:10px 14px;text-align:left;
                                        color:#606266;line-height:1.6}
            table.info th{background:#fafafa;color:#909399;font-weight:500;width:120px}
            table.info.list th{width:auto}
            table.info .empty{text-align:center;color:#c0c4cc;padding:36px 0}
            table.info .muted{color:#909399}
            .tag{display:inline-block;padding:2px 10px;border-radius:12px;font-size:12px;
                 color:#e6a23c;background:#fdf6ec;border:1px solid #faecd8}
            """;
}
