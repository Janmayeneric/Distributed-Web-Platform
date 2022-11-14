package controller;


import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import object.Community;
import object.User;
import relation.CommunityMember;
import relation.CommunityOwner;
import repository.CommunityRepository;
import repository.MemberRepository;
import repository.OwnerRepository;
import repository.UserRepository;
import service.Checker;
import service.CommunityService;
import service.CookieAuthentication;
import enumeration.Identity;


@RestController
@RequestMapping("/communities")
public class CommunityController {
	
	@Autowired
	private CommunityRepository communityRepository;
	
	@Autowired
	private Checker checker;
	
	@Autowired
	private CommunityService communityService;
	
	@Autowired
	private CookieAuthentication cookieAuthentication;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private OwnerRepository ownerRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	
	CommunityController(){
	}
	
	@PostMapping
	ResponseEntity<?> create(Community newCommunity,@CookieValue(value = "token") String token){
		Optional<Community> communityOption = communityRepository.findByName(newCommunity.getName());
		
		// check if it is legal login credential
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		 
		// check if the community with valid input
		if(!checker.communityChecker(newCommunity)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// check if the community is already created
		if(!communityOption.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		User user = cookieAuthentication.currentUser(token);
		// add new relation into database
		// owner of the community, member of the community
		communityService.CreateCommunity(newCommunity, user);
		
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PutMapping("/join/{communityname}")
	ResponseEntity<Community> join(@CookieValue(value = "token") String token,@PathVariable String communityname){
		Optional<Community> communityOptional = communityRepository.findByName(communityname);
		
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		// check if the community is existed in the repository
		if(communityOptional.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}	
		
		Community community = communityOptional.get();
		User user = cookieAuthentication.currentUser(token);
		if(memberRepository.existsByCommunityNameAndUserId(communityname, user.getId())) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		memberRepository.save(new CommunityMember(user.getId(),communityname));
		communityService.updateNumOfMembers(community);
		return new ResponseEntity<>(community,HttpStatus.OK);
		
	}
	
	
	
	@PutMapping("/leave/{communityname}")
	ResponseEntity<?> leave(@CookieValue(value = "token") String token,@PathVariable String communityname){
		
		// 	check the login credential
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		// 	check if the community exist by name
		Optional<Community> communityOptional = communityRepository.findByName(communityname);
		if(communityOptional.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		User user = cookieAuthentication.currentUser(token);
		Community community = communityOptional.get();
		
		//	check by database if the user is the member of the community before
		if(memberRepository.deleteByCommunityNameAndUserId(communityname, user.getId()) == 0) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		//	then check if there is any member in the community
		if(!memberRepository.existsByCommunityName(community.getName())) {
			//	if no member, delete the community and return the message to inform the front end
			communityService.deleteCommunity(community);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		}
		

		//	check if the user is the owner of the community
		if(ownerRepository.existsByCommunityNameAndUserId(community.getName(), user.getId())) {
			
			//	if it is, we will
			//		dismiss the ownership of the leaving user
			//		assign the ownership to random one in the members
			ownerRepository.deleteByCommunityNameAndUserId(communityname,user.getId());
			ownerRepository.save(new CommunityOwner(
			memberRepository.findFirst1ByCommunityName(community.getName()).getUserId(),
					community.getName()));
			
		}
		
		communityService.updateNumOfMembers(community);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	
	/**
	 * 
	 * @param type not case sensitive
	 * @param name case sensitive
	 * @return List of community sort by type and starting with same letter in the name
	 */
	@GetMapping("/search/{type}/{name}")
	ResponseEntity<List<Community>> getSearchedList(@PathVariable String type, @PathVariable String name){
		type = type.toLowerCase();
		if(type.equals("topic")) {
			List<Community> res = communityRepository.findByTopicStartingWith(name);
			return new ResponseEntity<List<Community>>(res,HttpStatus.OK);
		}
		if(type.equals("name")) {
			List<Community> res = communityRepository.findByNameStartingWith(name);
			return new ResponseEntity<List<Community>>(res,HttpStatus.OK);
		}
		if(type.equals("genre")) {
			List<Community> res = communityRepository.findByGenreStartingWith(name);
			return new ResponseEntity<List<Community>>(res,HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/detail/{communityname}")
	ResponseEntity<Community> getCommunity(@PathVariable String communityname){
		//	check if the community exist by name
		Optional<Community> communityOptional = communityRepository.findByName(communityname);
		if(communityOptional.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<Community>(communityOptional.get(),HttpStatus.OK);
	}
	
	@GetMapping("/owned/currentUser")
	ResponseEntity<List<Community>> ownedCommunitites(@CookieValue(value = "token") String token){
		
		//	check credential
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION); 
		}
		
		//	use current user information to get his owned communities list
		List<CommunityOwner> communityOwners =  ownerRepository.findByUserId(
				cookieAuthentication.currentUser(token).getId());
		
		//	transfer that list to list of communities and return back
		return new ResponseEntity<List<Community>>(
				communityService.ownedCommunitiesToCommunities(communityOwners),
				HttpStatus.OK);
		
	}
	
	@GetMapping("/joined/currentUser")
	ResponseEntity<List<Community>> joinedCommunitites(@CookieValue(value = "token") String token){
		
		//	check credential
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION); 
		}
		
		//	use current user information to get his owned communities list
		List<CommunityMember> communityMembers =  memberRepository.findByUserId(
				cookieAuthentication.currentUser(token).getId());
		
		//	transfer that list to list of communities and return back
		return new ResponseEntity<List<Community>>(
				communityService.joinedCommunitiesToCommunities(communityMembers),
				HttpStatus.OK);
		
	}
	
	@GetMapping("/popular")
	ResponseEntity<List<Community>> getPopularCommunities(){
		return new ResponseEntity<List<Community>>(
				communityRepository.findFirst5ByNameNotOrderByNumOfMembersDesc("global")
				,HttpStatus.OK);
	}
	
	@DeleteMapping("/delete")
	ResponseEntity<?> deleteCommunity(@CookieValue(value = "token") String token,
			@RequestParam(value = "message") String message,
			@RequestParam("communityname") String communityname){
		
		if(!cookieAuthentication.authenticate(token)) {
			return new ResponseEntity<>(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
		// then check if the community is existed in the database
		Optional<Community> communityOptional = communityRepository.findByName(communityname);
		if(communityOptional.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		Community community = communityOptional.get();
		
		// check if the user is admin or the owner of the community
		User currentUser = cookieAuthentication.currentUser(token);
		Identity identity = currentUser.getIdentity();
		if(identity != Identity.ADMIN) {
			if(!ownerRepository.existsByCommunityNameAndUserId(communityname,currentUser.getId())) {
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			}
		}
		
		communityService.getMemberLists(community).stream()
		.map(userRepository::findById)
		.filter(Optional::isPresent).map(Optional::get)
		.forEach(u -> {
			u.addNotification("Community " + community.getName() + " has been removed!<br><b>Creator message:</b><br> '" + message + "'");
			userRepository.save(u);
		});
		communityService.deleteCommunity(community);
		return new ResponseEntity<>(HttpStatus.OK); 
	}
}
