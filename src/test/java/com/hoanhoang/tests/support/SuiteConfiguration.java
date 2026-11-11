package com.hoanhoang.tests.support;

import com.hoanhoang.data.AccountPool;
import com.hoanhoang.report.logger.Log;
import com.hoanhoang.tests.BaseTest;
import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/** Limits UI concurrency before TestNG creates workers; browser-free suites are unchanged. */
public final class SuiteConfiguration implements IAlterSuiteListener {
    @Override
    public void alter(List<XmlSuite> suites) {
        for (XmlSuite suite : suites) {
            if (suite.getTests().stream().noneMatch(SuiteConfiguration::hasUiTests)) {
                continue;
            }
            int accountCount = AccountPool.getAccountCount();
            int requestedThreads = suite.getThreadCount();
            int requestedProviderThreads = suite.getDataProviderThreadCount();
            suite.setThreadCount(Math.min(requestedThreads, accountCount));
            suite.setDataProviderThreadCount(Math.min(requestedProviderThreads, accountCount));
            for (XmlTest test : suite.getTests()) {
                if (hasUiTests(test)) {
                    test.setThreadCount(Math.min(test.getThreadCount(), accountCount));
                }
            }
            Log.info(
                    "UI suite '"
                            + suite.getName()
                            + "': accounts="
                            + accountCount
                            + ", threads="
                            + requestedThreads
                            + " -> "
                            + suite.getThreadCount()
                            + ", data-provider threads="
                            + requestedProviderThreads
                            + " -> "
                            + suite.getDataProviderThreadCount());
        }
    }

    private static boolean hasUiTests(XmlTest test) {
        return test.getXmlClasses().stream()
                .anyMatch(xmlClass -> BaseTest.class.isAssignableFrom(xmlClass.getSupportClass()));
    }
}
