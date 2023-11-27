package linkscheck;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LinkCheckerTest {

    private WebDriver driver;

    @BeforeTest
    public void setUp() {
         driver = new ChromeDriver();
    }

    @Test
    public void testLinkAvailability() throws IOException {
        // Open the initial web page
        driver.get("https://rahulshettyacademy.com/AutomationPractice/"); // Replace with your URL

        // Find all links on the initial page
        List<WebElement> links = driver.findElements(By.tagName("a"));

        // Check each link on the initial page
        for (WebElement link : links) {
            // Verify if the link has a valid href (not empty or null)
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                System.out.println("Link href should not be null or empty for element: " + link);
                continue;
            }

            // Open the linked page in a new tab/window without clicking it
            String currentWindowHandle = driver.getWindowHandle();
            ((ChromeDriver) driver).executeScript("window.open();");
            switchToNewTab();

            // Navigate to the linked page
            driver.get(href);

            // Find all links on the newly opened page
            List<WebElement> innerLinks = driver.findElements(By.tagName("a"));

            // Check each link on the newly opened page
            for (WebElement innerLink : innerLinks) {
                // Verify if the link has a valid href (not empty or null)
                String innerHref = innerLink.getAttribute("href");
                if (innerHref == null || innerHref.isEmpty()) {
                    System.out.println("Inner link href should not be null or empty for element: " + innerLink);
                    continue;
                }

                // Verify the link response code
                int responseCode = getHttpResponseCode(innerHref);
                Assert.assertEquals(responseCode, 200, "Inner link " + innerHref + " is not working properly (HTTP Status Code: " + responseCode + ")");
            }

            // Close the newly opened tab/window
            driver.close();
            driver.switchTo().window(currentWindowHandle);
        }
    }

    private int getHttpResponseCode(String linkUrl) throws IOException {
        URL url = new URL(linkUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.connect();
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    private void switchToNewTab() {
        // Switch to the newly opened tab
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
        }
    }

    @AfterTest
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}