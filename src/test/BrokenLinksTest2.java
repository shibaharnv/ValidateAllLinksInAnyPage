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


public class BrokenLinksTest2 {
    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Properties properties = new Properties();
            try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Create an HTML report file
            BufferedWriter writer = new BufferedWriter(new FileWriter("Links_validation_report.html"));

            // Write the HTML header and table structure
            writer.write("<html><head><title>Link Status Report</title></head><body>");
            writer.write("<table border='1'><tr><th>Application Name</th><th>Page link</th><th>Total links count</th><th>LinksNotWorkingCount</th><th>Not working links with status code</th></tr>");

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String applicationName = (String) entry.getKey();
                String applicationUrl = (String) entry.getValue();

                WebDriver driver = new ChromeDriver();
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

                driver.quit(); // Close the WebDriver instance
            }

            // Close the table and the HTML body
            writer.write("</table></body></html>");

            // Close the writer
            writer.close();

            System.out.println("Links Validation Completed .Please refer links_validation_report in project directory");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LinkStatusReporter(BufferedWriter writer, String applicationName, String[] linksToCheck, String applicationUrl) {
        try {
            int totalLinks = linksToCheck.length;
            int notWorkingLinksCount = 0;

           // StringBuilder notWorkingLinks = new StringBuilder("[");
            StringBuilder notWorkingLinks = new StringBuilder();

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
                        //notWorkingLinks.append("[\"").append(urlToCheck).append("\", ").append(responseCode).append("],");
                        notWorkingLinks.append("[\"").append(urlToCheck).append("\", ").append("],");
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

            // Remove the trailing comma and close the array
            if (notWorkingLinks.length() > 1) {
                notWorkingLinks.deleteCharAt(notWorkingLinks.length() - 1);
            }
            //notWorkingLinks.append("]");

            // Create an HTML row with the link data
            writer.write("<tr>");
            writer.write("<td>" + applicationName + "</td>");
            writer.write("<td><a href='" + applicationUrl + "'>" + applicationUrl + "</a></td>");
            writer.write("<td>" + totalLinks + "</td>");
            writer.write("<td>" + notWorkingLinksCount + "</td>");

            // Conditionally set the "Not working links with statuscode" column
            if (notWorkingLinksCount > 0) {

                System.out.println(notWorkingLinks);
                ArrayList<String> urlStatusList=urlStatusCodeSplit(String.valueOf(notWorkingLinks));

                System.out.println(urlStatusList);
                System.out.println(urlStatusList.size());

               // writer.write("<td>[<a href='" + urlStatusList.get(0) + "'>" + urlStatusList.get(0) + "</a>,"+ urlStatusList.get(1) +"]</td>");
               // writer.write("<td>[<a href='" + urlStatusList.get(2) + "'>" + urlStatusList.get(2) + "</a>,"+ urlStatusList.get(3) +"]</td>");
              //  writer.write("<td>" + urlStatusList + "</td>");

//                for(int i=0;i<urlStatusList.size();i++)
//                {
//                    writer.write("<td>[<a href='" + urlStatusList.get(i) + "'>" + urlStatusList.get(0) + "</a>,"+ urlStatusList.get(i+1) +"]</td>");
//                    i=i+1;
//                }
              //  writer.write("<td>" + notWorkingLinks.toString() + "</td>");
                writer.write("<td><a href='" + notWorkingLinks + "'>" + notWorkingLinks + "</a></td>");
                //writer.write("<td><a href='http://stackoverflow.com/questions'></a>Testing</td>");
               // writer.write("<td>"<a href='http://stackoverflow.com/questions/579335/javascript-regexp-to-wrap-urls-and-emails-in-anchors'</a>+ "</td>");
                //<a href='http://stackoverflow.com/questions/579335/javascript-regexp-to-wrap-urls-and-emails-in-anchors'>stackoverflow.com</a>
              //  writer.write("<td><a href='" + notWorkingLinks.toString() + "'>" + notWorkingLinks.toString() + "</a></td>");
            } else {
                writer.write("<td>NA</td>");
            }

            writer.write("</tr>");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static  ArrayList<String> urlStatusCodeSplit(String fullString)
    {

        ArrayList<String> al = new ArrayList<>();

        String[] strarr= fullString.split(",");

        //System.out.println(strarr[0]);

        for(String test:strarr)
        {
            // System.out.println(test);

            if(test.contains("http"))
            {
                test= test.replace("[","");
                test = test.replaceAll("^\"|\"$", "");

            }
            else
            {
                test= test.replace("]","");
            }
            al.add(test);
            System.out.println(test);
        }

        return al;

    }

}


