package linkscheck;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.*;

public class BrokenLinksTestDeepRetest {


    public static void main(String[] args) throws SocketException {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");

        WebDriver driver = new ChromeDriver(chromeOptions);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Links_validation_report.html"))) {
            System.out.println("HTML file opened successfully.");

            Properties properties = loadProperties("config.properties");

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String applicationName = (String) entry.getKey();
                String applicationUrl = (String) entry.getValue();
                checkLinksAndGenerateReport(writer, applicationName, applicationUrl, driver);
            }

            System.out.println("HTML file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }


    private static Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            properties.load(input);
        }
        return properties;
    }

    private static void checkLinksAndGenerateReport(BufferedWriter writer, String applicationName, String applicationUrl, WebDriver driver) {
        try {
            driver.get(applicationUrl);
            driver.manage().window().maximize();
            Thread.sleep(4000);

            List<WebElement> allLinks = driver.findElements(By.tagName("a"));

            ArrayList<String> al = new ArrayList<>();

            for (WebElement link : allLinks) {
                String href = link.getAttribute("href");
                al.add(href);
            }

            String[] stringArray = new String[al.size()];
            for (int i = 0; i < al.size(); i++) {
                stringArray[i] = al.get(i);
            }

            LinkStatusReporter(writer, applicationName, stringArray, applicationUrl, driver);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static void LinkStatusReporter(BufferedWriter writer, String applicationName, String[] linksToCheck, String applicationUrl, WebDriver driver) {
        try {
            // Create an HTML row with table headers
            writer.write("<tr>");
            writer.write("<th>Application Name</th>");
            writer.write("<th>Application URL</th>");
            writer.write("<th>Total Links</th>");
            writer.write("<th>Not Working Links Count</th>");
            writer.write("<th>Not Working Links with Status Code</th>");
            writer.write("</tr>");

            int totalLinks = linksToCheck.length;
            int notWorkingLinksCount = 0;

            List<String> notWorkingLinksList = new ArrayList<>();

            for (String urlToCheck : linksToCheck) {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(urlToCheck);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    boolean linkIsWorking = responseCode == HttpURLConnection.HTTP_OK;

                    if (!linkIsWorking) {
                        String linkStatus = "<a href='" + urlToCheck + "'>" + urlToCheck + "</a>" + " " + responseCode;
                        notWorkingLinksList.add(linkStatus);
                        notWorkingLinksCount++;
                    }
                } catch (MalformedURLException e) {
                    System.err.println("Malformed URL: " + e.getMessage());
                } catch (IOException e) {
                    // Handle IOException if necessary
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            // Create an HTML row with the link data
            writer.write("<tr>");
            writer.write("<td>" + applicationName + "</td>");
            writer.write("<td><a href='" + applicationUrl + "'>" + applicationUrl + "</a></td>");
            writer.write("<td>" + totalLinks + "</td>");
            writer.write("<td>" + notWorkingLinksCount + "</td>");

            // Conditionally set the "Not working links with status code" column
            if (notWorkingLinksCount > 0) {
                writer.write("<td>");
                for (String notWorkingLink : notWorkingLinksList) {
                    writer.write(notWorkingLink + "<br>");
                }
                writer.write("</td>");
            } else {
                writer.write("<td>NA</td>");
            }

            writer.write("</tr>");

            // Open links in new tab and validate
            openLinksInNewTabAndValidate(driver, linksToCheck);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void switchToNewTab(WebDriver driver) {
        // Get the list of all window handles
        Set<String> handles = driver.getWindowHandles();

        // Switch to the new tab
        for (String handle : handles) {
            driver.switchTo().window(handle);
        }
    }


    private static void openLinksInNewTabAndValidate(WebDriver driver, String[] linksToOpen) {
        for (String linkToOpen : linksToOpen) {
            try {
                // Check if the URL is not empty and has a valid protocol
                if (isValidUrl(linkToOpen)) {
                    // Open the link in a new tab
                    ((ChromeDriver) driver).executeScript("window.open();");

                    // Switch to the new tab
                    switchToNewTab(driver);

                    // Navigate to the linked page
                    driver.get(linkToOpen);

                    // Perform further validations if needed
                } else {
                    System.err.println("Invalid URL: " + linkToOpen);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean isValidUrl(String url) {
        return url != null && !url.isEmpty();
    }


}
