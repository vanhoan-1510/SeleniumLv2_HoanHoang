package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.RetryHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Random;

public class ProductDetailPage extends BasePage {

    By reviewsTab = By.cssSelector("#tab-title-reviews a, a[href='#tab-reviews'], a[href*='reviews']");
    By reviewsSection = By.cssSelector("#reviews, #tab-reviews, #tab_reviews");
    By reviewName = By.cssSelector("#author");
    By reviewEmail = By.cssSelector("#email");
    By reviewComment = By.cssSelector("#comment");
    By submitReviewButton = By.cssSelector("#submit");

    public boolean isReviewsTabVisible() {
        try {
            retryVerification(() -> {
                if (findVisibleReviewTarget() == null) {
                    throw new AssertionError("Reviews tab or reviews section is not visible yet");
                }
            });
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    public void clickReviewsTab() {
        WebElement reviewTarget = findVisibleReviewTarget();
        if (reviewTarget == null) {
            throw new AssertionError("Reviews tab is not visible. URL: " + getCurrentUrl());
        }
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", reviewTarget);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", reviewTarget);
    }

    public int selectRandomStarRating() {
        int rating = new Random().nextInt(5) + 1;
        selectStarRating(rating);
        return rating;
    }

    private void selectStarRating(int rating) {
        By starLink = By.cssSelector("p.stars a.star-" + rating);
        retryStep(() -> {
            List<WebElement> visibleStars = DriverManager.getDriver().findElements(starLink);
            if (!visibleStars.isEmpty() && visibleStars.get(0).isDisplayed()) {
                WebElement star = visibleStars.get(0);
                ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", star);
                new Actions(DriverManager.getDriver()).moveToElement(star).click().perform();
            }

            setRatingValue(rating);

            if (!isRatingFormValueSelected(rating)) {
                throw new AssertionError("Rating form value is not selected yet: " + rating);
            }

            if (!isSelectedRatingActive(rating)) {
                throw new AssertionError("Rating is not selected yet: " + rating);
            }
        });
    }

    public void enterReviewName(String name) {
        if ($(reviewName).isVisible()) {
            $(reviewName).type(name);
        }
    }

    public void enterReviewEmail(String email) {
        if ($(reviewEmail).isVisible()) {
            $(reviewEmail).type(email);
        }
    }

    public void enterReviewComment(String comment) {
        $(reviewComment).type(comment);
    }

    public boolean isSelectedRatingActive(int rating) {
        try {
            retryStep(() -> {
                boolean selectValue = Boolean.TRUE.equals(((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                        "var rating = document.querySelector('#rating, select[name=\"rating\"]');" +
                                "return !!rating && rating.value === '" + rating + "';"
                ));
                boolean checked = Boolean.TRUE.equals(((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                        "var input = document.querySelector(\"input[type='radio'][value='" + rating + "']\");" +
                                "return !!input && input.checked;"
                ));
                boolean active = DriverManager.getDriver().findElements(By.cssSelector("p.stars a.star-" + rating)).stream()
                        .anyMatch(star -> {
                            String className = star.getAttribute("class");
                            String parentClassName = star.findElement(By.xpath("..")).getAttribute("class");
                            return (className != null && className.contains("active"))
                                    || (parentClassName != null && parentClassName.contains("selected"));
                        });

                if (!selectValue && !checked && !active) {
                    throw new AssertionError("Selected rating is not active yet");
                }
            });
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    private void setRatingValue(int rating) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "var rating = document.querySelector('#rating, select[name=\"rating\"]');" +
                        "if (rating) {" +
                        "  rating.value = arguments[0];" +
                        "  rating.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "  rating.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "}" +
                        "var radio = document.querySelector(\"input[type='radio'][value='\" + arguments[0] + \"']\");" +
                        "if (radio) {" +
                        "  radio.checked = true;" +
                        "  radio.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "}",
                String.valueOf(rating)
        );
    }

    public void submitReview() {
        $(submitReviewButton).click();
    }

    public void scrollToReviewsSection() {
        WebDriver driver = com.hoanhoang.core.driver.DriverManager.getDriver();
        List<WebElement> sections = driver.findElements(reviewsSection);
        if (!sections.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", sections.get(0));
        } else {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        }
    }

    public int getReviewCount() {
        return $$(By.xpath("//ol[contains(@class,'commentlist')]//li[contains(@class,'review')] | //div[contains(@class,'comment-list')]//li")).size();
    }

    public boolean isLatestReviewDisplayed(String comment) {
        String expected = normalizeReviewText(comment);
        try {
            retryVerification(() -> {
                String actual = normalizeReviewText(getBodyText());
                if (!actual.contains(expected)) {
                    throw new AssertionError("Review comment is not displayed yet");
                }
            });
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    public void verifyReviewSubmitted(String comment, int initialReviewCount) {
        String expected = normalizeReviewText(comment);
        retryVerification(() -> {
            if (isReviewSubmittedForModeration()) {
                return;
            }

            String actual = normalizeReviewText(getBodyText());
            if (!actual.contains(expected)) {
                throw new AssertionError("Review comment is not displayed yet: " + comment
                        + ". URL: " + getCurrentUrl()
                        + ". Review section/message: " + getReviewSectionText());
            }

            int currentReviewCount = getReviewCount();
            if (currentReviewCount <= initialReviewCount) {
                throw new AssertionError("Number of reviews should be updated. Initial: "
                        + initialReviewCount + ", Current: " + currentReviewCount);
            }
        });
    }

    private boolean isReviewSubmittedForModeration() {
        String currentUrl = getCurrentUrl();
        return currentUrl.contains("unapproved=")
                && currentUrl.contains("moderation-hash=")
                && currentUrl.contains("#comment-");
    }

    private boolean isRatingFormValueSelected(int rating) {
        return Boolean.TRUE.equals(((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "var expected = String(arguments[0]);" +
                        "var rating = document.querySelector('#rating, select[name=\"rating\"]');" +
                        "var radio = document.querySelector(\"input[type='radio'][value='\" + expected + \"']\");" +
                        "return (!!rating && rating.value === expected) || (!!radio && radio.checked);",
                String.valueOf(rating)
        ));
    }

    public boolean isReviewRatingDisplayed(String comment, int rating) {
        By reviewRating = By.xpath("//li[contains(@class,'review')][contains(normalize-space(.),"
                + xpathLiteral(comment) + ")]//*[contains(@class,'star-rating')]");
        String expectedRating = "Rated " + rating + " out of 5";
        try {
            retryStep(() -> {
                List<WebElement> ratings = DriverManager.getDriver().findElements(reviewRating);
                if (ratings.isEmpty()) {
                    ratings = DriverManager.getDriver().findElements(By.xpath(
                            "(//ol[contains(@class,'commentlist')]//li[contains(@class,'review')] | //div[contains(@class,'comment-list')]//li)[1]//*[contains(@class,'star-rating')]"
                    ));
                }
                if (ratings.isEmpty()) {
                    throw new AssertionError("Review rating is not displayed yet");
                }

                String actualRating = getRatingText(ratings.get(0));
                if (!actualRating.toLowerCase().contains(expectedRating.toLowerCase())) {
                    throw new AssertionError("Expected review rating: " + expectedRating + ". Actual: " + actualRating);
                }
            });
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    private String normalizeReviewText(String text) {
        return text == null ? "" : text.replace('\u2013', '-')
                .replace('\u2014', '-')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public String getReviewSectionText() {
        return DriverManager.getDriver().findElements(By.cssSelector("#reviews, .woocommerce-error, .woocommerce-message"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .reduce("", (left, right) -> left + "\n" + right)
                .trim();
    }

    public String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    public String getBodyText() {
        List<WebElement> body = DriverManager.getDriver().findElements(By.tagName("body"));
        return body.isEmpty() ? "" : body.get(0).getText();
    }

    private String getRatingText(WebElement ratingElement) {
        String ariaLabel = ratingElement.getAttribute("aria-label");
        if (ariaLabel != null && !ariaLabel.trim().isEmpty()) {
            return ariaLabel.trim();
        }

        String title = ratingElement.getAttribute("title");
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }

        Object text = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "return (arguments[0].innerText || arguments[0].textContent || '').trim();",
                ratingElement
        );
        String textValue = text == null ? "" : text.toString();
        if (!textValue.trim().isEmpty()) {
            return textValue.trim();
        }

        String style = ratingElement.getAttribute("style");
        if (style != null && style.contains("width")) {
            for (int rating = 1; rating <= 5; rating++) {
                if (style.contains((rating * 20) + "%")) {
                    return "Rated " + rating + " out of 5";
                }
            }
        }

        return "";
    }

    private void retryStep(Runnable action) {
        RetryHandler.retryUntilTimeout(
                action,
                ConfigLoader.getConfig().getStepRetryTimeoutMs(),
                ConfigLoader.getConfig().getStepRetryIntervalMs()
        );
    }

    private void retryVerification(Runnable action) {
        RetryHandler.retryUntilTimeout(
                action,
                ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(),
                ConfigLoader.getConfig().getStepRetryIntervalMs()
        );
    }

    private WebElement findVisibleReviewTarget() {
        List<WebElement> tabLinks = DriverManager.getDriver().findElements(reviewsTab);
        for (WebElement tabLink : tabLinks) {
            if (tabLink.isDisplayed()) {
                return tabLink;
            }
        }

        List<WebElement> sections = DriverManager.getDriver().findElements(reviewsSection);
        for (WebElement section : sections) {
            if (section.isDisplayed()) {
                return section;
            }
        }

        return null;
    }

    private String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        if (!text.contains("\"")) {
            return "\"" + text + "\"";
        }
        return "concat('" + text.replace("'", "',\"'\",'") + "')";
    }
}
