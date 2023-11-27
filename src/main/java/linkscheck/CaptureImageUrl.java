package linkscheck;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.SocketException;
import java.time.Duration;

public class CaptureImageUrl {
    public static void main(String[] args) throws SocketException, InterruptedException {

        WebDriver driver = new ChromeDriver();

        try {
            // Navigate to your web page
            driver.get("https://codepen.io/makaroni4/pen/WLzgpN");
            WebElement iframe = driver.findElement(By.id("result"));
            driver.switchTo().frame(iframe);

            // Wait for the image to be present on the page
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            //WebElement imageElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//img[@alt='Hi, I\\'m a broken image']")));
            WebElement imageElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//img[@alt=\"Hi, I'm a broken image\"]")));

            // Capture the value of the src attribute
            String imageUrl = imageElement.getAttribute("src");

            // Print the captured URL
            System.out.println("Image URL: " + imageUrl);
        } finally {
            // Close the WebDriver instance
            driver.quit();
        }
    }
    }
        // Set the path to the ChromeDriver executable

//
//        // Create a new instance of the ChromeDriver
//        WebDriver driver = new ChromeDriver();
//
//        try {
//            // Navigate to your web page
//            driver.get("https://codepen.io/makaroni4/pen/WLzgpN");
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//           // WebElement imageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img[@src='foobar.jpg']")));
//           // WebElement imageElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//img[@src='foobar.jpg']")));
//
//            Thread.sleep(5000);
//            // Find the image element using its attributes
//            WebElement imageElement = driver.findElement(By.xpath("//img[@src='foobar.jpg']"));
//
//            // Capture the value of the src attribute
//            String imageUrl = imageElement.getAttribute("src");
//
//            // Print the captured URL
//            System.out.println("Image URL: " + imageUrl);
//        } finally {
//            // Close the WebDriver instance
//            driver.quit();
//        }
//    }


