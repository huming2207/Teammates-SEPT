package teammates.ui.controller;

import java.util.Comparator;
import java.util.List;

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
import teammates.common.util.Logger;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.StatusMessage;
import teammates.common.util.StatusMessageColor;
import teammates.logic.core.StudentsLogic;
import teammates.ui.pagedata.InstructorCourseEnrollPageData;
import teammates.ui.pagedata.InstructorCourseEnrollResultPageData;

/**
 * Action: saving the list of enrolled students for a course of an instructor.
 */
public class InstructorCourseEnrollSaveAction extends Action {

    private static final Logger log = Logger.getLogger();

    @Override
    public ActionResult execute() throws EntityDoesNotExistException {

        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertPostParamNotNull(Const.ParamsNames.COURSE_ID, courseId);
        String studentsInfo = getRequestParamValue(Const.ParamsNames.STUDENTS_ENROLLMENT_INFO);
        String sanitizedStudentsInfo = SanitizationHelper.sanitizeForHtml(studentsInfo);
        Assumption.assertPostParamNotNull(Const.ParamsNames.STUDENTS_ENROLLMENT_INFO, studentsInfo);

        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, account.googleId);
        gateKeeper.verifyAccessible(instructor, logic.getCourse(courseId),
                                    Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT);

        /* Process enrollment list and setup data for page result */
        try {
            List<StudentAttributes>[] students = enrollAndProcessResultForDisplay(studentsInfo, courseId);
            boolean hasSection = StudentsLogic.studentListHasSection(students);

            InstructorCourseEnrollResultPageData pageData = new InstructorCourseEnrollResultPageData(account, sessionToken,
                                                                    courseId, students, hasSection, studentsInfo);

            statusToAdmin = "Students Enrolled in Course <span class=\"bold\">["
                            + courseId + "]:</span><br>" + sanitizedStudentsInfo.replace("\n", "<br>");

            return createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSE_ENROLL_RESULT, pageData);

        } catch (EnrollException | InvalidParametersException e) {
            setStatusForException(e);

            statusToAdmin += "<br>Enrollment string entered by user:<br>" + sanitizedStudentsInfo.replace("\n", "<br>");

            InstructorCourseEnrollPageData pageData =
                    new InstructorCourseEnrollPageData(account, sessionToken, courseId, studentsInfo);

            return createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSE_ENROLL, pageData);
        } catch (EntityAlreadyExistsException e) {
            setStatusForException(e);

            statusToUser.add(
                    new StatusMessage("The enrollment failed, possibly because some students were re-enrolled before "
                                      + "the previous enrollment action was still being processed by TEAMMATES database "
                                      + "servers. Please try again after about 10 minutes. If the problem persists, "
                                      + "please contact TEAMMATES support", StatusMessageColor.DANGER));

            InstructorCourseEnrollPageData pageData =
                    new InstructorCourseEnrollPageData(account, sessionToken, courseId, studentsInfo);

            log.severe("Entity already exists exception occurred when updating student: " + e.getMessage());
            return createShowPageResult(Const.ViewURIs.INSTRUCTOR_COURSE_ENROLL, pageData);
        }
    }

    private List<StudentAttributes>[] enrollAndProcessResultForDisplay(String studentsInfo, String courseId)
            throws EnrollException, EntityDoesNotExistException, InvalidParametersException, EntityAlreadyExistsException {
        CourseEnrollmentResult enrollResult = logic.enrollStudents(studentsInfo, courseId);
        List<StudentAttributes> students = enrollResult.studentList;

        // Adjust submissions for all feedback responses within the course
        List<FeedbackSessionAttributes> feedbackSessions = logic.getFeedbackSessionsForCourse(courseId);
        for (FeedbackSessionAttributes session : feedbackSessions) {
            // Schedule adjustment of submissions for feedback session in course
            taskQueuer.scheduleFeedbackResponseAdjustmentForCourse(
                    courseId, session.getFeedbackSessionName(), enrollResult.enrollmentList);
        }

        students.sort(Comparator.comparing(obj -> obj.updateStatus.numericRepresentation));
        return StudentsLogic.separateStudents(students);
    }
}
