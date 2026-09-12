package com.testproject.ECommerceWebsite;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TS-04  Price Calculation
 * Objective: Calculate and print the average price of the top 10 discounted
 *            products (i.e. after sorting by discount).
 *
 * Steps: search "Refrigerator" -> sort by Discount -> read the price of the
 *        first 10 products -> compute + log the average.
 *
 * The @Test method is a straight line; price extraction and averaging live in
 * the private helpers below.
 */
public class CromaAveragePriceTest extends BaseTest {

    private static final String TERM = "Refrigerator";
    private static final int TOP_N = 10;

    // Verified live DOM (2026-08-25): the sort trigger has a hidden 0x0 mobile
    // twin (.sort-by.icon-text) first in the DOM and a VISIBLE div.sort-by; the
    // dropdown option is <li data-testid="sort">Discount (Descending)</li>.
    // price -> <span class="amount plp-srp-new-amount"> inside <div class="new-price">.
    private static final By SORT_TRIGGER = By.cssSelector("div.sort-by");
    private static final By DISCOUNT_OPTION = By.xpath(
            "//li[@data-testid='sort'][contains(translate(normalize-space(.),"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'discount')]");
    private static final By PRODUCTS =
            By.cssSelector("li.product-item, .product-item, li[class*='product-item']");
    private static final By PRICE =
            By.cssSelector(".new-price .amount, .new-price, span.amount, [class*='amount']");

    @Test
    public void testAveragePriceOfTop10DiscountedProducts() {
        // Step 1: Search and sort by discount (reuse TS-03's approach).
        SearchHelper.search(driver, wait, TERM);
        applyDiscountSort();
        wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCTS));
        takeScreenshot("TS04_top_discounted");

        // Step 2: Extract prices of the top 10 products.
        List<WebElement> products = driver.findElements(PRODUCTS);
        Assert.assertFalse(products.isEmpty(), "No products found to price.");
        List<Double> prices = readTopPrices(products, TOP_N);
        Assert.assertFalse(prices.isEmpty(),
                "Could not read any prices from the top " + TOP_N + " products.");

        // Step 3: Compute and report the average.
        double average = average(prices);
        log(String.format("TS-04 average price of top %d discounted products = %.2f (n=%d)",
                TOP_N, average, prices.size()));
        System.out.printf("Average price of top %d discounted products: Rs. %.2f%n",
                prices.size(), average);
        Assert.assertTrue(average > 0, "Computed average price should be positive.");
    }

    /** Read the first parseable price from each of the first {@code n} product tiles. */
    private List<Double> readTopPrices(List<WebElement> products, int n) {
        List<Double> prices = new ArrayList<>();
        int limit = Math.min(n, products.size());
        for (int i = 0; i < limit; i++) {
            List<WebElement> priceEls = products.get(i).findElements(PRICE);
            if (priceEls.isEmpty()) {
                continue;
            }
            double price = parsePrice(priceEls.get(0).getText());
            if (price >= 0) {
                prices.add(price);
                log("  product[" + i + "] price = " + price);
            }
        }
        return prices;
    }

    private double average(List<Double> prices) {
        double sum = 0;
        for (double p : prices) {
            sum += p;
        }
        return sum / prices.size();
    }

    /**
     * Open the Sort control and pick the "Discount" option. Both the trigger and
     * the option have hidden 0x0 mobile twins that appear first in the DOM, so we
     * click the first VISIBLE match of each rather than the document-order first.
     */
    private void applyDiscountSort() {
        clickFirstVisible(SORT_TRIGGER);
        clickFirstVisible(DISCOUNT_OPTION);
        log("TS-04 applied 'Discount' sort.");
    }
}
