package teammates.common.datatransfer.questions;

import teammates.common.datatransfer.FeedbackSessionResultsBundle;
import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.util.Const;
import teammates.ui.template.InstructorFeedbackResultsResponseRow;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FeedbackFileQuestionDetails extends FeedbackQuestionDetails
{
    public FeedbackFileQuestionDetails()
    {
        super(FeedbackQuestionType.FILE);
    }

    @Override
    public String getQuestionTypeDisplayName()
    {
        return Const.FeedbackQuestionTypeNames.FILE;
    }

    @Override
    public String getQuestionWithExistingResponseSubmissionFormHtml(boolean sessionIsOpen, int qnIdx, int responseIdx, String courseId, int totalNumRecipients, FeedbackResponseDetails existingResponseDetails, StudentAttributes student)
    {
        return null;
    }

    @Override
    public String getQuestionWithoutExistingResponseSubmissionFormHtml(boolean sessionIsOpen, int qnIdx, int responseIdx, String courseId, int totalNumRecipients, StudentAttributes student)
    {
        return null;
    }

    @Override
    public String getQuestionSpecificEditFormHtml(int questionNumber)
    {
        return null;
    }

    @Override
    public String getNewQuestionSpecificEditFormHtml()
    {
        return null;
    }

    @Override
    public String getQuestionAdditionalInfoHtml(int questionNumber, String additionalInfoId)
    {
        return null;
    }

    @Override
    public String getQuestionResultStatisticsHtml(List<FeedbackResponseAttributes> responses, FeedbackQuestionAttributes question, String studentEmail, FeedbackSessionResultsBundle bundle, String view)
    {
        return null;
    }

    @Override
    public String getQuestionResultStatisticsCsv(List<FeedbackResponseAttributes> responses, FeedbackQuestionAttributes question, FeedbackSessionResultsBundle bundle)
    {
        return null;
    }

    @Override
    public boolean shouldChangesRequireResponseDeletion(FeedbackQuestionDetails newDetails)
    {
        return false;
    }

    @Override
    public String getCsvHeader()
    {
        return null;
    }

    @Override
    public List<String> getInstructions()
    {
        return null;
    }

    @Override
    public String getQuestionTypeChoiceOption()
    {
        return null;
    }

    @Override
    public List<String> validateQuestionDetails(String courseId)
    {
        return null;
    }

    @Override
    public List<String> validateResponseAttributes(List<FeedbackResponseAttributes> responses, int numRecipients)
    {
        return null;
    }

    @Override
    public String validateGiverRecipientVisibility(FeedbackQuestionAttributes feedbackQuestionAttributes)
    {
        return null;
    }

    @Override
    public boolean extractQuestionDetails(Map<String, String[]> requestParameters, FeedbackQuestionType questionType)
    {
        return false;
    }

    @Override
    public Comparator<InstructorFeedbackResultsResponseRow> getResponseRowsSortOrder()
    {
        return null;
    }
}
