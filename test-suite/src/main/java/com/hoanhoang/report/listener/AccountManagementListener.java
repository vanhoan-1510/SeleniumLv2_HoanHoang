package com.hoanhoang.report.listener;

import com.hoanhoang.data.AccountPool;
import com.hoanhoang.data.TestDataManager;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class AccountManagementListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            Object account = testResult.getAttribute("account");
            if (account instanceof TestDataManager.UserCredentials) {
                AccountPool.releaseAccount((TestDataManager.UserCredentials) account);
            }
        }
    }
}