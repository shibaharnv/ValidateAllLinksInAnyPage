// Import necessary packages
package linkscheck;// Impor


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class BrokenImagesTestCompleteDOM3 {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config2.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WebDriver driver = new ChromeDriver();
        BufferedWriter writer = null;

        try {
            // Create an HTML report file
            writer = new BufferedWriter(new FileWriter("BrokenImages_validation_report.html"));

            // Write the HTML header and table structure
            writer.write("<html><head><title>Broken Images Status Report</title></head><body>");
            writer.write("<table border='1'><tr><th>Application Name</th><th>Page link</th><th>Broken images count</th><th>Broken images with status code</th></tr>");

            Map<String, List<String>> brokenImagesMap = new HashMap<>();

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String applicationName = (String) entry.getKey();
                String applicationUrl = (String) entry.getValue();

                driver.get(applicationUrl);
                driver.manage().window().maximize();
                Thread.sleep(4000);

                // Get all images on the page
                List<WebElement> allImages = driver.findElements(By.tagName("img"));

                ArrayList<String> imagesList = getAttributeList(allImages, "src");

                String[] imagesArray = imagesList.toArray(new String[0]);

                int brokenImagesCount = getBrokenImagesCount(imagesArray);

                if (brokenImagesCount > 0) {
                    List<String> brokenImagesList = getBrokenImagesList(imagesArray);
                    brokenImagesMap.put(applicationUrl, brokenImagesList);
                }

                // Find all iframes on the page
                List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

                // Iterate through each iframe
                for (WebElement iframe : iframes) {
                    driver.switchTo().frame(iframe);

                    // Get all images within the iframe
                    List<WebElement> iframeImages = driver.findElements(By.tagName("img"));

                    ArrayList<String> iframeImagesList = getAttributeList(iframeImages, "src");

                    String[] iframeImagesArray = iframeImagesList.toArray(new String[0]);

                    int iframeBrokenImagesCount = getBrokenImagesCount(iframeImagesArray);

                    if (iframeBrokenImagesCount > 0) {
                        List<String> iframeBrokenImagesList = getBrokenImagesList(iframeImagesArray);
                        brokenImagesMap.put(applicationUrl + " (iframe)", iframeBrokenImagesList);
                    }

                    driver.switchTo().defaultContent(); // Switch back to the main content
                }
            }

            // Generate rows in the HTML report
            for (Map.Entry<String, List<String>> entry : brokenImagesMap.entrySet()) {
                String applicationInfo = entry.getKey();
                List<String> brokenImagesList = entry.getValue();
                int brokenImagesCount = brokenImagesList.size();

                ImageStatusReporter(writer, applicationInfo, brokenImagesCount, brokenImagesList);
            }

            // Close the table and the HTML body
            writer.write("</table></body></html>");

            System.out.println("Broken Images Validation Completed. Please refer BrokenImages_validation_report in the project directory");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    // Close the writer
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            driver.quit(); // Close the WebDriver instance
        }
    }

    private static ArrayList<String> getAttributeList(List<WebElement> elements, String attribute) {
        ArrayList<String> attributeList = new ArrayList<>();
        for (WebElement element : elements) {
            String value = element.getAttribute(attribute);
            if (value != null && !value.isEmpty()) {
                attributeList.add(value);
            }
        }
        return attributeList;
    }

    private static int getBrokenImagesCount(String[] imagesArray) {
        int brokenImagesCount = 0;
        for (String imageUrlToCheck : imagesArray) {
            if (!isLinkWorking(imageUrlToCheck)) {
                brokenImagesCount++;
            }
        }
        return brokenImagesCount;
    }

    private static List<String> getBrokenImagesList(String[] imagesArray) {
        List<String> brokenImagesList = new ArrayList<>();
        for (String imageUrlToCheck : imagesArray) {
            if (!isLinkWorking(imageUrlToCheck)) {
                brokenImagesList.add(imageUrlToCheck);
            }
        }
        return brokenImagesList;
    }

    private static boolean isLinkWorking(String urlToCheck) {
        try {
            URL url = new URL(urlToCheck);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + e.getMessage());
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static void ImageStatusReporter(BufferedWriter writer, String applicationInfo, int brokenImagesCount, List<String> brokenImagesList) {
        try {
            // Create an HTML row with the image data
            writer.write("<tr>");
            writer.write("<td>" + applicationInfo + "</td>");
            writer.write("<td><a href='" + applicationInfo.split(" ")[0] + "'>" + applicationInfo.split(" ")[0] + "</a></td>");
            writer.write("<td>" + brokenImagesCount + "</td>");
            writer.write("<td>");
            writeLinksList(writer, brokenImagesList);
            writer.write("</td>");
            writer.write("</tr>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLinksList(BufferedWriter writer, List<String> linksList) throws IOException {
        for (String brokenImage : linksList) {
            int responseCode = getResponseCode(brokenImage);
            writer.write("<a href='" + brokenImage + "'>" + brokenImage + "</a> (Status Code: " + responseCode + ")<br>");
        }
    }

    private static int getResponseCode(String urlToCheck) {
        try {
            URL url = new URL(urlToCheck);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode;
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + e.getMessage());
            return -1;
        } catch (IOException e) {
            return -1;
        }
    }
}
