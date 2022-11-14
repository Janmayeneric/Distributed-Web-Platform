package controller;


import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import exception.PostNotFoundException;
import exception.UserNotFoundException;
import object.Post;
import object.Token;
import object.User;
import relation.CommunityMember;
import relation.Following;
import repository.FollowingRepository;
import repository.MemberRepository;
import repository.PostRepository;
import repository.TokenRepository;
import repository.UserRepository;
import service.Checker;
import service.CookieAuthentication;
import service.UserService;

import org.springframework.web.bind.annotation.*;

import enumeration.Identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserRepository userRepository;
	
	@Autowired
	private CookieAuthentication cookieAuthentication;
	
	@Autowired
	private TokenRepository tokenRepository;
	
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private Checker checker;

	@Autowired
	private FollowingRepository followingRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MemberRepository memberRepository;

	UserController(UserRepository repository) {
		this.userRepository = repository;
	}
	
	@PostMapping
	ResponseEntity<?> register(User newUser) {
		// check the username is valid
		if (!checker.userChecker(newUser)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// prevent multiple users with the same username
		if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		
		newUser.initialise();

		// hash the password before saving
		newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
		newUser = userRepository.save(newUser);
		
		// add user into global community for the supergroup purpose
		memberRepository.save(new CommunityMember(newUser.getId(),"global"));
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PutMapping
	ResponseEntity<?> replacePassword(@CookieValue(value = "token") String token, @RequestParam("newPassword") String newPassword) {
		if (!checker.passwordChecker(newPassword)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		return processUser(token, user -> {
			user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
			return userRepository.save(user);
		});
	}
	
	@PostMapping("/rename")
	ResponseEntity<?> renameAccount(@CookieValue(value = "token") String key, @RequestParam("newName") String newName) {
		if(!checker.nameChecker(newName)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		if (userRepository.findByUsername(newName).isPresent()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		return processUser(key, user -> {
			user.rename(newName);
			return userRepository.save(user);
		});
	}

	private void maybeSendVote(Post post, String userId, Boolean vote) {
		String server = post.getServer();
		if (server != null) try {
			PropagationController prop = PropagationController.getInstance();
			prop.sendVote(post.getServer(), post.getId(), userId, vote);
		} catch (Exception e) {
			// failed to send vote to server
		}
	}

	@PostMapping("/vote/post/up")
	ResponseEntity<?> votePostUp(@CookieValue(value = "token") String key, @RequestParam("postId") String postId) {
		return processUser(key, user -> {
			Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
			maybeSendVote(post, user.getId(), true);

			if (user.removeVoteDown(postId)) post.removeVoteDown();
			if (user.addVoteUp(postId)) post.addVoteUp();

			postRepository.save(post);
			return userRepository.save(user);
		});
	}
	
	@PostMapping("/vote/post/up/remove")
	ResponseEntity<?> votePostRemoveUp(@CookieValue(value = "token") String key, @RequestParam("postId") String postId) {
		return processUser(key, user -> {
			Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
			maybeSendVote(post, user.getId(), null);

			if (user.removeVoteUp(postId)) post.removeVoteUp();

			postRepository.save(post);
			return userRepository.save(user);
		});
	}

	@PostMapping("/vote/post/down")
	ResponseEntity<?> votePostDown(@CookieValue(value = "token") String key, @RequestParam("postId") String postId) {
		return processUser(key, user -> {
			Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
			maybeSendVote(post, user.getId(), false);

			if (user.removeVoteUp(postId)) post.removeVoteUp();
			if (user.addVoteDown(postId)) post.addVoteDown();

			postRepository.save(post);
			return userRepository.save(user);
		});
	}

	@PostMapping("/vote/post/down/remove")
	ResponseEntity<?> votePostRemoveDown(@CookieValue(value = "token") String key, @RequestParam("postId") String postId) {
		return processUser(key, user -> {
			Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
			maybeSendVote(post, user.getId(), true);

			if (user.removeVoteDown(postId)) post.removeVoteDown();

			postRepository.save(post);
			return userRepository.save(user);
		});
	}

	@GetMapping("/vote/post/up")
	ResponseEntity<?> getVotesUp(@CookieValue(value = "token") String key) {
			return processUser(key, User::getVotesUp);
	}

	@GetMapping("/vote/post/down")
	ResponseEntity<?> getVotesDown(@CookieValue(value = "token") String key) {
			return processUser(key, User::getVotesDown);
	}

	@PostMapping("/followers/add")
	ResponseEntity<?> followUser(@CookieValue(value = "token") String key,  @RequestParam("username") String username) {;
		return processUser(key, user -> {
			if (user.getUsername().equals(username)) throw new IllegalArgumentException("Cannot follow yourself");
			User target = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
			Following following = new Following(target.getId(),cookieAuthentication.currentUser(key).getId());
			if(!followingRepository.existsByUserIdAndFollowerId(following.getUserId(),following.getFollowerId())){
				target.addNotification("You were followed by " + user.getUsername() + '!');
				followingRepository.save(following);
			}
			userRepository.save(target);
			return "finish";
		});
	}

	@PostMapping("/followers/remove")
	ResponseEntity<?> unfollowUser(@CookieValue(value = "token") String key, @RequestParam("username") String username) {
		return processUser(key, user -> {
			User target = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
			
			// could add an unfollow notification?
			//	it weird to tell you are unsubscribed by someone
			followingRepository.deleteByUserIdAndFollowerId(target.getId(),cookieAuthentication.currentUser(key).getId());
			return "finish";
		});
	}
	
	@GetMapping("/followers")
	ResponseEntity<List<String>> getFollowers(@CookieValue(value = "token") String key) {
		if(!cookieAuthentication.authenticate(key)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		User user = cookieAuthentication.currentUser(key);
		return new ResponseEntity<List<String>>(userService.getFollowersUsernames(user),HttpStatus.OK);
	}

	@GetMapping("/following")
	ResponseEntity<List<String>> getFollowing(@CookieValue(value = "token") String key) {
		if(!cookieAuthentication.authenticate(key)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		User user = cookieAuthentication.currentUser(key);
		return new ResponseEntity<List<String>>(userService.getFollowingsUsernames(user),HttpStatus.OK);
	}

	@GetMapping("/notifications")
	ResponseEntity<?> getNotifications(@CookieValue(value = "token") String key) {
		return processUser(key, User::getNotifications);
	}

	@PostMapping("/notifications")
	ResponseEntity<?> notificationsSeen(@CookieValue(value = "token") String key) {
		return processUser(key, user -> {
			user.getNotifications().forEach(notification -> notification.setSeen(true));
			return userRepository.save(user);
		});
	}

	@PutMapping("/admin/{username}")
	ResponseEntity<?> assignAdmin(@CookieValue(value = "token") String key, @PathVariable String username) {
		return processUser(key, user -> {
			// only admins can promote others to admin
			if (user.getIdentity() == Identity.ADMIN) {
				User target = userRepository.findByUsername(username).orElseThrow();
				target.setIdentity(Identity.ADMIN);
				return userRepository.save(target);
			} else {
				throw new IllegalArgumentException("Not an admin");
			}
		});
	}

	@GetMapping("/search/{username}")
	ResponseEntity<?> findUsers(@CookieValue(value = "token") String key, @PathVariable String username) {
		return processUser(key, __ -> 
		userRepository
		.findByUsernameStartingWithIgnoreCaseAndIdNot(
				username,cookieAuthentication
				.currentUser(key).getId())
		.stream().map(User::getUsername));
	}
	
	@PutMapping("/randomactofkindness")
	ResponseEntity<?> randomActOfKindness(@CookieValue(value = "token") String key){
		if(!cookieAuthentication.authenticate(key)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		User currentUser = cookieAuthentication.currentUser(key);
		
		Optional<User> targetOption = userRepository
				.findById(userService.findRandomId(currentUser.getId()));
		
		if(targetOption.isPresent()) {
			User target = targetOption.get();
			target.addNotification(currentUser.getUsername() + " says have a nice day!");
			userRepository.save(target);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
		
	}
	

	@DeleteMapping("/delete")
	ResponseEntity<?> deleteUser(@CookieValue(value = "token") String key) {
		return processToken(key, token -> {
			userRepository.deleteById(token.getUserId());
			return null;
		});
	}

	private ResponseEntity<?> processToken(String key, Function<? super Token, ?> function) {
		Optional<Token> token = tokenRepository.findByKey(key);
		if (token.isEmpty()|| System.currentTimeMillis() > token.get().getExpiry()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			return new ResponseEntity<>(function.apply(token.get()), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	private ResponseEntity<?> processUser(String key, Function<? super User, ?> function) {
		return processToken(key, token -> userRepository.findById(token.getUserId())
			.map(function).orElseThrow(() -> new UserNotFoundException(token.getUserId())));
	}

}
