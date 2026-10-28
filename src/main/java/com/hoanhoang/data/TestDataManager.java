package com.hoanhoang.data;

import com.google.gson.JsonObject;
import com.hoanhoang.core.constant.FrameworkConstants;
import com.hoanhoang.core.util.JsonMapper;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.data.model.ReviewData;
import com.hoanhoang.data.model.UserDto;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Local credentials and classpath sample data, read once and mapped with Gson. */
public final class TestDataManager {
    private TestDataManager() {}

    /** UI runs must never attempt to log in using the fictional classpath fixture. */
    public static void requireCredentials() {
        if (CredentialData.FILE == null) {
            throw new IllegalStateException(
                    "UI tests require local accounts: copy testdata/test-credentials.json to "
                            + ".local/test-credentials.json and fill in valid accounts, or set "
                            + "-Dcredentials.file / TEST_CREDENTIALS_FILE. See README.");
        }
        getAccounts();
    }

    /** Payment methods for data-driven TC03, read from testdata/payment-methods.json. */
    public static List<PaymentMethod> getPaymentMethods() {
        JsonObject json =
                JsonMapper.readResource(
                        FrameworkConstants.TEST_DATA_DIR + "payment-methods.json",
                        JsonObject.class);
        if (!json.has("paymentMethods") || !json.get("paymentMethods").isJsonArray()) {
            throw new IllegalStateException(
                    "Missing 'paymentMethods' array in payment-methods.json");
        }
        List<PaymentMethod> methods =
                Arrays.asList(
                        JsonMapper.convert(json.get("paymentMethods"), PaymentMethod[].class));
        // Gson maps an unknown enum name to null.
        if (methods.isEmpty() || methods.contains(null)) {
            throw new IllegalStateException(
                    "payment-methods.json must list valid PaymentMethod names: "
                            + Arrays.toString(PaymentMethod.values()));
        }
        return List.copyOf(methods);
    }

    /** All "validUserN" accounts, used by AccountPool. */
    public static List<UserDto> getAccounts() {
        List<UserDto> accounts =
                CredentialData.JSON.keySet().stream()
                        .filter(key -> key.matches("validUser[0-9]+"))
                        .sorted()
                        .map(TestDataManager::readAccount)
                        .toList();
        if (accounts.isEmpty()) {
            throw new IllegalStateException("No validUserN accounts in test-credentials.json");
        }
        if (accounts.stream().map(UserDto::getUsername).distinct().count() != accounts.size()) {
            throw new IllegalStateException("Account pool contains duplicate usernames");
        }
        return accounts;
    }

    public static BillingDetails getBillingInfo() {
        return section(CredentialData.JSON, "billingDetails", BillingDetails.class);
    }

    public static ReviewData getReviewData() {
        return section(ReviewDataSource.JSON, "reviewData", ReviewData.class);
    }

    private static UserDto readAccount(String key) {
        UserDto account = section(CredentialData.JSON, key, UserDto.class);
        if (isBlank(account.getUsername()) || isBlank(account.getPassword())) {
            throw new IllegalStateException("Missing username/password in test data: " + key);
        }
        return account;
    }

    private static <T> T section(JsonObject json, String key, Class<T> type) {
        if (!json.has(key) || !json.get(key).isJsonObject()) {
            throw new IllegalStateException("Missing data section: " + key);
        }
        try {
            return JsonMapper.convert(json.get(key), type);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid data section: " + key);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Path credentialFile() {
        String configured = System.getProperty("credentials.file");
        if (isBlank(configured)) {
            configured = System.getenv("TEST_CREDENTIALS_FILE");
        }
        if (!isBlank(configured)) {
            return Path.of(configured);
        }
        Path local = Path.of(".local", "test-credentials.json");
        return Files.exists(local) ? local : null;
    }

    private static final class CredentialData {
        private static final Path FILE = credentialFile();
        private static final JsonObject JSON =
                FILE != null
                        ? JsonMapper.readFile(FILE, JsonObject.class)
                        : JsonMapper.readResource(
                                FrameworkConstants.TEST_DATA_DIR + "test-credentials.json",
                                JsonObject.class);
    }

    private static final class ReviewDataSource {
        private static final JsonObject JSON =
                JsonMapper.readResource(
                        FrameworkConstants.TEST_DATA_DIR + "test-review.json", JsonObject.class);
    }
}
