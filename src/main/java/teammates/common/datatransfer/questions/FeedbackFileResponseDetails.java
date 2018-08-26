package teammates.common.datatransfer.questions;

import teammates.common.util.SanitizationHelper;

public class FeedbackFileResponseDetails extends FeedbackResponseDetails
{
    private String encodedBlob;

    public FeedbackFileResponseDetails()
    {
        super(FeedbackQuestionType.FILE);
    }

    public FeedbackFileResponseDetails(String encodedBlob)
    {
        super(FeedbackQuestionType.FILE);
        this.encodedBlob = encodedBlob;
    }

    @Override
    public void extractResponseDetails(FeedbackQuestionType questionType,
                                       FeedbackQuestionDetails questionDetails, String[] answer)
    {
        this.encodedBlob = answer[0];
    }

    @Override
    public String getAnswerString()
    {
        return String.format("<a download=\"submission.pdf\" href=\"%s\">" +
                "Download PDF submission</a>", this.encodedBlob);
    }

    @Override
    public String getAnswerHtmlInstructorView(FeedbackQuestionDetails questionDetails)
    {
        return String.format("<a download=\"submission.pdf\" href=\"%s\">" +
                "Download PDF submission</a>", this.encodedBlob);
    }

    @Override
    public String getAnswerCsv(FeedbackQuestionDetails questionDetails)
    {
        return SanitizationHelper.sanitizeForCsv(this.encodedBlob);
    }
}
