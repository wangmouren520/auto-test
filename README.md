# 龙财实习系统 · 自动化测试

自动打开 Firefox → 跑「注册 → 登录 → 申请实习」完整业务链路 → 自动校验结果。
双击 `运行测试.bat` 就跑，不用配任何东西。

被测系统是本地内嵌的实习系统（JDK HttpServer，随机端口），样式参考
`D:\zhowu\trainfrontback`（Vue3 + Element Plus）：渐变 `#667eea → #764ba2`、
白卡、主色 `#409eff`。浏览器、点击、页面跳转全是真的，只是服务端跑在本地。

## 目录

```
auto-test
├── 运行测试.bat                       双击运行
├── pom.xml                           Selenium + TestNG，没别的依赖
├── drivers\geckodriver.exe           Firefox 驱动
├── preview\                          各页面截图
└── src\test
    ├── java\auto
    │   ├── AutoBaseTest.java         基类：开浏览器 + 起实习系统（各包共用）
    │   ├── Pace.java                 操作节奏：慢放 / 逐字输入
    │   ├── auth                      注册 / 登录
    │   │   ├── LoginPage.java
    │   │   ├── RegisterPage.java
    │   │   └── test
    │   │       ├── LoginTest.java
    │   │       └── RegisterTest.java
    │   ├── train                     实习申请
    │   │   ├── StudentHomePage.java      学生首页
    │   │   ├── InternshipApplyPage.java  申请表单（两步）
    │   │   └── test
    │   │       └── InternshipFlowTest.java
    │   ├── baidu                     百度搜索（外网，独立）
    │   │   ├── BaiduPage.java
    │   │   └── test
    │   │       └── BaiduSearchTest.java
    │   └── mock
    │       ├── MockSite.java         实习系统：路由 + 用户表 + 申请记录
    │       └── MockPages.java        页面模板 + 全部 CSS
    └── resources\testng.xml
```

约定：每个业务包 = `包名` 放 Page 对象 + `包名.test` 放用例。新加业务线照 `auth` 复制，再在 `testng.xml` 登记。

## 被测系统的业务

```
/register  注册（用户名 3-30 位唯一、真实姓名、密码 ≥6 且两次一致）
   ↓ 302
/login     登录（用户名 + 密码 + 角色三者匹配）
   ↓
/student/dashboard  学生首页（欢迎语、申请数、最新状态）
   ↓
/student/apply      申请第一步：公司、岗位、部门、补贴、实习起止时间
   ↓ POST
/student/apply/step2    申请第二步：企业导师、职称、电话、地点、福利、工作内容、个人简介
   ↓ POST
/student/apply/submit   生成申请编号 SQyyyyMMddNNN，状态「待审批」
   ↓ 302
/student/apply-list     我的申请列表
```

预置演示账号（密码都是 `123456`）：`admin`（管理员）、`teacher01`（指导教师）、`20230001`（学生李明）。

## 怎么跑

**方式一**：双击 `运行测试.bat`

**方式二**：IDEA 里右键 `src\test\resources\testng.xml` → Run

**方式三**：命令行

```bash
mvn test                       # 弹浏览器，默认每步停 1 秒，看得清
mvn test -Dheadless=true       # 后台跑，不弹窗，慢放自动关闭
mvn test -Dslow=0              # 全速，约 60 秒跑完
mvn test -Dkeyword=自动化测试    # 换百度搜索词
```

只想跑实习系统、不联网（19 条里去掉百度 2 条）：把 `testng.xml` 里 `baidu` 那段 `<test>` 删掉。

## 慢放（看不清操作时用这个）

默认每步操作之间停 1 秒，输入框逐字输入，每条用例结束前多停 2 秒再关窗口。

`headless=true` 时慢放自动为 0，否则白白拖慢。

| 参数 | 效果 |
|---|---|
| 不传 | 每步 1 秒（默认） |
| `-Dslow=0` | 全速 |
| `-Dslow=2000` | 很慢，能看清每个点击 |
| `-Dslow=3000` | 讲解 / 录屏级别 |

控制台每条用例开头打印 `▶ 开始: test_register_invalid [ab, 张三, abc123456, ...]（每步停 1000ms）`，能对上浏览器里正在跑哪一组数据。

## 用例（19 条）

| 包 | 类 | 用例 | 断言 |
|---|---|---|---|
| auth | `RegisterTest` | 合法信息注册 | 提示"注册成功"，302 回 `/login` |
| auth | `RegisterTest` | 用户名太短 / 为空 / 缺姓名 / 密码 <6 / 两次不一致 / 用户名已存在（6 条数据驱动） | 各自的提示文案 |
| auth | `LoginTest` | 学生账号登录 | 进入 `/student/dashboard`，显示"登录成功" |
| auth | `LoginTest` | 教师账号登录 | 用教师身份也能进入系统 |
| auth | `LoginTest` | 密码错 / 用户不存在 / 全空（3 条） | "用户名或密码错误" |
| auth | `LoginTest` | 管理员用学生身份登录 | "角色不匹配，请选择正确的身份" |
| train | `InternshipFlowTest` | **注册 → 用新账号登录 → 申请实习** | 三步全通，拿到 `SQ` 编号、状态"待审批"、首页申请数变 1 |
| train | `InternshipFlowTest` | 未选公司直接下一步 | "请选择实习公司" |
| train | `InternshipFlowTest` | 结束时间早于开始时间 | "结束时间不能早于开始时间" |
| train | `InternshipFlowTest` | 未登录直接访问 `/student/apply` | 跳转 `/login` |
| baidu | `BaiduSearchTest` | 打开百度首页 / 搜索关键词 | 标题含"百度"、结果数 > 0 |

主链路是**一条用例贯穿三个页面**，验证的是"注册的账号真的能登录，登录的学生真的能提交申请"，不是三个孤立页面各自能打开。

## 界面

截图在 `preview\`：

| 文件 | 页面 |
|---|---|
| `1-login.png` | 登录（渐变背景 + 白卡 + 角色按钮组） |
| `2-register.png` | 注册（520px 宽卡 + 两列字段） |
| `3-register-ok.png` | 注册成功回登录页，绿色提示 + 用户名预填 |
| `4-dashboard.png` | 学生首页（顶栏 + 统计卡 + 申请入口） |
| `5-apply-step1.png` | 申请第一步（步骤条 + 公司与岗位） |
| `6-apply-step2.png` | 申请第二步（信息摘要 + 导师/内容/简介） |
| `7-apply-list.png` | 申请列表（编号、状态标签"待审批"） |

**改样式不要改 id**。这些 id 是用例依赖的契约：

```
登录    username / password / role-student / role-teacher / role-admin / submit / error
注册    username / realName / password / confirm / phone / email / submit / error
首页    welcome / user-line / apply-count / latest-status / to-apply / to-list
申请1   company / position / dept / startDate / endDate / next / error
申请2   mentorName / mentorTitle / mentorPhone / location / insurance / duty / intro / submit
列表    apply-no / apply-status / msg
```

改了 id，对应用例立刻红。

## 改起来

- 业务规则：`MockSite.validateRegister()` / `validateStep1()`，登录的角色校验在 `login()`
- 可选公司与岗位：`MockSite.COMPANIES` / `POSITIONS`
- 页面样式：`MockPages.CSS`，页面结构在各 `xxxPage()` 方法里
- 页面定位器：各 `*Page.java` 里的 `By.id(...)`
- 加新业务线：复制 `auth` 包结构 → 改包名 → 在 `testng.xml` 登记
- 接你自己的真实系统：把 `site.baseUrl()` 换成真实地址、定位器照着改，用例骨架不用动

## 出问题

| 现象 | 原因 | 处理 |
|---|---|---|
| `SessionNotCreatedException` | 驱动和 Firefox 版本不匹配 | 删掉 `drivers\geckodriver.exe`，让 Selenium 自动下载 |
| 注册用例提示"用户名已存在" | 同一 JVM 内重复跑，账号已注册 | 用例用时间戳生成账号，正常不会触发；手工复跑请重启 JVM |
| 未登录用例没跳转 | 会话 Cookie 残留 | 该用例每条都新开浏览器，正常不会出现；检查 `SITE` 单例是否被 `stop()` |
| 百度用例 skipped | IP 被限流弹验证码 | 等几分钟再跑，或删掉 `testng.xml` 里 `baidu` 段 |
| 百度结果 0 条 | 百度改版，选择器失效 | 改 `baidu/BaiduPage.RESULTS` |
