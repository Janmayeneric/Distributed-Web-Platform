"use strict";

let highlightedCommunities = [];

// Clears delete community modal and gets list of communties for dropdown
function prepareDeleteModal(){
    let communityList = $("#community_list");	// The list of communities
    let communitySelector = $("#deleteModal_community_id");	// The selector of the delete modal
    $("#deleteMessage_id").val("");	// Clears the delete modals message
    communitySelector.empty();	// Clears the options in the selector
    communitySelector.append("<option value=\"\" selected disabled>Select a community</option>");// Adds default option back
    // Adds each entry in communities list to selector in delete modal.
    communityList.children().each(function(){
        let communityName = $(this).find('#community_list_item_id').text();
        communitySelector.append("<option>" + communityName + "</option>");
    });
}

// Handles the creation of a community
function handleCommunityCreate(){
    let dataTransferObject = extractCreateModalDetails();
        if(dataTransferObject !== null){
            sendCommunityCreateToServer(dataTransferObject);
        }
}

// Handles the deletion of a community
function handleCommunityDelete(){
    let dataTransferObject = extractDeleteModalDetails();
        if(dataTransferObject !== null){
            sendCommunityDeleteToServer(dataTransferObject);
        }
}

// Gets data from the community create modal
function extractCreateModalDetails(){
    let dataTransferObject = new Object();
    if((dataTransferObject.name = $("#createCommunity_name_id").val()) === ""){
        $("#createCommunity_name_id").css('border-color', 'red');	//	 Incorrect field is highlighted
        Swal.fire(
            'Invalid Community!',
            'There is something wrong with your name choice. ',
            'error'
        )
        return null;
    }
    else{
        $("#createCommunity_name_id").css('border-color', '');
    }
    if((dataTransferObject.topic = $("#createCommunity_topic_id").val()) === ""){
        $("#createCommunity_topic_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Community!',
            'There is something wrong with your topic.',
            'error'
        )
        return null;
    }
    else{
        $("#createCommunity_topic_id").css('border-color', '');
    }
    if((dataTransferObject.genre = $("#createCommunity_genre_id").val()) === ""){
        $("#createCommunity_genre_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Community!',
            'There is something wrong with your genre.',
            'error'
        )
        return null;
    }
    else{
        $("#createCommunity_genre_id").css('border-color', '');
    }
    return dataTransferObject;
}

// Gets data from the community delete modal
function extractDeleteModalDetails(){
    let dataTransferObject = new FormData();
    let name, message;
    if((name = $("#deleteModal_community_id").val()) === null){
        $("#deleteModal_community_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Community!',
            'There is something wrong with your community choice. ',
            'error'
        )
        return null;
    }
    else{
        $("#deleteModal_community_id").css('border-color', '');
    }
    if((message = $("#deleteMessage_id").val()) === ""){
        $("#deleteMessage_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Message!',
            'There is something wrong with your message.',
            'error'
        )
        return null;
    }
    else{
        $("#deleteMessage_id").css('border-color', '');
    }
    dataTransferObject.append('communityname', name);
    dataTransferObject.append('message', message);
    return dataTransferObject;
}

// Sends request to create a community to the server
async function sendCommunityCreateToServer(dataTransferObject){
    $.post(rootPath + "communities", dataTransferObject, function(response){
            //user logged in successfully
            Swal.fire({ 
                title: "Community created",
                icon: "success",
                showConfirmButton: false,
                timer:750
            }).then(() =>{
                addCommunityToList(dataTransferObject.name, 0);
                addToAllCommunityList(dataTransferObject.name, 0);
                $('.owned-communities').css('display', 'block')
                $('.all-communities').css('display', 'block')
                $('#post_button_id').prop('disabled', false);
                $('#comm_delete_button').prop('disabled', false);
            });
            clearCreateModal();
        }).fail(function(jqXHR, textStatus, errorThrown){
            if(jqXHR.status == 500){
                Swal.fire(
                    'Invalid!',
                    'Could not create community; error at server.',
                    'error',
                    );
            }else if(jqXHR.status == 409){
                Swal.fire(
                    'Invalid!',
                    'Could not create community; name is already in use.',
                    'error'
                    );
            }else if(jqXHR.status == 400){
                Swal.fire(
                    'Invalid!',
                    'Could not create community; one of your fields contains an invalid character.',
                    'error'
                    );
            }else{
                Swal.fire(
                    'Invalid!',
                    'Could not create community',
                    'error'
                    );
            }
        });
}

// Sends request to delete  a community to the server
async function sendCommunityDeleteToServer(dataTransferObject){
    $.ajax({
    url: rootPath + 'communities/delete',
    type: 'DELETE',
    data: dataTransferObject,
    processData: false,
    contentType: false,
    success: function(result){
        Swal.fire({
                    title: 'Community deleted',
                    icon: 'success',
                    timer:750,
                    showConfirmButton: false,
                });
                removeCommunityFromList(dataTransferObject.get('communityname'));
                $('#delete_community_modal_id').modal('toggle');
        },
        error: function(){
        
            Swal.fire(
                'Invalid!',
                'Could not delete community.',
                'error'
                );
    },  
        });
}

// Clears community create modal
function clearCreateModal(){
    $("#createCommunity_name_id").val("");
    $("#createCommunity_topic_id").val("");
    $("#createCommunity_genre_id").val("");
    $('#create_community_modal_id').modal('toggle');
}

// Adds a community to your owned community list
function addCommunityToList(communityName, numberOfPosts){
    if($('#comm_delete_button').prop('disabled')){
        $('#comm_delete_button').prop('disabled', false);
    }
    let list = $('#community_list');
    let item  = document.createElement('li');
    $(item).attr({
        class: "list-group-item d-flex justify-content-between align-items-center community-lists",
    });
    $(item).data('communityName', communityName);
    $(item).data('numberOfPosts', numberOfPosts);
    let content  = '<p id="community_list_item_id" class="text-break" >' + communityName + 
                        `</p><span class="badge badge-primary badge-pill"><b id="numOfPosts_id">${numberOfPosts}</b></span></li>`;
    $(item).append(content);
    list.append(item);
}

// Removes a community from your community list
function removeCommunityFromList(communityName){
    let list = ['#community_list', '#all_community_list'];
    for(let i = 0; i < list.length; i++){
        $(list[i]).children().each(function(){
            if($(this).find('#community_list_item_id').text() === communityName){
                $(this).remove();
            }
        });
    }
    // Hides community list & disable POST & DELETE if there are no entries
    if($("#all_community_list").children().length == 0){
        $('.all-communities').css('display', 'none')
        $('.owned-communities').css('display', 'none')
        $('#post_button_id').prop('disabled', true);
        $('#comm_delete_button').prop('disabled', true);
        $('#no_posts_message_id').show();
    }
    removePostsFromCommunity(communityName);
    handleRefresh(); // to get posts
}

// Gets the communities owned by the user
function loadOwnedCommunities(){
    $.get(rootPath +'communities/owned/currentUser', function(communities){ // Get data
        if(communities.length > 0){
            //Making components visible
            $('.owned-communities').css('display', 'block')
            $('#post_button_id').prop('disabled', false);
            $('#comm_delete_button').prop('disabled', false);
            $.each(communities, function(index, element){ // For each community
                addCommunityToList(element.name, element.numOfPosts);
            });
        }else{ // disable delete communities button
            $('#comm_delete_button').prop('disabled', true);
        }
    });
}

// Gets the current most popular communities
function loadPopularCommunities(){
    $.get(rootPath + 'communities/popular', function(communities){ // Get data
        if(communities.length > 0){
            //Making components visible
            $('.popular-communities').css('display', 'block')
            $.each(communities, function(index, data){ // For each community
                addCommunityToPopularList(data.name, data.numOfMembers);
            });
        }
    });
}

// Adds a community to the popular list
function addCommunityToPopularList(name, numberOfMembers){
    let list = $('#popular_community_list');
    let item  = document.createElement('li');
    $(item).attr({
        class: "list-group-item d-flex justify-content-between align-items-center",
    });
    $(item).data('communityName', name);
    $(item).data('numMembers', numberOfMembers);
    let content  = '<p id="community_list_item_id" class="col-md-7 text-break" >' + name + 
                '</p><span class="col-md-5 text-break text-right popular-list-value"><b id="numMembers_id">' + numberOfMembers + '</b> <i class="fas fa-user-circle"></i></span></li>';
    $(item).append(content);
    list.append(item);
}

// Checks the list of all communities to see if we have a match with communityName
function checkListForCommunity(communityName){
    let list = $('#all_community_list');
    let result = false;
    $(list).children().each(function(){
        if($(this).find('#community_list_item_id').text() === communityName){
            result = true;
            return false; //Only way to exit this loop, returning true just skips to the next iteration
        }
    });
    return result;
}

// Gets all communities the user is a member of
function loadAllCommunities(){
    $.get(rootPath + 'communities/joined/currentUser', function(communities){ // Get data
        if(communities.length > 0){
            // Making components visible
            $('.all-communities').css('display', 'block')
            $('#post_button_id').prop('disabled', false);
            $('#comm_delete_button').prop('disabled', false);
            //Adding each community
            $.each(communities, function(index, element){ // For each community
                addToAllCommunityList(element.name, element.numOfPosts);
            });
        }
    });
}

// Adds a community to your community list
function addToAllCommunityList(communityName, numberOfPosts){
    let list = $('#all_community_list');
    let listItem  = document.createElement('li');
    $(listItem).attr('class', "list-group-item d-flex justify-content-between align-items-center community-lists");
    $(listItem).attr('data-toggle', 'tooltip');
    $(listItem).attr('title', `Double click: Leave '${communityName}'\n Single click : highlight`);
    let data = '<p id="community_list_item_id" class="text-break">' + communityName + 
        `</p><span class="badge badge-primary badge-pill"><b id="numOfPosts_id">${numberOfPosts}</b></span></li>`;
    $(listItem).data('communityName', communityName);
    $(listItem).data('numberOfPosts', numberOfPosts);
    $(listItem).append(data);
    list.tooltip({ selector: '[data-toggle=tooltip]' });

    // Adding event handlers
    $(listItem).dblclick(function(e){
        // https://sweetalert2.github.io/ for the warning message
        Swal.fire({
            title: `Are you sure you want to leave ${communityName}?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, adios'
        }).then((result) => {
            if (result.isConfirmed){
                leaveHomepageCommunity(e.currentTarget);
            }
        });
    });
    addClickEventToCommunityListItem(listItem);
    list.append(listItem);
}

// Adds a click event handler to community list item
function addClickEventToCommunityListItem(listItem){
    $(listItem).click(function(e){
        let communityName = $(e.currentTarget).find('#community_list_item_id').text();
        if($(e.currentTarget).hasClass('highlighted-posts')){ // Need to unhighlight
            $(e.currentTarget).removeClass('highlighted-posts');
            highlightPostsFromCommunity(communityName, false);
            // Removing community from list of highlighted communities
            const idx = highlightedCommunities.indexOf(communityName);
            if(idx > -1){
                highlightedCommunities.splice(idx, 1)
            }
        }else{
            $(e.currentTarget).addClass('highlighted-posts');
            highlightedCommunities.push(communityName);
            highlightPostsFromCommunity(communityName, true);
        }
    });
}

// Join community and add it to all communities list
function joinCommunity(searchElement, location){
    let communityName = $(searchElement).data('communityName');
    communityName = decodeURI(communityName); // All the spaces have been changed to %20
    $.ajax({
        url: rootPath + 'communities/join/' + communityName,
        type: 'PUT',
        success: function(data) {
            Swal.fire({
                    title: 'Community Joined',
                    icon: 'success',
                    timer:750,
                    showConfirmButton: false,
                    });
            if(location == "search_result"){
                switchCommunityButtons(searchElement, 'join');
            }
            $('#post_button_id').prop('disabled', false);
            $('#comm_delete_button').prop('disabled', false);
            $('.all-communities').css('display', 'block');
            addToAllCommunityList(communityName, data.numOfPosts);
            checkPopularListForUpdate('join', communityName);
            handleRefresh(); // to get posts
        },
        error: function(){
            Swal.fire(
                'Invalid!',
                'Could not join community; it doesn\'t exist',
                'error'
            );
        }, 
    });
}

// Leave community, removed from all communities list
function leaveCommunity(element, location){
    // Depending on where this function is being called from, the parsing operation is different
    let communityName = "";
    communityName = $(element).data('communityName');
    $.ajax({
        url: rootPath + 'communities/leave/' + communityName,
        type: 'PUT',
        success: function(returnedData) {
            Swal.fire({
                    title: 'Community Left',
                    icon: 'success',
                    timer: 750,
                    showConfirmButton: false,
                    });
            removeCommunityFromList(communityName);
            checkPopularListForUpdate('leave', communityName);
            if(location != "homepage"){
                switchCommunityButtons(element, 'leave');
            }
        },
        error: function(){
            Swal.fire(
                'Invalid!',
                'Could not leave community.',
                'error'
            );
        }, 
    });
}

// Removes the posts from a removed/left community without having to refresh
function removePostsFromCommunity(communityNameToDelete){
    $('#post_container_id').children().each(function(){
        let communityName = $(this).data('community');
        if(communityName == communityNameToDelete){
            $(this).remove();
        }
    });
}

// Handles the leave community operation when on the homepage
function leaveHomepageCommunity(element){
    $(element).tooltip('hide'); // Turns the tooltip off
    updateSearchResultsForCommunities(element);
    leaveCommunity(element, "homepage");
}

//Switches which button is showing when you leave or join a community.
function switchCommunityButtons(element, triggerButton){
    if(triggerButton == "join"){
        $(element).find('#search_leave_btn_id').show();
        $(element).find('#search_join_btn_id').hide();
    }else{
        $(element).find('#search_join_btn_id').show();
        $(element).find('#search_leave_btn_id').hide();
    }
}

// Updates the search results for communities
function updateSearchResultsForCommunities(element){
    let username = $(element).data('communityName');
    $('#search_results_container_id').children().each(function(){
        if($(this).data('communityName') == username){
            switchCommunityButtons(this, 'leave');
            return false; // Ends the loop
        }
    });
}

// Highlights the posts from a selected community
function highlightPostsFromCommunity(communityNameToDelete, flag){
    $('#post_container_id').children().each(function(){
        let communityName = $(this).data('community');
        if(communityName == communityNameToDelete && flag){
            $(this).addClass('highlighted-posts');
        }else if(communityName == communityNameToDelete && !flag){
            $(this).removeClass('highlighted-posts');
        }
    });
}

// Undoes a specific community search
function undoSpecificCommunitySearch(btn){
    $('#community_viewer_container_id').hide();
    $('#community_viewer_id').val('');
    if($('#post_container_id').children().length > 1){
        $('#no_posts_message_id').hide();
    }
    $('#post_container_id').show();
    $(btn).data('clicked', false);
    $(btn).addClass('btn-outline-info');
    $(btn).removeClass('btn-info');
}

//Gets all the posts from a specific community
function showSpecificCommunityPosts(btn){
    if($(btn).data('clicked')){ // Needed to revert back to posts
        undoSpecificCommunitySearch(btn);
    }else{
        let name = $('#community_viewer_id').val();
        if(name == ""){
            $('#community_viewer_id').css('border', '1px solid red');
            undoSpecificCommunitySearch(btn);
            handleRefresh();
            return;
        }
        if($('#followers_viewer_container_id').is(":visible")){
            undoShowFollowingPosts($('#following_viewer_btn_id'));
        }
        $(btn).removeClass('btn-outline-info');
        $(btn).addClass('btn-info');
        $('#community_viewer_container_id').empty();
        loadPostsAndComments(function(numberOfPosts){
            $('#community_viewer_container_id').show();
            $('#post_container_id').hide();
            if(numberOfPosts == 0){ //If there are no posts, a message is displayed.
                $('#no_posts_message_id').show();
            }
            $(btn).data('clicked', true);
        }, "community_viewer_container_id", 'posts/community/' + name);
    }
}

// Checks if the user has left/joined one of the popular communities
// so that the counter can be updated
function checkPopularListForUpdate(stateChange, communityName){
    $('#popular_community_list').children().each(function(){
        if($(this).data('communityName') == communityName){
            let currentNumOfMembers = $(this).data('numMembers');
            if(stateChange === 'leave'){ // user just left a community
                $(this).find('#numMembers_id').text(currentNumOfMembers - 1);
                $(this).data('numMembers', currentNumOfMembers - 1);
            }else{ // user just joined a community
                $(this).find('#numMembers_id').text(currentNumOfMembers + 1);
                $(this).data('numMembers', currentNumOfMembers + 1);
            }
            return false; // only way to exit .each loop
        }
    });
}

// Increments counter when the user makes a post
function checkCommunityListsForUpdate(stateChange, communityName){
    let listsToCheck = ["#all_community_list", "#community_list"];
    $(listsToCheck).each(function(index, item){
        $(item).children().each(function(){
            if($(this).data('communityName') == communityName){
                let currentNumOfPosts = $(this).data('numberOfPosts');
                if(stateChange === 'delete'){ // user just deleted a post
                    $(this).find('#numOfPosts_id').text(currentNumOfPosts - 1);
                    $(this).data('numberOfPosts', currentNumOfPosts - 1);
                }else{ // user just created a post
                    $(this).find('#numOfPosts_id').text(currentNumOfPosts + 1);
                    $(this).data('numberOfPosts', currentNumOfPosts + 1);
                }
                return false; // only way to exit .each loop
            }
        });
    });
}