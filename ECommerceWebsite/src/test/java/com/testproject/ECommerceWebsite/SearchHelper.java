package com.testproject.ECommerceWebsite;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Reusable "search for a term and land on the results page" step.
 * Scenarios TS-02 (brand filter), TS-03 (sort) and TS-04 (price) all start by
 * searching, so the step is factored out here to keep each test focused (DRY).
 */
public final class SearchHelper {

    // Croma renders TWO search inputs: a hidden #search (0x0) that appears FIRST
    // in the DOM, and the visible #searchV2. A multi-selector fallback would
    // match the hidden #search first (findElement returns document order) and
    // never become clickable — so we target the verified-visible #searchV2 only.
    private static final By SEARCH_BOX = By.id("searchV2");
    private static final By RESULT_TITLES = By.cssSelector(
            "h3.product-title, .product-title, h3[class*='product-title']");

    private SearchHelper() {
    }

    // Product title link. Croma renders it as <h3 class="product-title">
    // <a href="/.../p/<ID>" target="_blank"> — clicking opens a NEW TAB, so we
    // navigate to its href in the same tab to keep the PDP flow single-window.
    private static final By RESULT_LINKS = By.cssSelector("h3.product-title a, .product-title a");

    /** Type {@code term} into the Croma search box and wait for results. */
    public static void search(WebDriver driver, WebDriverWait wait, String term) {
        WebElement box = wait.until(
                ExpectedConditions.elementToBeClickable(SEARCH_BOX));
        box.clear();
        box.sendKeys(term);
        box.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_TITLES));
    }

    /**
     * Open the first search result's product-detail page in the same tab and
     * return its name. Navigates via the link's href (rather than clicking the
     * target="_blank" anchor) so the driver stays on one window.
     */
    public static String openFirstProduct(WebDriver driver, WebDriverWait wait) {
        WebElement link = wait.until(
                ExpectedConditions.presenceOfElementLocated(RESULT_LINKS));
        String name = link.getText();
        String href = link.getAttribute("href");
        driver.get(href);
        return name;
    }
}