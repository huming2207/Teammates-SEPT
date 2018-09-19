package teammates.test.durianmate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import teammates.test.driver.TestProperties;

public class SeleniumService {

    // This is NOT a singleton as it needs to close the browser every time.
    public static WebDriver getWebDriver() {

        WebDriver webDriver;

        if (TestProperties.BROWSER.equals("chrome")) {
            System.setProperty("webdriver.chrome.driver", TestProperties.CHROMEDRIVER_PATH);
            webDriver = new ChromeDriver();
        } else {
            System.setProperty("webdriver.gecko.driver", TestProperties.FIREFOX_PATH);
            webDriver = new FirefoxDriver();
        }

        return webDriver;
    }
}
