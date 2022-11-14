package service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import object.Token;
import object.User;
import repository.TokenRepository;
import repository.UserRepository;

@Service
public class CookieAuthentication {
	
	@Autowired
	private TokenRepository tokenRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	/**
	 * it check the login token inside the database
	 * false if:
	 * 		token is expired in the database
	 * 		token is deleted from the database(user logout)
	 * 		user is not existed now the database
	 * @param token	cookie from user's browsers
	 * @return
	 */
	public boolean authenticate(String token) {
		Optional<Token> loginOptional = tokenRepository.findByKey(token);
		
		if(loginOptional.isEmpty()) {
			return false;
		}
		
		Optional<User> userOptional = userRepository.findById(loginOptional.get().getUserId());
		
		if(userOptional.isEmpty()) {
			return false;
		}
		
		if(loginOptional.get().getExpiry() < System.currentTimeMillis()) {
			return false;
		}
		
		return true;
	}
	
	public User currentUser(String token) {
		Optional<Token> loginOptional = tokenRepository.findByKey(token);
		Optional<User> userOptional = userRepository.findById(loginOptional.get().getUserId());
		return userOptional.get();
	}
}
