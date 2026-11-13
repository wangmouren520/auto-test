package auto;

import auto.mock.MockSite;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * 用例基类：开浏览器 + 起本地实习系统，子类只写业务断言。
 * 登录页地址 site.url("/login")，注册页 site.url("/register")。
 */
public class AutoBaseTest {

    protected WebDriver driver;

    /** 本地实习系统，全局单例，所有测试类共用一份用户数据。 */
    protected static MockSite site;

    @BeforeClass(alwaysRun = true)
    public void startSite() {
        // 全局单例：所有测试类共用一个站点，前面用例注册的账号后面能直接登录
        site = MockSite.getInstance();
    }

    @BeforeMethod(alwaysRun = true)
    public void openBrowser(Method method, Object[] params) {
        // 工程自带 geckodriver；文件不在就让 Selenium 自动下载，都不用改代码
        Path exe = Path.of("drivers", "geckodriver.exe");
        if (Files.isRegularFile(exe)) {
            System.setProperty("webdriver.gecko.driver", exe.toAbsolutePath().toString());
        }

        FirefoxOptions options = new FirefoxOptions();
        if (Boolean.getBoolean("headless")) {
            options.addArguments("-headless");
        }

        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();

        String label = (params == null || params.length == 0) ? "" : " " + Arrays.toString(params);
        System.out.println("▶ 开始: " + method.getName() + label + "（每步停 " + Pace.stepMs() + "ms）");
        Pace.step();
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) {
            // 结果多留一会儿再关，否则最后一个页面一闪就没
            Pace.hold();
            driver.quit();
        }
        System.out.println();
    }
}
