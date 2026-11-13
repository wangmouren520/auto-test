package auto;

import org.openqa.selenium.WebElement;

/**
 * 操作节奏控制器：让浏览器慢下来，人眼能看清每一步。
 *
 * <p>默认值：非 headless 时每步停 1 秒、每条用例收尾停 2 秒；
 * headless 时全部为 0（无人看，不白等）。
 *
 * <p>调整方式（任意一项都能覆盖）：
 * <pre>
 * mvn test -Dslow=2500      // 更慢，适合录屏讲解
 * mvn test -Dslow=0         // 关闭慢放，回到全速
 * mvn test -Dheadless=true  // 自动为 0
 * </pre>
 */
public final class Pace {

    /** 每个操作之间的停顿（毫秒）。 */
    private static final long STEP_MS = Long.getLong("slow",
            Boolean.getBoolean("headless") ? 0L : 1000L);

    /** 逐字输入的间隔，比整步停顿细一点，能看清字一个个进去。 */
    private static final long TYPE_MS = STEP_MS == 0 ? 0L : Math.max(25L, STEP_MS / 15);

    private Pace() {
    }

    /** 一步动作之后停一下。 */
    public static void step() {
        sleep(STEP_MS);
    }

    /** 收尾停留：让最终结果在窗口里留够时间再关。 */
    public static void hold() {
        sleep(STEP_MS * 2);
    }

    /** 一个字一个字地输入，过程中能看清输入框在变化。 */
    public static void type(WebElement element, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        element.click();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            sleep(TYPE_MS);
        }
    }

    public static long stepMs() {
        return STEP_MS;
    }

    private static void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
