package com.hoanhoang.tests;

import com.hoanhoang.data.TestDataManager;
import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.ProductDetailPage;
import com.hoanhoang.pages.ShopPage;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TC10 extends BaseTest {

    @Test
    public void userCanPostReview(ITestContext context) {
        ShopPage shopPage = new ShopPage();
        ProductDetailPage productDetailPage = new ProductDetailPage();
        HeaderComponent header = new HeaderComponent();

        login();

        header.goToShopPage();
        shopPage.clickFirstProduct();

        if (!productDetailPage.isReviewsTabVisible()) {
            throw new AssertionError("Reviews tab should be visible");
        }

        productDetailPage.scrollToReviewsSection();
        productDetailPage.clickReviewsTab();

        int initialReviewCount = productDetailPage.getReviewCount();

        TestDataManager.ReviewData reviewData = TestDataManager.getReviewData();
        String testComment = "This is a test review from TC10 - " + System.currentTimeMillis();

        productDetailPage.enterReviewName(reviewData.getName());
        productDetailPage.enterReviewEmail(reviewData.getEmail());
        int selectedRating = productDetailPage.selectRandomStarRating();
        if (!productDetailPage.isSelectedRatingActive(selectedRating)) {
            throw new AssertionError("Selected rating should be active: " + selectedRating);
        }
        productDetailPage.enterReviewComment(testComment);
        productDetailPage.submitReview();
        productDetailPage.clickReviewsTab();

        productDetailPage.verifyReviewSubmitted(testComment, initialReviewCount);
    }
}
