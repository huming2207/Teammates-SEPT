package teammates.logic.core;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;
import com.google.gson.Gson;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import teammates.common.datatransfer.*;
import teammates.common.datatransfer.attributes.*;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.TeammatesException;
import teammates.common.util.*;
import teammates.storage.api.CoursesDb;

/**
 * Handles operations related to courses.
 *
 * @see CourseAttributes
 * @see CoursesDb
 */
public final class CoursesLogic {

    private static final Logger log = Logger.getLogger();

    private static CoursesLogic instance = new CoursesLogic();

    /* Explanation: This class depends on CoursesDb class but no other *Db classes.
     * That is because reading/writing entities from/to the datastore is the
     * responsibility of the matching *Logic class.
     * However, this class can talk to other *Logic classes. That is because
     * the logic related to one entity type can involve the logic related to
     * other entity types.
     */

    private static final CoursesDb coursesDb = new CoursesDb();

    private static final AccountsLogic accountsLogic = AccountsLogic.inst();
    private static final FeedbackSessionsLogic feedbackSessionsLogic = FeedbackSessionsLogic.inst();
    private static final InstructorsLogic instructorsLogic = InstructorsLogic.inst();
    private static final StudentsLogic studentsLogic = StudentsLogic.inst();

    private CoursesLogic() {
        // prevent initialization
    }

    public static CoursesLogic inst() {
        return instance;
    }

    public void createCourse(String courseId, String courseName, String courseTimeZone)
            throws InvalidParametersException, EntityAlreadyExistsException {

        CourseAttributes courseToAdd = validateAndCreateCourseAttributes(courseId, courseName, courseTimeZone);
        coursesDb.createEntity(courseToAdd);
    }

    /**
     * Creates a Course object and an Instructor object for the Course.
     */
    public void createCourseAndInstructor(String instructorGoogleId, String courseId, String courseName,
                                          String courseTimeZone)
            throws InvalidParametersException, EntityAlreadyExistsException {

        AccountAttributes courseCreator = accountsLogic.getAccount(instructorGoogleId);
        Assumption.assertNotNull("Trying to create a course for a non-existent instructor :" + instructorGoogleId,
                                 courseCreator);
        Assumption.assertTrue("Trying to create a course for a person who doesn't have instructor privileges :"
                                  + instructorGoogleId,
                              courseCreator.isInstructor);

        createCourse(courseId, courseName, courseTimeZone);

        /* Create the initial instructor for the course */
        InstructorPrivileges privileges = new InstructorPrivileges(
                Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        InstructorAttributes instructor = InstructorAttributes
                .builder(instructorGoogleId, courseId, courseCreator.name, courseCreator.email)
                .withPrivileges(privileges)
                .build();

        try {
            instructorsLogic.createInstructor(instructor);
        } catch (EntityAlreadyExistsException | InvalidParametersException e) {
            //roll back the transaction
            coursesDb.deleteCourse(courseId);
            String errorMessage = "Unexpected exception while trying to create instructor for a new course "
                                  + System.lineSeparator() + instructor.toString() + System.lineSeparator()
                                  + TeammatesException.toStringWithStackTrace(e);
            Assumption.fail(errorMessage);
        }
    }

    /**
     * Gets the course with the specified ID.
     */
    public CourseAttributes getCourse(String courseId) {
        return coursesDb.getCourse(courseId);
    }

    /**
     * Returns true if the course with ID courseId is present.
     */
    public boolean isCoursePresent(String courseId) {
        return coursesDb.getCourse(courseId) != null;
    }

    /**
     * Returns true if the course with ID courseId is a sample course.
     */
    public boolean isSampleCourse(String courseId) {
        Assumption.assertNotNull("Course ID is null", courseId);
        return StringHelper.isMatching(courseId, FieldValidator.REGEX_SAMPLE_COURSE_ID);
    }

    /**
     * Used to trigger an {@link EntityDoesNotExistException} if the course is not present.
     */
    public void verifyCourseIsPresent(String courseId) throws EntityDoesNotExistException {
        if (!isCoursePresent(courseId)) {
            throw new EntityDoesNotExistException("Course does not exist: " + courseId);
        }
    }

    /**
     * Returns a list of {@link CourseDetailsBundle} for all
     * courses a given student is enrolled in.
     *
     * @param googleId The Google ID of the student
     */
    public List<CourseDetailsBundle> getCourseDetailsListForStudent(String googleId)
                throws EntityDoesNotExistException {

        List<CourseAttributes> courseList = getCoursesForStudentAccount(googleId);
        CourseAttributes.sortById(courseList);
        List<CourseDetailsBundle> courseDetailsList = new ArrayList<>();

        for (CourseAttributes c : courseList) {

            StudentAttributes s = studentsLogic.getStudentForCourseIdAndGoogleId(c.getId(), googleId);

            if (s == null) {
                //TODO Remove excessive logging after the reason why s can be null is found
                StringBuilder logMsgBuilder = new StringBuilder();
                String logMsg = "Student is null in CoursesLogic.getCourseDetailsListForStudent(String googleId)"
                        + "<br> Student Google ID: "
                        + googleId + "<br> Course: " + c.getId()
                        + "<br> All Courses Retrieved using the Google ID:";
                logMsgBuilder.append(logMsg);
                for (CourseAttributes course : courseList) {
                    logMsgBuilder.append("<br>").append(course.getId());
                }
                log.severe(logMsgBuilder.toString());

                //TODO Failing might not be the best course of action here.
                //Maybe throw a custom exception and tell user to wait due to eventual consistency?
                Assumption.fail("Student should not be null at this point.");
            }

            // Skip the course existence check since the course ID is obtained from a
            // valid CourseAttributes resulting from query
            List<FeedbackSessionAttributes> feedbackSessionList =
                    feedbackSessionsLogic.getFeedbackSessionsForUserInCourseSkipCheck(c.getId(), s.email);

            CourseDetailsBundle cdd = new CourseDetailsBundle(c);

            for (FeedbackSessionAttributes fs : feedbackSessionList) {
                cdd.feedbackSessions.add(new FeedbackSessionDetailsBundle(fs));
            }

            courseDetailsList.add(cdd);
        }
        return courseDetailsList;
    }

    /**
     * Returns a list of section names for the course with ID courseId.
     */
    public List<String> getSectionsNameForCourse(String courseId) throws EntityDoesNotExistException {
        return getSectionsNameForCourse(courseId, false);
    }

    /**
     * Returns a list of section names for the specified course.
     */
    public List<String> getSectionsNameForCourse(CourseAttributes course) throws EntityDoesNotExistException {
        Assumption.assertNotNull("Course is null", course);
        return getSectionsNameForCourse(course.getId(), true);
    }

    /**
     * Returns a list of section names for a course with or without a need to
     * check if the course is existent.
     *
     * @param courseId Course ID of the course
     * @param isCourseVerified Determine whether it is necessary to check if the course exists
     */
    private List<String> getSectionsNameForCourse(String courseId, boolean isCourseVerified)
            throws EntityDoesNotExistException {
        if (!isCourseVerified) {
            verifyCourseIsPresent(courseId);
        }
        List<StudentAttributes> studentDataList = studentsLogic.getStudentsForCourse(courseId);

        Set<String> sectionNameSet = new HashSet<>();
        for (StudentAttributes sd : studentDataList) {
            if (!sd.section.equals(Const.DEFAULT_SECTION)) {
                sectionNameSet.add(sd.section);
            }
        }

        List<String> sectionNameList = new ArrayList<>(sectionNameSet);
        sectionNameList.sort(null);

        return sectionNameList;
    }

    /**
     * Returns a list of {@link SectionDetailsBundle} for a
     * given course using course attributes and course details bundle.
     *
     * @param course {@link CourseAttributes}
     * @param cdd {@link CourseDetailsBundle}
     */
    public List<SectionDetailsBundle> getSectionsForCourse(CourseAttributes course, CourseDetailsBundle cdd) {
        Assumption.assertNotNull("Course is null", course);

        List<StudentAttributes> students = studentsLogic.getStudentsForCourse(course.getId());
        StudentAttributes.sortBySectionName(students);

        List<SectionDetailsBundle> sections = new ArrayList<>();

        SectionDetailsBundle section = null;
        int teamIndexWithinSection = 0;

        for (int i = 0; i < students.size(); i++) {

            StudentAttributes s = students.get(i);
            cdd.stats.studentsTotal++;
            if (!s.isRegistered()) {
                cdd.stats.unregisteredTotal++;
            }

            if (section == null) { // First student of first section
                section = new SectionDetailsBundle();
                section.name = s.section;
                section.teams.add(new TeamDetailsBundle());
                cdd.stats.teamsTotal++;
                section.teams.get(teamIndexWithinSection).name = s.team;
                section.teams.get(teamIndexWithinSection).students.add(s);
            } else if (s.section.equals(section.name)) {
                if (s.team.equals(section.teams.get(teamIndexWithinSection).name)) {
                    section.teams.get(teamIndexWithinSection).students.add(s);
                } else {
                    teamIndexWithinSection++;
                    section.teams.add(new TeamDetailsBundle());
                    cdd.stats.teamsTotal++;
                    section.teams.get(teamIndexWithinSection).name = s.team;
                    section.teams.get(teamIndexWithinSection).students.add(s);
                }
            } else { // first student of subsequent section
                sections.add(section);
                if (!section.name.equals(Const.DEFAULT_SECTION)) {
                    cdd.stats.sectionsTotal++;
                }
                teamIndexWithinSection = 0;
                section = new SectionDetailsBundle();
                section.name = s.section;
                section.teams.add(new TeamDetailsBundle());
                cdd.stats.teamsTotal++;
                section.teams.get(teamIndexWithinSection).name = s.team;
                section.teams.get(teamIndexWithinSection).students.add(s);
            }

            boolean isLastStudent = i == students.size() - 1;
            if (isLastStudent) {
                sections.add(section);
                if (!section.name.equals(Const.DEFAULT_SECTION)) {
                    cdd.stats.sectionsTotal++;
                }
            }
        }

        return sections;
    }

    /**
     * Returns a list of {@link SectionDetailsBundle} for a given course using courseId.
     */
    public List<SectionDetailsBundle> getSectionsForCourseWithoutStats(String courseId)
            throws EntityDoesNotExistException {

        verifyCourseIsPresent(courseId);

        List<StudentAttributes> students = studentsLogic.getStudentsForCourse(courseId);
        StudentAttributes.sortBySectionName(students);

        List<SectionDetailsBundle> sections = new ArrayList<>();

        SectionDetailsBundle section = null;
        int teamIndexWithinSection = 0;

        for (int i = 0; i < students.size(); i++) {
            StudentAttributes s = students.get(i);

            if (section == null) { // First student of first section
                section = new SectionDetailsBundle();
                section.name = s.section;
                section.teams.add(new TeamDetailsBundle());
                section.teams.get(teamIndexWithinSection).name = s.team;
                section.teams.get(teamIndexWithinSection).students.add(s);
            } else if (s.section.equals(section.name)) {
                if (s.team.equals(section.teams.get(teamIndexWithinSection).name)) {
                    section.teams.get(teamIndexWithinSection).students.add(s);
                } else {
                    teamIndexWithinSection++;
                    section.teams.add(new TeamDetailsBundle());
                    section.teams.get(teamIndexWithinSection).name = s.team;
                    section.teams.get(teamIndexWithinSection).students.add(s);
                }
            } else { // first student of subsequent section
                sections.add(section);
                teamIndexWithinSection = 0;
                section = new SectionDetailsBundle();
                section.name = s.section;
                section.teams.add(new TeamDetailsBundle());
                section.teams.get(teamIndexWithinSection).name = s.team;
                section.teams.get(teamIndexWithinSection).students.add(s);
            }

            boolean isLastStudent = i == students.size() - 1;
            if (isLastStudent) {
                sections.add(section);
            }
        }

        return sections;
    }

    /**
     * Returns Teams for a particular courseId.<br>
     * <b>Note:</b><br>
     * This method does not returns any Loner information presently,<br>
     * Loner information must be returned as we decide to support loners<br>in future.
     *
     */
    public List<TeamDetailsBundle> getTeamsForCourse(String courseId) throws EntityDoesNotExistException {

        if (getCourse(courseId) == null) {
            throw new EntityDoesNotExistException("The course " + courseId + " does not exist");
        }

        List<StudentAttributes> students = studentsLogic.getStudentsForCourse(courseId);
        StudentAttributes.sortByTeamName(students);

        List<TeamDetailsBundle> teams = new ArrayList<>();

        TeamDetailsBundle team = null;

        for (int i = 0; i < students.size(); i++) {

            StudentAttributes s = students.get(i);

            // first student of first team
            if (team == null) {
                team = new TeamDetailsBundle();
                team.name = s.team;
                team.students.add(s);
            } else if (s.team.equals(team.name)) { // student in the same team as the previous student
                team.students.add(s);
            } else { // first student of subsequent teams (not the first team)
                teams.add(team);
                team = new TeamDetailsBundle();
                team.name = s.team;
                team.students.add(s);
            }

            // if last iteration
            if (i == students.size() - 1) {
                teams.add(team);
            }
        }

        return teams;
    }

    /**
     * Returns the {@link CourseDetailsBundle} course details for a course using {@link CourseAttributes}.
     */
    public CourseDetailsBundle getCourseSummary(CourseAttributes cd) {
        Assumption.assertNotNull("Supplied parameter was null", cd);

        CourseDetailsBundle cdd = new CourseDetailsBundle(cd);
        cdd.sections = (ArrayList<SectionDetailsBundle>) getSectionsForCourse(cd, cdd);

        return cdd;
    }

    // TODO: reduce calls to this function, use above function instead.
    /**
     * Returns the {@link CourseDetailsBundle} course details for a course using courseId.
     */
    public CourseDetailsBundle getCourseSummary(String courseId) throws EntityDoesNotExistException {
        CourseAttributes cd = coursesDb.getCourse(courseId);

        if (cd == null) {
            throw new EntityDoesNotExistException("The course does not exist: " + courseId);
        }

        return getCourseSummary(cd);
    }

    /**
     * Returns the {@link CourseSummaryBundle course summary}, including its
     * feedback sessions using the given {@link InstructorAttributes}.
     */
    public CourseSummaryBundle getCourseSummaryWithFeedbackSessionsForInstructor(
            InstructorAttributes instructor) throws EntityDoesNotExistException {
        CourseSummaryBundle courseSummary = getCourseSummaryWithoutStats(instructor.courseId);
        courseSummary.feedbackSessions.addAll(feedbackSessionsLogic.getFeedbackSessionListForInstructor(instructor));
        return courseSummary;
    }

    /**
     * Returns the {@link CourseSummaryBundle course summary} using the {@link CourseAttributes}.
     */
    public CourseSummaryBundle getCourseSummaryWithoutStats(CourseAttributes course) {
        Assumption.assertNotNull("Supplied parameter was null", course);

        return new CourseSummaryBundle(course);
    }

    /**
     * Returns the {@link CourseSummaryBundle course summary} using the courseId.
     */
    public CourseSummaryBundle getCourseSummaryWithoutStats(String courseId) throws EntityDoesNotExistException {
        CourseAttributes cd = coursesDb.getCourse(courseId);

        if (cd == null) {
            throw new EntityDoesNotExistException("The course does not exist: " + courseId);
        }

        return getCourseSummaryWithoutStats(cd);
    }

    /**
     * Returns a list of {@link CourseAttributes} for all courses a given student is enrolled in.
     *
     * @param googleId The Google ID of the student
     */
    public List<CourseAttributes> getCoursesForStudentAccount(String googleId) throws EntityDoesNotExistException {
        List<StudentAttributes> studentDataList = studentsLogic.getStudentsForGoogleId(googleId);

        if (studentDataList.isEmpty()) {
            throw new EntityDoesNotExistException("Student with Google ID " + googleId + " does not exist");
        }

        List<String> courseIds = new ArrayList<>();
        for (StudentAttributes s : studentDataList) {
            courseIds.add(s.course);
        }
        return coursesDb.getCourses(courseIds);
    }

    /**
     * Returns a list of {@link CourseAttributes} for all courses a given instructor belongs to.
     *
     * @param googleId The Google ID of the instructor
     */
    public List<CourseAttributes> getCoursesForInstructor(String googleId) {
        return getCoursesForInstructor(googleId, false);
    }

    /**
     * Returns a list of {@link CourseAttributes} for courses a given instructor belongs to.
     *
     * @param googleId The Google ID of the instructor
     * @param omitArchived if {@code true}, omits all the archived courses from the return
     */
    public List<CourseAttributes> getCoursesForInstructor(String googleId, boolean omitArchived) {
        List<InstructorAttributes> instructorList = instructorsLogic.getInstructorsForGoogleId(googleId, omitArchived);
        return getCoursesForInstructor(instructorList);
    }

    /**
     * Returns a list of {@link CourseAttributes} for all courses for a given list of instructors.
     */
    public List<CourseAttributes> getCoursesForInstructor(List<InstructorAttributes> instructorList) {
        Assumption.assertNotNull("Supplied parameter was null", instructorList);
        List<String> courseIdList = new ArrayList<>();

        for (InstructorAttributes instructor : instructorList) {
            courseIdList.add(instructor.courseId);
        }

        List<CourseAttributes> courseList = coursesDb.getCourses(courseIdList);

        // Check that all courseIds queried returned a course.
        if (courseIdList.size() > courseList.size()) {
            for (CourseAttributes ca : courseList) {
                courseIdList.remove(ca.getId());
            }
            log.severe("Course(s) was deleted but the instructor still exists: " + System.lineSeparator()
                    + courseIdList.toString());
        }

        return courseList;
    }

    /**
     * Returns course summaries for instructor.<br>
     * Omits archived courses if omitArchived == true<br>
     *
     * @param googleId The Google ID of the instructor
     * @return Map with courseId as key, and CourseDetailsBundle as value.
     *         Does not include details within the course, such as feedback sessions.
     */
    public Map<String, CourseDetailsBundle> getCourseSummariesForInstructor(String googleId, boolean omitArchived)
            throws EntityDoesNotExistException {

        instructorsLogic.verifyInstructorExists(googleId);

        List<InstructorAttributes> instructorAttributesList = instructorsLogic.getInstructorsForGoogleId(googleId,
                                                                                                         omitArchived);

        return getCourseSummariesForInstructor(instructorAttributesList);
    }

    /**
     * Returns course summaries for instructors.<br>
     *
     * @return Map with courseId as key, and CourseDetailsBundle as value.
     *         Does not include details within the course, such as feedback sessions.
     */
    public Map<String, CourseDetailsBundle> getCourseSummariesForInstructor(
            List<InstructorAttributes> instructorAttributesList) {

        HashMap<String, CourseDetailsBundle> courseSummaryList = new HashMap<>();
        List<String> courseIdList = new ArrayList<>();

        for (InstructorAttributes instructor : instructorAttributesList) {
            courseIdList.add(instructor.courseId);
        }

        List<CourseAttributes> courseList = coursesDb.getCourses(courseIdList);

        // Check that all courseIds queried returned a course.
        if (courseIdList.size() > courseList.size()) {
            for (CourseAttributes ca : courseList) {
                courseIdList.remove(ca.getId());
            }
            log.severe("Course(s) was deleted but the instructor still exists: " + System.lineSeparator()
                        + courseIdList.toString());
        }

        for (CourseAttributes ca : courseList) {
            courseSummaryList.put(ca.getId(), getCourseSummary(ca));
        }

        return courseSummaryList;
    }

    /**
     * Returns a Map (CourseId, {@link CourseSummaryBundle}
     * for all courses mapped to a given instructor.
     *
     * @param omitArchived if {@code true}, omits all the archived courses from the return
     */
    public Map<String, CourseSummaryBundle> getCoursesSummaryWithoutStatsForInstructor(
            String instructorId, boolean omitArchived) {

        List<InstructorAttributes> instructorList = instructorsLogic.getInstructorsForGoogleId(instructorId,
                                                                                               omitArchived);
        return getCourseSummaryWithoutStatsForInstructor(instructorList);
    }

    /**
     * Updates the course details.
     * @param courseId Id of the course to update
     * @param courseName new name of the course
     * @param courseTimeZone new time zone of the course
     */
    public void updateCourse(String courseId, String courseName, String courseTimeZone)
            throws InvalidParametersException, EntityDoesNotExistException {
        CourseAttributes newCourse = validateAndCreateCourseAttributes(courseId, courseName, courseTimeZone);
        CourseAttributes oldCourse = coursesDb.getCourse(newCourse.getId());

        if (oldCourse == null) {
            throw new EntityDoesNotExistException("Trying to update a course that does not exist.");
        }

        coursesDb.updateCourse(newCourse);
        if (!newCourse.getTimeZone().equals(oldCourse.getTimeZone())) {
            feedbackSessionsLogic.updateFeedbackSessionsTimeZoneForCourse(newCourse.getId(), newCourse.getTimeZone());
        }
    }

    /**
     * Delete a course from its given corresponding ID.
     * This will also cascade the data in other databases which are related to this course.
     */
    public void deleteCourseCascade(String courseId) {
        studentsLogic.deleteStudentsForCourse(courseId);
        instructorsLogic.deleteInstructorsForCourse(courseId);
        feedbackSessionsLogic.deleteFeedbackSessionsForCourseCascade(courseId);
        coursesDb.deleteCourse(courseId);
    }

    private Map<String, CourseSummaryBundle> getCourseSummaryWithoutStatsForInstructor(
            List<InstructorAttributes> instructorAttributesList) {

        HashMap<String, CourseSummaryBundle> courseSummaryList = new HashMap<>();

        List<String> courseIdList = new ArrayList<>();

        for (InstructorAttributes ia : instructorAttributesList) {
            courseIdList.add(ia.courseId);
        }
        List<CourseAttributes> courseList = coursesDb.getCourses(courseIdList);

        // Check that all courseIds queried returned a course.
        if (courseIdList.size() > courseList.size()) {
            for (CourseAttributes ca : courseList) {
                courseIdList.remove(ca.getId());
            }
            log.severe("Course(s) was deleted but the instructor still exists: " + System.lineSeparator()
                    + courseIdList.toString());
        }

        for (CourseAttributes ca : courseList) {
            courseSummaryList.put(ca.getId(), getCourseSummaryWithoutStats(ca));
        }

        return courseSummaryList;
    }

    /**
     * Returns a CSV for the details (name, email, status) of all students belonging to a given course.
     */
    public String getCourseStudentListAsCsv(String courseId, String googleId) throws EntityDoesNotExistException {

        Map<String, CourseDetailsBundle> courses = getCourseSummariesForInstructor(googleId, false);
        CourseDetailsBundle course = courses.get(courseId);
        boolean hasSection = hasIndicatedSections(courseId);

        StringBuilder export = new StringBuilder(100);
        String courseInfo = "Course ID," + SanitizationHelper.sanitizeForCsv(courseId) + System.lineSeparator()
                      + "Course Name," + SanitizationHelper.sanitizeForCsv(course.course.getName())
                      + System.lineSeparator() + System.lineSeparator() + System.lineSeparator();
        export.append(courseInfo);

        String header = (hasSection ? "Section," : "") + "Team,Full Name,Last Name,Status,Email" + System.lineSeparator();
        export.append(header);

        for (SectionDetailsBundle section : course.sections) {
            for (TeamDetailsBundle team : section.teams) {
                for (StudentAttributes student : team.students) {
                    String studentStatus = null;
                    if (student.googleId == null || student.googleId.isEmpty()) {
                        studentStatus = Const.STUDENT_COURSE_STATUS_YET_TO_JOIN;
                    } else {
                        studentStatus = Const.STUDENT_COURSE_STATUS_JOINED;
                    }

                    if (hasSection) {
                        export.append(SanitizationHelper.sanitizeForCsv(section.name)).append(',');
                    }

                    export.append(SanitizationHelper.sanitizeForCsv(team.name))
                            .append(',')
                            .append(SanitizationHelper.sanitizeForCsv(StringHelper.removeExtraSpace(student.name)))
                            .append(',')
                            .append(SanitizationHelper.sanitizeForCsv(StringHelper.removeExtraSpace(student.lastName)))
                            .append(',')
                            .append(SanitizationHelper.sanitizeForCsv(studentStatus))
                            .append(',').append(SanitizationHelper.sanitizeForCsv(student.email))
                            .append(System.lineSeparator());
                }
            }
        }
        return export.toString();
    }

    public String getCourseStudentBackupAsJson(String courseId, String googleId) throws EntityDoesNotExistException {

        Gson gson = new Gson();
        List<SectionDetailsBundle> sectionBundles = getCourseSummariesForInstructor(googleId, false).get(courseId).sections;

        return gson.toJson(sectionBundles);
    }

    public PDDocument getCourseStudentListAsPdf(String courseId, String googleId)
            throws IOException, EntityDoesNotExistException {
        PDDocument pdDocument = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);

        List<List> dataList = new ArrayList<>();
        Map<String, CourseDetailsBundle> courses = getCourseSummariesForInstructor(googleId, false);
        CourseDetailsBundle course = courses.get(courseId);
        boolean hasSection = hasIndicatedSections(courseId);

        // Generate a title
        PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
        contentStream.newLineAtOffset(50, 200);
        contentStream.showText("Student list of " + course.course.getName());
        contentStream.endText();
        contentStream.close();

        // Create a table header
        dataList.add(hasSection
                ? new ArrayList<>(Arrays.asList("Section", "Team", "Full Name", "Last Name", "Status", "Email"))
                : new ArrayList<>(Arrays.asList("Team", "Full Name", "Last Name", "Status", "Email")));

        // Add the elements (students)
        for (SectionDetailsBundle section : course.sections) {
            for (TeamDetailsBundle team : section.teams) {
                for (StudentAttributes student : team.students) {
                    List<String> rowElements = new ArrayList<>();
                    String studentStatus = null;
                    if (student.googleId == null || student.googleId.isEmpty()) {
                        studentStatus = Const.STUDENT_COURSE_STATUS_YET_TO_JOIN;
                    } else {
                        studentStatus = Const.STUDENT_COURSE_STATUS_JOINED;
                    }

                    if (hasSection) {
                        rowElements.add(section.name);
                    }

                    rowElements.addAll(
                            Arrays.asList(team.name, student.name, student.lastName, studentStatus, student.email));

                    dataList.add(rowElements);
                }
            }
        }


        // These positioning code comes from: https://github.com/dhorions/boxable/wiki
        //Dummy Table
        float margin = 50;
        // starting y position is whole page height subtracted by top and bottom margin
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        // we want table across whole page width (subtracted by left and right margin ofcourse)
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

        boolean drawContent = true;
        float bottomMargin = 70;
        // y position is your coordinate of top left corner of the table
        float yPosition = 550;

        // Use the existing CSV to generate PDF for now
        BaseTable baseTable = new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin,
                pdDocument, page, true, drawContent);
        DataTable dataTable = new DataTable(baseTable, page);

        // Add the data list into the table
        dataTable.addListToTable(dataList, DataTable.HASHEADER);
        baseTable.draw();

        pdDocument.addPage(page);
        return pdDocument;
    }

    public boolean hasIndicatedSections(String courseId) throws EntityDoesNotExistException {
        verifyCourseIsPresent(courseId);

        List<StudentAttributes> studentList = studentsLogic.getStudentsForCourse(courseId);
        for (StudentAttributes student : studentList) {
            if (!student.section.equals(Const.DEFAULT_SECTION)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of courseIds for all archived courses for all instructors.
     */
    public List<String> getArchivedCourseIds(List<CourseAttributes> allCourses,
                                             Map<String, InstructorAttributes> instructorsForCourses) {
        List<String> archivedCourseIds = new ArrayList<>();
        for (CourseAttributes course : allCourses) {
            InstructorAttributes instructor = instructorsForCourses.get(course.getId());
            if (instructor.isArchived) {
                archivedCourseIds.add(course.getId());
            }
        }
        return archivedCourseIds;
    }

    /**
     * Checks that {@code courseTimeZone} is valid and then returns a {@link CourseAttributes}.
     * Field validation is usually done in {@link CoursesDb} by calling {@link CourseAttributes#getInvalidityInfo()}.
     * However, a {@link CourseAttributes} cannot be created with an invalid time zone string.
     * Hence, validation of this field is carried out here.
     *
     * @throws InvalidParametersException containing error messages for all fields if {@code courseTimeZone} is invalid
     */
    private CourseAttributes validateAndCreateCourseAttributes(
            String courseId, String courseName, String courseTimeZone) throws InvalidParametersException {

        // Imitate `CourseAttributes.getInvalidityInfo`
        FieldValidator validator = new FieldValidator();
        String timeZoneErrorMessage = validator.getInvalidityInfoForTimeZone(courseTimeZone);
        if (!timeZoneErrorMessage.isEmpty()) {
            // Leave validation of other fields to `CourseAttributes.getInvalidityInfo`
            CourseAttributes dummyCourse = CourseAttributes
                    .builder(courseId, courseName, Const.DEFAULT_TIME_ZONE)
                    .build();
            List<String> errors = dummyCourse.getInvalidityInfo();
            errors.add(timeZoneErrorMessage);
            // Imitate exception throwing in `CoursesDb`
            throw new InvalidParametersException(errors);
        }

        // If time zone field is valid, leave validation  of other fields to `CoursesDb` like usual
        return CourseAttributes
                .builder(courseId, courseName, ZoneId.of(courseTimeZone))
                .build();
    }
}
