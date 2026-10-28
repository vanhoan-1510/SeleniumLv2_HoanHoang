package com.hoanhoang.pages.components;

import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.Review;
import com.hoanhoang.data.model.ReviewData;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public class ReviewComponent {
    private static final Pattern RATING_TEXT =
            Pattern.compile(
                    "(?:Rated\\s+)?([1-5])(?:\\.0)?\\s+(?:out of|of)\\s+5",
                    Pattern.CASE_INSENSITIVE);
    private final By rows = By.cssSelector("#reviews ol.commentlist li.review");
    private final By tab = By.id("tab_reviews");

    @Step("Open REVIEWS tab")
    public void open() {
        UiWait.click(tab);
        UiWait.visible(By.id("reviews"));
    }

    public Element reviewsTab() {
        return ElementFactory.$(tab);
    }

    @Step("Select {rating} star(s)")
    public void selectRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be 1..5");
        }
        UiWait.click(By.cssSelector("p.stars a.star-" + rating));
        UiWait.until("Rating selection updated", d -> getSelectedRating() == rating);
    }

    public int getSelectedRating() {
        var selected = DriverManager.getDriver().findElements(By.id("rating"));
        if (selected.isEmpty()) {
            return 0;
        }
        String value = selected.get(0).getDomProperty("value");
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }

    @Step("Submit review: {comment}")
    public void submit(ReviewData author, String comment) {
        optionalType("author", author.name());
        optionalType("email", author.email());
        WebElement input = UiWait.clickable(By.id("comment"));
        input.clear();
        input.sendKeys(comment);
        WebElement submitButton = UiWait.clickable(By.id("submit"));
        submitButton.click();
        UiWait.until(
                "Review submission response",
                d ->
                        isPendingModeration()
                                || findByComment(comment).isPresent()
                                || UiWait.isVisible(
                                        By.cssSelector(".woocommerce-error, .wp-die-message"))
                                || isStale(submitButton));
        ApplicationWait.idle();
    }

    public int getCount() {
        return UiWait.visibleElements(rows).size();
    }

    public Optional<Review> findByComment(String comment) {
        return UiWait.until(
                "Read review rows",
                d -> {
                    List<Review> matches =
                            UiWait.visibleElements(rows).stream()
                                    .filter(row -> readComment(row).equals(comment))
                                    .map(row -> new Review(readComment(row), readRating(row)))
                                    .toList();
                    return matches.stream().findFirst();
                });
    }

    public boolean isPendingModeration() {
        String url = DriverManager.getDriver().getCurrentUrl();
        return url.contains("unapproved=") && url.contains("moderation-hash=");
    }

    public void waitForComment(String comment) {
        UiWait.until(
                "Submitted review visible or pending moderation",
                d -> isPendingModeration() || findByComment(comment).isPresent());
    }

    private static String readComment(WebElement row) {
        return row.findElement(By.cssSelector(".description")).getText().trim();
    }

    private static int readRating(WebElement row) {
        WebElement stars = row.findElement(By.cssSelector(".star-rating"));
        String text = stars.getAttribute("aria-label");
        if (text == null || text.isBlank()) {
            text = stars.getAttribute("title");
        }
        if (text == null || text.isBlank()) {
            text = stars.getAttribute("textContent");
        }
        var ratingMatcher = RATING_TEXT.matcher(text);
        if (!ratingMatcher.find()) {
            throw new IllegalStateException("Cannot read review rating: " + text);
        }
        return Integer.parseInt(ratingMatcher.group(1));
    }

    private boolean isStale(WebElement element) {
        try {
            element.isEnabled();
            return false;
        } catch (StaleElementReferenceException e) {
            return true;
        }
    }

    private void optionalType(String id, String value) {
        if (UiWait.isVisible(By.id(id))) {
            WebElement element = UiWait.clickable(By.id(id));
            element.clear();
            element.sendKeys(value);
        }
    }
}
