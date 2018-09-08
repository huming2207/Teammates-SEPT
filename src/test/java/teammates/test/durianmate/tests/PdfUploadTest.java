package teammates.test.durianmate.tests;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import teammates.test.driver.Priority;
import teammates.test.driver.TestProperties;
import teammates.test.durianmate.SeleniumService;
import teammates.test.durianmate.pages.LoginPage;
import teammates.test.durianmate.pages.StudentFeedbackPage;

import java.util.concurrent.TimeUnit;

@Test(singleThreaded = true)
public class PdfUploadTest
{
    private WebDriver webDriver;

    @BeforeMethod
    private void prepareBrowser()
    {
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
    private void killBrowser()
    {
        //webDriver.close();
    }

    @Test
    public void testTinyMCE()
    {
        // Click the input box and see what's happen next
        StudentFeedbackPage studentFeedbackPage = PageFactory.initElements(webDriver, StudentFeedbackPage.class);
        studentFeedbackPage.clickTextBox();
        Assert.assertTrue(studentFeedbackPage.getAnswerTextBox().getAttribute("class").contains("mce-edit-focus"));
    }

    @Test
    public void testPdfUpload()
    {
        //TODO: Too hard to test something with complex, webpack transcompiled JavaScript
        // As a result, a human is necessary to select the file and click "Open" to upload it
        StudentFeedbackPage studentFeedbackPage = PageFactory.initElements(webDriver, StudentFeedbackPage.class);
        studentFeedbackPage.clickTextBox();
        studentFeedbackPage.clickInsertAttachmentLinkButton();


        // The code below doesn't work for auto selection, so we have to do it by real human
        /*

        // The upload button itself is not clickable, so we need to move the selenium's cursor to there and click it.
        Actions uploadButtonAction = new Actions(webDriver);
        uploadButtonAction.moveToElement(studentFeedbackPage.getUploadButton()).click().perform();

        // Same for the "Ok" button
        Actions okayButtonAction = new Actions(webDriver);
        okayButtonAction.moveToElement(studentFeedbackPage.getOkayButtonDiv()).click().perform(); */

        // Now wait for 15 seconds for a real human to complete a file selection
        webDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

        // Now probe and see how's going on there
        Assert.assertTrue(
                webDriver.findElements(By.partialLinkText("http://localhost:8080/public/getDoc")).size() > 1);
    }
}
