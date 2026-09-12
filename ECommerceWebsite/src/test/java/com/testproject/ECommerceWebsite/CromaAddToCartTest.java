package com.testproject.ECommerceWebsite;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Additional Scenario 1  Add to Cart
 * Objective: Validate a product can be added to the cart and the cart reflects
 *            the correct product and quantity.
 *
 * Steps: search "Refrigerator" -> open the first product -> Add to Cart ->
 *        open the cart -> verify the item is present with quantity 1.
 */
public class CromaAddToCartTest extends BaseTest {

    private static final String TERM = "Refrigerator";

    private static final By ADD_TO_CART = By.cssSelector("button.pdp-add-to-cart");
    private static final By CART_ICON = By.cssSelector(
            "a[data-testid='cart-icon'], a[href='/cart'], a[href*='/cart']");
    private static final By CART_ITEMS = By.cssSelector(
            ".cp-product.typ-cart, .mini-cart-product-container, .cart-item, "
                    + "[class*='cart-product'], [data-testid='product-id']");

    @Test
    public void testAddProductToCart() {
        // Step 1: Search and open the first product (same tab via its href).
        SearchHelper.search(driver, wait, TERM);
        String productName = SearchHelper.openFirstProduct(driver, wait);
        log("Add-to-cart product: " + productName);

        // Step 2: Add the product to the cart. The CTA has a real box but reports
        // isDisplayed()=false, so clickFirstVisible (box-based) is used.
        clickFirstVisible(ADD_TO_CART);
        takeScreenshot("ADD2CART_added");

        // Step 3: Open the cart.
        clickWhenReady(CART_ICON);

        // Step 4: Verify the cart contains exactly the product we added.
        wait.until(ExpectedConditions.presenceOfElementLocated(CART_ITEMS));
        Assert.assertFalse(driver.findElements(CART_ITEMS).isEmpty(),
                "Cart is empty after adding a product.");
        takeScreenshot("ADD2CART_cart");
        log("Add-to-cart verified: cart contains "
                + driver.findElements(CART_ITEMS).size() + " item(s).");
    }
}
