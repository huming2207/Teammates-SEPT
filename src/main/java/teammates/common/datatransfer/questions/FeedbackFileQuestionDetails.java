package teammates.common.datatransfer.questions;

import teammates.common.datatransfer.FeedbackSessionResultsBundle;
import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.util.Const;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.Templates;
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
        return Templates.populateTemplate(
                Templates.FeedbackQuestion.FormTemplates.TEXT_SUBMISSION_FORM,
                Templates.FeedbackQuestion.Slots.IS_SESSION_OPEN, Boolean.toString(sessionIsOpen),
                Templates.FeedbackQuestion.Slots.FEEDBACK_RESPONSE_TEXT, Const.ParamsNames.FEEDBACK_RESPONSE_TEXT,
                Templates.FeedbackQuestion.Slots.QUESTION_INDEX, Integer.toString(qnIdx),
                Templates.FeedbackQuestion.Slots.RESPONSE_INDEX, Integer.toString(responseIdx),
                Templates.FeedbackQuestion.Slots.TEXT_EXISTING_RESPONSE, existingResponseDetails.getAnswerString());
    }

    @Override
    public String getQuestionWithoutExistingResponseSubmissionFormHtml(boolean sessionIsOpen, int qnIdx, int responseIdx, String courseId, int totalNumRecipients, StudentAttributes student)
    {
        return Templates.populateTemplate(
                Templates.FeedbackQuestion.FormTemplates.TEXT_SUBMISSION_FORM,
                Templates.FeedbackQuestion.Slots.IS_SESSION_OPEN, Boolean.toString(sessionIsOpen),
                Templates.FeedbackQuestion.Slots.FEEDBACK_RESPONSE_TEXT, Const.ParamsNames.FEEDBACK_RESPONSE_TEXT,
                Templates.FeedbackQuestion.Slots.QUESTION_INDEX, Integer.toString(qnIdx),
                Templates.FeedbackQuestion.Slots.RESPONSE_INDEX, Integer.toString(responseIdx),
                Templates.FeedbackQuestion.Slots.TEXT_EXISTING_RESPONSE, "");
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
        return "";
    }

    @Override
    public boolean shouldChangesRequireResponseDeletion(FeedbackQuestionDetails newDetails)
    {
        return false;
    }

    @Override
    public String getCsvHeader()
    {
        return "Feedback";
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
