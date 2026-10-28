package com.hoanhoang.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.hoanhoang.pages.HeaderComponent;
import com.hoanhoang.pages.HomePage;
import com.hoanhoang.pages.ShopPage;
import java.util.Comparator;
import org.testng.annotations.Test;

public class TC04 extends BaseTest {

    @Test(description = "TC04 - Verify users can sort items by price")
    public void userCanSortItemsByPrice() {
        new HomePage().open();
        login();

        new HeaderComponent().goToShopPage();
        ShopPage shop = new ShopPage();
        shop.haveTitle(ShopPage.TITLE);

        shop.sortByPriceLowToHigh();
        assertThat(shop.getAllProductPrices())
                .as("Visible catalog: ascending prices")
                .hasSizeGreaterThanOrEqualTo(2)
                .isSorted();

        shop.sortByPriceHighToLow();
        assertThat(shop.getAllProductPrices())
                .as("Visible catalog: descending prices")
                .hasSizeGreaterThanOrEqualTo(2)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }
}
