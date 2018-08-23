package teammates.ui.controller;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreFailureException;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import teammates.common.util.AppUrl;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.common.util.Logger;
import teammates.ui.pagedata.FileUploadPageData;

import java.util.List;
import java.util.Map;

/**
 * Action: uploads a document to Google Cloud Storage.
 */
public class PdfFileUploadAction extends Action {

    private static final Logger log = Logger.getLogger();

    private FileUploadPageData data;

    @Override
    protected ActionResult execute() {
        data = prepareData();

        return createAjaxResult(data);
    }

    protected String getDocKeyParam() {
        return Const.ParamsNames.DOC_TO_UPLOAD;
    }

    protected FileUploadPageData prepareData() {
        FileUploadPageData data = new FileUploadPageData(account, sessionToken);
        BlobInfo blobInfo = extractDocumentKey(getDocKeyParam());

        if (blobInfo == null) {
            data.isFileUploaded = false;
            data.fileSrcUrl = null;
            log.warning("Failed to upload the document");
            statusToAdmin = "Failed to upload the document";
            return data;
        }

        BlobKey blobKey = blobInfo.getBlobKey();

        data.isFileUploaded = true;
        AppUrl fileSrcUrl = Config.getAppUrl(Const.ActionURIs.PUBLIC_DOC_SERVE)
                .withParam(Const.ParamsNames.BLOB_KEY, blobKey.getKeyString());
        String absoluteFileSrcUrl = fileSrcUrl.toAbsoluteString();
        data.fileSrcUrl = fileSrcUrl.toString();

        log.info("New document Uploaded : " + absoluteFileSrcUrl);
        statusToAdmin = "New document Uploaded : " + "<a href=" + data.fileSrcUrl + " target=\"_blank\">"
                + absoluteFileSrcUrl + "</a>";
        data.ajaxStatus = "Document Successfully Uploaded to Google Cloud Storage";

        return data;
    }

    /**
     * Extracts the document metadata by the passed document key parameter.
     */
    protected BlobInfo extractDocumentKey(String param) {
        try {
            Map<String, List<BlobInfo>> blobsMap = BlobstoreServiceFactory.getBlobstoreService().getBlobInfos(request);
            List<BlobInfo> blobs = blobsMap.get(param);

            if (blobs == null || blobs.isEmpty()) {
                data.ajaxStatus = Const.StatusMessages.NO_DOC_GIVEN;
                isError = true;
                return null;
            }

            BlobInfo pdfBlob = blobs.get(0);
            return validateImage(pdfBlob);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Validates the document by size and content type.
     */
    protected BlobInfo validateImage(BlobInfo document) {
        if (document.getSize() > Const.SystemParams.MAX_PDF_DOC_SIZE) {
            this.deleteDocument(document.getBlobKey());
            isError = true;
            data.ajaxStatus = Const.StatusMessages.DOC_TOO_LARGE;
            return null;
        } else if (!document.getContentType().contains("application/pdf")) {
            this.deleteDocument(document.getBlobKey());
            isError = true;
            data.ajaxStatus = Const.StatusMessages.FILE_NOT_A_DOC;
            return null;
        }

        return document;
    }

    /**
     * Deletes the uploaded document.
     */
    protected void deleteDocument(BlobKey blobKey) {
        if (blobKey.equals(new BlobKey(""))) {
            return;
        }

        try {
            deleteUploadedFile(blobKey);
        } catch (BlobstoreFailureException bfe) {
            statusToAdmin = Const.ACTION_RESULT_FAILURE
                    + " : Unable to delete picture (possible unused picture with key: "
                    + blobKey.getKeyString()
                    + " || Error Message: "
                    + bfe.getMessage() + System.lineSeparator();
        }
    }

    /**
     * Deletes the uploaded file from Google Cloud Storage.
     */
    protected void deleteUploadedFile(BlobKey blobKey) {
        logic.deleteUploadedFile(blobKey);
    }
}
