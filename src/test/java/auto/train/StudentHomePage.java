package auto.train;

import auto.Pace;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** 学生首页：登录后的落地页，提供「申请实习」入口。 */
public class StudentHomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public StudentHomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String welcome() {
        return text("welcome");
    }

    /** 顶部欢迎语：欢迎回来，姓名（账号），当前身份：学生。 */
    public String userLine() {
        return text("user-line");
    }

    /** 已提交的申请数量。 */
    public String applyCount() {
        return text("apply-count");
    }

    public String latestStatus() {
        return text("latest-status");
    }

    /** 进入申请实习页。 */
    public void goApply() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("to-apply"))).click();
        Pace.step();
    }

    /** 进入我的申请列表。 */
    public void goApplyList() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("to-list"))).click();
        Pace.step();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private String text(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id))).getText();
    }
}
