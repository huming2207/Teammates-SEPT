package teammates.test.durianmate.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class LoginPage {

    @FindBy(id = "userEmail")
    private WebElement userEmailTextBox;

    @FindBy(id = "role-dropdown")
    private WebElement roleSelection;

    @FindBy(id = "signin-button")
    private WebElement loginButton;

    public void selectRole(String roleName) {
        new Select(roleSelection).selectByVisibleText(roleName);
    }

    public void inputUserEmail(String email) {
        userEmailTextBox.sendKeys(email);
    }

    public void login() {
        loginButton.click();
    }
}
