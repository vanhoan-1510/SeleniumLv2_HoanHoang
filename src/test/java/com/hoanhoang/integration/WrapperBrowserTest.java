package com.hoanhoang.integration;

import static com.hoanhoang.core.element.ElementFactory.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.BrowserType;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.util.JavaScript;
import com.hoanhoang.data.constant.PaymentMethod;
import com.hoanhoang.data.model.BillingDetails;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.data.model.ReviewData;
import com.hoanhoang.integration.support.HtmlFixture;
import com.hoanhoang.pages.CheckoutPage;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.OrderHistoryPage;
import com.hoanhoang.pages.ShopPage;
import com.hoanhoang.pages.components.BillingFormComponent;
import com.hoanhoang.pages.components.CartTableComponent;
import com.hoanhoang.pages.components.PaymentComponent;
import com.hoanhoang.pages.components.ReviewComponent;
import com.hoanhoang.pages.support.ApplicationWait;
import com.hoanhoang.tests.support.PurchaseAssertions;
import java.io.IOException;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Optional Chrome checks on local HTML. Does not contact the demo site or use real accounts. */
public class WrapperBrowserTest {
    @BeforeClass
    public void startBrowser() {
        ConfigLoader.load("dev");
        DriverManager.startDriver(BrowserType.CHROME, true);
    }

    @BeforeMethod
    public void resetDocument() throws IOException {
        HtmlFixture.open("fixtures/wrapper-page.html");
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        DriverManager.quitDriver();
    }

    @Test
    public void cartReadsWaitForBlankCellsButRejectInvalidPrices() {
        script(
                """
                const price = document.querySelector('.product-price .amount');
                price.textContent = '';
                setTimeout(() => price.textContent = '$10.00', 500);
                """);
        CartTableComponent cart = new CartTableComponent();

        assertThat(cart.getItem("AirPods").unitPrice()).isEqualByComparingTo("10.00");

        script("document.querySelector('.product-price .amount').textContent = 'invalid price';");
        assertThatThrownBy(cart::getItems)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot parse USD price");
    }

    @Test
    public void addToCartKeepsTheDetailsButtonWhenTheHoverButtonAppears() {
        Product product = new ShopPage().addFirstProductToCart();

        assertThat(product.name()).isEqualTo("AirPods");
        assertThat(script("return window.catalogAdds;")).isEqualTo(1L);
        assertThat(script("return window.hoverCartClicks;")).isEqualTo(0L);
        assertThat($(By.cssSelector(".footer-product a")).getDomAttribute("class"))
                .doesNotContain("added");
    }

    @Test
    public void readinessTimeoutReportsTheRequestsAndBlockersStillActive() {
        script(
                """
                window.jQuery = {active: 2};
                const blocker = document.createElement('div');
                blocker.className = 'blockUI blockOverlay';
                blocker.style.cssText = 'position:fixed;inset:0';
                document.body.append(blocker);
                """);

        assertThatThrownBy(ApplicationWait::idle)
                .isInstanceOf(TimeoutException.class)
                .hasMessageContaining(
                        "readyState", "complete", "jqueryActive", "2", "blockOverlay");
    }

    @Test
    public void nestedElementsFollowRealDomReplacement() {
        Element title =
                $(By.cssSelector(".content-product")).find(By.cssSelector("h2.product-title"));
        assertThat(title.getText()).isEqualTo("AirPods");

        script(
                "const card = document.querySelector('.content-product');"
                        + "const replacement = card.cloneNode(true);"
                        + "replacement.querySelector('h2').textContent = 'Replacement';"
                        + "card.replaceWith(replacement);");

        assertThat(title.getText()).isEqualTo("Replacement");
    }

    @Test
    public void billingBlurAndHiddenPaymentSelectionWork() {
        var billing =
                new BillingDetails(
                        "Test",
                        "Customer",
                        "123 Test Street",
                        "New York",
                        "10001",
                        "2025550123",
                        "customer@example.com");
        new BillingFormComponent().fill(billing);

        assertThat(script("return window.billingChanges;")).isEqualTo(7L);
        assertThat(script("return document.activeElement.tagName;")).isEqualTo("BODY");
        assertThat($(By.id("billing_email")).getValue()).isEqualTo(billing.email());

        PaymentComponent payment = new PaymentComponent();
        payment.select(PaymentMethod.CHEQUE);
        assertThat($(By.id("payment_method_cheque")).isVisible()).isFalse();
        assertThat($(By.id("payment_method_cheque")).isSelected()).isTrue();
        assertThat(payment.getSelectedMethod()).isEqualTo(PaymentMethod.CHEQUE.getDisplayName());
    }

    @Test
    public void sortingWaitsForReplacementAndSelectsBothValues() {
        ShopPage shop = new ShopPage();
        shop.sortByPriceLowToHigh();
        shop.sortByPriceHighToLow();

        assertThat($(By.name("orderby")).getDomProperty("value")).isEqualTo("price-desc");
        assertThat(script("return window.sortChanges;")).isEqualTo(2L);
    }

    @DataProvider(name = "cartAutoUpdateDelays")
    public Object[][] cartAutoUpdateDelays() {
        return new Object[][] {{1500}, {0}};
    }

    @Test(dataProvider = "cartAutoUpdateDelays")
    public void cartTypingClicksUpdateAndWaitsForTheServerSnapshot(int autoUpdateDelay) {
        script("window.cartAutoUpdateDelay = " + autoUpdateDelay + ";");
        Product product = new ShopPage().addFirstProductToCart();
        assertThat(product.name()).isEqualTo("AirPods");
        CartTableComponent cart = new CartTableComponent();
        cart.setQuantity(product.name(), 4);

        assertThat(cart.getItem(product.name()).quantity()).isEqualTo(4);
        assertThat(cart.getItem(product.name()).subtotal()).isEqualByComparingTo("40.00");
        assertThat(script("return window.manualCartUpdates;")).isEqualTo(1L);
        assertThat(script("return window.cartInputChanges;")).isEqualTo(1L);

        cart.changeQuantity(product.name(), -1);
        assertThat(cart.getItem(product.name()).quantity()).isEqualTo(3);
        assertThat(cart.getItem(product.name()).subtotal()).isEqualByComparingTo("30.00");
        assertThat(script("return window.manualCartUpdates;")).isEqualTo(1L);
        assertThat(script("return window.cartInputChanges;")).isEqualTo(2L);

        cart.removeAll();
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    public void completedCartUpdateWithWrongQuantityReachesTheAssertion() {
        assertCartUpdateRejected("window.cartResponseQuantity = 2;", "quantity");
    }

    @Test
    public void completedCartUpdateWithWrongSubtotalReachesTheAssertion() {
        assertCartUpdateRejected("window.cartResponseSubtotal = '$7.00';", "subtotal");
    }

    private void assertCartUpdateRejected(String responseOverride, String expectation) {
        script(responseOverride);
        Product product = new ShopPage().addFirstProductToCart();
        CartTableComponent cart = new CartTableComponent();
        cart.setQuantity(product.name(), 4);

        assertThatThrownBy(
                        () ->
                                PurchaseAssertions.assertCart(
                                        cart.getItems(),
                                        List.of(product, product, product, product)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(expectation);
    }

    @Test
    public void reviewsReadHiddenRatingAndSubmittedRowsAfterReopeningTheTab() {
        ReviewComponent reviews = new ReviewComponent();
        reviews.open();
        reviews.selectRating(4);
        assertThat(reviews.getSelectedRating()).isEqualTo(4);

        reviews.submit(
                new ReviewData("Test Reviewer", "reviewer@example.com"), "Wrapper browser review");
        assertThat(reviews.findByComment("Wrapper browser review")).isEmpty();
        reviews.open();
        assertThat(reviews.findByComment("Wrapper browser review"))
                .hasValueSatisfying(review -> assertThat(review.rating()).isEqualTo(4));
    }

    @Test
    public void pendingReviewIsFoundWhenItIsVisible() {
        script("window.reviewPending = true;");
        ReviewComponent reviews = submitFixtureReview("Pending review");

        assertThat(reviews.findByComment("Pending review"))
                .hasValueSatisfying(review -> assertThat(review.rating()).isEqualTo(4));
    }

    @Test
    public void submittedReviewReadsTheActualStarsInsteadOfTheSelectedRating() {
        script("window.reviewResponseRating = 2;");
        ReviewComponent reviews = submitFixtureReview("Wrong stars review");

        assertThat(reviews.findByComment("Wrong stars review"))
                .hasValueSatisfying(review -> assertThat(review.rating()).isEqualTo(2));
    }

    private ReviewComponent submitFixtureReview(String comment) {
        ReviewComponent reviews = new ReviewComponent();
        reviews.open();
        reviews.selectRating(4);
        reviews.submit(new ReviewData("Test Reviewer", "reviewer@example.com"), comment);
        reviews.open();
        return reviews;
    }

    @Test
    public void hiddenOrUnrelatedReviewsDoNotCountAsTheSubmittedReview() {
        script("window.reviewHidden = true;");
        ReviewComponent reviews = submitFixtureReview("Hidden review");

        assertThat($(By.cssSelector("#reviews li.review")).exists()).isTrue();
        assertThat(reviews.findByComment("Hidden review")).isEmpty();
        assertThat(reviews.findByComment("Different review")).isEmpty();
    }

    @Test
    public void rejectedReviewReportsTheSiteMessageInsteadOfWaitingForAComment() {
        script("window.reviewError = 'Review rejected: please try again later.';");
        ReviewComponent reviews = new ReviewComponent();
        reviews.open();
        reviews.selectRating(4);

        assertThatThrownBy(
                        () ->
                                reviews.submit(
                                        new ReviewData("Test Reviewer", "reviewer@example.com"),
                                        "Rejected review"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Review submission failed", "Review rejected: please try again later.");
    }

    @Test
    public void validationMessagesDoNotSubstituteForHighlightedFields() {
        script(
                """
                document.querySelector('form.checkout').insertAdjacentHTML('afterbegin',
                  '<ul class="woocommerce-error"><li data-id="billing_postcode">Postcode is required.</li></ul>');
                """);
        CheckoutPage checkout = new CheckoutPage();
        assertThat(checkout.getFieldErrorMessages()).containsKey("billing_postcode");
        assertThat(checkout.getInvalidBillingFieldIds()).isEmpty();
        assertThat(checkout.isPostcodeRequired()).isTrue();

        script(
                "document.getElementById('billing_postcode_field').classList.add('woocommerce-invalid');");
        assertThat(checkout.getInvalidBillingFieldIds()).containsExactly("billing_postcode");
        script(
                "document.getElementById('billing_postcode_field').classList.remove('validate-required');");
        assertThat(checkout.isPostcodeRequired()).isFalse();
    }

    @Test
    public void loginReportsCriticalErrorsBeforeWaitingForAccountNavigation() {
        assertThatThrownBy(() -> new LoginPage().login("fixture-user", "fixture-password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WordPress critical error");
    }

    @Test
    public void checkoutReportsCriticalErrorsBeforeWaitingForItsForm() {
        script(
                "document.body.innerHTML = '<p>There has been a critical error on this website</p>';");

        assertThatThrownBy(() -> new CheckoutPage().waitUntilLoaded())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WordPress critical error");
    }

    @Test
    public void orderHistoryReportsCriticalErrorsWhileWaitingForItsTable() {
        script(
                "setTimeout(() => document.body.innerHTML ="
                        + " '<p>There has been a critical error on this website</p>', 300);");
        assertThatThrownBy(() -> new OrderHistoryPage().getOrders())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WordPress critical error");
    }

    private static Object script(String script) {
        return JavaScript.execute(script);
    }
}
