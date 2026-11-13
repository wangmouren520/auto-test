package auto.baidu.test;

import auto.AutoBaseTest;
import auto.baidu.BaiduPage;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/** 自动打开百度 → 自动搜索 → 自动校验结果。 */
public class BaiduSearchTest extends AutoBaseTest {

    private static final String KEYWORD = System.getProperty("keyword", "selenium");

    @Test(description = "打开百度首页")
    public void test_open() {
        BaiduPage page = new BaiduPage(driver).open();

        System.out.println("已打开: " + page.title() + " -> " + page.currentUrl());

        Assert.assertTrue(page.title().contains("百度"), "首页标题异常: " + page.title());
        Assert.assertTrue(page.searchBoxVisible(), "搜索框不可见");
    }

    @Test(description = "搜索关键词并校验结果")
    public void test_search() {
        BaiduPage page = new BaiduPage(driver).open();
        page.search(KEYWORD);

        List<WebElement> results = page.results();
        if (results.isEmpty() && page.blockedByCaptcha()) {
            throw new SkipException("百度弹验证码（IP 被限流），不是用例问题: " + page.currentUrl());
        }

        System.out.println("搜索 \"" + KEYWORD + "\" 得到 " + results.size() + " 条结果:");
        results.stream().limit(5).forEach(r -> {
            String text = r.getText().replaceAll("\\s+", " ").trim();
            System.out.println("  - " + (text.length() > 60 ? text.substring(0, 60) + "..." : text));
        });

        Assert.assertFalse(results.isEmpty(), "搜索结果为空，地址=" + page.currentUrl());
        Assert.assertTrue(page.title().toLowerCase().contains(KEYWORD.toLowerCase()),
                "结果页标题未包含关键词: " + page.title());
    }
}
