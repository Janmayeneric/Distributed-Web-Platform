"use strict";

let following_timeout_obj;

// Sets an interval timer to repeatedly get a list of the users followers
function setFollowersTimeout(){
    following_timeout_obj = setInterval(loadFollowers, 120000); // every 2 minutes
}

// follows user 
function follow(element, location){
    let username = $(element).find("#username_id").text();
    $.ajax({
        url: rootPath + 'users/followers/add',
        data: {'username' : username},
        type: 'POST',
        success: function(data) {
            Swal.fire({
                    title: 'Now following',
                    icon: 'success',
                    timer:750,
                    showConfirmButton: false,
                    });
            AddFollowingToList(username);
            if(location == "search_result"){
                switchFollowingButtons(element, 'add');
            }
        },
        error: function(){
            Swal.fire(
                'Invalid!',
                'Could not follow',
                'error'
            );
        }, 
    });
}

// unfollows user
function unfollow(element, location){
    let username;
    username =  $(element).data('username');
    $.ajax({
        url: rootPath + 'users/followers/remove',
        data: {'username' : username},
        type: 'POST',
        success: function(data) {
            Swal.fire({
                    title: 'Unfollowed',
                    icon: 'success',
                    timer:750,
                    showConfirmButton: false,
                    });
            RemoveFromFollowingList(username);
            if(location == "search_result"){
                switchFollowingButtons(element, 'remove');
            }
        },
        error: function(){
            Swal.fire(
                'Invalid!',
                'Couldn\'t unfollow.',
                'error'
            );
        }, 
    });
}

// Adds user to list of following
function AddFollowingToList(username){
    // Shows the following header
    $('#no_following_message_id').hide();
    // Adds followed user to list 
    let list = $("#following_list_id");
    list.append('<li class="list-group-item following-entry text-break" id="following_entry_id"' +
    ` data-toggle="tooltip" title="Click to remove '${username}' from following list"` + `data-username=${username} onclick="RemoveFollowingProfilePage($(this))">` +
    '<div class="md-v-line">' + `</div><i class="fas fa-user-friends mr-4 pr-3"></i>${username}</li>`);
    list.tooltip({ selector: '[data-toggle=tooltip]' });
}

// Removes user from list of following
function RemoveFromFollowingList(username){
    let list = $("#following_list_id");
    $(list).children().each(function(){
        if($(this).data('username') === username){
            $(this).remove();
            if($("#following_list_id").children().length == 1){ // No followers left
                $('#no_following_message_id').show();
            }
            return false;
        }
    });
}

//Checks if a user is in your following list
function checkListForFollowing(username){
    let list = $('#following_list_id');
    let result = false;
    $(list).children().each(function(){
        if($(this).data('username') === username){
            result = true;
            return false; //Only way to exit this loop, returning true just skips to the next iteration
        }
    });
    return result;
}

// Loads the users following list in
function loadFollowing(){
    $.get(rootPath + 'users/following', function(following){ // Get data
        if(following.length > 0){
            $.each(following, function(index, element){ // For each community
                AddFollowingToList(element);
            });
        }
    });
}

// Removes person being followed when the user clicks on their name on the profile page
function RemoveFollowingProfilePage(element){
    $(element).tooltip('hide'); // Turns the tooltip off
    updateSearchResultsForFollower(element);
    unfollow($(element), 'profile_page');
}

//Switches which button is showing when you follow or unfollow a user.
function switchFollowingButtons(element, triggerButton){
    if(triggerButton == "add"){
        $(element).find('#search_remove_btn_id').show();
        $(element).find('#search_add_btn_id').hide();
    }else{
        $(element).find('#search_add_btn_id').show();
        $(element).find('#search_remove_btn_id').hide();
    }
}

// Updates the search results for following
function updateSearchResultsForFollower(element){
    let username = $(element).text();
    $('#search_username_container_id').children().each(function(){
        if($(this).data('username') == username){
            switchFollowingButtons(this, 'remove');
            return false; // Ends the loop
        }
    });
}

// Followers - get people following me and display
function loadFollowers(){
    if($('#followers_list_id').children().length > 1){
        $('#followers_list_id').children().not(':last').remove(); // clearing away old ones
    }
    $.get(rootPath + 'users/followers', function(following){ // Get data
        if(following.length > 0){
            $.each(following, function(index, element){ // For each community
                AddToFollowersToList(element);
            });
        }
    }).fail(function (){
        clearInterval(following_timeout_obj); // doesn't repeatedly query if something wrong
    });
}

// Adds new follower to the list of follower
function AddToFollowersToList(username){
    // Shows the following header
    $('#no_followers_message_id').hide();
    // Adds followed user to list 
    let list = $("#followers_list_id");
    list.prepend('<li class="list-group-item followers-entry text-break" id="followers_entry_id"' +
    ` data-toggle="tooltip" data-username=${username}">` +
    '<div class="md-v-line">' + `</div><i class="fas fa-user-friends mr-4 pr-3"></i>${username}</li>`);
    list.tooltip({ selector: '[data-toggle=tooltip]' });
}

// Undoes a show following posts operation
function undoShowFollowingPosts(btn){
    $('#followers_viewer_container_id').hide();
        if($('#post_container_id').children().length > 1){
            $('#no_posts_message_id').hide();
        }
        $('#post_container_id').show();
        $(btn).data('clicked', false);
        $(btn).addClass('btn-outline-info');
        $(btn).removeClass('btn-info');
}

// Shows all the posts made by people the user is following
function showFollowingPosts(btn){
    if($(btn).data('clicked')){ // Needed to revert back to posts
        undoShowFollowingPosts(btn);
    }else{
        if($('#community_viewer_container_id').is(":visible")){
            undoSpecificCommunitySearch($('#community_viewer_btn_id'));
        }
        $(btn).removeClass('btn-outline-info');
        $(btn).addClass('btn-info');
        $('#followers_viewer_container_id').empty();
        loadPostsAndComments(function(numberOfPosts){
            $('#followers_viewer_container_id').show();
            $('#post_container_id').hide();
            if(numberOfPosts == 0){ // If there are no posts, a message is displayed.
                $('#no_posts_message_id').show();
            }
            $(btn).data('clicked', true);
        }, "followers_viewer_container_id", 'posts/following/');
    }
}