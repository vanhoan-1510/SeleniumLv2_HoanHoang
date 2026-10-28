package com.hoanhoang.unit.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

public class CredentialChecks {
    @Test
    public void credentialsResolveWithoutPublishingOrFallingBackFromInvalidOverrides()
            throws Exception {
        Path root =
                Files.createTempDirectory(Path.of("target"), "credential-check-").toAbsolutePath();
        Path configured = root.resolve("configured.json");
        Path environment = root.resolve("environment.json");
        Files.writeString(configured, fixture("configured"));
        Files.writeString(environment, fixture("environment"));
        Path malformed = root.resolve("malformed.json");
        Files.writeString(malformed, "{\"password\":\"private-sentinel-value\", broken");
        for (String mode :
                List.of("sample", "local", "environment", "configured", "missing", "malformed")) {
            Path directory = Files.createDirectory(root.resolve(mode));
            if (!mode.equals("sample")) {
                Files.createDirectory(directory.resolve(".local"));
                Files.writeString(
                        directory.resolve(".local/test-credentials.json"), fixture("local"));
            }
            List<String> command = new ArrayList<>();
            command.add(
                    Path.of(
                                    System.getProperty("java.home"),
                                    "bin",
                                    System.getProperty("os.name").startsWith("Windows")
                                            ? "java.exe"
                                            : "java")
                            .toString());
            if (List.of("configured", "missing", "malformed").contains(mode)) {
                Path override =
                        switch (mode) {
                            case "configured" -> configured;
                            case "malformed" -> malformed;
                            default -> root.resolve("does-not-exist.json");
                        };
                command.add("-Dcredentials.file=" + override);
            }
            command.addAll(
                    List.of(
                            "-cp",
                            System.getProperty(
                                    "surefire.test.class.path",
                                    System.getProperty("java.class.path")),
                            "com.hoanhoang.unit.support.CredentialSourceProbe",
                            List.of("missing", "malformed").contains(mode) ? "invalid" : mode));
            Path output = directory.resolve("process.log");
            ProcessBuilder builder =
                    new ProcessBuilder(command)
                            .directory(directory.toFile())
                            .redirectErrorStream(true)
                            .redirectOutput(output.toFile());
            builder.environment().remove("TEST_CREDENTIALS_FILE");
            if (List.of("environment", "configured", "missing", "malformed").contains(mode)) {
                builder.environment().put("TEST_CREDENTIALS_FILE", environment.toString());
            }
            Process child = builder.start();
            try {
                assertThat(child.waitFor(20, TimeUnit.SECONDS))
                        .as("Credentials mode %s", mode)
                        .isTrue();
                String log = Files.readString(output);
                assertThat(log).doesNotContain("private-sentinel-value");
                assertThat(child.exitValue()).as("Credentials mode %s: %s", mode, log).isZero();
            } finally {
                if (child.isAlive()) {
                    child.destroyForcibly();
                }
            }
        }
    }

    private static String fixture(String username) {
        return "{\"validUser1\":{\"username\":\"" + username + "\",\"password\":\"fixture-only\"}}";
    }
}
