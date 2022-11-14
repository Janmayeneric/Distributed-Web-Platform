package controller;

import java.util.Optional;

import java.util.List;
import java.io.IOException;


import object.Post;
import object.User;
import object.Comment;
import object.Community;
import repository.CommentRepository;
import repository.CommunityRepository;
import repository.PostRepository;
import repository.UserRepository;
import service.Checker;
import service.CommunityService;
import service.CookieAuthentication;
import service.PhotoService;
import service.PostService;
import service.UserService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import enumeration.Identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/posts")
public class PostController {
	
	@Autowired
	private CookieAuthentication cookieAuthentication;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PhotoService photoService;
	
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private CommunityService communityService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private Checker checker;
	
	PostController(){
		
	}

	@PostMapping
	public ResponseEntity<?> makepost(@RequestParam("title") String title,
			@RequestParam("content") String content,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam("community")String communityName,
			@RequestParam(value = "netVotes", required = false)String netVotes,
			@RequestParam(value = "timeOfCreation", required = false) String timeOfCreation,
			@CookieValue(value = "token")String token){
		
		//	check login credential
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<String>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		//	set every attribute for the post
		User currentUser = cookieAuthentication.currentUser(token);
		Post post = new Post();
		post.setUsername(currentUser.getUsername());
		post.setContent(content);
		post.setTitle(title);
		post.updateTime();
		post.setUserId(currentUser.getId());
		long epoch = checker.epochStringConverter(timeOfCreation);
		
		
		//	if the timeOfCreation is valid use that to indicate the creation time of post
		//	if not valid, we create one for the post
		if(epoch > 0) {
			post.setTimeOfCreation(epoch);
		}else {
			epoch = System.currentTimeMillis();
			post.setTimeOfCreation(epoch);
		}
		
		
		Optional<Community> communityOption = communityRepository.findByName(communityName);
		
		//	if the community the user want make post on is not exists
		//	return the error
		if(communityOption.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		post.setCommunity(communityName);
		
		Community community = communityOption.get();
		
		
		try {
			if(!(image == null)) {
				if(!image.isEmpty()) {
					post.setphotoID(photoService.addPhoto("image", image));
				}
			}
		}catch(IOException e) {
			return new ResponseEntity<String>(HttpStatus.SERVICE_UNAVAILABLE);
		}

		
		//	for notification 
		userService.getFollowersIds(currentUser).stream()
			.map(userRepository::findById)
			.filter(Optional::isPresent).map(Optional::get)
			.forEach(u -> {
				u.addNotification(currentUser.getUsername() + " created a new post");
				userRepository.save(u);
			});

		post = postRepository.save(post);
		
		//	remind the community to update the number of the posts
		communityService.updateNumOfPosts(community);

        return new ResponseEntity<>(new String[]{post.getId(), Long.toString(post.getTimeOfCreation())},HttpStatus.OK);
	}

    @PostMapping(value = "/makecomment",consumes = { "application/json" })
	public ResponseEntity<?> makecomment(@CookieValue(value = "token")String token,
			@RequestBody Comment newcomment){
		//    	check login credential
		if (!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<String>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}

		if (newcomment.getPostID() == null || newcomment.getContent() == null || newcomment.getContent().trim().length() == 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		User user = cookieAuthentication.currentUser(token);

		Optional<Post> postOption = postRepository.findById(newcomment.getPostID());
		if (postOption.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// 	update the post time when there is a new comment
		Post post = postOption.get();
		post.updateTime();

		postRepository.save(post);

		Comment comment = new Comment();
		comment.setUsername(user.getUsername());
		comment.setUserId(user.getId());
		comment.setContent(newcomment.getContent());
		comment.setTimeOfCreation(System.currentTimeMillis());
		comment.setPostID(newcomment.getPostID());

		if (post.getServer() != null) try {
			PropagationController prop = PropagationController.getInstance();
			comment = prop.postComment(post.getServer(), comment);
		} catch (Exception e) {
			// failed to create comment on remote server
		}

		if (newcomment.getParentID() != null && commentRepository.existsById(newcomment.getParentID())) {
			comment.setParentID(newcomment.getParentID());
		}

		comment = commentRepository.save(comment);

		return new ResponseEntity<>(new String[]{comment.getId(), Long.toString(comment.getTimeOfCreation())},HttpStatus.OK);
	}
    
    

	@GetMapping("/community/{communityname}")
	public ResponseEntity<List<Post>> getPostByCommunity(@PathVariable String communityname){
		return new ResponseEntity<List<Post>>(
				postService.getComments(
					postRepository.
					findByCommunityOrderByLatestUpdateTimeAsc(communityname)),
				HttpStatus.OK);
	}
	
	
	/**
	 * 	it will return the post:
	 * 		following communities' posts
	 * 		user's post
	 * 		user's following user posts
	 * @param token
	 * @return
	 */
	@GetMapping("/currentUser")
	public ResponseEntity<List<Post>> getPostByCurrentUser(@CookieValue(value = "token")String token){
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<List<Post>>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		User currentUser = cookieAuthentication.currentUser(token);
		
		List<String> userIds = userService.getFollowingsIds(currentUser);
		
		return new ResponseEntity<List<Post>>(
				postService.getComments(
						postRepository.findByUserIdInOrCommunityInOrderByLatestUpdateTimeAsc(
								userIds,
								communityService.getJoinedCommunitiesNames(currentUser)))
				,HttpStatus.OK);
	}
	
	@GetMapping("/following")
	public ResponseEntity<List<Post>> getPostByFollowing(@CookieValue(value = "token")String token){
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		return new ResponseEntity<List<Post>>(
				postService.getComments(
					postRepository
					.findByUserIdInOrderByLatestUpdateTimeAsc(
							userService.
							getFollowingsIds(
									cookieAuthentication.currentUser(token)))),
				HttpStatus.OK);
		}

	
	
	/**
	 * 	two step is done here:
	 * 		-	delete the post here
	 * 		-	remind the post's community to update the number of posts
	 *	some extra measure is taken if community is already deleted before this post(for redundancy)
	 * @param postId
	 * @return
	 */
	@DeleteMapping("/{postId}")
	public ResponseEntity<?> deletePost(@PathVariable String postId,@CookieValue(value = "token")String token){
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		User user = cookieAuthentication.currentUser(token);
		Optional<Post> postOption = postRepository.findById(postId);
		if(postOption.isPresent()) {
			Post post = postOption.get();
			if(!post.getUserId().equals(user.getId())||(user.getIdentity() == Identity.ADMIN)) {
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			}
			Optional<Community> communityOption = communityRepository.findByName(post.getCommunity());
			postRepository.delete(post);
			
			//	update in the community about the number of posts
			if(communityOption.isPresent()) {
				communityService.updateNumOfPosts(communityOption.get());
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
