package teammates.test.durianmate.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import teammates.test.driver.Priority;
import teammates.test.driver.TestProperties;
import teammates.test.durianmate.SeleniumService;
import teammates.test.durianmate.pages.LoginPage;
import teammates.test.durianmate.pages.PdfUploadTestPage;

import java.io.File;

@Test(singleThreaded = true)
public class PdfUploadTest
{
    private WebDriver webDriver;

    @BeforeMethod
    private void startBrowser()
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

        // Now get into the PDF upload test page
        webDriver.get(TestProperties.TEAMMATES_URL + "/pdfupload-test.html");
    }

    @AfterMethod
    private void killBrowser()
    {
        webDriver.close();
    }

    @Test
    @Priority(1)
    private void testCreateUploadLink()
    {
        PdfUploadTestPage pdfUploadTestPage = PageFactory.initElements(webDriver, PdfUploadTestPage.class);
        pdfUploadTestPage.clickCreateUrlButton();

        // Wait until the page is fully loaded
        WebDriverWait waitUploadEndpoint = new WebDriverWait(webDriver, 10);
        waitUploadEndpoint.until(ExpectedConditions.visibilityOfElementLocated(By.id("step1-result")));

        try {
            Assert.assertTrue(webDriver.findElement(new By.ById("step1-result")).getText().contains("/_ah/upload/"));
        } catch (NoSuchElementException exception) {
            System.out.println("It's gonna fail...");
            Assert.fail("Element not found");
        }
    }

    @Test
    @Priority(2)
    private void testPdfUpload()
    {
        // Get the upload endpoint URL first
        PdfUploadTestPage pdfUploadTestPage = PageFactory.initElements(webDriver, PdfUploadTestPage.class);
        pdfUploadTestPage.clickCreateUrlButton();

        // Wait until the page is fully loaded
        WebDriverWait waitUploadEndpoint = new WebDriverWait(webDriver, 10);
        waitUploadEndpoint.until(ExpectedConditions.visibilityOfElementLocated(By.id("step1-result")));

        // Now try to upload a file
        pdfUploadTestPage.addFileToFileInput(getFullFilePath("/TestPdf.pdf"));
        pdfUploadTestPage.clickUploadButton();

        // Wait until the upload result is fully loaded
        WebDriverWait waitUploadResult = new WebDriverWait(webDriver, 10);
        waitUploadResult.until(ExpectedConditions.visibilityOfElementLocated(By.id("step2-result")));

        // Now have a look at the result
        try {
            WebElement element = webDriver.findElement(new By.ById("step2-result"));
            Assert.assertTrue(element.getText().contains("File uploaded successfully"));
            Assert.assertEquals("a", element.getTagName().toLowerCase());
        } catch (NoSuchElementException exception) {
            System.out.println("It's gonna fail...");
            Assert.fail("Element not found");
        }
    }

    @Test
    @Priority(3)
    private void testInvalidFile()
    {
        // Get the upload endpoint URL first
        PdfUploadTestPage pdfUploadTestPage = PageFactory.initElements(webDriver, PdfUploadTestPage.class);
        pdfUploadTestPage.clickCreateUrlButton();

        // Wait until the page is fully loaded
        WebDriverWait waitUploadEndpoint = new WebDriverWait(webDriver, 10);
        waitUploadEndpoint.until(ExpectedConditions.visibilityOfElementLocated(By.id("step1-result")));

        // Now try to upload a file
        pdfUploadTestPage.addFileToFileInput(getFullFilePath("/FeedbackSessionsLogicTest.json"));
        pdfUploadTestPage.clickUploadButton();

        // Wait until the upload result is fully loaded
        WebDriverWait waitUploadResult = new WebDriverWait(webDriver, 10);
        waitUploadResult.until(ExpectedConditions.visibilityOfElementLocated(By.id("step2-result")));

        // Now have a look at the result
        try {
            WebElement element = webDriver.findElement(new By.ById("step2-result"));
            Assert.assertTrue(element.getText().contains("The document that you have uploaded is not a PDF file"));
            Assert.assertEquals("p", element.getTagName().toLowerCase());
        } catch (NoSuchElementException exception) {
            System.out.println("It's gonna fail...");
            Assert.fail("Element not found");
        }
    }

    private static String getFullFilePath(String testFile)
    {
        return new File(TestProperties.TEST_DATA_FOLDER + testFile).getAbsolutePath();
    }
}
