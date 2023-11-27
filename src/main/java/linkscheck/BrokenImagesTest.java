package linkscheck;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BrokenImagesTest {
    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config2.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WebDriver driver = new ChromeDriver();

        try {
            // Create an HTML report file
            BufferedWriter writer = new BufferedWriter(new FileWriter("BrokenImages_validation_report.html"));

            // Write the HTML header and table structure
            writer.write("<html><head><title>Broken Images Status Report</title></head><body>");
            writer.write("<table border='1'><tr><th>Application Name</th><th>Page link</th><th>Total images count</th><th>Broken images count</th><th>Broken images with status code</th></tr>");

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String applicationName = (String) entry.getKey();
                String applicationUrl = (String) entry.getValue();

                driver.get(applicationUrl);
                driver.manage().window().maximize();
                Thread.sleep(4000);
                List<WebElement> allImages = driver.findElements(By.tagName("img"));

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

    public static void ImageStatusReporter(BufferedWriter writer, String applicationName, String[] imagesToCheck, String applicationUrl) {
        try {
            int totalImages = imagesToCheck.length;
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
            writer.write("<td>" + totalImages + "</td>");
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

