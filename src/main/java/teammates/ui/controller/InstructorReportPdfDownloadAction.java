package teammates.ui.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import java.io.IOException;

public class InstructorReportPdfDownloadAction extends Action
{
    @Override
    protected ActionResult execute() throws EntityDoesNotExistException
    {
        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.COURSE_ID, courseId);
        gateKeeper.verifyAccessible(
                logic.getInstructorForGoogleId(courseId, account.googleId),
                logic.getCourse(courseId));
        try {
            PDDocument document = logic.getCourseStudentFeedbackAsPdf(courseId, account.googleId);
            String fileName = "Instructor-Report";
            statusToAdmin = "Students Feedback was downloaded";
            return createPdfDownloadResult(fileName, document);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}