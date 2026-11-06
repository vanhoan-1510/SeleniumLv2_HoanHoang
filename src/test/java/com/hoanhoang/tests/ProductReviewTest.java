package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ProductDetailPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.components.ReviewComponent;
import java.util.UUID;
import org.testng.annotations.Test;

public class ProductReviewTest extends BaseTest {

    @Test(description = "TC10 - Verify users can post a review")
    public void userCanPostReview() {
        new HomePage().open();
        login();

        new HeaderComponent().goToShopPage();
        new ShopPage().clickFirstProduct();
        ReviewComponent reviews = new ProductDetailPage().reviews();
        reviews.open();

        int rating = 4;
        String comment = "TC10 review " + UUID.randomUUID();
        reviews.selectRating(rating);

        reviews.submit(TestDataManager.getReviewData(), comment);
        reviews.open();

        assertThat(reviews.findByComment(comment))
                .as("Submitted review is visible")
                .hasValueSatisfying(
                        review ->
                                assertThat(review.rating())
                                        .as("Submitted review stars")
                                        .isEqualTo(rating));
    }
}
