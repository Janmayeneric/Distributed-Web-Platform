package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import object.User;
import relation.Following;
import repository.FollowingRepository;
import repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private FollowingRepository followingRepository;
	
	
	
	
	public UserService() {
		
	}
	
	/**
	 * return any random id from database except input id
	 * @param id
	 * @return
	 */
	public String findRandomId(String id) {
		Random random = new Random();
		List<User> users = userRepository.findByIdNot(id);
		return users.get(random.nextInt(users.size())).getId();
	}

	private List<String> idsToUsernames(List<String> userIds){
		List<String> res = new ArrayList<String>();
		
		for(String userId : userIds) {
			String username = idToUsername(userId);
			if(username != null) {
				res.add(username);
			}
		}
		return res;
	}
	
	/**
	 * transfer user id to username
	 * @param id
	 * @return username
	 * 			null if id not found
	 */
	private String idToUsername(String id) {
		Optional<User> userOption = userRepository.findById(id);
		if(userOption.isPresent()) {
			return userOption.get().getUsername();
		}
		return null;
	}
	
	/**
	 * 	transfer following object to list of followers' id
	 * @param followings
	 * @return
	 */
	private List<String> followingToFollowersIds(List<Following> followings){
		List<String> res = new ArrayList<String>();
		
		for(Following following:followings) {
			res.add(following.getFollowerId());
		}
		
		return res;
	}
	
	/**
	 * 	transfer following object to list of following users' id
	 * @param followings
	 * @return
	 */
	private List<String> followingToFollowingsIds(List<Following> followings){
		List<String> res = new ArrayList<String>();
		
		for(Following following:followings) {
			res.add(following.getUserId());
		}
		
		return res;
	}
	
	/**
	 * get user's followers' id
	 * @param user
	 * @return
	 */
	public List<String> getFollowersIds(User user){
		List<String> res = new ArrayList<String>();
		
		//	if user is not in database, return empty list
		Optional<User> userOption = userRepository.findById(user.getId());
		if(userOption.isEmpty()) {
			return res;
		}
		
		return followingToFollowersIds(followingRepository.findByUserId(user.getId()));
	}
	
	/**
	 * get user's following users' id
	 * @param user
	 * @return
	 */
	public List<String> getFollowingsIds(User user){
		List<String> res = new ArrayList<String>();
		
		//	if user is not in database, return empty list
		Optional<User> userOption = userRepository.findById(user.getId());
		if(userOption.isEmpty()) {
			return res;
		}
		
		return followingToFollowingsIds(followingRepository.findByFollowerId(user.getId()));
	}
	
	/**
	 * get user's followers' username
	 * @param user
	 * @return
	 */
	public List<String> getFollowersUsernames(User user){
		List<String> res = new ArrayList<String>();
		
		//	if user is not in database, return empty list
		Optional<User> userOption = userRepository.findById(user.getId());
		if(userOption.isEmpty()) {
			return res;
		}
		
		return idsToUsernames(
				followingToFollowersIds(
						followingRepository.findByUserId(user.getId())));
	}
	
	/**
	 * get user's following users' username
	 * @param user
	 * @return
	 */
	public List<String> getFollowingsUsernames(User user){
		List<String> res = new ArrayList<String>();
		
		//	if user is not in database, return empty list
		Optional<User> userOption = userRepository.findById(user.getId());
		if(userOption.isEmpty()) {
			return res;
		}
		
		return idsToUsernames(
				followingToFollowingsIds(
						followingRepository.findByFollowerId(user.getId())));
	}
	
}
