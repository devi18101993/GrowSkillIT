package com.testproject.ECommerceWebsite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TS-01  Search Functionality (data-driven)
 * Objective: Verify search returns relevant results for the given term
 *            (the brief's primary term is "Refrigerator").
 *
 * Data-driven testing: search terms and their expected keyword come from
 * testdata.csv (the brief specifies Excel; a CSV DataProvider is used here so
 * the test compiles/runs without Apache POI. Swap in a POI-backed provider to
 * read .xlsx unchanged — the @Test signature stays the same).
 *
 * The @Test method is intentionally a straight line of steps + assertions;
 * all iteration/branching lives in the private helpers below.
 */
public class CromaSearchTest extends BaseTest {

    // Product-title locator (fallbacks widen tolerance to DOM changes).
    private static final By RESULT_TITLES =
            By.cssSelector("h3.product-title, .product-title, h3[class*='product-title']");

    private static final Path DATA_FILE = Paths.get("testdata.csv");

    @DataProvider(name = "searchTerms")
    public Object[][] searchTerms() throws Exception {
        List<Object[]> rows = new ArrayList<>();
        // Fall back to the brief's default term if the data file is missing.
        if (!Files.exists(DATA_FILE)) {
            return new Object[][] {{"Refrigerator", "refrigerator"}};
        }
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split(",");
                // columns: searchTerm, expectedKeyword, brands, topN
                if (cols.length >= 2) {
                    rows.add(new Object[] {cols[0].trim(), cols[1].trim()});
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "searchTerms")
    public void testSearchReturnsRelevantResults(String term, String expectedKeyword) {
        log("TS-01 searching for '" + term + "', expecting keyword '" + expectedKeyword + "'");

        // Step 1: Search (helper types the term, submits and waits for results).
        SearchHelper.search(driver, wait, term);
        takeScreenshot("TS01_search_" + term.replaceAll("[^A-Za-z0-9]+", "_"));

        // Step 2: Collect the result titles.
        List<WebElement> titles = driver.findElements(RESULT_TITLES);
        Assert.assertFalse(titles.isEmpty(), "No search results returned for '" + term + "'.");

        // Step 3: At least half of the top results must match the keyword.
        int inspected = Math.min(titles.size(), 10);
        int relevant = countRelevant(titles, expectedKeyword, inspected);
        int threshold = (inspected / 2) + (inspected % 2);
        Assert.assertTrue(relevant >= threshold,
                "Search results not relevant to '" + expectedKeyword + "': only "
                        + relevant + "/" + inspected + " titles matched.");
        log("TS-01 relevance: " + relevant + "/" + inspected + " titles matched.");
    }

    /**
     * Count how many of the first {@code inspected} titles are relevant to the
     * search term. The expected-keyword column may hold pipe-separated synonyms
     * (e.g. "air conditioner|split ac|ac") because Croma titles a category by its
     * product form, not the search phrase - an "Air Conditioner" search returns
     * titles that read "Split AC" / "Inverter Split AC" and never the literal
     * words "air conditioner". A title counts as relevant if it matches ANY
     * synonym. Matching is on a word boundary so a short token like "ac" hits the
     * standalone word "AC" but not the "ac" inside "machine".
     */
    private int countRelevant(List<WebElement> titles, String keyword, int inspected) {
        String[] synonyms = keyword.toLowerCase().split("\\|");
        int relevant = 0;
        for (int i = 0; i < inspected; i++) {
            String title = titles.get(i).getText();
            log("  result[" + i + "] = " + title);
            if (matchesAny(title.toLowerCase(), synonyms)) {
                relevant++;
            }
        }
        return relevant;
    }

    /** True when {@code title} contains any synonym as a whole word/phrase. */
    private boolean matchesAny(String title, String[] synonyms) {
        for (String synonym : synonyms) {
            String needle = synonym.trim();
            if (!needle.isEmpty() && title.matches(".*\\b" + java.util.regex.Pattern.quote(needle) + "\\b.*")) {
                return true;
            }
        }
        return false;
    }
}