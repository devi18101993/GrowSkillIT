package com.testproject.ECommerceWebsite;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Additional Scenario 2  Product Comparison
 * Objective: Select multiple refrigerators, use the compare feature and verify
 *            the comparison view shows details for each selected product.
 *
 * Steps: search "Refrigerator" -> tick "Compare" on 2 products -> open the
 *        comparison view -> assert 2 products appear with attribute rows.
 */
public class CromaProductComparisonTest extends BaseTest {

    private static final String TERM = "Refrigerator";
    private static final int TO_COMPARE = 2;

    // Locators verified against the live Croma DOM (2026-08-25):
    //   PLP title        -> <h3 class="product-title plp-prod-title">
    //   compare checkbox -> <input type="checkbox" name="addToCompare"
    //                        id="compare{ID}"> per tile (28x13; native input whose
    //                        skin is a sibling, so we JS-click it)
    //   compare CTA      -> <button class="btn btn-default compare-cta">Compare</button>
    //                        in the sticky tray (.cp-compare.plp-compare-tray).
    //                        Clicking it navigates to /compare=products?...
    //   compare columns  -> one <div class="cp-product"> per selected product
    //                        inside the swiper on the Compare page (each holds a
    //                        <p class="product-sub-title">Product Id: NNN</p>)
    private static final By RESULT_TITLES =
            By.cssSelector("h3.product-title, .product-title, h3[class*='product-title']");
    private static final By COMPARE_CHECKBOX = By.cssSelector("input[name='addToCompare']");
    private static final By COMPARE_NOW = By.cssSelector("button.compare-cta, .compare-cta");
    private static final By COMPARE_COLUMNS =
            By.cssSelector(".swiper-slide .cp-product, .compare-product");

    @Test
    public void testCompareTwoProducts() {
        // Step 1: Search for refrigerators.
        SearchHelper.search(driver, wait, TERM);
        wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_TITLES));

        // Step 2: Tick "Compare" on the first two products (helper does the loop).
        List<WebElement> checkboxes = driver.findElements(COMPARE_CHECKBOX);
        Assert.assertTrue(checkboxes.size() >= TO_COMPARE,
                "Not enough products expose a Compare option.");
        markForComparison(checkboxes, TO_COMPARE);
        takeScreenshot("COMPARE_selected");

        // Step 3: Open the comparison view. The tray CTA reports isDisplayed()
        // =false despite a real box, so we click the first rendered match.
        clickFirstVisible(COMPARE_NOW);

        // Step 4: Verify both products are shown side by side.
        wait.until(ExpectedConditions.presenceOfElementLocated(COMPARE_COLUMNS));
        List<WebElement> columns = driver.findElements(COMPARE_COLUMNS);
        Assert.assertEquals(columns.size(), TO_COMPARE,
                "Comparison view did not show the expected number of products.");
        takeScreenshot("COMPARE_view");
        log("Comparison verified for " + columns.size() + " products.");
    }

    /**
     * Tick the first {@code count} compare checkboxes. The native inputs are
     * small (28x13) and their visible skin is a sibling label, so a JS click on
     * the input itself is the reliable way to toggle each one.
     */
    private void markForComparison(List<WebElement> checkboxes, int count) {
        for (int i = 0; i < count; i++) {
            jsClick(checkboxes.get(i));
            log("Marked product " + (i + 1) + " for comparison.");
        }
    }
}