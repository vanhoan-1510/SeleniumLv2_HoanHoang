package com.hoanhoang.data;

import com.hoanhoang.data.TestDataManager.UserCredentials;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccountPool {

    private static final List<UserCredentials> accounts;
    private static final Set<Integer> inUseAccounts = Collections.synchronizedSet(new HashSet<>());
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    static {
        List<UserCredentials> loadedAccounts = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            try {
                loadedAccounts.add(TestDataManager.loadCredentials("validUser" + i));
            } catch (Exception e) {
            }
        }
        accounts = Collections.unmodifiableList(loadedAccounts);
        initialized.set(true);
    }

    public static UserCredentials getAvailableAccount() {
        synchronized (inUseAccounts) {
            for (int i = 0; i < accounts.size(); i++) {
                if (!inUseAccounts.contains(i)) {
                    inUseAccounts.add(i);
                    return accounts.get(i);
                }
            }
        }
        return null;
    }

    public static void releaseAccount(UserCredentials account) {
        if (account == null) return;

        synchronized (inUseAccounts) {
            int index = accounts.indexOf(account);
            if (index >= 0) {
                inUseAccounts.remove(index);
            }
        }
    }

    public static int getAccountCount() {
        return accounts.size();
    }

    public static int getAvailableAccountCount() {
        synchronized (inUseAccounts) {
            return accounts.size() - inUseAccounts.size();
        }
    }

    public static boolean hasAvailableAccount() {
        return getAvailableAccountCount() > 0;
    }

    public static void reset() {
        synchronized (inUseAccounts) {
            inUseAccounts.clear();
        }
    }
}
