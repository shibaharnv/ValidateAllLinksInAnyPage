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
public class BrokenLinksTest {
    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WebDriver driver = new ChromeDriver(); // Create WebDriver instance once

        try {
            // Create an HTML report file
            BufferedWriter writer = new BufferedWriter(new FileWriter("Links_validation_report.html"));

            // Write the HTML header and table structure
            writer.write("<html><head><title>Link Status Report</title></head><body>");
            writer.write("<table border='1'><tr><th>Application Name</th><th>Page link</th><th>Total links count</th><th>LinksNotWorkingCount</th><th>Not working links with status code</th></tr>");

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String applicationName = (String) entry.getKey();
                String applicationUrl = (String) entry.getValue();

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

                LinkStatusReporter(writer, applicationName, stringArray, applicationUrl);
            }

            // Close the table and the HTML body
            writer.write("</table></body></html>");

            // Close the writer
            writer.close();

            System.out.println("Links Validation Completed. Please refer links_validation_report in the project directory");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Close the WebDriver instance
        }
    }


    public static void LinkStatusReporter(BufferedWriter writer, String applicationName, String[] linksToCheck, String applicationUrl) {
        try {
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
                    String responseMessage = connection.getResponseMessage();
                    boolean linkIsWorking = responseCode == HttpURLConnection.HTTP_OK;

                    if (!linkIsWorking) {
                        String linkStatus = "<a href='" + urlToCheck + "'>" + urlToCheck + "</a>" + " " + responseCode;
                        notWorkingLinksList.add(linkStatus);
                        notWorkingLinksCount++;
                    }
                } catch (MalformedURLException e) {
                    System.err.println("Malformed URL: " + e.getMessage());
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
