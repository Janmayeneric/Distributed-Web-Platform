"use strict";

// Loads in the HTML modal objects and then sets the page event handlers
async function loadModals(){
    $('#post_modal_container').load('modals/post_modal.html', () => {
        $('#create_communities_modal_container').load('modals/create_communities_modal.html', () => {
            $('#delete_communities_modal_container').load('modals/delete_communities_modal.html', () => {
                $('#comments_modal_container').load('modals/comments_modal.html', () => {
                    $('#change_username_modal_container').load('modals/change_username_modal.html', () => {
                        $('#change_password_modal_container').load('modals/change_password_modal.html', () => {
                            $('#post_container_id').load('components/post_component.html', () => {
                                $('#search_results_container_id').load('components/community_search_component.html', () => {
                                    $('#search_username_container_id').load('components/user_search_component.html', () => {
                                        $('#profile_modal_container').load('modals/profile_modal.html', () =>{
                                            $('#comments_component_container_id').load('components/comment_component.html', ()=>{
                                                setupPage();
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    });
}

// Checks if the user is logged in and therefore has the required cookie
function checkForCookie(){
    //Takes username from cookie given at login/registration
    if((username = $.cookie('username')) != undefined){
        return true;
    }
    return false;
}

// Deletes all the cookies added to the user's browser when visiting this domain when logging out
function deleteCookies(){
    let cookies = $.cookie();
    for(let cookie in cookies) {
        $.removeCookie(cookie);
    }
}

// Adds the event handlers to the page and loads in the available posts/comments
function setupPage(){
    let userLoggedIn = checkForCookie();
    if(userLoggedIn){
        loadPostsAndComments(function(numPostsAdded){
            if(numPostsAdded == 0){ //If there are no posts, a message is displayed.
                $('#no_posts_message_id').show();
            }
        }, "post_container_id", 'posts/currentUser');
        changeStateOfUIComponents();
        loadInData();
        addEventHandlers();
    } else{ //if user not logged in
        $('.alert').show()// Displays the cookie alert
        $("#unknown_user_options_id").show();
        $('#unknown_user_img_id').show();
        $("#searchbar_id").prop('disabled', true);
        $('#post_button_id').prop('disabled', true); // Disable POST button
        $('#comm_delete_button').prop('disabled', true); // Disable Delete Community button
    }
}

// Changes the state of UI components on the page that were previously hidden
function changeStateOfUIComponents(){
    // Show components
    $("#unknown_user_img_id").hide();
    $('#comm_delete_button').show();
    $('#post_button_id').show();
    $('#comm_create_btn_id').show();
    $('#random_act_of_kindness_btn_id').show();
    $('.refresh-button-container').css('display', 'block');
    //Setting username
    $("#displayUsername_id").text(username);
    $("#username_id").val(username);
    $("#knownUser_id").show(); //showing user icon
    // Setting user details modal
    $("#user_details_username_id").val(username);
    $('#profile_username_id').val(username);
    //For storing comments with a post when the modal is closed.
    $('#content_modal_id').on('hidden.bs.modal', function () {
        addModalDataToPost();
        clearCommentModal();
    })
    // Toggling UI component state (right-hand side buttons)
    $('#post_button_id').prop('disabled', true); // Disable POST button
    $('#comm_delete_button').prop('disabled', true); // Disable Delete community button
    $('#community_viewer_id').change(function(){ // For the community viewer button
        $('#community_viewer_btn_id').data('clicked', false);
    });
    $('#community_viewer_id').on('input', function(){ // If there was previously an error
        $('#community_viewer_id').css('border', '');
    });
    // filter options
    $('.following-viewer').show();
    $('.community-viewer').show();
    // Showing search components
    $('#radio_btns_div_id').show();
    $('#searchbar_id').show();
}

// Loads in all the data from backend
function loadInData(){
    setNotificationTimeout();
    setFollowersTimeout();
    setUpNotificationsUsingToastr();
    loadNotifications(); // Loads in the notifications for the user
    loadOwnedCommunities();	// Gets communities owned by user
    loadAllCommunities(); // Gets all communities
    loadFollowing(); // Gets the people the user is following
    loadFollowers(); // Gets the user's followers
    loadPopularCommunities(); // Gets list of most popular communities
}

function addEventHandlers(){
    const timer_ms = 250;
    $("#searchbar_id").on('input', debounce(search, timer_ms)); // For automatic searching by genre
    //For creating a post
    $("#send_post_btn_id").click(function(){
        if($('#community_viewer_container_id').is(":visible")){
            $('#community_viewer_container_id').hide();
            $('#post_container_id').show();
        }else{
            if($('#followers_viewer_container_id').is(":visible")){
                $('#followers_viewer_container_id').hide();
                $('#post_container_id').show();
            }else{
                if($('#search_results_id').is(":visible")){
                    $('#search_results_id').hide();
                    $('#searchbar_id').val('');
                    $('#content_column_id').show();
                }
            }
        }
        handlePostCreation();
    });
    // Notifications
    $('#notifications_dropdown_id').click(function(){
        let btn = $('#notifications_dropdown_id');
        if($(btn).data('clicked')){ // Hide dropdown
            $('#notifications_area_id').drop
            $(btn).data('clicked', false);
        }else{ // Show dropdown
            $(btn).parent().toggleClass('open');
            $(btn).data('clicked', true);
            setNotificationsAsSeen();
        }   
    });

    // When the user changes radio button
    $('input[name="search_selector"]').change(function(){
        let selected_value = $('input[name="search_selector"]:checked').val();
        $('#searchbar_id').val(`${selected_value}:`);
        extractSearchType(`${selected_value}:`); // changes the colour
    });
    $('#search_name_selector_id').trigger('click'); // default option

    // Allows the user to logout
    $("#logout_option").on('click', function(){
        deleteCookies();
        $.ajax(rootPath + '/logout', {
            type: 'DELETE'
        }); // doesn't really matter whether they fail or not
        location.reload();
    });
}