package com.hoanhoang.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hoanhoang.core.exception.ConfigLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages test data loading from JSON files.
 * Provides centralized access to test credentials and test data.
 */
public class TestDataManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Represents user credentials for testing.
     */
    public static class UserCredentials {
        private final String username;
        private final String password;

        public UserCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    /**
     * Loads user credentials from test data file.
     *
     * @param credentialType type of credentials to load ("validUser", "invalidUser", etc.)
     * @return UserCredentials containing username and password
     * @throws ConfigLoadException if file not found or parsing fails
     */
    public static UserCredentials loadCredentials(String credentialType) {
        String jsonPath = System.getProperty("user.dir") + "/data/test-credentials.json";

        try (InputStream is = TestDataManager.class.getClassLoader()
                .getResourceAsStream("data/test-credentials.json")) {

            if (is == null) {
                throw new ConfigLoadException(
                        credentialType,
                        "Test data file not found in classpath: data/test-credentials.json",
                        null
                );
            }

            JsonObject json = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);

            if (!json.has(credentialType)) {
                throw new ConfigLoadException(
                        credentialType,
                        "Credential type '" + credentialType + "' not found in test data file",
                        null
                );
            }

            JsonObject credentialsJson = json.getAsJsonObject(credentialType);
            String username = credentialsJson.get("username").getAsString();
            String password = credentialsJson.get("password").getAsString();

            return new UserCredentials(username, password);

        } catch (IOException e) {
            throw new ConfigLoadException(
                credentialType,
                "Failed to load test data file: " + jsonPath + ". " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Gets default valid user credentials for testing.
     *
     * @return UserCredentials with valid username and password
     */
    public static UserCredentials getValidCredentials() {
        return loadCredentials("validUser");
    }

    /**
     * Gets invalid user credentials for negative testing.
     *
     * @return UserCredentials with invalid username and password
     */
    public static UserCredentials getInvalidCredentials() {
        return loadCredentials("invalidUser");
    }

    private TestDataManager() {
        // Prevent instantiation
    }

    public static class BillingDetails {
        private final String firstName;
        private final String lastName;
        private final String address;
        private final String city;
        private final String postcode;
        private final String phone;

        public BillingDetails(String firstName, String lastName, String address, String city, String postcode, String phone) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.city = city;
            this.postcode = postcode;
            this.phone = phone;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getAddress() { return address; }
        public String getCity() { return city; }
        public String getPostcode() { return postcode; }
        public String getPhone() { return phone; }
    }

    public static BillingDetails loadBillingInfo(String billingType) {
        try (InputStream is = TestDataManager.class.getClassLoader()
                .getResourceAsStream("data/test-credentials.json")) {

            if (is == null) {
                throw new ConfigLoadException(billingType, "Test data file not found", null);
            }

            JsonObject json = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);

            if (!json.has(billingType)) {
                throw new ConfigLoadException(billingType, "Billing type '" + billingType + "' not found", null);
            }

            JsonObject billingJson = json.getAsJsonObject(billingType);

            return new BillingDetails(
                    billingJson.get("firstName").getAsString(),
                    billingJson.get("lastName").getAsString(),
                    billingJson.get("address").getAsString(),
                    billingJson.get("city").getAsString(),
                    billingJson.get("postcode").getAsString(),
                    billingJson.get("phone").getAsString()
            );
        } catch (IOException e) {
            throw new ConfigLoadException(billingType, "Failed to load billing data: " + e.getMessage(), e);
        }
    }

    public static BillingDetails getBillingInfo() {
        return loadBillingInfo("billingDetails");
    }

    public static class ReviewData {
        private final String name;
        private final String email;

        public ReviewData(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    public static ReviewData getReviewData() {
        try (InputStream is = TestDataManager.class.getClassLoader()
                .getResourceAsStream("data/test-review.json")) {

            if (is == null) {
                throw new ConfigLoadException("reviewData", "Test review data file not found", null);
            }

            JsonObject json = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
            JsonObject reviewJson = json.getAsJsonObject("reviewData");

            String name = reviewJson.get("name").getAsString();
            String email = reviewJson.get("email").getAsString();

            return new ReviewData(name, email);
        } catch (IOException e) {
            throw new ConfigLoadException("reviewData", "Failed to load review data: " + e.getMessage(), e);
        }
    }
}
