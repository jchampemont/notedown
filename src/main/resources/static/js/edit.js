/*
 * Copyright (C) 2014 NoteDown
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
$(function() {

    var handleResize = function() {
        var windowHeight = window.innerHeight;
        var textareaHeight = windowHeight - 145;
        $('#editor').css('height', textareaHeight);
        $('#preview').css('height', textareaHeight);
        render();
    };

    var render = function() {
        if($('#toggle-preview-pane').is(":checked") && $('#toggle-preview-pane').is(":visible")) {
            $('#preview').html(marked($('#editor').val()));
        }
    };

    var scroll = function() {
        if($('#toggle-preview-pane').is(":checked") && $('#toggle-preview-pane').is(":visible")) {
            $("#preview").scrollTop($("#editor").scrollTop());
        }
    };

    var scrollToEndWhenEditAtEnd = function() {
        if($('#toggle-preview-pane').is(":checked")) {
            var length = $('#editor').val().length;
            var caret = $('#editor').caret();
            if (caret == length) {
                $("#preview").scrollTop($("#editor").prop("scrollHeight"));
            }
        }
    };

    var togglePreviewPane = function() {
        $('#preview').parent().toggleClass('hide');
        $('#editor').parent().toggleClass('col-sm-6 col-sm-12');
        if($('#toggle-preview-pane').is(":checked")) {
            render();
            scroll();
        }
    };

    var saveInProgress = false;
    var save = function() {
        if(!saveInProgress && ($("#title").val().length > 0 || $("#editor").val().length > 0)) {
            saveInProgress = true;
            $("#submit-btn").button("loading");
            var note = {
                id: $("#id").val(),
                title: $("#title").val(),
                content: $("#editor").val()
            };
            var headers = {};
            headers[$("meta[name='_csrf_header']").attr("content")] = $("meta[name='_csrf']").attr("content");
            $.ajax({
                type: "POST",
                url: "/api/note/",
                data: note,
                headers: headers,
                success: function(data) {
                    $("#id").val(data.id);
                    saveInProgress = false;
                    $("#submit-btn").button("reset");
                }
            });
        }
    };

    var lastAutoSave = undefined;
    var autoSave = function() {
        if($("#toggle-autosave").is(':checked')) {
            clearTimeout(lastAutoSave);
            lastAutoSave = setTimeout(function() {
                save();
            }, 2000);
        }
    };

    var toggleAutoSave = function() {
        if($("#toggle-autosave").is(':checked')) {
            save();
        }
    };

    $(window).resize(handleResize);

    $('#editor').keyup(function() {
        autoSave();
        render();
        scrollToEndWhenEditAtEnd();
    });

    $("#editor").scroll(scroll);

    $('#toggle-preview-pane').change(togglePreviewPane);
    $("#toggle-autosave").change(toggleAutoSave)

    handleResize();
    render();
});