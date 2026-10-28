package com.hoanhoang.unit;

import com.hoanhoang.unit.checks.ConfigDataChecks;
import com.hoanhoang.unit.checks.CredentialChecks;
import com.hoanhoang.unit.checks.ElementDriverChecks;
import com.hoanhoang.unit.checks.PurchaseChecks;
import com.hoanhoang.unit.checks.RetryChecks;
import org.testng.annotations.Factory;

/** Stable Maven entry point for the browser-free checks, grouped by responsibility. */
public class FrameworkTest {
    @Factory
    public Object[] frameworkChecks() {
        return new Object[] {
            new ConfigDataChecks(),
            new CredentialChecks(),
            new ElementDriverChecks(),
            new PurchaseChecks(),
            new RetryChecks()
        };
    }
}
