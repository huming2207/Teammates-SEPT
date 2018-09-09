package teammates.test.durianmate.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class PdfUploadTestPage
{
    @FindBy(how = How.ID, using = "get-url-button")
    private WebElement createUrlButton;

    @FindBy(how = How.ID, using = "file-selection")
    private WebElement fileInput;

    @FindBy(how = How.ID, using = "upload-file-button")
    private WebElement uploadButton;

    public void clickCreateUrlButton()
    {
        createUrlButton.click();
    }

    public void addFileToFileInput(String filePath)
    {
        fileInput.sendKeys(filePath);
    }

    public void clickUploadButton()
    {
        uploadButton.click();
    }
}
