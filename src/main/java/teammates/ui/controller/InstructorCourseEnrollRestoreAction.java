package teammates.ui.controller;

import teammates.common.datatransfer.CourseEnrollmentResult;
import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.EnrollException;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.logic.core.StudentsLogic;
import teammates.ui.pagedata.InstructorCourseEnrollPageData;
import teammates.ui.pagedata.InstructorCourseEnrollResultPageData;

import java.util.Comparator;
import java.util.List;

public class InstructorCourseEnrollRestoreAction extends Action {

    @Override
    protected ActionResult execute() throws EntityDoesNotExistException {

        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.COURSE_ID, courseId);
        String backupJson = getRequestParamValue(Const.ParamsNames.STUDENTS_ENROLLMENT_RESTORE);
        Assumption.assertPostParamNotNull(Const.ParamsNames.STUDENTS_ENROLLMENT_INFO, backupJson);

        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, account.googleId);
        gateKeeper.verifyAccessible(instructor, logic.getCourse(courseId),
                Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT);

        try {
            List<StudentAttributes>[] students = restoreAndRender(backupJson, courseId);
            boolean hasSection = StudentsLogic.studentListHasSection(students);

            InstructorCourseEnrollResultPageData pageData = new InstructorCourseEnrollResultPageData(account, sessionToken,
                    courseId, students, hasSection, backupJson);

            statusToAdmin = "Student restored: " + backupJson + "<br>";

            return createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSE_ENROLL_RESULT, pageData);

        } catch (InvalidParametersException | EnrollException | EntityAlreadyExistsException e) {
            setStatusForException(e);
            statusToAdmin = "Student restore failed: " + e.getMessage() + "<br>" +
                    "Content: " + backupJson + "<br>";

            InstructorCourseEnrollPageData pageData =
                    new InstructorCourseEnrollPageData(account, sessionToken, courseId, backupJson);

            return createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSE_ENROLL, pageData);

        }
    }

    private List<StudentAttributes>[] restoreAndRender(String backupJson, String courseId)
            throws InvalidParametersException, EnrollException, EntityDoesNotExistException, EntityAlreadyExistsException {
        CourseEnrollmentResult enrollResult = logic.restoreStudents(backupJson, courseId);

        // Adjust submissions for all feedback responses within the course
        List<FeedbackSessionAttributes> feedbackSessions = logic.getFeedbackSessionsForCourse(courseId);
        for (FeedbackSessionAttributes session : feedbackSessions) {
            // Schedule adjustment of submissions for feedback session in course
            taskQueuer.scheduleFeedbackResponseAdjustmentForCourse(
                    courseId, session.getFeedbackSessionName(), enrollResult.enrollmentList);
        }

        enrollResult.studentList.sort(Comparator.comparing(obj -> obj.updateStatus.numericRepresentation));
        return StudentsLogic.separateStudents(enrollResult.studentList);
    }


}
