package teammates.ui.controller;

import java.io.IOException;

import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;

import org.apache.pdfbox.pdmodel.PDDocument;

public class InstructorCourseStudentPdfDownloadAction extends Action {

    @Override
    protected ActionResult execute() throws EntityDoesNotExistException {
        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.COURSE_ID, courseId);

        gateKeeper.verifyAccessible(
                logic.getInstructorForGoogleId(courseId, account.googleId),
                logic.getCourse(courseId));

        try {
            PDDocument document = logic.getCourseStudentListAsPdf(courseId, account.googleId);

            String fileName = courseId + "-student-list";

            statusToAdmin = "Students data for Course " + courseId + " was downloaded";
            return createPdfDownloadResult(fileName, document);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
