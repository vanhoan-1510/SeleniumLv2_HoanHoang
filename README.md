# Selenium Automation Framework - TestArchitect Demo

[![Java](https://img.shields.io/badge/Java-11-orange?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.18.1-green?logo=selenium&logoColor=white)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.9.0-blue?logo=testng&logoColor=white)](https://testng.org/)
[![Allure](https://img.shields.io/badge/Allure-2.25.0-purple?logo=allure&logoColor=white)](https://docs.qameta.io/allure/)
[![Log4j](https://img.shields.io/badge/Log4j-2.25.3-blue?logo=apache-log4j&logoColor=white)](https://logging.apache.org/log4j/2.x/)

A robust Java-based test automation framework using Selenium 4, TestNG, and Allure Report for testing the TestArchitect demo website.

## 🚀 Features

- **Multi-browser support**: Chrome, Firefox, Edge
- **Page Object Model**: Clean and maintainable test architecture
- **Data-driven testing**: JSON data models and TestDataManager
- **Allure Reports**: Beautiful HTML test reports with screenshots
- **Environment configuration**: Easy switching between dev, staging, prod
- **Headless mode**: Run tests without GUI for CI/CD
- **Parallel execution**: TestNG support for parallel testing
- **Account Pool**: Manage multiple test accounts for concurrent execution
- **Multiple Payment Methods**: Support for Bank Transfer, Cheque, and COD

## 📋 Test Cases

| TC | Description | Precondition |
|----|-------------|--------------|
| TC01 | Buy an item successfully | User logged in |
| TC02 | Buy multiple items successfully | User logged in |
| TC03 | Buy with different payment methods (Bank Transfer, Cheque, COD) | User logged in |
| TC04 | Sort items by price (low to high, high to low) | User logged in |
| TC05 | Verify orders appear in order history | User placed 2 orders |
| TC06 | Buy item as guest (grid/list view) | User NOT logged in |
| TC07 | Error handling when mandatory fields are blank | User at checkout |
| TC08 | Clear the shopping cart | Items in cart |
| TC09 | Update product quantity in cart | Item in cart |
| TC10 | Post a review for a product | User logged in |

## 📦 Project Structure

```
SeleniumLv2_HoanHoang/
├── framework-core/              # Core framework components
│   ├── config/                  # Configuration management
│   ├── driver/                  # WebDriver management
│   └── element/                 # Element wrappers
│
├── framework-data/              # Data management
│   ├── model/                   # Data models
│   ├── builder/                 # Builder pattern
│   ├── constant/                # Constants (PaymentMethod)
│   ├── util/                    # Utilities
│   ├── AccountPool.java         # Account pool management
│   └── TestDataManager.java     # Load test data from JSON
│
├── framework-report/            # Reporting
│   ├── allure/                  # Allure integration
│   ├── listener/                # TestNG listeners
│   └── logger/                  # Logging
│
└── test-suite/                  # Test implementations
    ├── src/main/java/pages/     # Page Object Model
    │   ├── BasePage.java
    │   ├── HomePage.java
    │   ├── LoginPage.java
    │   ├── ShopPage.java
    │   ├── ProductDetailPage.java
    │   ├── ShoppingCartPage.java
    │   ├── CheckoutPage.java
    │   ├── OrderSuccessPage.java
    │   ├── MyAccountPage.java
    │   └── OrderHistoryPage.java
    ├── src/test/java/tests/     # Test classes
    │   ├── BaseTest.java
    │   ├── TC01.java to TC10.java
    └── src/test/resources/
        ├── data/                # Test data JSON files
        └── testng.xml           # Test suite config
```

## 🏗 Setup

### 1. Clone the repository
```bash
git clone https://github.com/vanhoan-1510/SeleniumLv2_HoanHoang
cd SeleniumLv2_HoanHoang
```

### 2. Configure test data

Edit `test-suite/src/test/resources/data/test-credentials.json`:
```json
{
  "validUser1": {
    "username": "user1@testarchitect.com",
    "password": "password123"
  },
  "billingDetails": {
    "firstName": "Test",
    "lastName": "User",
    "email": "test.user@example.com",
    "phone": "0123456789",
    "address": "123 Test Street",
    "city": "Test City",
    "state": "TS",
    "postcode": "10000",
    "country": "Test Country"
  }
}
```

Edit `test-suite/src/test/resources/data/test-review.json`:
```json
{
  "reviewData": {
    "name": "Test Reviewer",
    "email": "reviewer@example.com"
  }
}
```

### 3. Build the project
```bash
mvn clean install
```

### 4. Install Allure CLI (optional, for viewing reports)

**Windows:**
```bash
scoop install allure
```

**Mac:**
```bash
brew install allure
```

**Linux:**
```bash
sudo apt-get install allure
```

## 🎯 How to Run Tests

### Run all tests
```bash
mvn clean test
```

### Run specific test case
```bash
mvn test -Dtest=TC01
mvn test -Dtest=TC02
mvn test -Dtest=TC03
mvn test -Dtest=TC04
mvn test -Dtest=TC05
mvn test -Dtest=TC06
mvn test -Dtest=TC07
mvn test -Dtest=TC08
mvn test -Dtest=TC09
mvn test -Dtest=TC10
```

### Run multiple test cases
```bash
mvn test -Dtest=TC01,TC02,TC03
```

### Run tests from IDE
- Open `test-suite/src/test/resources/testng.xml`
- Right-click → Run 'testng.xml'

## 📊 How to View Reports

### Generate and open Allure report
```bash
mvn allure:report
allure open target/allure-report
```

### View history of reports
```bash
allure serve target/allure-results
```

## ⚙️ Configuration

### Browser Options
```bash
mvn test -Dbrowser=chrome
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
```

### Headless Mode
```bash
mvn test -Dheadless=true
```

## ✍️ Writing New Tests

### 1. Create a Page Object
```java
package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class NewPage extends BasePage {
    
    By usernameField = By.id("username");
    By submitButton = By.cssSelector("button[type='submit']");
    
    public void submitForm(String username) {
        $(usernameField).type(username);
        $(submitButton).click();
    }
}
```

### 2. Create a Test Class
```java
package com.hoanhoang.tests;

import com.hoanhoang.pages.NewPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class NewTest extends BaseTest {
    
    @Test
    public void testNewFeature(ITestContext context) {
        NewPage newPage = new NewPage();
        newPage.submitForm("testuser");
        // Add assertions here
    }
}
```

## 🔄 Git Workflow

### Branch Strategy
- `main` - Production code
- `develop` - Development branch (current working branch)
- `feature/*` - New features
- `fix/*` - Bug fixes

### Workflow
1. Create feature branch from `develop`
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/my-feature
   ```

2. Make changes and commit
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

3. Push and create Pull Request
   ```bash
   git push origin feature/my-feature
   ```

4. Request review at: thuong.dang@agest.vn

## 🐛 Troubleshooting

### WebDriver not found
Ensure Chrome browser is installed. Selenium Manager will automatically download the correct ChromeDriver.

### Tests fail with "Element not found"
- Increase timeout in config
- Check if selectors are correct
- Verify element is not in an iframe

### Allure report not generated
- Ensure tests ran successfully
- Check `target/allure-results` folder
- Verify Allure TestNG listener is configured

## 📚 Additional Resources

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [TestNG Documentation](https://testng.org/doc/)
- [Allure Report](https://docs.qameta.io/allure/)
- [Page Object Model](https://www.selenium.dev/documentation/test_practices/encouraged_page_object_models/)

## 📝 License

This project is for learning and demonstration purposes.
