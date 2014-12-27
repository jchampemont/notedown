$(function() {

    var handleResize = function() {
        var windowHeight = window.innerHeight;
        var textareaHeight = windowHeight - 145;
        $('#editor').css('height', textareaHeight);
        $('#preview').css('height', textareaHeight);
    };

    var render = function() {
        $('#preview').html(marked($('#editor').val()));
    };

    var scroll = function() {
        $("#preview").scrollTop($("#editor").scrollTop());
    };

    var scrollToEndWhenEditAtEnd = function() {
        var length = $('#editor').val().length;
        var caret = $('#editor').caret();
        if(caret == length) {
            $("#preview").scrollTop($("#editor").prop("scrollHeight"));
        }
    };

    $(window).resize(handleResize);

    $('#editor').keyup(function() {
        render();
        scrollToEndWhenEditAtEnd();
    });

    $("#editor").scroll(scroll);

    handleResize();
    render();
});