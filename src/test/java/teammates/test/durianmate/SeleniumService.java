package teammates.test.durianmate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import teammates.test.driver.TestProperties;

public class SeleniumService
{
    private static WebDriver webDriver = null;

    public static WebDriver getWebDriver()
    {
        if(webDriver == null) {
            System.setProperty("webdriver.chrome.driver", TestProperties.CHROMEDRIVER_PATH);
            webDriver = new ChromeDriver();
        }

        return webDriver;
    }
}
