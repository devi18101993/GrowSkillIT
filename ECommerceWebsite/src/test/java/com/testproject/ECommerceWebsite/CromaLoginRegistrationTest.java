package com.testproject.ECommerceWebsite;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Additional Scenario 4  User Login and Registration
 * Objective: Ensure login/registration entry points work correctly.
 *
 * NOTE: Croma uses a phone-number/email + OTP based sign-in (no password), so a
 * full end-to-end login/registration cannot be automated without access to the
 * OTP. These tests validate that the sign-in flow opens and requests a mobile
 * number / email (the registration/login entry point), which is the
 * automatable, deterministic portion of the scenario.
 *
 */
public class CromaLoginRegistrationTest extends BaseTest {

    private static final By ACCOUNT_ICON = By.cssSelector("[data-testid='myaccount']");
    // Dropdown "Login" control (verified live: <button class="link
    // header-logout-btn-style">Login</button>, 119x66, revealed on hover).
    private static final By LOGIN_BUTTON = By.cssSelector(
            "button.header-logout-btn-style, .myaccount-logout-text, "
                    + ".cp-dropdown button.login, [data-testid='login-active']");
    // Sign-in modal field (verified live: <input class="mobile-input-box
    // input-login" placeholder="Enter your Email ID or phone number">, 362x48).
    private static final By MOBILE_INPUT = By.cssSelector(
            "input.mobile-input-box, input[placeholder*='phone'], input[placeholder*='Mobile']");

    @Test
    public void testLoginEntryRequestsMobileNumber() {
        // Step 1: Open the account dropdown and start the sign-in flow.
        openLoginModal();

        // Step 2: Verify the sign-in form asks for a mobile number / email.
        // Croma's CSS transforms make isDisplayed() unreliable, so we assert the
        // field has actually rendered (present with a non-zero box).
        boolean promptShown = isRenderedWithin(MOBILE_INPUT, java.time.Duration.ofSeconds(20));
        takeScreenshot("LOGIN_mobile_prompt");
        Assert.assertTrue(promptShown,
                "Sign-in panel did not present the mobile-number field.");
        log("Login entry point verified: mobile-number field is shown.");
    }

    /**
     * Hover the account block to reveal its dropdown, then click "Login" to open
     * the mobile/email sign-in modal. The dropdown "Login" control reports
     * isDisplayed()=false despite a real box, so it is clicked via the rendered-
     * box path rather than an elementToBeClickable wait.
     */
    private void openLoginModal() {
        WebElement account = wait.until(
                ExpectedConditions.presenceOfElementLocated(ACCOUNT_ICON));
        new Actions(driver).moveToElement(account).perform();
        clickFirstVisible(LOGIN_BUTTON);
    }
}
