package linkscheck;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class CaptureImageUrlsWithJsoup {


        public static void main (String[]args){
            // URL of the HTML page
           String url = "https://codepen.io/makaroni4/pen/WLzgpN";
        // String url="https://rahulshettyacademy.com/AutomationPractice/";

        // String url="https://www.google.com/";
            try {
                // Set a custom User-Agent header
                Connection connection = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

                // Fetch the HTML content using Jsoup
                Document document = connection.get();

                // Select all image elements in the HTML DOM
                Elements imageElements = document.select("img");

                // Iterate through each image element and capture the src attribute
                for (Element imageElement : imageElements) {
                    String imageUrl = imageElement.attr("src");
                    System.out.println("Image URL: " + imageUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
