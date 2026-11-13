package auto.auth.test;

import auto.AutoBaseTest;
import auto.auth.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** 注册：成功注册 + 6 条校验规则。 */
public class RegisterTest extends AutoBaseTest {

    @Test(description = "信息合法 → 注册成功并回到登录页")
    public void test_register_success() {
        String account = "reg" + (System.currentTimeMillis() % 1_000_000);

        RegisterPage page = new RegisterPage(driver);
        page.open(site.baseUrl());
        page.submit(account, "注册测试", "abc123456", "abc123456");

        String msg = page.message();
        Assert.assertTrue(msg.contains("注册成功"), "实际: " + msg);
        Assert.assertTrue(page.currentUrl().contains("/login"), "注册后应回登录页: " + page.currentUrl());
        System.out.println("注册成功: " + account + " -> " + msg);
    }

    @DataProvider(name = "invalidForms")
    public Object[][] invalidForms() {
        return new Object[][]{
                {"ab", "张三", "abc123456", "abc123456", "用户名长度需在 3 到 30 个字符"},
                {"", "张三", "abc123456", "abc123456", "请输入用户名"},
                {"newuser", "", "abc123456", "abc123456", "请输入真实姓名"},
                {"newuser", "张三", "123", "123", "密码至少 6 位"},
                {"newuser", "张三", "abc123456", "abc123457", "两次密码输入不一致"},
                {"admin", "张三", "abc123456", "abc123456", "用户名已存在"},
        };
    }

    @Test(dataProvider = "invalidForms", description = "非法输入 → 对应提示")
    public void test_register_invalid(String username, String realName,
                                      String password, String confirm, String expect) {
        RegisterPage page = new RegisterPage(driver);
        page.open(site.baseUrl());
        page.submit(username, realName, password, confirm);

        String error = page.error();
        Assert.assertTrue(error.contains(expect), "期望包含 [" + expect + "]，实际: " + error);
        System.out.println("[" + expect + "] 校验通过: " + error);
    }
}
