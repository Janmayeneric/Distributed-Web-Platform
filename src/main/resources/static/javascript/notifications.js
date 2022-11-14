"use strict";

let seen = false; // So its only sent once per user
let notification_timeout_obj;


// Helpful tool : https://codeseven.github.io/toastr/demo.html
// Sets up toastr.js for making notifications
function setUpNotificationsUsingToastr(){
    toastr.options = {
        "closeButton": true,
        "debug": false,
        "newestOnTop": true,
        "progressBar": false,
        "positionClass": "toast-bottom-right",
        "preventDuplicates": false,
        "onclick": null,
        "hideDuration": "1000",
        "timeOut": "0",
        "extendedTimeOut": "0",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut",
        "allowHtml": true,
      }
}

// Sets an interval timer to repeatedly get notification updates from the backend
function setNotificationTimeout(){
    notification_timeout_obj = setInterval(loadNotifications, 120000); // every two minutes
}

// Loads in the notifications
function loadNotifications(){
    if($('#notifications_area_id').children().length > 1){
        $('#notifications_area_id').children().not(':last').remove(); // clearing away old ones
    }
    $.ajax({
        url: rootPath + 'users/notifications',
        type: 'GET',
        success: function(notifications) {
            if(notifications.length > 0){ // if there are notifications
                $('#no_notifications_message_id').hide();
            }
            $.each(notifications, function(index, notification){
                addToNotificationsSection(notification);
            });
            setNotificationsAsSeen();
        },
        error: function(){
            Swal.fire(
                'Failed!',
                'Could not load notifications; server error',
                'error'
            );
            clearInterval(notification_timeout_obj); // doesn't repeatedly query if something wrong
        }, 
    });
}

// Set the notications as seen
function setNotificationsAsSeen(){
    if(!seen){
        $.ajax({
            url: rootPath + 'users/notifications',
            type: 'POST',
            success: function(){
            }, 
        });
    }
}

// Add notification to list
function addToNotificationsSection(notificationObject){
    let message;
    let time = convertEpochToTimeAndDate(Number(notificationObject.time));
    if(notificationObject.seen){ // If the notification has already been seen
        message = `<div class='dropdown-item text-break ` + 
            `seen-notification notification'><h5>${notificationObject.content}</h5> at ${time}</div>`;
    }else{
        message = `<div class='dropdown-item text-break notification'>` + 
            `<h5>${notificationObject.content}</h5> at ${time}</div>`;
            Command: toastr["info"](`${notificationObject.content} at ${time}`);
        seen = false;
    }
    $('#notifications_area_id').prepend(message);
}