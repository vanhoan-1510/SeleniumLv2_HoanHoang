package com.hoanhoang.unit.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoanhoang.data.TestDataManager;

/** Separate JVM isolates the cached credentials and working directory from developer accounts. */
public final class CredentialSourceProbe {
    private CredentialSourceProbe() {}

    public static void main(String[] args) {
        switch (args[0]) {
            case "sample" -> {
                assertThat(TestDataManager.getAccounts().getFirst().getUsername())
                        .isEqualTo("example-user-1");
                assertThatThrownBy(TestDataManager::requireCredentials)
                        .hasMessageContaining("UI tests require local accounts");
            }
            case "invalid" -> {
                try {
                    TestDataManager.getAccounts();
                    throw new AssertionError("Invalid configured credentials must fail");
                } catch (ExceptionInInitializerError e) {
                    assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
                    assertThat(e.getCause().getMessage()).contains("Cannot read JSON file");
                    assertThat(e.getCause().getCause()).isNull();
                }
            }
            default -> {
                TestDataManager.requireCredentials();
                assertThat(TestDataManager.getAccounts().getFirst().getUsername())
                        .isEqualTo(args[0]);
            }
        }
    }
}
