package auto.auth.test;

import auto.AutoBaseTest;
import auto.auth.LoginPage;
import auto.train.StudentHomePage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** 登录：成功路径 + 错误凭据 + 角色校验。 */
public class LoginTest extends AutoBaseTest {

    @Test(description = "预置学生账号登录 → 进入学生首页")
    public void test_login_success() {
        LoginPage page = new LoginPage(driver);
        page.open(site.baseUrl());
        page.submit("20230001", "123456");

        Assert.assertTrue(page.currentUrl().contains("/student/dashboard"),
                "未跳转学生首页: " + page.currentUrl());
        Assert.assertEquals(new StudentHomePage(driver).welcome(), "登录成功");
        System.out.println("登录成功 -> " + page.currentUrl());
    }

    @Test(description = "教师账号用教师身份登录 → 也能进入系统")
    public void test_login_teacher() {
        LoginPage page = new LoginPage(driver);
        page.open(site.baseUrl());
        page.submit("teacher01", "123456", "teacher");

        Assert.assertTrue(page.currentUrl().contains("/student/dashboard"),
                "教师登录未进入系统: " + page.currentUrl());
        System.out.println("教师登录成功 -> " + page.currentUrl());
    }

    @DataProvider(name = "badCredentials")
    public Object[][] badCredentials() {
        return new Object[][]{
                {"20230001", "wrong123", "密码错"},
                {"nobody", "123456", "用户不存在"},
                {"", "", "全空"},
        };
    }

    @Test(dataProvider = "badCredentials", description = "错误凭据 → 提示用户名或密码错误")
    public void test_login_fail(String username, String password, String caseName) {
        LoginPage page = new LoginPage(driver);
        page.open(site.baseUrl());
        page.submit(username, password);

        String error = page.error();
        Assert.assertTrue(error.contains("用户名或密码错误"), "[" + caseName + "] 提示异常: " + error);
        System.out.println("[" + caseName + "] 已拦截: " + error);
    }

    @Test(description = "管理员账号用学生身份登录 → 提示角色不匹配")
    public void test_login_role_mismatch() {
        LoginPage page = new LoginPage(driver);
        page.open(site.baseUrl());
        page.submit("admin", "123456", "student");

        String error = page.error();
        Assert.assertTrue(error.contains("角色不匹配"), "角色校验缺失，实际提示: " + error);
        System.out.println("[角色不匹配] 已拦截: " + error);
    }
}
