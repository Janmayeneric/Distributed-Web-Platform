"use strict";

// Determines if the system should search by name, genre, topic or user
function extractSearchType(search_phrase){
    const result = search_phrase.split(':');
    if(result.length == 2){
        if(result[0] == "Topic"){
            $('#searchbar_id').css('color', 'red');
        }else if(result[0] == "Name"){
            $('#searchbar_id').css('color', 'green');
        }else if(result[0] == "User"){
            $('#searchbar_id').css('color', 'orange');
        }else if(result[0] == "Genre"){
            $('#searchbar_id').css('color', 'blue');
        }else{ // Unknown input
            // Defaulting to using genre
            $('#searchbar_id').css('color', 'black');
            $('#search_error_message_id').css('opacity', '1');
            return ["Genre", search_phrase];
        }
    }else{
        $('#searchbar_id').css('color', 'black');
        $('#search_error_message_id').css('opacity', '1');
        return ["Genre", result[0]];
    }
    $('#search_error_message_id').css('opacity', '0'); // hide error message if showing
    return result;
}

// Performs a search of communities or usernames
function search(){
    $('.community-viewer').hide();
    $('.following-viewer').hide();
    let search_phrase = $('#searchbar_id').val();
    if(search_phrase == ""){
        $('#search_error_message_id').css('opacity', '0');
        clearSearchResults();
        $('.community-viewer').show();
        $('.following-viewer').show();
        // Removing all search results except for the default hidden one
        $("#search_results_id").hide();
        $("#content_column_id").show();
        return;
    }
    let search_values = extractSearchType(search_phrase);
    if(search_values[1] != ""){
        if(search_values[0] == "User"){
            searchForUsernames(search_values[1], search_values[0]);
        }else{
            searchForCommunities(search_values[1], search_values[0]);
        }
    }
    if($('#community_viewer_btn_id').data('clicked')){
        undoSpecificCommunitySearch($('#community_viewer_btn_id'));
    }
    if($('#following_viewer_btn_id').data('clicked')){
        undoShowFollowingPosts($('#following_viewer_btn_id'));
    }
}

// Searches for communities
function searchForCommunities(search_phrase, search_type){
    $.get(rootPath + 'communities/search/' + search_type + "/" + search_phrase, function(communities){ // Get data
        clearSearchResults();
        //Set search phrase in search results header
        $("#search_phrase").text(search_phrase);
        //Hide posts container & show search results
        $("#content_column_id").hide()
        $("#search_results_id").show();
        if(communities.length > 0){
            let counter = 0;
            $.each(communities, function(index, element){ // For each community
                addToSearchResultsCommunities(element, counter);
                counter++;
            });
        }else{
            $('#no_matching_results').show();
        }
    }).fail(function (){
        clearSearchResults();
    });
}

// Clears all of the previous search results
function clearSearchResults(){
    $("#search_username_container_id").children().slice(1).remove();
    $("#search_results_container_id").children().slice(1).remove();
    $('#no_matching_results').hide();
}

// Searches for usernames 
function searchForUsernames(search_phrase, search_type){
    $.get(rootPath + 'users/search/' + search_phrase, function(usernames){ // Get data
        clearSearchResults();
        //Set search phrase in search results header
        $("#search_phrase").text(search_phrase);
        //Hide posts container & show search results
        $("#content_column_id").hide()
        $("#search_results_id").show();
        if(usernames.length > 0){
            let counter = 0;
            $.each(usernames, function(index, element){ // For each community
                addToSearchResultsUsernames(element, counter, search_type);
                counter++;
            });
        }else{
            $('#no_matching_results').show();
        }
    }).fail(function (){
        clearSearchResults();
    });
}

// Adds community name to search results list
function addToSearchResultsCommunities(communityObject, index){
    let template = $('#search_default_row_id').clone();
    template.data('communityName', communityObject.name);
    template.attr("id", "search_" + index + "_row_id");
    template.removeAttr("style"); //removing the default hidden CSS
    let str;
    if(communityObject.name == "global"){ // special case for global community
        str = communityObject.name;
    }else{
        str = communityObject.name + " (" + communityObject.topic + "/" + communityObject.genre + ")";
    }
    $(template).find('#community_name_id').text(str);
    // Deciding whether to show the join or leave button
    if(checkListForCommunity(communityObject.name)){
        $(template).find('#search_leave_btn_id').show();
    }else{
        $(template).find('#search_join_btn_id').show();
    }
    let resultsList = $("#search_results_container_id");
    resultsList.append(template);
}

//Adds the usernames to search results list
function addToSearchResultsUsernames(username, index){
    let template = $('#search_username_default_row_id').clone();
    template.data('username', username);
    template.attr("id", "search_username_" + index + "_row_id");
    template.removeAttr("style"); //removing the default hidden CSS
    $(template).find('#username_id').text(username);
    // Deciding whether to show the join or leave button
    if(checkListForFollowing(username)){
        $(template).find('#search_remove_btn_id').show();
    }else{
        $(template).find('#search_add_btn_id').show();
    }
    let resultsList = $("#search_username_container_id");
    resultsList.append(template);
}

// Debounce code for search - limits number of api requests
var debounce = function(fn, delay) {
    var timer = null;
    return function () {
        var context = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    };
}
