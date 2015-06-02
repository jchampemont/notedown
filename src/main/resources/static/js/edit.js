/*
 * Copyright (C) 2014, 2015 NoteDown
 *
 * This file is part of the NoteDown project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(function () {

    var handleResize = function () {
        var windowHeight = window.innerHeight;
        var textareaHeight = windowHeight - 145;
        $('#editor').css('height', textareaHeight);
        $('#preview').css('height', textareaHeight);
        render();
    };

    var render = function () {
        if ($('#toggle-preview-pane').is(":checked") && $('#toggle-preview-pane').is(":visible")) {
            $('#preview').html(marked($('#editor').val()));
        }
    };

    var scroll = function () {
        if ($('#toggle-preview-pane').is(":checked") && $('#toggle-preview-pane').is(":visible")) {
            $("#preview").scrollTop($("#editor").scrollTop());
        }
    };

    var scrollToEndWhenEditAtEnd = function () {
        if ($('#toggle-preview-pane').is(":checked")) {
            var length = $('#editor').val().length;
            var caret = $('#editor').caret();
            if (caret == length) {
                $("#preview").scrollTop($("#editor").prop("scrollHeight"));
            }
        }
    };

    var togglePreviewPane = function () {
        $('#preview').parent().toggleClass('hide');
        $('#editor').parent().toggleClass('col-sm-6 col-sm-12');
        if ($('#toggle-preview-pane').is(":checked")) {
            render();
            scroll();
        }
    };

    var saveInProgress = false;
    var save = function () {
        if (!saveInProgress && ($("#title").val().length > 0 || $("#editor").val().length > 0)) {
            saveInProgress = true;
            $("#saved-label").addClass("hide");
            $("#saving-label").removeClass("hide");
            var note = {
                id: $("#id").val(),
                title: $("#title").val(),
                content: $("#editor").val(),
                version: $("#version").val()
            };
            var headers = {};
            headers[$("meta[name='_csrf_header']").attr("content")] = $("meta[name='_csrf']").attr("content");
            $.ajax({
                type: "POST",
                url: "/api/note/",
                data: note,
                headers: headers,
                success: function (data) {
                    $("#id").val(data.id);
                    history.replaceState(null, null, data.id);
                    $("#version").val(data.version);
                    saveInProgress = false;
                    $("#saved-label #timeago").remove();
                    $("#saved-label").append("<span id=\"timeago\" title=\"" + new Date().toISOString() + "\"></span>");
                    $("#timeago").timeago();
                    $("#saved-label").removeClass("hide");
                    $("#saving-label").addClass("hide");
                    $(".error-label").addClass("hide");
                    shouldAutoSave = true;
                },
                error: function (err) {
                    $("#saving-label").addClass("hide");
                    shouldAutoSave = false;
                    saveInProgress = false;
                    if (err.status == 409) {
                        $("#error-version-label").removeClass("hide");
                    } else {
                        $("#error-saving-label").removeClass("hide");
                    }
                }
            });
        }
    };

    var lastAutoSave = undefined;
    var saveNeeded = false;
    var shouldAutoSave = true;
    var autoSave = function () {
        saveNeeded = true;
        if (shouldAutoSave) {
            clearTimeout(lastAutoSave);
            lastAutoSave = setTimeout(function () {
                save();
                saveNeeded = false;
            }, 2000);
        }
    };

    $(window).resize(handleResize);

    $('#editor').keyup(function () {
        autoSave();
        render();
        scrollToEndWhenEditAtEnd();
    });

    $("#editor").scroll(scroll);

    $("#toggle-preview-pane").change(togglePreviewPane);

    if ($("#timeago").prop("title").length > 0) {
        $("#timeago").timeago();
        $("#saved-label").removeClass("hide");
    }

    $(window).bind("beforeunload", function () {
        if (saveNeeded) {
            return notedown.javascript.exitWarning;
        }
    });

    $(document).keydown(function (event) {
            // If Control or Command key is pressed and the S key is pressed
            // run save function. 83 is the key code for S.
            if ((event.ctrlKey || event.metaKey) && event.which == 83) {
                save();
                event.preventDefault();
                return false;
            }
        }
    );

    handleResize();
    render();

    tabOverride.set($("#editor")[0]);
});