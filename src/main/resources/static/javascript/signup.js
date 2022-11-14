"use strict";

let rootPath = "/";

// Adds key event handlers to page
function addKeyEventHandlers(){
    $('#username_id').keyup(function(){
        if($('#username_error_message_id').is(':visible')){
            $('#username_error_message_id').hide();
        }
    });
    
    $('#password_id').keyup(function(){
        if($('#password_error_message_id').is(':visible')){
            $('#password_error_message_id').hide();
        }
        if($('#confirmed_password_error_message_id').is(':visible')){
            $('#confirmed_password_error_message_id').hide();
        }
    });
    
    $('#confirmed_password_id').keyup(function(){
        if($('#confirmed_password_error_message_id').is(':visible')){
            $('#confirmed_password_error_message_id').hide();
        }
    });
}

// Sends sign up request
function sendSignUpRequest(dto){
    $.post(rootPath + "users", dto, function(response){
        //user logged in successfully
        Swal.fire({ 
            title: "Account created",
            text: "Please now login",
            icon: "success"
        }).then(() =>{
            window.location.href = 'signin.html';
        });
    }).fail(function(jqXHR){
        if(jqXHR.status == 400){
            Swal.fire(
                'Invalid!',
                'Your username or password is invalid; username can\t contain a space and password must be >= 6 characters.',
                'error'
                )
        }else{
            Swal.fire(
                'Invalid!',
                'Username taken',
                'error'
                )
        }
    });
}

// Validation code for sign-up page
$(document).ready(function(){
    $('#submit_button_id').click(function(){
        let dto = new Object();
        let regex = /^\w+$/;
        dto.username = $("#username_id").val();
        dto.password = $("#password_id").val();
        let confirmedPassword = $("#confirmed_password_id").val();
        if(dto.username == ""){
            $('#username_error_message_id').show();
            return;
        }else if(!regex.test(dto.username)){
            $('#username_error_message_id').show();
            return;
        }else if(dto.password == ""){
            $('#password_error_message_id').show();
            return;
        }else if(confirmedPassword == "" || confirmedPassword != dto.password){
            $('#confirmed_password_error_message_id').show();
            return;
        }
        sendSignUpRequest(dto);
    });
    addKeyEventHandlers();
});