package com.hoanhoang.data;

import com.hoanhoang.data.model.UserDto;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** One lease per account; waiting threads block until an account is returned. */
public final class AccountPool {
    private static final List<UserDto> ACCOUNTS = TestDataManager.getAccounts();
    private static final BlockingQueue<UserDto> AVAILABLE =
            new ArrayBlockingQueue<>(ACCOUNTS.size(), true, ACCOUNTS);
    private static final Set<UserDto> LEASED = ConcurrentHashMap.newKeySet();

    private AccountPool() {}

    public static UserDto getAvailableAccount() {
        UserDto account = AVAILABLE.poll();
        if (account != null) {
            LEASED.add(account);
        }
        return account;
    }

    public static UserDto waitForAvailableAccount(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Account timeout must not be negative");
        }
        try {
            UserDto account = AVAILABLE.poll(seconds, TimeUnit.SECONDS);
            if (account != null) {
                LEASED.add(account);
            }
            return account;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for an account", e);
        }
    }

    public static void releaseAccount(UserDto account) {
        if (account != null && LEASED.remove(account)) {
            AVAILABLE.add(account);
        }
    }

    public static int getAccountCount() {
        return ACCOUNTS.size();
    }

    public static int getAvailableAccountCount() {
        return AVAILABLE.size();
    }
}
