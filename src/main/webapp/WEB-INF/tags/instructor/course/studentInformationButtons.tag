<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag description="instructorCourseDetails - Student Information Helper" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/instructor/course" prefix="course" %>
<%@ tag import="teammates.common.util.Const" %>
<%@ attribute name="courseDetails" type="teammates.common.datatransfer.CourseDetailsBundle" required="true" %>
<%@ attribute name="courseRemindButton" type="teammates.ui.template.ElementTag" required="true" %>
<%@ attribute name="courseDeleteAllButton" type="teammates.ui.template.ElementTag" required="true" %>

<div class="form-group">
  <div class="align-center">
    <button type="submit" tabindex="1" value="Remind Students to Join" ${courseRemindButton.attributesToString}>
      <span class="glyphicon glyphicon-envelope"></span>
      Remind Students to Join
    </button>

    <button type="submit" value="Delete All Students" ${courseDeleteAllButton.attributesToString}>
      <span class="glyphicon glyphicon-trash"></span>
      Delete All Students
    </button>

    <form method="post" action="<%=Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_LIST_DOWNLOAD%>" style="display:inline;">
      <button id="button_download" type="submit" class="btn btn-primary" value="Delete All Students"
              name="<%=Const.ParamsNames.FEEDBACK_RESULTS_UPLOADDOWNLOADBUTTON%>">
        <span class="glyphicon glyphicon-download-alt"></span>
        Download Information as csv
      </button>
      <input type="hidden" name="<%=Const.ParamsNames.USER_ID%>" value="${data.account.googleId}">
      <input type="hidden" name="<%=Const.ParamsNames.COURSE_ID%>" value="${courseDetails.course.id}">
    </form>

    <form method="post" action="<%=Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_LIST_PDF_DOWNLOAD%>" style="display: inline">
      <button id="button_download_pdf" type="submit" class="btn btn-primary" value="Delete All Students"
              name="<%=Const.ParamsNames.FEEDBACK_RESULTS_UPLOADDOWNLOADBUTTON%>">
        <span class="glyphicon glyphicon-download-alt"></span>
        Download Information as PDF
      </button>
      <input type="hidden" name="<%=Const.ParamsNames.USER_ID%>" value="${data.account.googleId}">
      <input type="hidden" name="<%=Const.ParamsNames.COURSE_ID%>" value="${courseDetails.course.id}">
    </form>

    <form method="post" action="<%=Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_BACKUP%>" style="display: inline">
      <button id="button_backup_json" type="submit" class="btn btn-primary" value="Delete All Students"
              name="<%=Const.ParamsNames.FEEDBACK_RESULTS_UPLOADDOWNLOADBUTTON%>">
        <span class="glyphicon glyphicon-download-alt"></span>
        Backup course as JSON
      </button>
      <input type="hidden" name="<%=Const.ParamsNames.USER_ID%>" value="${data.account.googleId}">
      <input type="hidden" name="<%=Const.ParamsNames.COURSE_ID%>" value="${courseDetails.course.id}">
    </form>

    <form method="post" action="#" id="form_restore_course_dummy" style="display: inline">
      <button id="button_restore_json" class="btn btn-primary" value="Delete All Students"
              name="<%=Const.ParamsNames.FEEDBACK_RESULTS_UPLOADDOWNLOADBUTTON%>">
        <span class="glyphicon glyphicon-upload"></span>
        Restore course from JSON
      </button>
    </form>

    <form method="post" id="form_restore_course"
          action="<%=Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_RESTORE%>" style="display: inline">
      <button class="btn btn-primary" value="Delete All Students"
              name="<%=Const.ParamsNames.FEEDBACK_RESULTS_UPLOADDOWNLOADBUTTON%>" style="display: none">
        <span class="glyphicon glyphicon-upload"></span>
        Restore course from JSON
      </button>
      <input type="hidden" id="course_json_text" name="<%=Const.ParamsNames.STUDENTS_ENROLLMENT_RESTORE%>" value="">
      <input type="hidden" name="<%=Const.ParamsNames.USER_ID%>" value="${data.account.googleId}">
      <input type="hidden" name="<%=Const.ParamsNames.COURSE_ID%>" value="${courseDetails.course.id}">
    </form>

    <div>
      <span class="help-block">
        Non-English characters not displayed properly in the downloaded file?
        <span class="btn-link" data-toggle="modal" data-target="#studentTableWindow" id="btn-display-table">
          Click here.
        </span>
      </span>
    </div>

    <form id="csvToHtmlForm">
      <input type="hidden" name="<%=Const.ParamsNames.COURSE_ID%>" value="${courseDetails.course.id}">
      <input type="hidden" name="<%=Const.ParamsNames.USER_ID%>" value="${data.account.googleId}">
      <input type="hidden" name="<%=Const.ParamsNames.CSV_TO_HTML_TABLE_NEEDED%>" value="true">
    </form>
    <course:studentTableModal />
  </div>
</div>
