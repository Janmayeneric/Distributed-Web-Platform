package service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import object.Community;
import object.User;

@Service
public class Checker {
	
	public Checker() {
	}
	/**
	 * it check the validation of input username
	 * mainly for the registration
	 * not allows:
	 * 		any space in the string
	 * @return
	 */
	
	
	/**
	 * it will receive the epoch string and do some check
	 * 		return -1 when
	 * 		the string is not in long
	 * 		the string is null or empty
	 * @return
	 */
	public long epochStringConverter(String epochString) {
		if(epochString == null) {
			return -1;
		}
		if(epochString.trim().length() == 0) {
			return -1;
		}
		try {
			return Long.parseLong(epochString);
		}catch(NumberFormatException e) {
			return -1;
		}
	}
	
	private boolean usernameChecker(String input) {
		Pattern space = Pattern.compile("\\s");
		Matcher haveSpace = space.matcher(input);
		if(haveSpace.find()) {
			return false;
		}
		return nameChecker(input);
	}
	
	/**
	 * it check the validation of the input password
	 * not allowed:
	 * 		string length less than 6 digit
	 * 		
	 * @param input
	 * @return
	 */
	public boolean passwordChecker(String input) {
		if(input.length() < 6) {
			return false;
		}
		return true;
	}
	
	/**
	 * check the user with it name and password
	 * @param user
	 * @return
	 */
	public boolean userChecker(User user) {
		if(!usernameChecker(user.getUsername())) {
			return false;
		}
		if(!passwordChecker(user.getPassword())) {
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * it check the validation of the community name, topic and genre
	 * not allowed:
	 * 		any special case
	 * 		it will trim the space before storage
	 *      empty string	
	 */
	public boolean nameChecker(String input){
		if(input == null) {
			return false;
		}
		input = input.trim();
	
		if(input.length() < 1) {
			return false;
		}
		
		Pattern special = Pattern.compile("[\\W]"); 
		Matcher haveSpecial = special.matcher(input);
		if(haveSpecial.find()) {
			return false;
		}
		return true;
		
	}
	
	/**
	 * check every attribute for the community
	 * @param community
	 * @return
	 */
	public boolean communityChecker(Community community) {
		
		if(!nameChecker(community.getName())) {
			return false;
		}
		if(!nameChecker(community.getGenre())) {
			return false;
		}
		if(!nameChecker(community.getTopic())) {
			return false;
		}
		return true;
	}
}
