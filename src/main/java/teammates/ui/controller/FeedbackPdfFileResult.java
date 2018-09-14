package teammates.ui.controller;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.exception.PageNotFoundException;
import teammates.common.util.StatusMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class FeedbackPdfFileResult extends ActionResult {

    /** The Google Cloud Storage blob key for the image. */
    public String blobKey;

    public FeedbackPdfFileResult(String destination, String blobKey, AccountAttributes account,
                                 List<StatusMessage> status) {
        super(destination, account, status);
        this.blobKey = blobKey;
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if (blobKey.isEmpty()) {
            throw new PageNotFoundException("File blob key is empty!");
        } else {
            resp.setContentType("application/pdf");
            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            blobstoreService.serve(new BlobKey(blobKey), resp);
        }
    }

}
