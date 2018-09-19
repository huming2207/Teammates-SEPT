package teammates.test.durianmate.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class PortalPage {

    @FindBy(id = "btnLogout")
    private WebElement logoutButton;

    public void logOut() {
        logoutButton.click();
    }
}
