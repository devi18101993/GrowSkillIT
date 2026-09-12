package com.testproject.ECommerceWebsite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Shared setup/teardown for the Croma e-commerce Selenium suite.
 *
 * Responsibilities (map to the brief's cross-cutting scenarios):
 *   - Browser lifecycle: fresh Edge per @Test method for isolation.
 *   - TS-05 Screenshot Capture: {@link #takeScreenshot(String)} saves a PNG
 *     at any critical step; a screenshot is also auto-captured on failure.
 *   - TS-06 Log Generation: {@link #log(String)} writes timestamped lines to
 *     both the console and a per-run log file under ./logs.
 *
 * Notes vs. the brief's sample:
 *   - No hardcoded msedgedriver path required — Selenium Manager (4.6+)
 *     resolves it; set -Dedge.driver.path=... only if that resolution fails.
 *   - Explicit WebDriverWait everywhere instead of relying on Thread.sleep.
 *   - In a full Maven framework the log() calls would be Log4j2 and the
 *     screenshots would be embedded into Extent Reports; here they are kept
 *     dependency-light (plain files) so the classes compile and run stand-alone.
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL = "https://www.croma.com/";

    /**
     * Delivery pincode. Croma gates its product-listing (PLP) API on a selected
     * delivery pincode, so the search grid stays empty until one is set.
     * "400001" (Mumbai GPO) is a real, serviceable Croma pincode; change it to
     * one serviceable in your region if the grid does not populate.
     */
    //protected static final String DELIVERY_PINCODE = "400001";

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    protected static final Path SCREENSHOT_DIR = Paths.get("screenshots");
    protected static final Path LOG_DIR = Paths.get("logs");

    private Path logFile;
    private String currentTest;
    
    @BeforeMethod
    public void setup(java.lang.reflect.Method method) {
        currentTest = method.getName();

        System.setProperty("webdriver.edge.driver", "C:\\Users\\user\\Downloads\\Softwares\\edgedriver_win64\\msedgedriver.exe");

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        //options.addArguments("--window-size=1920,1080");
        // Reduce automation fingerprinting (Croma uses Akamai bot mitigation).
        // Edge is Chromium-based, so the same flags apply as they did on Chrome.
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches",
                java.util.Collections.singletonList("enable-automation"));
//        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
//                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36 Edg/151.0.0.0");
        configureBrowserBinary(options);
        configureHeadless(options);

        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("profile.default_content_setting_values.geolocation", 2);
        options.setExperimentalOption("prefs", prefs);

        driver = new EdgeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        prepareLogFile();
        log("===== START TEST: " + currentTest + " =====");
        driver.get(BASE_URL);
        // Make sure the document has actually finished loading before touching
        // the DOM (mirrors the readyState gate from the working QKart harness).
        wait.until(d -> "complete".equals(
                ((JavascriptExecutor) d).executeScript("return document.readyState")));
        // Croma's "shop with video" modal appears ~2-4s after load; wait for the
        // close button, then dismiss it (and any web-push opt-in) so it cannot
        // intercept later clicks. The product grid loads without a pincode, so
        // no pincode step is needed here.
        waitForAutoModal();
        dismissPopups();
    }

    /**
     * Point Edge at a specific browser binary when {@code -Dedge.binary=...}
     * (or the EDGE_BINARY env var) is supplied — e.g. a non-default Edge
     * install location. Left unset, Edge is auto-discovered.
     */
    private void configureBrowserBinary(EdgeOptions options) {
        String binary = System.getProperty("edge.binary", System.getenv("EDGE_BINARY"));
        if (binary != null && !binary.isEmpty()) {
            options.setBinary(binary);
        }
    }

    /**
     * Run headless when {@code -Dheadless=true} is passed explicitly; defaults
     * to headed (false) since Edge's headless mode has weaker anti-detection
     * cover against Akamai than a real, visible browser. Pass
     * {@code -Dheadless=true} for CI / no-display machines.
     */
    private void configureHeadless(EdgeOptions options) {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        }
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        // Auto-capture a screenshot on failure to aid debugging (TS-05/TS-06).
        if (result.getStatus() == ITestResult.FAILURE) {
            log("TEST FAILED: " + result.getThrowable());
            takeScreenshot(currentTest + "_FAILURE");
        }
        log("===== END TEST: " + currentTest
                + " (" + statusText(result.getStatus()) + ") =====");
        if (driver != null) {
            driver.quit();
        }
    }

    // ------------------------------------------------------------------
    // TS-05  Screenshot capture
    // ------------------------------------------------------------------

    /** Save a PNG screenshot named after the given step; returns its path. */
    protected Path takeScreenshot(String stepName) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String name = stepName + "_" + LocalDateTime.now().format(FILE_TS) + ".png";
            Path dest = SCREENSHOT_DIR.resolve(name);
            Files.copy(src.toPath(), dest);
            log("Screenshot captured: " + dest);
            return dest;
        } catch (Exception e) {
            log("Screenshot FAILED for '" + stepName + "': " + e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------------------
    // TS-06  Log generation
    // ------------------------------------------------------------------

    /** Timestamped log line -> console + per-run log file. */
    protected void log(String message) {
        String line = "[" + LocalDateTime.now().format(TS) + "] " + message;
        System.out.println(line);
        if (logFile != null) {
            try {
                Files.write(logFile,
                        (line + System.lineSeparator()).getBytes(),
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException ignored) {
                // console line already emitted; do not fail the test on log I/O.
            }
        }
    }

    private void prepareLogFile() {
        try {
            Files.createDirectories(LOG_DIR);
            logFile = LOG_DIR.resolve(
                    currentTest + "_" + LocalDateTime.now().format(FILE_TS) + ".log");
        } catch (IOException e) {
            logFile = null; // fall back to console-only logging
        }
    }

    private String statusText(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASSED";
            case ITestResult.FAILURE: return "FAILED";
            case ITestResult.SKIP:    return "SKIPPED";
            default:                  return "UNKNOWN";
        }
    }

    // ------------------------------------------------------------------
    // Shared helpers used across the Croma scenarios
    // ------------------------------------------------------------------

    /**
     * Croma opens a "shop with video" MUI dialog on load (and occasionally a
     * web-push opt-in) that intercepts clicks. Best-effort dismissal so tests
     * stay stable. Selectors verified against the live DOM (2026-08-25):
     *   auto-open video modal -> <button id="triggerClose" class="icon icon-close">
     *   web-push opt-in        -> #wzrk-cancel (CleverTap, injected at runtime)
     * Locators are centralized here for easy replacement against the live DOM.
     */
    protected void dismissPopups() {
        String[] closeSelectors = {
                "#triggerClose",                             // auto-open "shop with video" modal
                "#wzrk-cancel",                              // web push opt-in (CleverTap)
                ".MuiDialog-container .icon.icon-close",     // any MUI dialog close (X)
                ".modal-wrap .icon.icon-close",              // generic modal close (X)
                ".modal-wrap .close-btn"                     // generic modal close (button)
        };
        for (String sel : closeSelectors) {
            try {
                List<WebElement> closers = driver.findElements(By.cssSelector(sel));
                for (WebElement c : closers) {
                    if (c.isDisplayed()) {
                        c.click();
                        log("Dismissed popup via selector: " + sel);
                    }
                }
            } catch (Exception ignored) {
                // popup not present / already gone — carry on.
            }
        }
    }

    /**
     * The auto-open "shop with video" modal renders a couple of seconds after
     * load. Give it a short, bounded window to appear so {@link #dismissPopups()}
     * can close it; absence is fine (best-effort, never fails setup).
     */
    private void waitForAutoModal() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6)).until(
                    ExpectedConditions.presenceOfElementLocated(By.id("triggerClose")));
        } catch (Exception ignored) {
            // modal did not appear this run — nothing to dismiss.
        }
    }

    /**
     * Robustly click the element at {@code locator}. Croma re-injects its
     * "shop with video" modal on navigation and renders CTAs below the fold, so
     * a plain WebElement.click() hits an overlay or an off-screen point. This
     * dismisses popups, scrolls the element to centre, then performs a JS click
     * (which is not blocked by overlay interception). Centralized here so the
     * @Test methods stay simple, straight-line calls.
     */
    protected void clickWhenReady(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        // Re-dismiss the modal Croma re-injects on navigation, right before the
        // click, so it cannot intercept. Then JS-click (overlay-proof).
        dismissPopups();
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", el);
    }

    /**
     * Click the first VISIBLE element matching {@code locator}, via a JS click.
     *
     * Croma renders mobile+desktop twins of several controls (e.g. the sort
     * button) where the first DOM match is a hidden 0x0 duplicate. Selenium's
     * findElement returns document order, so a plain click hits the hidden one.
     * This waits until at least one match is present, then JS-clicks the first
     * with a non-zero box. Kept here so the @Test methods stay simple.
     */
    protected void clickFirstVisible(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        dismissPopups();
        List<WebElement> matches = driver.findElements(locator);
        WebElement visible = firstVisible(matches);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});"
                        + "arguments[0].click();", visible);
    }

    /**
     * JS-click the first element matching {@code locator} even when it is a
     * visually-hidden native control (e.g. a 0x0 styled radio/checkbox whose
     * visible skin is a sibling label). Waits only for DOM presence, then clicks
     * via JavascriptExecutor so zero-size / off-screen does not block it. No-op
     * (best effort) if the element never appears, so optional steps stay simple.
     */
    protected void selectHidden(By locator) {
        waitForPresenceQuietly(locator);
        List<WebElement> matches = driver.findElements(locator);
        if (matches.isEmpty()) {
            log("selectHidden: no element for " + locator + " (skipped).");
            return;
        }
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", matches.get(0));
        log("selectHidden clicked: " + locator);
    }

    /**
     * Wait for {@code locator} to render (present with a non-zero box) and return
     * whether it did. Croma applies CSS animations/transforms that make Selenium's
     * isDisplayed() report false for elements that are visually on screen (e.g.
     * the sticky Add-to-Cart bar, the checkout login modal), so a plain
     * visibility wait is unreliable. This gates on the rendered box instead.
     */
    protected boolean isRenderedWithin(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(driver -> {
                List<WebElement> matches = driver.findElements(locator);
                return firstWithBox(matches) != null;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** First element with a non-zero rendered box, or null when none qualifies. */
    private WebElement firstWithBox(List<WebElement> elements) {
        for (WebElement e : elements) {
            if (e.getRect().getWidth() > 0 && e.getRect().getHeight() > 0) {
                return e;
            }
        }
        return null;
    }

    /** Wait (bounded, non-fatal) for a locator to be present in the DOM. */
    private void waitForPresenceQuietly(By locator) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(
                    ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception ignored) {
            // element optional / not rendered this run — caller treats as no-op.
        }
    }

    /**
     * JS-click a specific element (scrolling it into view first). Use when the
     * caller already holds the exact WebElement - e.g. ticking several distinct
     * checkboxes - and Croma's isDisplayed()/overlay quirks make a native click
     * unreliable. Complements {@link #clickFirstVisible(By)}, which picks one
     * element from a locator.
     */
    protected void jsClick(WebElement el) {
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});"
                        + "arguments[0].click();", el);
    }

    /** First element with a non-zero rendered box, or the first element as a fallback. */
    private WebElement firstVisible(List<WebElement> elements) {
        for (WebElement e : elements) {
            if (e.getRect().getWidth() > 0 && e.getRect().getHeight() > 0) {
                return e;
            }
        }
        return elements.get(0);
    }

    /**
     * Select a delivery pincode so Croma loads its product grid.
     *
     * Croma's PLP/search API is gated on a serviceable delivery pincode; without
     * one the results grid never populates. This opens the header "deliver to"
     * modal, types the pincode and confirms.
     *
     * Verified header pincode-modal markup (from the JS bundle, 2026-08-22):
     *   trigger -> <div class="delivery-location delivery-location-maindiv">
     *   modal   -> <div class="modal-wrap modal-md header-pincode-modal">
     *   input   -> <input name="pin" placeholder="Enter pincode" maxLength="6">
     *   submit  -> <button id="apply-pincode-btn" type="submit">
     *
     * Best-effort: if the pincode is already set (cookie) or the modal markup
     * differs, it logs and continues rather than failing the test in setup.
     *
     * @param pincode a 6-digit serviceable delivery pincode
     */
    protected void setPincode(String pincode) {
        if (pincode == null || !pincode.matches("\\d{6}")) {
            log("setPincode skipped: '" + pincode + "' is not a 6-digit pincode.");
            return;
        }
        try {
            // Open the "deliver to" location modal from the header.
            WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".delivery-location-maindiv, .delivery-pincode")));
            trigger.click();

            // Type the pincode into the modal input.
            WebElement pinInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".header-pincode-modal input[name='pin'], input[name='pin']")));
            pinInput.clear();
            pinInput.sendKeys(pincode);

            // Confirm.
            WebElement apply = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#apply-pincode-btn, .delivery-pincode-btn button")));
            apply.click();

            log("Delivery pincode set to: " + pincode);
        } catch (Exception e) {
            // Pincode may already be set (cookie) or the modal did not appear.
            log("setPincode('" + pincode + "') skipped/failed (non-fatal): "
                    + e.getMessage());
        }
    }

    /**
     * Parse a Croma price string ("₹ 32,990" / "Rs. 32,990.00") into a double.
     * Returns -1 when nothing numeric is present.
     */
    protected static double parsePrice(String raw) {
        if (raw == null) {
            return -1;
        }
        String digits = raw.replaceAll("[^0-9.]", "");
        if (digits.isEmpty() || digits.equals(".")) {
            return -1;
        }
        // Guard against multiple dots from concatenated text.
        int firstDot = digits.indexOf('.');
        if (firstDot >= 0) {
            digits = digits.substring(0, firstDot)
                    + digits.substring(firstDot).replace(".", "");
            digits = new StringBuilder(digits).insert(firstDot, ".").toString();
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Small helper mirroring List construction used by a few tests. */
    protected static <T> List<T> newList() {
        return new ArrayList<>();
    }
}