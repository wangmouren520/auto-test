package auto.baidu;

import auto.Pace;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/** 百度首页 + 搜索结果页。 */
public class BaiduPage {

    public static final String HOME = "https://www.baidu.com";

    private static final By SEARCH_BOX = By.id("kw");
    private static final By RESULTS = By.cssSelector("div.result, div.result-op, div.c-container");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public BaiduPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public BaiduPage open() {
        driver.get(HOME);
        Pace.step();
        return this;
    }

    public boolean searchBoxVisible() {
        return driver.findElement(SEARCH_BOX).isDisplayed();
    }

    public void search(String keyword) {
        Pace.type(driver.findElement(SEARCH_BOX), keyword);
        Pace.step();
        driver.findElement(SEARCH_BOX).sendKeys(Keys.ENTER);
        Pace.step();
    }

    /** 结果异步渲染，等节点真出现再返回；超时返回空列表。 */
    public List<WebElement> results() {
        try {
            return wait.until(d -> {
                List<WebElement> list = d.findElements(RESULTS);
                return list.isEmpty() ? null : list;
            });
        } catch (TimeoutException e) {
            return List.of();
        }
    }

    public boolean blockedByCaptcha() {
        String url = driver.getCurrentUrl();
        return driver.getTitle().contains("安全验证") || url.contains("wappass") || url.contains("captcha");
    }

    public String title() {
        return driver.getTitle();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
