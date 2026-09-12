package com.testproject.ECommerceWebsite;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TS-03  Sorting by Discount
 * Objective: Verify products can be sorted in descending order of discount.
 *
 * Steps: search "Refrigerator" -> open the Sort dropdown -> choose the
 *        "Discount" option -> read each product's discount % and assert a
 *        descending trend (top item = max discount; top half averages higher
 *        than the bottom half). Croma's sort is relevance-weighted, so adjacent
 *        values are not strictly monotonic and a trend check is the robust one.
 *
 * The @Test method is a straight line; reading discounts and checking the
 * ordering happen in the private helpers below.
 */
public class CromaSortByDiscountTest extends BaseTest {

    private static final String TERM = "Refrigerator";

    // Verified live DOM (2026-08-25): the sort trigger has a hidden 0x0 mobile
    // twin (.sort-by.icon-text) first in the DOM and a VISIBLE div.sort-by; the
    // dropdown option is <li data-testid="sort">Discount (Descending)</li>.
    private static final By SORT_TRIGGER = By.cssSelector("div.sort-by");
    private static final By DISCOUNT_OPTION = By.xpath(
            "//li[@data-testid='sort'][contains(translate(normalize-space(.),"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'discount')]");
    private static final By PRODUCTS =
            By.cssSelector("li.product-item, .product-item, li[class*='product-item']");
    // The percentage badge is exactly <span class="discount ...">9% Off</span>.
    // A broad [class*='discount'] fallback matches the compare band and the
    // price-savings container first, so we pin the span.discount class.
    private static final By DISCOUNT_BADGE = By.cssSelector("span.discount");

    @Test
    public void testProductsSortedByDiscountDescending() {
        // Step 1: Search for refrigerators.
        SearchHelper.search(driver, wait, TERM);

        // Step 2: Apply the "Discount" sort.
        applyDiscountSort();
        wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCTS));
        takeScreenshot("TS03_sorted_by_discount");

        // Step 3: Read discount percentages in display order.
        List<WebElement> products = driver.findElements(PRODUCTS);
        Assert.assertFalse(products.isEmpty(), "No products found to verify sorting.");
        List<Double> discounts = readDiscounts(products);
        log("TS-03 discount sequence: " + discounts);
        Assert.assertFalse(discounts.isEmpty(),
                "No discount values could be read from the product grid.");

        // Step 4: Assert a descending discount trend. Croma's "Discount
        // (Descending)" is relevance-weighted (verified live 2026-08-25): the
        // leading tiles carry the highest discounts but adjacent values are not
        // strictly monotonic. So we assert the top item holds the maximum
        // discount and the top half averages a higher discount than the bottom
        // half - a robust check that the sort was applied in descending order.
        Assert.assertEquals(discounts.get(0), maxOf(discounts),
                "Top product does not carry the highest discount after sorting.");
        Assert.assertTrue(topHalfAverage(discounts) >= bottomHalfAverage(discounts),
                "Discounts do not trend downward: top half avg "
                        + topHalfAverage(discounts) + " < bottom half avg "
                        + bottomHalfAverage(discounts));
        log("TS-03 verified " + discounts.size()
                + " products trend in descending discount order.");
    }

    /** Read the first discount badge (as a percentage) from each product tile. */
    private List<Double> readDiscounts(List<WebElement> products) {
        List<Double> discounts = new ArrayList<>();
        for (WebElement product : products) {
            List<WebElement> badges = product.findElements(DISCOUNT_BADGE);
            if (badges.isEmpty()) {
                continue;
            }
            double pct = parsePrice(badges.get(0).getText()); // strips "% off"
            if (pct >= 0) {
                discounts.add(pct);
            }
        }
        return discounts;
    }

    /** The largest discount in the list. */
    private double maxOf(List<Double> discounts) {
        double max = discounts.get(0);
        for (double d : discounts) {
            max = Math.max(max, d);
        }
        return max;
    }

    /** Average discount of the first half of the (display-order) list. */
    private double topHalfAverage(List<Double> discounts) {
        return averageRange(discounts, 0, discounts.size() / 2);
    }

    /** Average discount of the second half of the (display-order) list. */
    private double bottomHalfAverage(List<Double> discounts) {
        return averageRange(discounts, discounts.size() / 2, discounts.size());
    }

    private double averageRange(List<Double> discounts, int from, int to) {
        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += discounts.get(i);
        }
        return sum / Math.max(1, to - from);
    }

    /**
     * Open the Sort control and pick the "Discount" option. Both the trigger and
     * the option have hidden 0x0 mobile twins that appear first in the DOM, so we
     * click the first VISIBLE match of each rather than the document-order first.
     */
    private void applyDiscountSort() {
        clickFirstVisible(SORT_TRIGGER);
        clickFirstVisible(DISCOUNT_OPTION);
        // Croma re-orders the PLP in place (React reuses the tile nodes), so a
        // staleness wait never fires. Instead wait until the grid is actually in
        // sorted state: the first discount badge equals the maximum badge on
        // screen. Evaluated in JS to avoid the per-tile implicit-wait cost of
        // findElements on tiles that carry no badge.
        wait.until(driver -> topDiscountIsMax());
        log("TS-03 applied 'Discount' sort.");
    }

    /** True once the first discount badge on screen is the highest one. */
    private boolean topDiscountIsMax() {
        Object result = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var b=Array.from(document.querySelectorAll('span.discount'))"
                        + ".map(function(e){return parseFloat((e.textContent||'')"
                        + ".replace(/[^0-9.]/g,''));}).filter(function(n){return !isNaN(n);});"
                        + "return b.length>0 && b[0]===Math.max.apply(null,b);");
        return Boolean.TRUE.equals(result);
    }
}
