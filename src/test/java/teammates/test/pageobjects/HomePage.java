package teammates.test.pageobjects;

import teammates.test.driver.TestProperties;

/**
 * Represents the home page of the website (i.e., index.jsp).
 */
public class HomePage extends AppPage {

    public HomePage(Browser browser) {
        super(browser);
    }

    @Override
    protected boolean containsExpectedPageContents() {
        return getPageSource().contains("Designed for Simplicity, Flexibility, and Power:");
    }

    public LoginPage clickInstructorLogin() {
        browser.driver.get(TestProperties.TEAMMATES_URL + "/login?instructor");
        waitForPageToLoad();

        String pageSource = getPageSource();
        if (InstructorHomePage.containsExpectedPageContents(pageSource)) {
            //already logged in. We need to logout because the return type of
            //  this method is a LoginPage
            logout();
            browser.driver.get(TestProperties.TEAMMATES_URL + "/login?instructor");
            waitForPageToLoad();
        }
        return createCorrectLoginPageType(browser);

    }

    public LoginPage clickStudentLogin() {
        browser.driver.get(TestProperties.TEAMMATES_URL + "/login?student");
        waitForPageToLoad();

        String pageSource = getPageSource();
        if (StudentHomePage.containsExpectedPageContents(pageSource)) {
            //already logged in. We need to logout because the return type of
            //  this method is a LoginPage
            logout();
            browser.driver.get(TestProperties.TEAMMATES_URL + "/login?student");
            waitForPageToLoad();
        }
        return createCorrectLoginPageType(browser);
    }

}
