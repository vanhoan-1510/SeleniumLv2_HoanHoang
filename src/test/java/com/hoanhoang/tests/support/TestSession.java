package com.hoanhoang.tests.support;

import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.data.AccountPool;
import com.hoanhoang.data.model.UserDto;

/** Owns each test thread's account lease and browser cleanup. */
public final class TestSession implements AutoCloseable {
    private final ThreadLocal<UserDto> accounts = new ThreadLocal<>();

    public UserDto account() {
        UserDto account = accounts.get();
        if (account == null) {
            int seconds = Integer.parseInt(System.getProperty("account.timeout", "60"));
            account = AccountPool.waitForAvailableAccount(seconds);
            if (account == null) {
                throw new IllegalStateException(
                        "No account available after " + seconds + " seconds");
            }
            accounts.set(account);
        }
        return account;
    }

    @Override
    public void close() {
        UserDto account = accounts.get();
        accounts.remove();
        try {
            DriverManager.quitDriver();
        } finally {
            AccountPool.releaseAccount(account);
        }
    }
}
