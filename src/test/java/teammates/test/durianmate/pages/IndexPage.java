package teammates.test.durianmate.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class IndexPage {

    @FindBy(how = How.XPATH, using = "//a[@href=\"/login.html\"]")
    private WebElement loginButton;

    public void login() {
        loginButton.click();
    }
}
