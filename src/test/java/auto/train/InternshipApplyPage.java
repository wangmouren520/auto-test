package auto.train;

import auto.Pace;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * 申请实习页：两步表单。
 * 第一步选公司与岗位、填实习时间；第二步填导师、工作内容并提交。
 */
public class InternshipApplyPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public InternshipApplyPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /** 第一步。传空字符串表示该字段留空（用于校验类用例）。 */
    public void fillStep1(String company, String position, String dept, String start, String end) {
        select("company", company);
        select("position", position);
        Pace.type(driver.findElement(By.id("dept")), dept);
        Pace.step();
        Pace.type(driver.findElement(By.id("startDate")), start);
        Pace.step();
        Pace.type(driver.findElement(By.id("endDate")), end);
        Pace.step();
    }

    public void next() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("next"))).click();
        Pace.step();
    }

    /** 第二步。 */
    public void fillStep2(String mentor, String mentorTitle, String mentorPhone,
                          String location, String duty, String intro) {
        Pace.type(driver.findElement(By.id("mentorName")), mentor);
        Pace.step();
        Pace.type(driver.findElement(By.id("mentorTitle")), mentorTitle);
        Pace.step();
        Pace.type(driver.findElement(By.id("mentorPhone")), mentorPhone);
        Pace.step();
        Pace.type(driver.findElement(By.id("location")), location);
        Pace.step();
        driver.findElement(By.id("insurance")).click();
        Pace.step();
        Pace.type(driver.findElement(By.id("duty")), duty);
        Pace.step();
        Pace.type(driver.findElement(By.id("intro")), intro);
        Pace.step();
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submit"))).click();
        Pace.step();
    }

    /** 校验失败提示（第一步或第二步都会出现）。 */
    public String error() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error"))).getText();
    }

    /** 提交成功后的申请编号。 */
    public String applyNo() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("apply-no"))).getText();
    }

    /** 提交成功后的申请状态。 */
    public String applyStatus() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("apply-status"))).getText();
    }

    /** 提交成功提示。 */
    public String message() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg"))).getText();
    }

    /** 第二步页面上回显的公司名。 */
    public String summaryCompany() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sum-company"))).getText();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private void select(String id, String visibleText) {
        if (visibleText != null && !visibleText.isEmpty()) {
            new Select(driver.findElement(By.id(id))).selectByVisibleText(visibleText);
            Pace.step();
        }
    }
}
