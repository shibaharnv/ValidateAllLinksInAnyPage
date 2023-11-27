package linkscheck;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BrokenImagesTest5 {

    static WebDriver driver;
    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config2.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

         driver = new ChromeDriver();

        try {
            // Create an HTML report file
            BufferedWriter writer = new BufferedWriter(new FileWriter("BrokenImages_validation_report.html"));

            // Write the HTML header and table structure
            writer.write("<html><head><title>Broken Images Status Report</title></head><body>");
            writer.write("<table border='1'><tr><th>Application Name</th><th>Page link</th><th>Broken images count</th><th>Broken images with status code</th></tr>");

            for (Object key : properties.keySet()) {
                String applicationName = (String) key;
                String applicationUrl = properties.getProperty(applicationName);

                driver.get(applicationUrl);
                driver.manage().window().maximize();
                Thread.sleep(4000);

                List<WebElement> allImages = getImages(driver);
                ArrayList<String> imagesList = getAttributeList(allImages, "src");
                String[] imagesArray = imagesList.toArray(new String[0]);

                ImageStatusReporter(writer, applicationName, imagesArray, applicationUrl);
            }

            // Close the table and the HTML body
            writer.write("</table></body></html>");

            // Close the writer
            writer.close();

            System.out.println("Broken Images Validation Completed. Please refer BrokenImages_validation_report in the project directory");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Close the WebDriver instance
        }
    }


    private static List<WebElement> getImages(WebDriver driver) {
        List<WebElement> allImages = driver.findElements(By.tagName("img"));

        // Include images inside iframes
        List<WebElement> iframeElements = driver.findElements(By.tagName("iframe"));
        for (WebElement iframeElement : iframeElements) {
            try {
                driver.switchTo().frame(iframeElement);
                List<WebElement> iframeImages = driver.findElements(By.tagName("img"));
                allImages.addAll(iframeImages);
            } catch (StaleElementReferenceException e) {
                // Handle stale element exception, e.g., by retrying the operation
                System.out.println("Stale element exception. Retrying...");
                iframeElements = driver.findElements(By.tagName("iframe"));
                for (WebElement newIframeElement : iframeElements) {
                    driver.switchTo().frame(newIframeElement);
                    List<WebElement> iframeImages = driver.findElements(By.tagName("img"));
                    allImages.addAll(iframeImages);
                    driver.switchTo().defaultContent();
                }
            } finally {
                driver.switchTo().defaultContent();
            }
        }

        return allImages;
    }

//    private static ArrayList<String> getAttributeList(List<WebElement> elements, String attribute) {
//        ArrayList<String> attributeList = new ArrayList<>();
//        for (WebElement element : elements) {
//            String value = element.getAttribute(attribute);
//            if (value != null && !value.isEmpty()) {
//                attributeList.add(value);
//            }
//        }
//        return attributeList;
//    }
private static ArrayList<String> getAttributeList(List<WebElement> elements, String attribute) {
    ArrayList<String> attributeList = new ArrayList<>();
    for (WebElement element : elements) {
        try {
            // Retry the operation if a StaleElementReferenceException occurs
            String value = element.getAttribute(attribute);
            if (value != null && !value.isEmpty()) {
                attributeList.add(value);
            }
        } catch (StaleElementReferenceException e) {
            // Handle stale element exception, e.g., by retrying the operation
            System.out.println("Stale element exception in getAttributeList. Retrying...");
            // You may need to re-obtain the elements before retrying
            elements = getImages(driver);  // Ensure you have access to the driver instance
            for (WebElement newElement : elements) {
                try {
                    String value = newElement.getAttribute(attribute);
                    if (value != null && !value.isEmpty()) {
                        attributeList.add(value);
                    }
                } catch (StaleElementReferenceException ex) {
                    // Log or handle the exception as needed
                    System.out.println("Stale element exception in getAttributeList. Skipping...");
                }
            }
        }
    }
    return attributeList;
}

    private static void ImageStatusReporter(BufferedWriter writer, String applicationName, String[] imagesToCheck, String applicationUrl) {
        try {
            int brokenImagesCount = 0;
            List<String> brokenImagesList = new ArrayList<>();

            for (String imageUrlToCheck : imagesToCheck) {
                if (!isLinkWorking(imageUrlToCheck)) {
                    brokenImagesList.add(imageUrlToCheck);
                    brokenImagesCount++;
                }
            }

            // Create an HTML row with the image data
            writer.write("<tr>");
            writer.write("<td>" + applicationName + "</td>");
            writer.write("<td><a href='" + applicationUrl + "'>" + applicationUrl + "</a></td>");
            writer.write("<td>" + brokenImagesCount + "</td>");
            writer.write("<td>");
            writeLinksList(writer, brokenImagesList);
            writer.write("</td>");
            writer.write("</tr>");

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static void writeLinksList(BufferedWriter writer, List<String> linksList) throws IOException {
        for (String brokenImage : linksList) {
            writer.write("<a href='" + brokenImage + "'>" + brokenImage + "</a><br>");
        }
    }
}
