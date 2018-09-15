package teammates.ui.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import teammates.common.datatransfer.attributes.AccountAttributes;
import teammates.common.util.StatusMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class PdfDownloadResult extends ActionResult
{
    private String fileName;
    private PDDocument document;

    public PdfDownloadResult(String destination, AccountAttributes account, List<StatusMessage> status)
    {
        super(destination, account, status);
    }


    public PdfDownloadResult(String destination, AccountAttributes account, List<StatusMessage> status,
                             String fileName, PDDocument document)
    {
        super(destination, account, status);
        this.fileName = fileName;
        this.document = document;
    }

    @Override
    public void send(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        // Of course it should be a PDF...
        resp.setContentType("application/pdf");

        // Suggests it acts as a attachment with suggested file name
        resp.setHeader("Content-Disposition", "Content-Disposition: attachment; filename=\"" +
                this.fileName + "\"");

        document.save(resp.getOutputStream());
    }
}

