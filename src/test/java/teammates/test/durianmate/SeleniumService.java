package teammates.test.durianmate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import teammates.test.driver.TestProperties;

public class SeleniumService
{
    private static WebDriver webDriver = null;

    public static WebDriver getWebDriver()
    {
        if(webDriver == null) {
            if(TestProperties.BROWSER.equals("chrome")) {
                System.setProperty("webdriver.chrome.driver", TestProperties.CHROMEDRIVER_PATH);
                webDriver = new ChromeDriver();
            } else {
                System.setProperty("webdriver.gecko.driver", TestProperties.FIREFOX_PATH);
                webDriver = new FirefoxDriver();
            }

        }

        return webDriver;
    }
}
