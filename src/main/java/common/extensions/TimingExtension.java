package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private Map<String, Long> startTimes = new HashMap<>();
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        // формат времени начало
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault());
        // формат времени конец

        String testName = extensionContext.getRequiredTestClass().getPackageName()
                + "." + extensionContext.getDisplayName();
        startTimes.put(testName, System.currentTimeMillis());
        System.out.println("Thread " + Thread.currentThread().getName()
                +  "Test started: " + testName);
    //    System.out.println("Start time: " + formatter.format(Instant.ofEpochMilli(System.currentTimeMillis())));
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        String testName = extensionContext.getRequiredTestClass().getPackageName()
                + "." + extensionContext.getDisplayName();
        Long testDuration = System.currentTimeMillis() - startTimes.get(testName);
        System.out.println("Thread " + Thread.currentThread().getName()
                +  " Test finished: " + testName + " Test duration: " + testDuration + " ms");
    }
}
