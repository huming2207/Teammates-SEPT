package teammates.ui.controller;

import teammates.common.util.Const;
import teammates.common.util.GoogleCloudStorageHelper;
import teammates.common.util.Url;
import teammates.ui.pagedata.CreateDocUploadUrlAjaxPageData;

import com.google.appengine.api.blobstore.BlobstoreFailureException;

/**
 * Action: creates a URL for uploading a PDF document.
 */
public class CreateDocUploadUrlAction extends Action {

    @Override
    protected ActionResult execute() {
        return createAjaxResult(getCreateDocUploadUrlPageData());
    }

    protected final CreateDocUploadUrlAjaxPageData getCreateDocUploadUrlPageData() {
        CreateDocUploadUrlAjaxPageData data = new CreateDocUploadUrlAjaxPageData(account, sessionToken);

        try {
            data.nextUploadUrl = getUploadUrl();
            data.ajaxStatus = "Document upload url created, proceed to uploading";
        } catch (BlobstoreFailureException | IllegalArgumentException e) {
            data.nextUploadUrl = null;
            isError = true;
            data.ajaxStatus = "An error occurred when creating upload URL, please try again";
        }

        return data;
    }

    protected String getUploadUrl() {
        String callbackUrl =
                Url.addParamToUrl(Const.ActionURIs.DOC_UPLOAD, Const.ParamsNames.SESSION_TOKEN, sessionToken);
        return GoogleCloudStorageHelper.getNewUploadUrl(callbackUrl);
    }

}
