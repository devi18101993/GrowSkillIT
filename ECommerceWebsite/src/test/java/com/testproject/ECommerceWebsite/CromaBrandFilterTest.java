package com.testproject.ECommerceWebsite;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TS-02  Brand Filtering
 * Objective: Ensure the brand filter works when selecting multiple brands
 *            (Samsung, LG, Whirlpool) and only those brands are shown.
 *
 * Steps: search "Refrigerator" -> tick Samsung/LG/Whirlpool in the Brand facet
 *        -> verify every visible product title belongs to one of those brands.
 *
 * The @Test method is a straight line; the per-brand and per-title iteration
 * live in the private helpers below.
 */
public class CromaBrandFilterTest extends BaseTest {

    private static final String TERM = "Refrigerator";
    private static final String[] BRANDS = {"Samsung", "LG", "Whirlpool"};

    private static final By RESULT_TITLES =
            By.cssSelector("h3.product-title, .product-title, h3[class*='product-title']");

    @Test
    public void testBrandFilterShowsOnlySelectedBrands() {
        // Step 1: Search for refrigerators.
        SearchHelper.search(driver, wait, TERM);
        takeScreenshot("TS02_before_brand_filter");

        // Step 2: Apply each brand filter (helper handles the per-brand loop).
        applyBrandFilters(BRANDS);
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_TITLES));
        takeScreenshot("TS02_after_brand_filter");

        // Step 3: Verify results exist and every product matches a selected brand.
        List<WebElement> titles = driver.findElements(RESULT_TITLES);
        Assert.assertFalse(titles.isEmpty(), "No products displayed after applying brand filters.");
        String offending = firstBrandMismatch(titles, BRANDS);
        Assert.assertNull(offending,
                "Product not from a selected brand (Samsung/LG/Whirlpool): " + offending);
        log("TS-02 verified " + titles.size() + " products all belong to the selected brands.");
    }

    /** Tick each brand checkbox in turn, waiting for the grid to refresh. */
    private void applyBrandFilters(String[] brands) {
        for (String brand : brands) {
            applyBrandFilter(brand);
        }
    }

    /**
     * Tick a single brand checkbox inside the Brand facet.
     *
     * Croma facet markup: <ul class="filter-list cp-checkbox"> ... <label>
     * <span class="text">{Brand} (count)</span></label>. Clicking the label
     * toggles the checkbox; we match the brand text case-insensitively and
     * scope it to the filter-list so we don't hit unrelated checkboxes.
     *
     * The facet label reports isDisplayed()=false despite a real box (Croma's
     * CSS transforms), so elementToBeClickable times out. We click the first
     * rendered match via clickFirstVisible (box-based + JS click) instead.
     */
    private void applyBrandFilter(String brand) {
        By brandLabel = By.xpath(
                "//ul[contains(@class,'filter-list')]"
                        + "//label[.//span[contains(@class,'text') and "
                        + "starts-with(translate(normalize-space(.),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                        + "'" + brand.toLowerCase() + "')]]");
        clickFirstVisible(brandLabel);
        log("TS-02 applied brand filter: " + brand);
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_TITLES));
    }

    /**
     * Return the text of the first product title that does NOT belong to any of
     * the given brands, or {@code null} when every title matches.
     */
    private String firstBrandMismatch(List<WebElement> titles, String[] brands) {
        for (WebElement title : titles) {
            String text = title.getText();
            if (!matchesAnyBrand(text, brands)) {
                return text;
            }
        }
        return null;
    }

    private boolean matchesAnyBrand(String text, String[] brands) {
        String lower = text.toLowerCase();
        for (String brand : brands) {
            if (lower.contains(brand.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}