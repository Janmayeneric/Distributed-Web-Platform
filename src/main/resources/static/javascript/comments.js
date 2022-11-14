"use strict";

//Loads all the comments for a post
function loadComment(comment, container_id, isRootComment){
    let commentNum = createComment(comment.id, comment, container_id) - 1;
    let newContainer = $(`#comment_${commentNum}_id`).find("#comment_content_id");
    $.each(comment.comments, function(index, element){ //for each child comment
        loadComment(element, newContainer, false);
    });
}

//Extracts the information for a comment.
//@commentType is used as a flag to differentiate between the different types of comments.
function getCommentInfo(commentType, container){
    let commentObj = new Object();
    commentObj.username = username; // Global var
    commentObj.timeOfCreation = null;
    let postHtmlID;
    if(commentType == 'quick_comment'){
        commentObj.postID = $(container).data('uniquePostID');
    }else{
        postHtmlID = $("#content_modal_id").data('postID');
        commentObj.postID = $($("#content_column_id").find("#" + postHtmlID)).data('uniquePostID');
    }
    if(commentType == 'reply'){
        commentObj.parentID = $(container).data('uniqueCommentID');
        commentObj.content = $("#commentBox_id").val();
    } else{
        commentObj.parentID = rootCommentParent; //meaning no parent
        if(commentType == 'quick_comment'){
            commentObj.content = $(container).find("#primaryCommentBox_id").val();
        } else{
            commentObj.content = $("#modal_comment_id").val();
        }
    }
    /*
        * A comment has the attributes:
        * -> username
        * -> timeOfCreation
        * -> postID (the post it belongs to)
        * -> parentID (the comment it belongs to if we have a nested comment)
        * -> content
        */
    return commentObj;
}

//Creates the input components for allowing the user to enter a reply  
function addReplyBox(event,clickedBtnContainer){
    stopPropagation(event);
    let commentSectionOfParent = $(clickedBtnContainer);
    let result = $(commentSectionOfParent).find('#comment_box_container_id');
    if(result.length > 0){ // Allows closing of comment box without having to submit comment
        result.remove();
        return;
    }
    let objectToAdd = "<div class=\"row d-flex reply-to-comment\" id=\"comment_box_container_id\"><div class=\"d-flex justify-content-center ml-4 mt-1\">"
        + "<input class=\"form-control\" type=\"text-box\" id=\"commentBox_id\"></div>"
        + "<div class=\"d-flex justify-content-center ml-4 mt-1\"><button class=\"btn btn-secondary form-control\""
        + "id=\"submit_comment_id\" onclick=\"addReply($(this));\">POST</button></div></div>";
    $(commentSectionOfParent).append(objectToAdd);
}
       
//*Adds a reply (a comment to another comment) to the modal's body.
//Called when the user clicks on the POST button.
function addReply(btn){
    let commentBoxValue = $("#commentBox_id").val(); // The comment's content
    if(commentBoxValue == ''){
        Swal.fire(
                'Invalid Reply!',
                'Please enter some text.',
                'error'
            )
    } else{
        sendCommentToServer('reply', $(btn).parent().parent().parent().parent());
    }
}

/* Sends comments to the server, there are 3 types of comment:
* 1. Normal comment made through the modal.
* 2. A reply comment made to another comment through the modal.
* 3. A quick comment made without opening the modal. (since we don't use the modal, this comment has to be treated differently)
*/
async function sendCommentToServer(commentType, parentContainer){
    let commentDataObject = getCommentInfo(commentType, parentContainer);
    $.ajax(rootPath + 'posts/makecomment', {
        type: 'POST',
        data: JSON.stringify(commentDataObject),
        contentType: 'application/json',
        success: function(data){
            let uniqueCommentID = data[0];
            commentDataObject.timeOfCreation = data[1];
            //console.log("Unique comment id  = " + uniqueCommentID);
            if(uniqueCommentID != commentErrorID){ // If theres a problem at the server end
                if(commentType == 'reply'){
                    $(parentContainer).find('#comment_box_container_id').remove(); // Removes the components used to enter the comment
                    console.log($(parentContainer).find('#comment_content_id:first'));
                    createComment(uniqueCommentID, commentDataObject, $(parentContainer).find('#comment_content_id:first'));
                }else if(commentType == 'quick_comment'){
                    addQuickComment(uniqueCommentID, commentDataObject, parentContainer);
                }
                else{
                    createComment(uniqueCommentID, commentDataObject, parentContainer);
                    $("#modal_comment_id").val(''); //emptying the text area on modal
                }
            } else{
                Swal.fire(
                    'Server Error!',
                    'We can\'t create your comment right now, sorry.',
                    'error'
                )
            }
        },
        error: function(){
            Swal.fire(
            'Connection Error!',
            'Bad network connection : Failed to comment.',
            'error'
            )
        },
    });
}

//Creates a comment and adds it to the modal (where on the modal depends on the type of comment)
function createComment(uniqueCommentID, commentDataObject, container){
    //Cloning a default comment component.
    let newComment = $("#default_comment_id").clone(); 
    newComment.removeAttr("style"); //the default is hidden so we need to remove this css
    $(newComment).find("#comment_username_id").text("Posted by u/" + commentDataObject.username + 
                    " at " + convertEpochToTimeAndDate(Number(commentDataObject.timeOfCreation)));
    $(newComment).attr("id", "comment_" + numberOfComments + "_id");
    // number of comments used for unique id tags.
    $(newComment).find("#comment_id").text(commentDataObject.content);
    $(newComment).data('uniqueCommentID', uniqueCommentID);
    if($("#no_comments_message_id").is(":visible")){
        $("#no_comments_message_id").hide();
    }
    // If its not a top-level comment
    if($(container).attr('id') != "comments_section_id"){
        $(newComment).removeClass('row'); // gives the nesting effect
    }
    $(container).append(newComment);
    numberOfComments += 1;
    return numberOfComments;
}

//Creates a quick comment.
function createQuickComment(btn){
    let post = $(btn).parent().parent().parent().parent();
    //There is an error if the user has multiple quick comment boxes open, so we localise the search here:
    let commentBox = $(post).find('#primaryCommentBox_id');
    if(commentBox.val() == ""){ //gets the comment text
        Swal.fire(
                'Invalid Quick Comment!',
                'Please enter some text.',
                'error'
            )
    } else {
        sendCommentToServer('quick_comment', post);
    }
}

function addQuickComment(uniqueCommentID, commentDataObject, parentContainer){
    let newComment = $("#default_comment_id").clone(); //cloning the default comment template again
    newComment.removeAttr("style"); //so its not hidden like the template
    $(newComment).find("#comment_username_id").text("Posted by u/" + commentDataObject.username + " at " + getTimeStamp());
    $(newComment).data('uniqueCommentID', uniqueCommentID);
    //Changing the numberOfComments counter, whose value is unique to each post
    let currentNumOfComments = $(parentContainer).data('numberOfComments');
    $(newComment).attr("id", "comment_" + currentNumOfComments + "_id"); //using counter to generate a new id
    currentNumOfComments += 1;
    $(parentContainer).data('numberOfComments', currentNumOfComments);
    //
    $(newComment).find("#comment_id").text(commentDataObject.content); //setting comment text
    //If this is the first comment on the POST, we need to treat it differently
    if($(parentContainer).data('comments')){ 
        //if other comments have already been made for the post, we just need to update the data object
        let comments = $(parentContainer).data('comments');
        $(comments).append(newComment);
        $(parentContainer).data('comments', comments);
    }else{ //first comment
        let comments = $("#comments_section_id").clone(); //clone the comments section
        $(comments).append(newComment);  //add our comment
        $(comments).find('#no_comments_message_id').hide();
        $(parentContainer).data('comments', comments); //store comments with post.
    }
    $(parentContainer).find(".fa-comments").trigger("click"); //this handles the removal of the input components
    Swal.fire(
            'Comment Created!',
            'Double tap to see more comments.',
            'success'
    )
}

//Clears the comment modal fields after we've created the comment.
function clearCommentModal(){
    $("#modal_comment_id").val(''); //emptying the text area
    $("#comments_section_id .user-comment").not(":first").remove();
    //removes all comments except the first one since this is the default template
    $('#image_container_id').empty(); // Removes image if it exists 
}

//Creates a comment on the modal
function createCommentOnModal(){
    if($("#modal_comment_id").val() == ''){ //comments can't be empty
            Swal.fire(
                'Invalid Comment!',
                'Please enter some text.',
                'error'
            )
    } else{
        sendCommentToServer('comment', $('#comments_section_id'));
    }
}
