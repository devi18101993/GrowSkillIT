package com.testproject.ECommerceWebsite;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Additional Scenario 3  Checkout Process
 * Objective: Validate the checkout flow from cart to the point where the app
 *            asks for login/delivery and presents payment options.
 *
 * NOTE: A real Croma checkout requires an authenticated account, a valid
 * delivery pincode and would place a live order. This test therefore stops at
 * the "proceed to checkout -> login/delivery + payment options shown" gate and
 * asserts the checkout page loaded, rather than completing a real purchase.
 */
public class CromaCheckoutTest extends BaseTest {

    private static final String TERM = "Refrigerator";

    // Locators verified against the live Croma DOM (2026-08-25):
    //   PDP add-to-cart -> <button class="btn btn-secondary pdp-add-to-cart">
    //   cart icon       -> <a data-testid="cart-icon" href="/cart">
    // The PDP Add-to-Cart is a single <button class="pdp-add-to-cart"> in a
    // sticky action bar. It has a real 224x55 box but Selenium's isDisplayed()
    // heuristic reports it hidden (verified live 2026-08-25), so a visibility
    // wait times out. We click it via clickFirstVisible, which gates on the
    // rendered box, not isDisplayed().
    private static final By ADD_TO_CART = By.cssSelector("button.pdp-add-to-cart");
    private static final By CART_ICON = By.cssSelector(
            "a[data-testid='cart-icon'], a[href='/cart'], a[href*='/cart']");
    // Verified live (2026-08-25): the cart's proceed CTA is <button class="btn
    // btn-default checkout-btn">Checkout</button> (450x49 visible); a 0x0 hidden
    // ".paymment-btn-float" twin also matches "Checkout", so we click the first
    // VISIBLE match. The cart line item is <div class="cp-product ... typ-cart">.
    private static final By PROCEED_TO_CHECKOUT = By.cssSelector("button.checkout-btn");
    // After "Checkout", Croma opens a login modal (verified live 2026-08-25:
    // <div class="user-wrap custom-login cp-dropdown"> containing
    // <input class="mobile-input-box input-login" placeholder="Enter your Email
    // ID or phone number">) before delivery + payment. Reaching this login gate
    // confirms the checkout flow without placing a real order.
    private static final By CHECKOUT_GATE =
            By.cssSelector("input.mobile-input-box, .custom-login, .cp-dropdown .input-login");

    @Test
    public void testCheckoutReachesPaymentGate() {
        // Step 1: Add a product to the cart (open PDP in the same tab via href).
        SearchHelper.search(driver, wait, TERM);
        SearchHelper.openFirstProduct(driver, wait);
        clickFirstVisible(ADD_TO_CART);

        // Step 2: Open the cart and proceed to checkout (robust clicks).
        clickWhenReady(CART_ICON);
        clickFirstVisible(PROCEED_TO_CHECKOUT);

        // Step 3: Verify we reached the checkout gate (the login modal). Croma's
        // CSS transforms make isDisplayed() unreliable here, so we assert the
        // modal input has actually rendered (present with a non-zero box).
        boolean reachedGate = isRenderedWithin(CHECKOUT_GATE, java.time.Duration.ofSeconds(20));
        takeScreenshot("CHECKOUT_gate");
        Assert.assertTrue(reachedGate,
                "Checkout did not reach the login/delivery/payment stage.");
        log("Checkout reached the payment/login gate as expected.");
    }
}
