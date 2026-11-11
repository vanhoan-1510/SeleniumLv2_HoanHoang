package com.hoanhoang.pages.components;

import static com.hoanhoang.core.element.ElementFactory.$;
import static com.hoanhoang.core.element.ElementFactory.$$;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.core.util.WebDriverWaitHelper;
import com.hoanhoang.data.model.Review;
import com.hoanhoang.data.model.ReviewData;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import org.openqa.selenium.By;

public class ReviewComponent {
    private static final Pattern RATING_TEXT =
            Pattern.compile(
                    "(?:Rated\\s+)?([1-5])(?:\\.0)?\\s+(?:out of|of)\\s+5",
                    Pattern.CASE_INSENSITIVE);
    private static final String RATING_STAR_SELECTOR = "p.stars a.star-%d";
    private static final By COMMENT = By.cssSelector(".description");
    private static final By STAR_RATING = By.cssSelector(".star-rating");
    private final Element reviewsTab = $(By.id("tab_reviews"));
    private final Elements rows = $$(By.cssSelector("#reviews ol.commentlist li.review"));
    private final Element reviewsPanel = $(By.id("reviews"));
    private final Elements ratingFields = $$(By.id("rating"));
    private final Element authorInput = $(By.id("author"));
    private final Element emailInput = $(By.id("email"));
    private final Element commentInput = $(By.id("comment"));
    private final Element submitButton = $(By.id("submit"));
    private final Element submissionError =
            $(By.cssSelector(".woocommerce-error, .wp-die-message"));

    @Step("Open REVIEWS tab")
    public void open() {
        reviewsTab.click();
        reviewsPanel.waitUntilVisible();
    }

    @Step("Select {rating} star(s)")
    public void selectRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be 1..5");
        }
        ratingStar(rating).click();
        ratingFields.waitUntil(
                "Rating selection updated", selected -> readSelectedRating(selected) == rating);
    }

    public int getSelectedRating() {
        return ratingFields.readSnapshot(ReviewComponent::readSelectedRating);
    }

    @Step("Submit review: {comment}")
    public void submit(ReviewData author, String comment) {
        optionalType(authorInput, author.name());
        optionalType(emailInput, author.email());
        commentInput.type(comment);
        BooleanSupplier formReplaced = submitButton.watchForReplacement();
        submitButton.click();
        WebDriverWaitHelper.until(
                "Review submission response",
                d -> {
                    failIfSubmissionError();
                    return hasComment(comment) || formReplaced.getAsBoolean();
                });
        ApplicationWait.idle();
        failIfSubmissionError();
    }

    public Optional<Review> findByComment(String comment) {
        return rows.visible()
                .readSnapshot(
                        visible ->
                                visible.stream()
                                        .filter(row -> readComment(row).equals(comment))
                                        .map(row -> new Review(readComment(row), readRating(row)))
                                        .findFirst());
    }

    private void failIfSubmissionError() {
        ApplicationWait.failIfCriticalError();
        if (submissionError.isVisible()) {
            throw new IllegalStateException(
                    "Review submission failed: " + submissionError.getText());
        }
    }

    private boolean hasComment(String comment) {
        return rows.visible()
                .readSnapshot(
                        visible ->
                                visible.stream().anyMatch(row -> readComment(row).equals(comment)));
    }

    private static int readSelectedRating(List<Element> selected) {
        if (selected.isEmpty()) {
            return 0;
        }
        String value = selected.getFirst().getDomProperty("value");
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }

    private static String readComment(Element row) {
        return row.find(COMMENT).getText();
    }

    private static int readRating(Element row) {
        Element stars = row.find(STAR_RATING);
        String text = stars.getDomAttribute("aria-label");
        if (text == null || text.isBlank()) {
            text = stars.getDomAttribute("title");
        }
        if (text == null || text.isBlank()) {
            text = stars.getDomProperty("textContent");
        }
        var ratingMatcher = RATING_TEXT.matcher(text);
        if (!ratingMatcher.find()) {
            throw new IllegalStateException("Cannot read review rating: " + text);
        }
        return Integer.parseInt(ratingMatcher.group(1));
    }

    private Element ratingStar(int rating) {
        return $(By.cssSelector(RATING_STAR_SELECTOR.formatted(rating)));
    }

    private void optionalType(Element input, String value) {
        if (input.isVisible()) {
            input.type(value);
        }
    }
}
