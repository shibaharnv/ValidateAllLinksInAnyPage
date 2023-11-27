package linkscheck;



import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ImageValidationWIthoutHTTP {

    static WebDriver driver;

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config2.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        driver = new ChromeDriver();
        BufferedWriter writer = null;

        try {
            // Create an HTML report file
            writer = new BufferedWriter(new FileWriter("ImageValidationReport.html"));

            // Write the HTML header and table structure
            writer.write("<html><head><title>Image Validation Report</title></head><body>");
            writer.write("<table border='1'><tr><th>Application Name</th><th>Page link</th><th>Total Images Count</th><th>Missing Images Count</th><th>Missing Images</th></tr>");

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String applicationName = (String) entry.getKey();
                String applicationUrl = (String) entry.getValue();

                driver.get(applicationUrl);
                driver.manage().window().maximize();
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Get all images on the page
                List<WebElement> allImages = driver.findElements(By.tagName("img"));

                ///*************
                ArrayList<String> imagesList = getAttributeList(allImages, "src");

                String[] imagesArray = imagesList.toArray(new String[0]);

                int totalImagesCount = imagesArray.length;
                System.out.println("Total Images "+totalImagesCount);
                int missingImagesCount = getMissingImagesCount(imagesArray);

                List<String> missingImagesList = getMissingImagesList(imagesArray);
                System.out.println("Missing Images "+missingImagesCount);

                // Generate row in the HTML report
                ImageValidationReport(writer, applicationName, applicationUrl, totalImagesCount, missingImagesCount, missingImagesList);
            }

            // Close the table and the HTML body
            writer.write("</table></body></html>");

            System.out.println("Image Validation Completed. Please refer ImageValidationReport in the project directory");

        } catch (IOException e) {
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
            try {
                // Try to get the attribute value
                String value = element.getAttribute(attribute);

                // If the value is not null and not empty, add it to the list
                if (value != null && !value.isEmpty()) {
                    attributeList.add(value);
                }
            } catch (StaleElementReferenceException e) {
                // Handle stale element exception by re-locating the element
                System.out.println("StaleElementReferenceException occurred. Trying to re-locate the element.");
                elements = driver.findElements(By.tagName("img")); // or re-locate the elements as needed
                continue; // Retry the operation for the current element
            }
        }

        return attributeList;
    }

    private static int getMissingImagesCount(String[] imagesArray) {
        int missingImagesCount = 0;
        for (String imageUrlToCheck : imagesArray) {
            if (!isImageDisplayed(imageUrlToCheck)) {
                missingImagesCount++;
            }
        }
        return missingImagesCount;
    }

    private static List<String> getMissingImagesList(String[] imagesArray) {
        List<String> missingImagesList = new ArrayList<>();
        for (String imageUrlToCheck : imagesArray) {
            if (!isImageDisplayed(imageUrlToCheck)) {
                missingImagesList.add(imageUrlToCheck);
            }
        }
        return missingImagesList;
    }

    private static boolean isImageDisplayed(String imageUrl) {
        try {
            // Check if the image is displayed on the page
            WebElement imageElement = driver.findElement(By.cssSelector("img[src='" + imageUrl + "']"));
            return imageElement.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException | StaleElementReferenceException e) {
            // If the image element is not found or stale, consider it not displayed
            return false;
        }
    }

    private static void ImageValidationReport(BufferedWriter writer, String applicationName, String applicationUrl, int totalImagesCount, int missingImagesCount, List<String> missingImagesList) {
        try {
            // Create an HTML row with the image data
            writer.write("<tr>");
            writer.write("<td>" + applicationName + "</td>");
            writer.write("<td><a href='" + applicationUrl + "'>" + applicationUrl + "</a></td>");
            writer.write("<td>" + totalImagesCount + "</td>");
            writer.write("<td>" + missingImagesCount + "</td>");
            writer.write("<td>");
            writeLinksList(writer, missingImagesList);
            writer.write("</td>");
            writer.write("</tr>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLinksList(BufferedWriter writer, List<String> linksList) throws IOException {
        for (String missingImage : linksList) {
            writer.write("<a href='" + missingImage + "'>" + missingImage + "</a><br>");
        }
    }
}
