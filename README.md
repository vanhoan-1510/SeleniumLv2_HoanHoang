# Selenium Automation Framework

A robust Java-based test automation framework using Selenium 4, TestNG, and Allure Report.

## 🚀 Features

- **Multi-browser support**: Chrome, Firefox, Edge
- **Page Object Model**: Clean and maintainable test architecture
- **Data-driven testing**: JSON data models and builders
- **Allure Reports**: Beautiful HTML test reports with screenshots
- **Environment configuration**: Easy switching between dev, staging, prod
- **Headless mode**: Run tests without GUI for CI/CD
- **Parallel execution**: TestNG support for parallel testing

## 🛠 Tech Stack

- **Java 11+**
- **Selenium 4.18.1**
- **TestNG 7.9.0**
- **Allure Report 2.25.0**
- **Maven 3.8+**
- **Log4j2** for logging
- **Gson** for JSON handling

## 📋 Prerequisites

1. **Java 11+** installed and configured
   ```bash
   java -version
   ```
2. **Maven 3.8+** installed
   ```bash
   mvn -version
   ```
3. **Chrome browser** (latest version)
4. **ChromeDriver** (automatically managed by Selenium Manager)

## 📦 Project Structure

```
SeleniumLv2_HoanHoang/
├── framework-core/              # Core framework components
│   ├── config/                  # Configuration management
│   │   ├── ConfigLoader.java    # Load environment configs
│   │   └── EnvironmentConfig.java  # Config model
│   ├── driver/                  # WebDriver management
│   │   ├── DriverFactory.java   # Create WebDriver instances
│   │   ├── DriverManager.java   # Thread-safe driver storage
│   │   └── BrowserType.java     # Browser enum
│   └── element/                 # Element wrappers
│       ├── BaseElement.java     # Enhanced WebElement wrapper
│       ├── Element.java         # WebElement interface
│       └── ElementFactory.java  # Create element wrappers
│
├── framework-data/              # Data management
│   ├── model/                   # Data models
│   │   └── UserDto.java         # User data model
│   ├── builder/                 # Builder pattern
│   │   └── UserBuilder.java     # Build test data
│   ├── constant/                # Constants
│   │   └── AppConstant.java     # Application constants
│   └── util/                    # Utilities
│       └── JsonMapper.java      # JSON conversion
│
├── framework-report/            # Reporting
│   ├── allure/                  # Allure integration
│   │   └── AllureManager.java   # Screenshot, video, logs
│   ├── listener/                # TestNG listeners
│   │   └── TestListener.java    # Test lifecycle hooks
│   └── logger/                  # Logging
│       └── Log.java             # Log4j2 wrapper
│
└── test-suite/                  # Test implementations
    ├── src/test/
    │   ├── java/
    │   │   ├── pages/           # Page Object Model
    │   │   │   └── BasePage.java
    │   │   └── tests/           # Test classes
    │   │       ├── BaseTest.java    # Test setup/teardown
    │   │       └── LoginTest.java   # Example test
    │   └── resources/
    │       ├── config/         # Environment configs
    │       │   ├── dev.properties
    │       │   ├── staging.properties
    │       │   └── sample.properties
    │       └── testng.xml      # Test suite config
    └── pom.xml
```

## 🏗 Setup

### 1. Clone the repository
```bash
git clone https://github.com/vanhoan-1510/SeleniumLv2_HoanHoang
cd SeleniumLv2_HoanHoang
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Configure environment

Copy the sample config and customize:
```bash
cp test-suite/src/test/resources/config/sample.properties \
   test-suite/src/test/resources/config/dev.properties
```

Edit `dev.properties`:
```properties
baseUrl=https://your-application-url.com
browser=chrome
headless=false
timeout=30
```

### 4. Install Allure CLI (optional, for viewing reports)

**Windows:**
```bash
scoop install allure
```
or download from: https://docs.qameta.io/allure/#_installing_a_commandline

**Mac:**
```bash
brew install allure
```

**Linux:**
```bash
sudo apt-get install allure
```

## 🎯 How to Run Tests

### Run all tests (default environment: dev)
```bash
cd test-suite
mvn clean test
```

### Run tests for specific environment
```bash
mvn clean test -Denv=staging
mvn clean test -Denv=prod
```

### Run specific test class
```bash
mvn test -Dtest=LoginTest
```

### Run specific test method
```bash
mvn test -Dtest=LoginTest#testValidLogin
```

### Run tests in headless mode
Set in config file: `headless=true`

### Run tests from IDE
- Open `test-suite/src/test/resources/testng.xml`
- Right-click → Run 'testng.xml'

## 📊 How to View Reports

### Generate and open Allure report
```bash
cd test-suite
mvn allure:report
allure open target/allure-results
```

### Generate report only
```bash
mvn allure:report
```
Report will be in: `test-suite/target/allure-report/index.html`

### View history of reports
```bash
allure serve target/allure-results
```
This starts a local server and opens the report in browser.

## ⚙️ Configuration

### Browser Options
Available browsers in `BrowserType.java`:
- `chrome`
- `firefox`
- `edge`

Set in config file:
```properties
browser=chrome
```

### Headless Mode
```properties
headless=true
```
Useful for CI/CD pipelines.

### Timeout
Default wait time for explicit waits:
```properties
timeout=30
```
Recommended: 10-60 seconds depending on application.

## ✍️ Writing New Tests

### 1. Create a Page Object
```java
package com.hoanhoang.pages;

import com.hoanhoang.core.element.BaseElement;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    
    private final BaseElement usernameField = new BaseElement(By.id("username"));
    private final BaseElement passwordField = new BaseElement(By.id("password"));
    private final BaseElement loginButton = new BaseElement(By.cssSelector("button[type='submit']"));
    
    public void login(String username, String password) {
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }
}
```

### 2. Create a Test Class
```java
package com.hoanhoang.tests;

import com.hoanhoang.pages.LoginPage;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    
    @Test
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.login("testuser", "password123");
        // Add assertions here
    }
}
```

### 3. Add to testng.xml
```xml
<test name="Authentication Tests">
    <classes>
        <class name="com.hoanhoang.tests.LoginTest"/>
    </classes>
</test>
```

### 4. Run your test
```bash
mvn test -Dtest=LoginTest
```

## 📁 Environment Configurations

Create environment-specific configs in `test-suite/src/test/resources/config/`:

- `dev.properties` - Development environment
- `staging.properties` - Staging environment
- `prod.properties` - Production environment

Run with specific environment:
```bash
mvn test -Denv=staging
```

## 🔄 Git Workflow

### Branch Naming
- `feature/feature-name` - New features
- `fix/bug-description` - Bug fixes
- `chore/task-description` - Maintenance tasks

### Workflow
1. Create branch from `develop`
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

3. Push to remote
   ```bash
   git push origin feature/my-feature
   ```

4. Create Pull Request to `develop`
   - Request code review
   - Fix any issues
   - Merge after approval

5. Update local `develop`
   ```bash
   git checkout develop
   git pull origin develop
   ```

## 🐛 Troubleshooting

### WebDriver not found
Ensure Chrome browser is installed. Selenium Manager will automatically download the correct ChromeDriver.

### Tests fail with "Element not found"
- Increase timeout in config file
- Check if selectors are correct
- Verify element is not in an iframe

### Allure report not generated
- Ensure tests ran successfully
- Check `target/allure-results` folder
- Verify Allure TestNG listener is configured

### Config file not found
- Ensure config file exists in `test-suite/src/test/resources/config/`
- Use correct `-Denv` parameter
- Check file name matches environment name

## 📚 Additional Resources

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [TestNG Documentation](https://testng.org/doc/)
- [Allure Report](https://docs.qameta.io/allure/)
- [Page Object Model](https://www.selenium.dev/documentation/test_practices/encouraged_page_object_models/)