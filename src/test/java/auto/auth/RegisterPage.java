package auto.auth;

import auto.Pace;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** 龙财实习系统 - 注册页。 */
public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/register");
        Pace.step();
    }

    /** 只填必填项。 */
    public void submit(String username, String realName, String password, String confirm) {
        submit(username, realName, password, confirm, "", "");
    }

    public void submit(String username, String realName, String password, String confirm,
                       String phone, String email) {
        Pace.type(driver.findElement(By.id("username")), username);
        Pace.step();
        Pace.type(driver.findElement(By.id("realName")), realName);
        Pace.step();
        Pace.type(driver.findElement(By.id("password")), password);
        Pace.step();
        Pace.type(driver.findElement(By.id("confirm")), confirm);
        Pace.step();
        Pace.type(driver.findElement(By.id("phone")), phone);
        Pace.step();
        Pace.type(driver.findElement(By.id("email")), email);
        Pace.step();
        driver.findElement(By.id("submit")).click();
        Pace.step();
    }

    /** 注册失败提示。 */
    public String error() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error"))).getText();
    }

    /** 注册成功后跳回登录页，成功提示在 #msg。 */
    public String message() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg"))).getText();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
