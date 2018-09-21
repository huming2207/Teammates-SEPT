package teammates.ui.controller;

import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.StatusMessage;
import teammates.common.util.StringHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class JsonDownloadResult extends ActionResult {

    private String fileContent = "";
    private String fileName = "";

    public JsonDownloadResult(String destination, AccountAttributes account,
                             List<StatusMessage> status) {
        super(destination, account, status);
    }

    public JsonDownloadResult(
            String destination, AccountAttributes account,
            List<StatusMessage> status,
            String fileName, String fileContent) {
        super(destination, account, status);
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Set to JSON content type
        resp.setContentType("application/json; charset=UTF-8");

        // Content-Disposition is a header on the HTTP response to suggest a filename
        // if the contents of the response is saved to a file.
        resp.setHeader("Content-Disposition", getContentDispositionHeader());
        PrintWriter writer = resp.getWriter();
        writer.append(fileContent);
    }

    /**
     * Suggests a filename for the content of the response to be saved as.
     * @return value of the HTTP Content-Disposition header
     */
    public String getContentDispositionHeader() {
        return "attachment; filename=\"" + getAsciiOnlyJsonFileName() + "\";"
                + "filename*= UTF-8''" + getUrlEscapedJsonFileName();
    }

    private String getAsciiOnlyJsonFileName() {
        return StringHelper.removeNonAscii(fileName) + ".json";
    }

    private String getUrlEscapedJsonFileName() {
        return SanitizationHelper.sanitizeForUri(fileName) + ".json";
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFileContent() {
        return this.fileContent;
    }

}
