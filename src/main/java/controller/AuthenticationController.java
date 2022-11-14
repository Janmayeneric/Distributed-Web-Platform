package controller;

import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import authentication.TokenManager;
import object.User;
import repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.bcrypt.BCrypt;


@RestController
public class AuthenticationController {

	
	private final UserRepository userRepository;
	
	private final TokenManager manager;
	
	private final HttpServletResponse response;
	
	AuthenticationController(UserRepository userRepository,TokenManager manager,HttpServletResponse response){
		this.manager = manager;
		this.userRepository = userRepository;
		this.response = response;
	}
	
	@PostMapping(value = "/login", consumes = { "application/json" })
	public ResponseEntity<?> login(@RequestBody User newUser){
		
		Optional<User> user = userRepository.findByUsername(newUser.getUsername());
	
		// it need to implement, with error message 
		if(user.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		//System.out.println("Password : " + user.get().getPassword());
		
		
		// it need to use BCrypt comparison method to compare plain text and encrypted password
		if(!BCrypt.checkpw(newUser.getPassword(),user.get().getPassword())) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		//System.out.println("Here : " + user.get().getId());
		String key = manager.createToken(user.get().getId());
		Cookie cookie = new Cookie("token",key);
		Cookie usernameC = new Cookie("username", user.get().getUsername());
		response.addCookie(cookie);
		response.addCookie(usernameC);
		//System.out.println("here = " + newUser.getUsername());
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	
	
	@DeleteMapping("/logout")
	public ResponseEntity<?> Logout(@CookieValue(value = "token") String token){
		try {
			manager.deleteToken(token);
		}catch(IllegalStateException e) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
}
