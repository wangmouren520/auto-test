package auto.auth;

import auto.Pace;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** 龙财实习系统 - 登录页。 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
        Pace.step();
    }

    /** 以学生身份登录。 */
    public void submit(String username, String password) {
        submit(username, password, "student");
    }

    /** @param role student / teacher / admin，对应页面上的角色按钮 */
    public void submit(String username, String password, String role) {
        Pace.type(driver.findElement(By.id("username")), username);
        Pace.step();
        Pace.type(driver.findElement(By.id("password")), password);
        Pace.step();
        driver.findElement(By.id("role-" + role)).click();
        Pace.step();
        driver.findElement(By.id("submit")).click();
        Pace.step();
    }

    /** 登录失败提示。 */
    public String error() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error"))).getText();
    }

    /** 注册成功后回到登录页时的成功提示。 */
    public String message() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg"))).getText();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
