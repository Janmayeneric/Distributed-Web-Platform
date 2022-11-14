"use strict";

// For reading in images
let reader = new FileReader();

// only need to be read in once and then maintain on the front-end.
// they indicate which posts have been upvoted/downvoted.
let setOfUpvotedPosts = null;
let setOfDownvotedPosts = null;

// Main method for getting posts and associated comments from db
async function loadPostsAndComments(callback, container, path){
    if(setOfDownvotedPosts == null || setOfUpvotedPosts == null){ // only does it once
        getUpvoteAndDownvoteLists().then(function(){
            getPostsAndComments(callback, container, path);
        });
    }else{
        getPostsAndComments(callback, container, path);
    }
}

// Gets all posts and comments related to the user
async function getPostsAndComments(callback, container, path){
    $.get(rootPath + path, function(posts){ // Get data
        $.each(posts, function(index, element){ // For each post
            let formData = new FormData();
            for (var key in element) {
                formData.append(key, element[key]);
            }
            formData.set('timeOfCreation', convertEpochToTimeAndDate(Number(formData.get('timeOfCreation'))));
            if(setOfUpvotedPosts != null && setOfUpvotedPosts.has(element.id)){ // check if its been upvoted by the user
                createPost(formData, element.id, 'load', container, 0);
            }else if(setOfDownvotedPosts != null && setOfDownvotedPosts.has(element.id)){ // downvoted
                createPost(formData, element.id, 'load', container, 1);
            }else{ // neither
                createPost(formData, element.id, 'load', container, 2);
            }
            numberOfComments = 0; //resetting to 0
            $.each(element.comments, function(index, data){ //For each comment
                loadComment(data, "#comments_section_id", true); // true as its the root comment
            });
            let comments = $("#comments_section_id").clone(true); //storing the comments with the post
            $(`#content_${numberOfPosts - 1}_row_id`).data("comments", comments);
            $(`#content_${numberOfPosts - 1}_row_id`).data("numberOfComments", numberOfComments);
            clearCommentModal();
            //Storing the number of comments to enable dynamic generation of comment ids on modal
        });
    }).fail(function(){
        callback(numberOfPosts);
    }).done(function(){
        callback(numberOfPosts);
    });
}

//Stores all the data from the modal with the post that opened it.
function addModalDataToPost(){
    let postID = $('#content_modal_id').data('postID');
    let comments = $("#comments_section_id").clone(true);
    $('#' + postID).data("comments", comments);
    $('#' + postID).data("numberOfComments", numberOfComments);
}

//Handles the creation of posts on the UI after sending it to the server
function handlePostCreation(){
    let dataTransferObject = extractPostDetails();
    if(dataTransferObject !== null){
        dataTransferObject.append('timeOfCreation', null);
        sendPostToServer(dataTransferObject);
    }
}

// Converts epoch time to local time and date
function convertEpochToTimeAndDate(epoch_value){
    let d = new Date(epoch_value);
    let unparsedTime = d.toString();
    return (unparsedTime.split('('))[0];
}

//Sends the new post data to the server.
async function sendPostToServer(dataTransferObject){
    $.ajax(rootPath + 'posts', {
        type: 'POST',
        data: dataTransferObject,
        processData: false,
        contentType: false,
        success: function(data){
            let uniquePostID = data[0];
            if(uniquePostID != postErrorID){
                dataTransferObject.set('timeOfCreation', convertEpochToTimeAndDate(Number(data[1])));
                console.log(dataTransferObject.get('timeOfCreation'));
                createPost(dataTransferObject, uniquePostID, 'modal', 'post_container_id');
                clearPostModal();
                Swal.fire({
                    title: 'Post Created',
                    icon: 'success',
                    timer: 750,
                    showConfirmButton: false,
                });
                checkCommunityListsForUpdate('create', dataTransferObject.get('community'));
            }else{
                Swal.fire(
                    'Server Error!',
                    'We can\'t create your post right now, sorry.',
                    'error'
                )
            }
        },
        error: function(){
            Swal.fire(
                    'Connection Error!',
                    'Bad network connection. Failed to post.',
                    'error'
            )
        },
    });
}

//Extracts POST details from the modal.
function extractPostDetails(){
    let dataTransferObject = new FormData();
    let username, community, title, content;
    if((username = $("#username_id").val()) === ""){
        Swal.fire(
            'Invalid Post!',
            'There is something wrong with your username. ',
            'error'
        )
        return null;
    }
    if((community = $("#community_id").val()) === null){
        $("#community_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Post!',
            'There is something wrong with your community choice. ',
            'error'
        )
        return null;
    }
    else{
        $("#community_id").css('border-color', '');
    }
    if((title = $("#postTitle_id").val()) === ""){
        $("#postTitle_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Post!',
            'There is something wrong with your post title. ',
            'error'
        )
        return null;
    }
    else{
        $("#postTitle_id").css('border-color', '');
    }
    if((content = $("#postContent_id").val()) === ""){
        $("#postContent_id").css('border-color', 'red');
        Swal.fire(
            'Invalid Post!',
            'There is something wrong with your post content.',
            'error'
        )
        return null;
    }
    else{
        $("#postContent_id").css('border-color', '');
    }
    dataTransferObject.append('username', username);
    dataTransferObject.append('community', community);
    dataTransferObject.append('title', title);
    dataTransferObject.append('content', content);
    dataTransferObject.append('netVotes', 0);
    return saveImageFromPost(dataTransferObject);
}

// Saves the image that was uploaded as part of the post if it exists
function saveImageFromPost(dto){
    let files = $('#upload_id')[0].files;
    // Check file selected or not
    if(files.length > 0 ){
        let filesize = ((files[0].size/1024)/1024).toFixed(4); // MB
        if(filesize > 16){ // 16 MB limit
            Swal.fire(
                'Invalid!',
                `Exceeded file size limit(${filesize})`,
                'error'
            );
            return null;
        }
        dto.append('image',files[0]);
    }else{
        dto.append('image',null);
    }
    return dto;
}

/* Creates a post on the UI 
* Note that this function also works by copying a default post template stored in the HTML 
* and generates the new post by updating the fields on the template with the user-inputted data.*/
function createPost(dataTransferObject, uniquePostID, contentType, container, vote_flag){
    let newPost = $("#content_default_row_id").clone();
    setNewPostAttributes(newPost, dataTransferObject, contentType, uniquePostID);
    checkIfPostNeedsToBeHighlighted(newPost, dataTransferObject.get('community'));
    addDoubleClickEventHandlerToNewPost(newPost, contentType);
    addClickHandlerToQuickCommentIcon(newPost);
    addDropdownPostOptionEventHandlers(newPost, uniquePostID);

    //If the error message is up, we remove it since they're adding a post.
    if($("#no_posts_message_id").is(':visible')){
        $('#no_posts_message_id').hide();
    }
    
    // dealing with vote flag to know whether post has been previously upvotes/downvoted
    if(vote_flag == 0){ // been upvoted
        let element = $(newPost).find('#upvote_id');
        $(element).data('clicked', true);
        $(element).addClass('btn-success');
        $(element).removeClass('btn-outline-success');
    }else{
        if(vote_flag == 1){ // downvoted
            let element = $(newPost).find('#downvote_id');
            $(element).data('clicked', true);
            $(element).addClass('btn-danger');
            $(element).removeClass('btn-outline-danger');
        }
    }
    //Adds the post to the top of the user's feed 
    $("#" + container).prepend(newPost);
    numberOfPosts += 1;
}

// Sets the attributes for a new post
function setNewPostAttributes(newPost, dataTransferObject, contentType, uniquePostID){
    // Setting attributes 
    newPost.attr("id", "content_" + numberOfPosts + "_row_id");
    newPost.removeAttr("style"); //removing the default hidden CSS
    //Updating fields
    $(newPost).find("#post_community_id").text("community/" + dataTransferObject.get('community'));
    $(newPost).find("#post_username_id").text("Posted by u/" + dataTransferObject.get('username') + " at " + dataTransferObject.get('timeOfCreation'));
    $(newPost).find("#post_title_id").text(dataTransferObject.get('title'));
    $(newPost).find("#vote_value_id").text(dataTransferObject.get('netVotes'));
    // Loading in image
    if(contentType == "load"){
        let photoId = dataTransferObject.get('photoId');
        if(photoId != 'null'){
            $(newPost).find('#loaded_img_id').attr('src', `/photos/${photoId}`);
            $(newPost).find('#loaded_img_id').css('display', 'block');
        }
    }else{
        let imageData = dataTransferObject.get('image');
        if(imageData != 'null'){
            reader.onload = function (e) {
                $(newPost).find('#loaded_img_id').attr('src', e.target.result);
                $(newPost).find('#loaded_img_id').css('display', 'block');
            }
            reader.readAsDataURL(imageData);
        }
    }
    //Adding data attributes
    $(newPost).find("#post_content_id").data("obj", dataTransferObject); 
    $(newPost).data("numberOfComments", 0); //storing with post object
    $(newPost).data("community", dataTransferObject.get('community')); //storing with post object
    $(newPost).data("uniquePostID", uniquePostID); //storing with post object
}

// Copies an image from either the local modal or database into the post object
function copyImageIntoPostObject(dto, contentType, newPost){
    if(dto.get('image') != 'null' || dto.get('photoId') != 'null'){
        // Copying image
        if(contentType == 'load'){
            let clonedImage = $(newPost).find('#loaded_img_id').clone();
            $(clonedImage).attr({
                id:'stored_img_id',
                class: 'stored-image'
            });
            $('#image_container_id').append(clonedImage);
        }else{
            let image = dto.get('image');
            if(image != 'null'){
                reader.onload = function (e) {
                    let img = document.createElement('img');
                    $(img).attr({
                        id:'stored_img_id',
                        class: 'stored-image',
                        src: e.target.result
                    });
                    $('#image_container_id').append(img);
                }
                reader.readAsDataURL(image);
            }
        }
    }
}

//Opens the comments modal when the user double clicks on a post.
//It works by copying the relevant data from the post onto the modal.
function addDoubleClickEventHandlerToNewPost(newPost, contentType){
    newPost.dblclick(function(){
        $('#content_modal_id').modal('toggle'); //displaying the modal
        //Getting data from the post by accessing the dto orginally used to create the post
        let dto = $(this).find('#post_content_id').data('obj');
        //Using the dto set fields on the modal
        $('#content_modal_title_id').text(dto.get('title'));
        $('#post_maker_id').text($(this).find("#post_username_id").text());
        $("#post_maker_community_id").text(dto.get('community'));
        $('#modal_content_id').text(dto.get('content'));
        copyImageIntoPostObject(dto, contentType, newPost);
        $('#modal_comment_label_id').text("Comment as '" + username + "'");
        //Storing the ID of the post as a data attribute on the modal.
        $('#content_modal_id').data("postID", $(newPost).attr('id'));
        let comments = $(this).data('comments'); //getting all the comments for a post
        numberOfComments = $(this).data("numberOfComments");
        //Sets the global variable 'numberOfComments' to the number of comments stored for this post.
        if(numberOfComments > 0){ //if there are any comments, we must add them to the modal.
            $('#comments_section_id').remove();
            $("#modal_body_id").append(comments);
            $("#no_comments_message_id").hide();
        } else{ //shows a 'no comments' message if there are no comments.
            $("#no_comments_message_id").show();
        }
    });
}

// Adds a click event to the fa comments icon for allowing quick comments
function addClickHandlerToQuickCommentIcon(newPost){
    //Allows the user to enter a quick comment when they interact with the comment icon on a post.
    $(newPost).find(".fa-comments").click(function(){ 
        if($(this).data('clicked') == false){
            //Generating and appending comment box to body of post
            let comment_container = $(document.createElement("div")).attr("class","row d-flex justify-content-center content-comments");
            let column1 = $(document.createElement("div")).attr("class", "col-md-9");
            let column2 = $(document.createElement("div")).attr("class", "col-md-3");
            let text_area = $(document.createElement("textarea")).attr({ class:"form-control", rows:"1", placeholder:"What are your thoughts?", id:"primaryCommentBox_id" });
            let comment_button = $(document.createElement("button")).attr({class:"btn btn-secondary form-control", onClick:"createQuickComment(this)"});
            comment_button.html("Comment");
            column1.append(text_area);
            column2.append(comment_button);
            comment_container.append(column1);
            comment_container.append(column2);
            $(this).parent().parent().parent().append(comment_container);
            $(this).data('clicked', true); //Use a data attribute to toggle between creation and destruction of input components.
        } else{
            $(this).parent().parent().parent().find(".content-comments").remove();
            $(this).data('clicked', false);
        }
    });
}

function addDropdownPostOptionEventHandlers(newPost, uniquePostID){
     // Handler for if user clicks hide option
     $(newPost).find('#hide_option_id').on('click', {param1: newPost}, function(e){
        $(e.data.param1).remove();
    });
    // Event handler for if user selects delete option
    $(newPost).find('#delete_option_id').on('click', {param1: newPost, param2: uniquePostID}, function(e){
        Swal.fire({
            title: `Are you sure you want to delete this?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes'
        }).then((result) => {
            if (result.isConfirmed){
                deletePostFromServer(e.data.param2, e.data.param1);
            }
        });
    });
}

//Clears the post modal fields after we've created the post.
function clearPostModal(){
    $("#community_id").val("");
    $("#postTitle_id").val("");
    $("#postContent_id").val("");
    $('#upload_id').val("");
    $('#uploaded_img_id').css('display', 'none'); // Hiding last image
    $('#post_modal_id').modal('toggle');
}

// Gets list of communities to put in add post modal
function preparePostModal(){
    let communityList = $("#all_community_list");	// The list of all communities
    let communitySelector = $("#community_id");	// The community selector of the post modal
    communitySelector.empty();	// Clears the options in the selector
    communitySelector.append('<option value="" selected disabled>Select a community</option>')	// Adds default option back
    // Adds each entry in communities list to selector in post modal.
    communityList.children().each(function(){
        let communityName = $(this).find('#community_list_item_id').text();
        // Gets all of the text before the start of the span tag
        communitySelector.append("<option>" + communityName + "</option>");
    });
}

// Checks if the post needs to be highlighted
function checkIfPostNeedsToBeHighlighted(newPost, community){
    let index = highlightedCommunities.indexOf(community);
    if(index > -1){ // It is in the list
        $(newPost).addClass('highlighted-posts');
    }
}

// Deletes a post
async function deletePostFromServer(uniquePostID, element){
    $.ajax({
        url: rootPath + 'posts/' + uniquePostID,
        type: 'DELETE',
        success: function(){
                checkCommunityListsForUpdate('delete', $(element).data('community'));
                $(element).remove();
                if($('#post_container_id').children().length == 1){
                    $('#no_posts_message_id').show();
                }
                Swal.fire({
                    title: 'Post deleted',
                    icon: 'success',
                    timer: 750,
                    showConfirmButton: false,
                });
            },
        error: function(){
            Swal.fire(
                'Invalid!',
                'Could not delete post.',
                'error'
            );
        },  
    });
}

// Performs the upvote operation
function upvote(element){
    let postId = $(element).parent().parent().parent().data('uniquePostID');
    let valueHolder = $(element).parent().parent().find('#vote_value_id');
    if(!$(element).data('clicked')){ // Not been clicked before
        performUpvote(postId, element, valueHolder);
    }else{ // Undoing previous click
        undoUpvote(postId, element, valueHolder);
    }
}

// Performs the upvote operation
function performUpvote(postId, element, valueHolder){
    $.post(rootPath + 'users/vote/post/up', {'postId': postId}, function(){
        let newValue = parseInt($(valueHolder).text()) + 1;
        $(element).data('clicked', true);
        $(element).addClass('btn-success');
        $(element).removeClass('btn-outline-success');
        setOfUpvotedPosts.add(postId);
        let element_downvote = $(element).parent().parent().find('#downvote_id');
        if($(element_downvote).data('clicked')){ // if the post is currently downvoted
            $(element_downvote).data('clicked', false);
            $(element_downvote).removeClass('btn-danger');
            $(element_downvote).addClass('btn-outline-danger');
            setOfDownvotedPosts.delete(postId);
            newValue += 1; // add another for cancelled out downvote
        }
        $(valueHolder).text(newValue);
    }).fail(function(){
        Swal.fire(
            'Failed!',
            'Couldn\'t perform upvote.',
            'error'
        );
    });
}

// Undoes the upvote operation
function undoUpvote(postId, element, valueHolder){
    $.post('/users/vote/post/up/remove',{'postId': postId}, function(){
        let newValue = parseInt($(valueHolder).text()) - 1;
        $(valueHolder).text(newValue);
        $(element).data('clicked', false);
        $(element).removeClass('btn-success');
        $(element).addClass('btn-outline-success');
        setOfUpvotedPosts.delete(postId);
    }).fail(function(){
        Swal.fire(
            'Failed!',
            'Couldn\'t undo upvote.',
            'error'
        );
    });
}

// Performs the downvote operation
function downvote(element){
    let postId = $(element).parent().parent().parent().data('uniquePostID');
    let valueHolder = $(element).parent().parent().find('#vote_value_id');
    if(!$(element).data('clicked')){ // Not been clicked before
        performDownvote(postId, element, valueHolder);
    }else{ // Undoing previous click
        undoDownvote(postId, element, valueHolder);
    }
}

// Performs the downvote operation
function performDownvote(postId, element, valueHolder){
    $.post(rootPath + 'users/vote/post/down',{'postId': postId}, function(){
        let newValue = parseInt($(valueHolder).text()) - 1;
        $(valueHolder).text(newValue);
        $(element).data('clicked', true);
        $(element).addClass('btn-danger');
        $(element).removeClass('btn-outline-danger');
        setOfDownvotedPosts.add(postId);
        let element_upvote = $(element).parent().parent().find('#upvote_id');
        if($(element_upvote).data('clicked')){ // if the post is currently upvoted
            $(element_upvote).data('clicked', false);
            $(element_upvote).removeClass('btn-success');
            $(element_upvote).addClass('btn-outline-success');
            setOfUpvotedPosts.delete(postId);
            newValue -= 1; // remove another for cancelled out upvote
        }
        $(valueHolder).text(newValue);
    }).fail(function(){
        Swal.fire(
            'Failed!',
            'Couldn\'t perform downvote.',
            'error'
        );
    });
}

// Undoes downvote operation
function undoDownvote(postId, element, valueHolder){
    $.post('/users/vote/post/down/remove', {'postId': postId}, function(){
        let newValue = parseInt($(valueHolder).text()) + 1;
        $(valueHolder).text(newValue);
        $(element).data('clicked', false);
        $(element).removeClass('btn-danger');
        $(element).addClass('btn-outline-danger');
        setOfDownvotedPosts.delete(postId);
    }).fail(function(){
        Swal.fire(
            'Failed!',
            'Couldn\'t undo downvote.',
            'error'
        );
    });
}

// Displays an image
function displayImage(input) {
    if (input.files && input.files[0]) {
        let reader = new FileReader();
        reader.onload = function (e) {
            $('#uploaded_img_id').attr('src', e.target.result);
            $('#uploaded_img_id').css('display', 'block');
        }
        reader.readAsDataURL(input.files[0]);
        $('#clear_id').show();
    }else{
        $('#uploaded_img_id').css('display', 'none');
    }
}

// Clears an uploaded file
function clearUploadedFile(){
    let input = $('#upload_id'); // clear selector
    input.replaceWith(input.val('').clone(true));
    $("#uploaded_img_id").attr('src', '#'); // clear preview image
    $("#uploaded_img_id").hide();
    $("#clear_id").hide();
}

// Stops double click propagating up to the parent in posts
function stopPropagation(event){
    event.stopPropagation();
}

// Gets list of posts that the user has already downvotes/upvoted
async function getUpvoteAndDownvoteLists(){
    $.get(rootPath + 'users/vote/post/up', function(list1){
        setOfUpvotedPosts = new Set(list1); // o(n) but gives constant look up time
        $.get(rootPath + 'users/vote/post/down', function(list2){
            setOfDownvotedPosts = new Set(list2); // o(n) but gives constant look up time
        }).fail(function(){
            Swal.fire(
                'Failed!',
                'Server error; failed to retrieve downvote information',
                'error'
            );
        });
    }).fail(function(){
        Swal.fire(
            'Failed!',
            'Server error; failed to retrieve upvote information',
            'error'
        );
    });
}