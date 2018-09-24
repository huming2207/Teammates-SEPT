package teammates.test.driver;

import teammates.common.util.EmailWrapper;
import teammates.logic.api.EmailSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows mocking of the {@link EmailSender} API used in production.
 *
 * <p>Instead of actually sending the email via the configured email sending service,
 * the API will perform some operations that allow the sent emails to be tracked.
 */
public class MockEmailSender extends EmailSender {

    private List<EmailWrapper> sentEmails = new ArrayList<>();

    @Override
    public void sendEmail(EmailWrapper email) {
        sentEmails.add(email);
    }

    @Override
    public List<EmailWrapper> getEmailsSent() {
        return sentEmails;
    }

}
