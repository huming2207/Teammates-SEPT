import {showModalConfirmation,} from '../common/bootboxWrapper';

import {BootstrapContextualColors,} from '../common/const';

import {
    attachEventToDeleteAllStudentLink,
    attachEventToDeleteStudentLink,
    attachEventToSendInviteLink,
    prepareInstructorPages,
    selectElementContents,
} from '../common/instructor';

import {toggleSort,} from '../common/sortBy';

import {setStatusMessage,} from '../common/statusMessage';

function submitFormAjax() {
    const formObject = $('#csvToHtmlForm');
    const formData = formObject.serialize();
    const content = $('#detailsTable');
    const ajaxStatus = $('#ajaxStatus');

    const retryButtonHtml = '<button class="btn btn-info" id="instructorCourseDetailsRetryButton"> retry</button>';
    $('#instructorCourseDetailsRetryButton').on('click', submitFormAjax);

    $.ajax({
        type: 'POST',
        url: `/page/instructorCourseDetailsPage?${formData}`,
        beforeSend() {
            content.html("<img src='/images/ajax-loader.gif'/>");
        },
        error() {
            ajaxStatus.html('Failed to load student table. Please try again.');
            content.html(retryButtonHtml);
        },
        success(data) {
            setTimeout(() => {
                if (data.isError) {
                    ajaxStatus.html(data.errorMessage);
                    content.html(retryButtonHtml);
                } else {
                    const table = data.studentListHtmlTableAsString;
                    content.html(`<small>${table}</small>`);
                    selectElementContents(content.get(0));
                }

                setStatusMessage(data.statusForAjax);
            }, 500);
        },
    });
}

function attachEventToCourseRestoreButton() {
    $('#form_restore_course_dummy').submit((event) => {
        // Prevent form auto submission
        event.preventDefault();

        // Create a dummy file input and ask the user for the JSON file
        const input = document.createElement('input');
        input.type = 'file';

        // Prepare the reader
        const reader = new FileReader();
        reader.onload = function () {
            const text = reader.result;
            console.log('Got JSON text: ');
            console.log(text);
            $('#course_json_text').val(text); // Set to the hidden input field
            $('#form_restore_course').submit();
        };

        // Read the JSON file
        input.onchange = function () {
            if (input.files && input.files[0]) {
                reader.readAsText(input.files[0]);
            }
        };

        input.click();
    });
}

function attachEventToRemindStudentsButton() {
    $('#button_remind').on('click', (event) => {
        const $clickedButton = $(event.currentTarget);
        const messageText = `${'Usually, there is no need to use this feature because TEAMMATES sends an automatic '
                          + 'invite to students at the opening time of each session. Send a join request to '
                          + 'all yet-to-join students in '}${$clickedButton.data('courseId')} anyway?`;
        const okCallback = function okCallback() {
            window.location = $clickedButton.attr('href');
        };

        showModalConfirmation('Confirm sending join requests', messageText, okCallback, null,
                null, null, BootstrapContextualColors.INFO);
    });
}

$(document).ready(() => {
    prepareInstructorPages();

    if ($('#button_sortstudentsection').length) {
        toggleSort($('#button_sortstudentsection'));
    } else {
        toggleSort($('#button_sortstudentteam'));
    }

    attachEventToRemindStudentsButton();
    attachEventToSendInviteLink();
    attachEventToDeleteStudentLink();
    attachEventToDeleteAllStudentLink();
    attachEventToCourseRestoreButton();

    $('#btn-select-element-contents').on('click', () => {
        selectElementContents($('#detailsTable').get(0));
    });

    $('#btn-display-table').on('click', () => {
        submitFormAjax();
    });
});
