package com.hoanhoang.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Random;

public class ProductDetailPage extends BasePage {

    By reviewsTab = By.id("tab_reviews");
    By reviewName = By.cssSelector("#author");
    By reviewEmail = By.cssSelector("#email");
    By reviewComment = By.cssSelector("#comment");
    By submitReviewButton = By.cssSelector("#submit");

    public boolean isReviewsTabVisible() {
        return $(reviewsTab).isVisible();
    }

    public void clickReviewsTab() {
        $(reviewsTab).click();
    }

    public void selectRandomStarRating() {
        int rating = new Random().nextInt(5) + 1;
        By starLocator = By.cssSelector("input[type='radio'][value='" + rating + "']");
        $(starLocator).click();
    }

    public void enterReviewName(String name) {
        $(reviewName).type(name);
    }

    public void enterReviewEmail(String email) {
        $(reviewEmail).type(email);
    }

    public void enterReviewComment(String comment) {
        $(reviewComment).type(comment);
    }

    public void submitReview() {
        $(submitReviewButton).click();
    }

    public void scrollToReviewsSection() {
        WebDriver driver = com.hoanhoang.core.driver.DriverManager.getDriver();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public int getReviewCount() {
        return $$(By.xpath("//div[@class='comment-list']//li")).size();
    }

    public boolean isLatestReviewDisplayed(String comment) {
        By latestReview = By.xpath("//div[@class='comment-list']//p[contains(text(),'" + comment + "')]");
        return $(latestReview).isVisible();
    }
}