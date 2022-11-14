"use strict";

let rootPath = "/"; // easy to change the root of all api calls if required

// Performs validation checks on sign-in page
$(document).ready(function(){
    $('#submit_button_id').click(function(){
        let dto = new Object();
        dto.username = $("#email_id").val();
        dto.password = $("#password_id").val();
        $.ajax(rootPath +'login', {
            type: 'POST',
            data: JSON.stringify(dto),
            contentType: 'application/json',
            success: function(response){
                Swal.fire({ 
                    title: "You passed!",
                    text: "Logging in",
                    icon: "success"
                }).then(() =>{
                    window.location.href = 'homepage.html';
                });
            },
            error: function(){
                Swal.fire(
                'Invalid credentials!',
                'Please try again.',
                'error'
                )
            },
        });
    });
});