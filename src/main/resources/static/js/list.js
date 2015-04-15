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
$(function() {

    var toggleShowPreviewIcon = function() {
        $(this).find('.preview-icon').toggleClass('invisible');
    };

    var renderPreviewModal = function(note) {
        $('#preview-modal .modal-title').html(note.title);
        $('#preview-modal .modal-body').html(marked(note.content));
        $('#preview-modal').modal('show');
    };

    $('#note-list tr').hover(toggleShowPreviewIcon);

    $('.preview-icon').click(function() {
        var id = $(this).data('id');
        $.ajax({
            type: 'GET',
            url: '/api/note/'+ id,
            success: renderPreviewModal
        });
    });
});