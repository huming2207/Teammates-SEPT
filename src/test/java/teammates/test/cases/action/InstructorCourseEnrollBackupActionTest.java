package teammates.test.cases.action;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import teammates.common.datatransfer.SectionDetailsBundle;
import teammates.common.datatransfer.TeamDetailsBundle;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.util.Const;
import teammates.ui.controller.*;

import java.util.List;

public class InstructorCourseEnrollBackupActionTest extends BaseActionTest {

    @Override
    protected String getActionUri() {
        return Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_BACKUP;
    }

    @Override
    protected Action getAction(String... params) {
        return gaeSimulation.getActionObject(getActionUri(), params);
    }

    @Override
    protected void testExecuteAndPostProcess() throws Exception {

        InstructorAttributes instructor1OfCourse1 = typicalBundle.instructors.get("instructor1OfCourse1");
        StudentAttributes student1InCourse1 = typicalBundle.students.get("student1InCourse1");

        // Mock a request
        ______TS("backup: test backup JSON");
        gaeSimulation.loginAsInstructor(instructor1OfCourse1.googleId);

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, instructor1OfCourse1.courseId,
        };

        InstructorCourseEnrollBackupAction action = (InstructorCourseEnrollBackupAction) getAction(submissionParams);
        JsonDownloadResult result = getJsonDownloadResult(action);

        // Deserialize JSON and do an assertion
        List<SectionDetailsBundle> bundles = new Gson().fromJson(result.getFileContent(),
                new TypeToken<List<SectionDetailsBundle>>(){}.getType());
        assertTrue(findStudentsInBundle(bundles, student1InCourse1));
    }

    @Override
    protected void testAccessControl() throws Exception {

    }

    private boolean findStudentsInBundle(List<SectionDetailsBundle> bundles, StudentAttributes desiredStudent) {
        for(SectionDetailsBundle bundle : bundles) {
            for(TeamDetailsBundle team : bundle.teams) {
                for(StudentAttributes student : team.students) {
                    if(student.key.equals(desiredStudent.key)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
