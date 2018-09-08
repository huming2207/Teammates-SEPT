package teammates.test.durianmate.tests;

import com.gargoylesoftware.htmlunit.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;
import teammates.test.driver.TestProperties;
import teammates.test.durianmate.SeleniumService;
import teammates.test.durianmate.pages.IndexPage;
import teammates.test.durianmate.pages.LoginPage;

@Test(singleThreaded = true)
public class LoginTest
{
    private WebDriver webDriver;

    @BeforeMethod
    private void prepareBrowser()
    {
        webDriver = SeleniumService.getWebDriver();
        webDriver.get(TestProperties.TEAMMATES_URL + "/login.html");
    }

    @AfterMethod
    private void killBrowser()
    {
        webDriver.close();
    }

    @Test
    private void testAdminLogin()
    {
        // Fill in the admin info and login
        LoginPage loginPage = PageFactory.initElements(webDriver, LoginPage.class);
        loginPage.inputUserEmail(TestProperties.TEST_ADMIN_ACCOUNT);
        loginPage.selectRole("Administrator");
        loginPage.login();

        // Wait until the page is fully loaded
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-brand")));

        // Now do some test...
        Assert.assertEquals(webDriver.getCurrentUrl(), TestProperties.TEAMMATES_URL + "/admin/adminHomePage");
    }

    @Test
    private void testInstructorLogin()
    {
        // Fill in the instructor info and login
        LoginPage loginPage = PageFactory.initElements(webDriver, LoginPage.class);
        loginPage.inputUserEmail(TestProperties.TEST_INSTRUCTOR_ACCOUNT);
        loginPage.selectRole("Instructor");
        loginPage.login();

        // Wait until the page is fully loaded
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-brand")));


        // Now do some test...
        Assert.assertEquals(webDriver.getCurrentUrl(), TestProperties.TEAMMATES_URL + "/page/instructorHomePage");
    }

    @Test
    private void testStudentLogin()
    {
        // Fill in the student info and login
        LoginPage loginPage = PageFactory.initElements(webDriver, LoginPage.class);
        loginPage.inputUserEmail(TestProperties.TEST_STUDENT1_ACCOUNT);
        loginPage.selectRole("Student");
        loginPage.login();

        // Wait until the page is fully loaded
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-brand")));


        // Now do some test...
        Assert.assertEquals(webDriver.getCurrentUrl(), TestProperties.TEAMMATES_URL + "/page/studentHomePage");
    }

    @Test
    private void testBlockNonRmitAccount()
    {
        // Fill in the student info and login
        LoginPage loginPage = PageFactory.initElements(webDriver, LoginPage.class);
        loginPage.inputUserEmail(TestProperties.TEST_ADMIN_ACCOUNT);
        loginPage.selectRole("Student");
        loginPage.login();

        // Wait until the page is fully loaded
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-brand")));


        // Now do some test...
        Assert.assertTrue(webDriver.getCurrentUrl().contains("nonrmit.jsp"));
        Assert.assertTrue(webDriver.getPageSource().contains("You are not a RMIT user, " +
                "please contact the ITS to retrieve an account."));
    }
}
