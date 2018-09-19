package teammates.test.durianmate.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import teammates.test.driver.TestProperties;
import teammates.test.durianmate.SeleniumService;
import teammates.test.durianmate.pages.LoginPage;
import teammates.test.durianmate.pages.StudentFeedbackPage;

@Test(singleThreaded = true)
public class FeedbackEditorTest {

    private WebDriver webDriver;

    @BeforeMethod
    private void prepareBrowser() {

        webDriver = SeleniumService.getWebDriver();
        webDriver.get(TestProperties.TEAMMATES_URL + "/login.html");

        // Fill in the student info and login
        LoginPage loginPage = PageFactory.initElements(webDriver, LoginPage.class);
        loginPage.inputUserEmail(TestProperties.TEST_STUDENT1_ACCOUNT);
        loginPage.selectRole("Student");
        loginPage.login();

        // Wait until the page is fully loaded
        WebDriverWait waitHomePage = new WebDriverWait(webDriver, 10);
        waitHomePage.until(ExpectedConditions.elementToBeClickable(By.className("navbar-brand")));

        // Click the submission button at the student home page
        WebElement startSubmission = webDriver.findElement(By.linkText("Start Submission"));
        startSubmission.click();

        // Wait for a while before the page is fully loaded
        // Ref: https://stackoverflow.com/questions/5868439/wait-for-page-load-in-selenium
        Wait<WebDriver> waitFeedbackPage = new WebDriverWait(webDriver, 30);
        waitFeedbackPage.until(driver -> {
            assert driver != null;
            return String
                    .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                    .equals("complete");
        });
    }

    @AfterMethod
    private void killBrowser() {
        webDriver.close();
    }

    @Test
    public void testTinyMce() {
        // Click the input box and see what's happen next
        StudentFeedbackPage studentFeedbackPage = PageFactory.initElements(webDriver, StudentFeedbackPage.class);
        studentFeedbackPage.clickTextBox();
        Assert.assertTrue(studentFeedbackPage.getAnswerTextBox().getAttribute("class").contains("mce-edit-focus"));
    }
}
