package teammates.test.pageobjects;

import org.openqa.selenium.By;

import static org.testng.AssertJUnit.assertTrue;

public class AdminAccountDetailsPage extends AppPage {

    public AdminAccountDetailsPage(Browser browser) {
        super(browser);
    }

    @Override
    protected boolean containsExpectedPageContents() {
        return getPageSource().contains("<h1>Instructor Account Details</h1>");
    }

    public AdminAccountDetailsPage clickRemoveInstructorFromCourse(String courseId) {
        waitForElementPresence(By.id("instructor_" + courseId));
        click(browser.driver.findElement(By.id("instructor_" + courseId)));
        waitForPageToLoad();
        return this;
    }

    public AdminAccountDetailsPage clickRemoveStudentFromCourse(String courseId) {
        waitForElementPresence(By.id("student_" + courseId));
        click(browser.driver.findElement(By.id("student_" + courseId)));
        waitForPageToLoad();
        return this;
    }

    public void verifyIsCorrectPage(String instructorId) {
        assertTrue(containsExpectedPageContents());
        assertTrue(getPageSource().contains("<p class=\"form-control-static\">" + instructorId + "</p>"));
    }

}
