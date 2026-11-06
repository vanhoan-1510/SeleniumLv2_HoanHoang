package com.hoanhoang.unit.checks;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.unit.support.ChildProcesses;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class RetryChecks {
    private final ChildProcesses children = new ChildProcesses();

    @AfterMethod(alwaysRun = true)
    public void stopChildProcesses() {
        children.close();
    }

    @Test
    public void retryBudgetIsIndependentForRepeatedAndParallelInvocations() throws Exception {
        // A separate JVM keeps TestNG/Allure lifecycle and retryCount isolated from this suite.
        var directory =
                Files.createTempDirectory(Path.of("target"), "retry-regression-").toAbsolutePath();
        var logging = directory.resolve("log4j2.xml");
        Files.writeString(
                logging,
                """
                <Configuration status="WARN"><Appenders><Console name="console">
                <PatternLayout pattern="%m%n"/></Console></Appenders>
                <Loggers><Root level="warn"><AppenderRef ref="console"/></Root></Loggers></Configuration>
                """);
        String javaExecutable =
                Path.of(
                                System.getProperty("java.home"),
                                "bin",
                                System.getProperty("os.name").startsWith("Windows")
                                        ? "java.exe"
                                        : "java")
                        .toString();
        var output = directory.resolve("process.log");
        Process child =
                children.start(
                        new ProcessBuilder(
                                        javaExecutable,
                                        "-DretryCount=1",
                                        "-Dallure.results.directory="
                                                + directory.resolve("allure-results"),
                                        "-Dlog4j.configurationFile=" + logging.toUri(),
                                        "-cp",
                                        System.getProperty(
                                                "surefire.test.class.path",
                                                System.getProperty("java.class.path")),
                                        "com.hoanhoang.unit.support.RetryLifecycleProbe")
                                .redirectErrorStream(true)
                                .redirectOutput(output.toFile()));
        assertThat(child.waitFor(30, TimeUnit.SECONDS))
                .as("Retry regression process completed: %s", output)
                .isTrue();
        assertThat(child.exitValue()).as(Files.readString(output)).isZero();
    }
}
