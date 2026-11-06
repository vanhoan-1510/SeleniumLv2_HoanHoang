package com.hoanhoang.unit.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.config.EnvironmentConfig;
import com.hoanhoang.core.config.JsonConfigLoader;
import com.hoanhoang.core.util.JsonMapper;
import com.hoanhoang.data.AccountPool;
import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.builder.BillingBuilder;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.data.model.UserDto;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.tests.CartQuantityTest;
import com.hoanhoang.tests.support.SuiteConfiguration;
import com.hoanhoang.unit.FrameworkTest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class ConfigDataChecks {
    private final ThreadLocal<List<UserDto>> accounts = ThreadLocal.withInitial(ArrayList::new);

    @AfterMethod(alwaysRun = true)
    public void releaseAccounts() {
        List<UserDto> leased = accounts.get();
        accounts.remove();
        leased.forEach(AccountPool::releaseAccount);
    }

    @Test
    public void finalConfigurationValidatesOverridesWithoutMutatingTheOriginal() {
        var original = JsonConfigLoader.loadFromJson("sample");
        Properties overrides = new Properties();
        overrides.setProperty("browser", " firefox ");
        overrides.setProperty("headless", "true");
        overrides.setProperty("windowWidth", "1366");
        overrides.setProperty("remote", "true");
        overrides.setProperty("gridUrl", "https://grid.example.test:4444");
        var resolved = original.withOverrides(overrides);
        resolved.validate("sample");
        assertThat(resolved.isHeadless()).isTrue();
        assertThat(resolved.isRemote()).isTrue();
        assertThat(resolved.getWindowWidth()).isEqualTo(1366);
        assertThat(original.getWindowWidth()).isEqualTo(1920);
        overrides.setProperty("headless", "tru");
        assertThatThrownBy(() -> original.withOverrides(overrides))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("headless");
        overrides.setProperty("headless", "false");
        overrides.setProperty("windowWidth", "-1");
        assertThatThrownBy(() -> original.withOverrides(overrides).validate("sample"))
                .isInstanceOf(IllegalArgumentException.class);
        overrides.setProperty("windowWidth", "1366");
        overrides.setProperty("gridUrl", "file:///bad-grid");
        assertThatThrownBy(() -> original.withOverrides(overrides).validate("sample"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gridUrl");
    }

    @Test
    public void testDataJsonIsMappedToDtosAndBack() {
        BillingDetails billing = TestDataManager.getBillingInfo();
        assertThat(billing.firstName()).isNotBlank();
        assertThat(billing.email()).contains("@");
        assertThat(TestDataManager.getReviewData().name()).isNotBlank();
        assertThat(TestDataManager.getPaymentMethods()).isNotEmpty().doesNotContainNull();
        assertThat(TestDataManager.getAccounts())
                .isNotEmpty()
                .allSatisfy(
                        user ->
                                assertThat(user.toString())
                                        .contains("REDACTED")
                                        .doesNotContain(user.getPassword()));
        String json = JsonMapper.toJson(billing);
        assertThat(JsonMapper.fromJson(json, BillingDetails.class)).isEqualTo(billing);
        BillingDetails blank = BillingBuilder.blank().build();
        assertThat(blank.firstName()).isEmpty();
        assertThat(BillingBuilder.from(billing).withEmail("x@y.z").build())
                .isEqualTo(
                        new BillingDetails(
                                billing.firstName(),
                                billing.lastName(),
                                billing.address(),
                                billing.city(),
                                billing.postcode(),
                                billing.phone(),
                                "x@y.z"));
    }

    @Test
    public void moneyParsingPreservesThousandsAndDecimals() {
        assertThat(PriceUtils.parse("$1,234.50")).isEqualByComparingTo("1234.50");
        assertThat(PriceUtils.parse("$\u00a099.99")).isEqualByComparingTo("99.99");
        assertThat(PriceUtils.format(new BigDecimal("1234.5"))).isEqualTo("$1,234.50");
        assertThatThrownBy(() -> PriceUtils.parse("$100.00 $80.00"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void configIsReadOnceAndEnvironmentCannotChangeDuringRun() {
        ConfigLoader.load("dev");
        EnvironmentConfig first = ConfigLoader.getConfig();
        ConfigLoader.load(" DEV ");
        assertThat(ConfigLoader.getConfig()).isSameAs(first);
        assertThatThrownBy(() -> ConfigLoader.load("prod"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void invalidConfigurationFailsBeforeBrowserCreation() {
        assertThatThrownBy(() -> new EnvironmentConfig().validate("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JsonConfigLoader.loadFromJson("../dev"))
                .isInstanceOf(IllegalArgumentException.class);
        for (String env : List.of("dev", "prod", "sample")) {
            assertThat(JsonConfigLoader.loadFromJson(env).getWindowWidth()).isPositive();
        }
    }

    @Test
    public void accountPoolReturnsLeasesWithoutDuplicates() {
        List<UserDto> leased = accounts.get();
        int count = AccountPool.getAccountCount();
        for (int i = 0; i < count; i++) {
            var account = AccountPool.waitForAvailableAccount(0);
            assertThat(account).as("Account lease %s", i).isNotNull();
            leased.add(account);
        }
        assertThat(AccountPool.getAvailableAccount()).isNull();
        assertThat(leased.stream().map(UserDto::getUsername).distinct().count())
                .isEqualTo((long) count);
        releaseAccounts();
        AccountPool.releaseAccount(leased.get(0));
        assertThat(AccountPool.getAvailableAccountCount()).isEqualTo(count);
    }

    @Test
    public void uiConcurrencyCannotExceedTheAccountPool() {
        int accounts = AccountPool.getAccountCount();
        XmlSuite suite = new XmlSuite();
        suite.setName("UI concurrency regression");
        suite.setThreadCount(accounts + 2);
        suite.setDataProviderThreadCount(accounts + 3);
        XmlTest test = new XmlTest(suite);
        test.setThreadCount(accounts + 4);
        test.setXmlClasses(List.of(new XmlClass(CartQuantityTest.class)));

        new SuiteConfiguration().alter(List.of(suite));

        assertThat(suite.getThreadCount()).isEqualTo(accounts);
        assertThat(suite.getDataProviderThreadCount()).isEqualTo(accounts);
        assertThat(test.getThreadCount()).isEqualTo(accounts);
    }

    @Test
    public void uiConcurrencyPreservesAnExplicitSerialRun() {
        XmlSuite suite = new XmlSuite();
        suite.setThreadCount(1);
        suite.setDataProviderThreadCount(1);
        XmlTest test = new XmlTest(suite);
        test.setXmlClasses(List.of(new XmlClass(CartQuantityTest.class)));

        new SuiteConfiguration().alter(List.of(suite));

        assertThat(suite.getThreadCount()).isEqualTo(1);
        assertThat(suite.getDataProviderThreadCount()).isEqualTo(1);
        assertThat(test.getThreadCount()).isEqualTo(1);
    }

    @Test
    public void browserFreeSuitesKeepTheirRequestedConcurrency() {
        int requested = AccountPool.getAccountCount() + 2;
        XmlSuite suite = new XmlSuite();
        suite.setThreadCount(requested);
        suite.setDataProviderThreadCount(requested);
        XmlTest test = new XmlTest(suite);
        test.setXmlClasses(List.of(new XmlClass(FrameworkTest.class)));

        new SuiteConfiguration().alter(List.of(suite));

        assertThat(suite.getThreadCount()).isEqualTo(requested);
        assertThat(suite.getDataProviderThreadCount()).isEqualTo(requested);
    }
}
