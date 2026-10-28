package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.data.model.Review;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ProductDetailPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.components.ReviewComponent;
import java.util.UUID;
import org.testng.annotations.Test;

public class TC10 extends BaseTest {

    @Test(description = "TC10 - Verify users can post a review")
    public void userCanPostReview() {
        new HomePage().open();
        login();

        new HeaderComponent().goToShopPage();
        new ShopPage().clickFirstProduct();
        ReviewComponent reviews = new ProductDetailPage().reviews();
        reviews.reviewsTab().shouldHaveText("Reviews");
        reviews.open();
        int reviewCountBefore = reviews.getCount();

        int rating = 4;
        String comment = "TC10 review " + UUID.randomUUID();
        reviews.selectRating(rating);
        assertThat(reviews.getSelectedRating()).as("Selected review stars").isEqualTo(rating);

        reviews.submit(TestDataManager.getReviewData(), comment);
        reviews.waitForComment(comment);

        assertPublishedReview(reviews, comment, rating);
        assertThat(reviews.getCount())
                .as("Review count increased")
                .isGreaterThan(reviewCountBefore);
    }

    private void assertPublishedReview(ReviewComponent reviews, String comment, int rating) {
        assertThat(reviews.isPendingModeration())
                .as("Review must be published to satisfy TC10")
                .isFalse();
        Review actualReview = reviews.findByComment(comment).orElse(null);
        assertThat(actualReview).as("Review with exact submitted comment").isNotNull();
        assertThat(actualReview.comment()).as("Published review content").isEqualTo(comment);
        assertThat(actualReview.rating()).as("Published review stars").isEqualTo(rating);
    }
}
