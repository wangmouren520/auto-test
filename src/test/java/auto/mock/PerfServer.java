package auto.mock;

/**
 * 性能测试用的启动入口：把实习系统单独跑起来，等 JMeter 来打。
 *
 * <p>和自动化测试用的是同一份 MockSite，所以压的接口就是实验四里跑的接口。
 * 启动后第一行打印 {@code PERF_BASE=http://127.0.0.1:端口}，
 * 后续脚本从这一行取端口。
 *
 * <pre>
 * mvn -q test-compile
 * java -cp "target/test-classes;target/classes" auto.mock.PerfServer
 * </pre>
 */
public final class PerfServer {

    private PerfServer() {
    }

    public static void main(String[] args) throws Exception {
        MockSite site = MockSite.getInstance();
        System.out.println("PERF_BASE=" + site.baseUrl());
        System.out.flush();
        Thread.currentThread().join();
    }
}
