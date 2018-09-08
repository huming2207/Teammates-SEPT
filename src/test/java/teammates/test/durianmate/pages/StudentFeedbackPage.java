package teammates.test.durianmate.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import teammates.ui.controller.Action;

public class StudentFeedbackPage
{
    @FindBy(how = How.ID, using = "responsetext-2-0")
    private WebElement answerTextBox;

    @FindBy(how = How.XPATH, using = "//div[@class=\"evalueeForm-2 form-group margin-0\"]")
    private WebElement responseTextBoxDiv;

    @FindBy(how = How.XPATH, using = "//i[@class=\"mce-ico mce-i-link\"]/..")
    private WebElement insertAttachmentLinkButton;

    @FindBy(how = How.XPATH, using = "//i[@class=\"mce-ico mce-i-browse\"]/../..")
    private WebElement uploadButtonDiv;

    @FindBy(how = How.XPATH, using = "//span[@class=\"mce-txt\"][text() = 'Ok']/../..")
    private WebElement okayButtonDiv;

    public void clickTextBox()
    {
        responseTextBoxDiv.click();
    }

    public void clickInsertAttachmentLinkButton()
    {
        insertAttachmentLinkButton.click();
    }

    public WebElement getUploadButton()
    {
        return this.uploadButtonDiv;
    }

    public WebElement getAnswerTextBox()
    {
        return this.answerTextBox;
    }

    public WebElement getOkayButtonDiv()
    {
        return this.okayButtonDiv;
    }
}
