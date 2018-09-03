package teammates.ui.controller;

import teammates.common.datatransfer.attributes.FeedbackResponseAttributes;
import teammates.common.datatransfer.attributes.FeedbackResponseCommentAttributes;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.ui.pagedata.InstructorFeedbackResponseCommentAjaxPageData;

public class InstructorFeedbackResponseCommentPraiseDeleteAction extends InstructorFeedbackResponseCommentAbstractAction {

    @Override
    protected ActionResult execute() throws EntityDoesNotExistException {
        //
        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.COURSE_ID, courseId);
        String feedbackSessionName = getRequestParamValue(Const.ParamsNames.FEEDBACK_SESSION_NAME);
        Assumption.assertPostParamNotNull(Const.ParamsNames.FEEDBACK_SESSION_NAME, feedbackSessionName);
        String feedbackResponseId = getRequestParamValue(Const.ParamsNames.FEEDBACK_RESPONSE_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.FEEDBACK_RESPONSE_ID, feedbackResponseId);
        String feedbackResponseCommentId = getRequestParamValue(Const.ParamsNames.FEEDBACK_RESPONSE_COMMENT_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.FEEDBACK_RESPONSE_COMMENT_ID, feedbackResponseCommentId);


        //数据库查询基本信息
        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, account.googleId);
        FeedbackSessionAttributes session = logic.getFeedbackSession(feedbackSessionName, courseId);
        FeedbackResponseAttributes response = logic.getFeedbackResponse(feedbackResponseId);
        Assumption.assertNotNull(response);

        FeedbackResponseCommentAttributes frc =
                logic.getFeedbackResponseComment(Long.parseLong(feedbackResponseCommentId));
        Assumption.assertNotNull("FeedbackResponseComment should not be null", frc);
        verifyAccessibleForInstructorToFeedbackResponseComment(
                feedbackResponseCommentId, instructor, session, response);


        frc.praiseTo.remove(instructor.name);
        FeedbackResponseCommentAttributes updatedComment = null;
        try {
            updatedComment = logic.updateFeedbackResponseComment(frc);

            //TODO: move putDocument to task queue
            logic.putDocument(updatedComment);
        } catch (InvalidParametersException e) {
            setStatusForException(e);
        }




        statusToAdmin += "InstructorFeedbackResponseCommentDeleteAction:<br>"
                + "Praising feedback response comment: " + feedbackResponseCommentId + "<br>"
                + "in course/feedback session: " + courseId + "/" + feedbackSessionName + "<br>";

        InstructorFeedbackResponseCommentAjaxPageData data =
                new InstructorFeedbackResponseCommentAjaxPageData(account, sessionToken);

        String commentGiverName = logic.getInstructorForEmail(courseId, frc.giverEmail).name;
        String commentEditorName = instructor.name;

        // createdAt and lastEditedAt fields in updatedComment as well as sessionTimeZone
        // are required to generate timestamps in editedCommentDetails
        data.comment = updatedComment;
        data.sessionTimeZone = session.getTimeZone();

        data.editedCommentDetails = data.createEditedCommentDetails(commentGiverName, commentEditorName);
        data.isLiked=false;

        return createAjaxResult(data);
    }

}
