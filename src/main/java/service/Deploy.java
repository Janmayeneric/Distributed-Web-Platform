package service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import object.Community;
import object.Notification;
import object.User;
import relation.CommunityMember;
import relation.CommunityOwner;
import repository.CommunityRepository;
import repository.MemberRepository;
import repository.OwnerRepository;
import repository.UserRepository;

@Service
public class Deploy {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OwnerRepository communityOwnerRepository;
	
	@Autowired
	private MemberRepository communityMemberRepository;
	
	@Autowired
	private CommunityRepository communityRepository;
	public Deploy(){
		
	}

	
	/**
	 * it will create global value for user and communities
	 * for the super group stuff
	 */
	@Bean
	public void setGlobalUserAndCommunity() {
		communityRepository.deleteByName("global");
		userRepository.deleteByUsername("global");
	
		User user = new User();
		user.setUsername("global");
		user.setNotifications(new ArrayList<Notification>());
		user = userRepository.save(user);
		Community community = new Community();
		community.setName("global");
		community.setNumOfMembers(-1);
		community.setNumOfPosts(0);
		communityRepository.save(community);
		communityOwnerRepository.save(new CommunityOwner(user.getId(),community.getName()));
		communityMemberRepository.save( new CommunityMember(user.getId(),community.getName()));
		
	}
}
