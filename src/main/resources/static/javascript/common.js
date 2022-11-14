"use strict";

//Gets the current time and formats it into a stream.
function getTimeStamp(){
    //Getting the time
    let date = new Date();
    let hours = date.getHours();
    let minutes = date.getMinutes();
    if(minutes < 10){
        minutes = "0" + minutes;
    }
    let time;
    if(hours <= 12){
        time = date.getHours() + ":" + minutes + "am";
    } else{
        time = date.getHours() + ":" + minutes + "pm";
    }
    return time;
}


// Handles request to change password
function handlePasswordChange(){
    let password = extractNewPassword();
    if(password !== null){
        $('#user_details_modal_id').modal('toggle');
        sendPasswordRequestToServer(password);
        clearNewPassword();
    }
}

// Extracts password from
function extractNewPassword(){
    let passwordBox1 = $('#new_password_id');
    let passwordBox2 = $('#verify_new_password_id');
    if(passwordBox1.val() === ""){ 
        passwordBox1.css('border-color', 'red');
        Swal.fire(
            'Invalid Password!',
            'There is something wrong with your password choice. ',
            'error'
        )
        return null;
    }
    else{
        passwordBox1.css('border-color', '');
    }
    if(passwordBox2.val() === ""){
        passwordBox2.css('border-color', 'red');
        Swal.fire(
            'Invalid Password!',
            'There is something wrong with your password choice. ',
            'error'
        )
        return null;
    }
    else{
        passwordBox2.css('border-color', '');
    }
    if(passwordBox1.val() !== passwordBox2.val()){
        passwordBox2.css('border-color', 'red');
        Swal.fire(
            'Invalid Password!',
            'Your passwords do not match. ',
            'error'
        )
        return null;
    }
    else{
        passwordBox2.css('border-color', '');
    }
    return passwordBox1.val();
}

// Sends request to change password to server
function sendPasswordRequestToServer( password){
    $.ajax({
    url: rootPath + 'users/',
    type: 'PUT',
    data: "newPassword=" + password,
    success: function(data) {
            Swal.fire({
                title: 'Password changed',
                icon: 'success',
                showConfirmButton: false,
                timer:750
            });
    },
    error: function(){
            Swal.fire(
                'Invalid!',
                'Could not change password.',
                'error'
            );
        }, 
});
}

// Clears the password boxes
function clearNewPassword(){
    $('#new_password_id').val("");
    $('#verify_new_password_id').val("");
    $('#change_password_modal_id').modal('toggle');
}

// Handles request to change username
function handleUsernameChange(){
    let username = extractNewUsername();
    if(username !== null){
        $('#change_username_modal_id').modal('toggle');
        sendUsernameRequestToServer(username);
        clearNewUsername();
        handleRefresh();
    }
}

// Extracts username from modal
function extractNewUsername(){
    let oldUsernameBox = $('#user_details_username_id');
    let newUsernameBox = $('#new_username_id');
    if(newUsernameBox.val() === ""){ 
        newUsernameBox.css('border-color', 'red');
        Swal.fire(
            'Invalid Username!',
            'There is something wrong with your username choice. ',
            'error'
        )
        return null;
    }
    else{
        newUsernameBox.css('border-color', '');
    }
    if(newUsernameBox.val() === oldUsernameBox.val()){
        newUsernameBox.css('border-color', 'red');
        Swal.fire(
            'Invalid Username!',
            'New username matches current username. ',
            'error'
        )
        return null;
    }
    else{
        newUsernameBox.css('border-color', '');
    }
    return newUsernameBox.val();
}

// Sends request to change username to server
function sendUsernameRequestToServer(new_username){
    $.ajax({
    url: rootPath + 'users/rename',
    type: 'POST',
    data: "newName=" + new_username,
    success: function(data) {
        Swal.fire({
            title: 'Username changed',
            icon: 'success',
            showConfirmButton: false,
            timer:750
        });
        // Change username displayed
        $("#username_id").val(new_username);
        $('#profile_username_id').val(new_username);
        username = new_username;
        $.cookie('username', new_username);
    },
    error: function(jqXHR){
            if(jqXHR.status == 409){
                Swal.fire(
                    'Invalid!',
                    'Username already taken.',
                    'error'
                );
            }else{
                Swal.fire(
                    'Invalid!',
                    'Could not change username.',
                    'error'
                );
            }
        }, 
    });
}

// Clears the new username boxe
function clearNewUsername(){
    $('#new_username_id').val("");
    $('#change_username_modal_id').modal('toggle');
}

// Handles the refreshing of the page
function handleRefresh(){
    let path;
    let container;
    //deciding what to refresh
    let copy = $('#content_default_row_id').clone();
    if($('#post_container_id').is(":visible") || $('#search_results_id').is(":visible")){
        container = "post_container_id";
        path = 'posts/currentUser';
    }else if($('#community_viewer_container_id').is(":visible")){
        container = "community_viewer_container_id";
        path = 'posts/community/' + $('#community_viewer_id').val();
    }else if($('#followers_viewer_container_id').is(":visible")){
        container = "followers_viewer_container_id";
        path = 'posts/following/';
    }else{
        console.log("Error");
    } 
    $('#' + container).empty();
    $('#' + container).prepend(copy);
    loadPostsAndComments(function(numPostsAdded){
        if(numPostsAdded == 0 && (container == 'post_container_id' || container == 'search_results_id')){ //If there are no posts, a message is displayed.
            $('#no_posts_message_id').show();
        }else{
            $('#no_posts_message_id').hide();
        }
    }, container, path);
}


// Deletes a user's account
function deleteUserAccount(){
    // https://sweetalert2.github.io/ for the warning message
    Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!'
      }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: rootPath + 'users/delete',
                type: 'DELETE',
                success: function(data) {
                    deleteCookies();
                    location.reload();
                },
                error: function(){
                    Swal.fire(
                        'Invalid!',
                        'Could not change username.',
                        'error'
                    );
                }, 
            });
        }
    })
}

// Sends a random kind message to another user
function randomActOfKindness(){
    $.ajax({
        url: rootPath + 'users/randomactofkindness',
        type: 'PUT',
        success: function(data) {
            Command: toastr["success"](`Today's random act of kindness completed`);
        },
        error: function(){
                Swal.fire(
                    'Invalid!',
                    'Could not commit random act of kindness.',
                    'error'
                );
            },
        });
}