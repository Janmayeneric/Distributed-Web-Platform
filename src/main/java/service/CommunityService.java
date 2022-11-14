package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import object.Community;
import object.User;
import relation.CommunityMember;
import relation.CommunityOwner;
import repository.CommunityRepository;
import repository.MemberRepository;
import repository.OwnerRepository;
import repository.PostRepository;


@Service
public class CommunityService {
	
	@Autowired
	private OwnerRepository communityOwnerRepository;
	
	@Autowired
	private MemberRepository communityMemberRepository;
	
	@Autowired
	private CommunityRepository communityRepository;
	
	@Autowired
	private PostRepository postRepository;
	

	
	public CommunityService() {
		
	}
	
	
	/**
	 * 	process to create the community
	 * 		-	store the new community into community database
	 * 		-	assign creator as the member
	 * 		-	assign creator as the owner
	 * 		-	set the number of members to 1
	 * 		-	set the number of posts to 0
	 * @param community	community we want to create
	 */
	public void CreateCommunity(Community community, User user) {
		community.setNumOfMembers(1);
		community.setNumOfPosts(0);
		communityRepository.save(community);
		communityOwnerRepository.save(new CommunityOwner(user.getId(),community.getName()));
		communityMemberRepository.save( new CommunityMember(user.getId(),community.getName()));
	}
	
	
	
	/**
	 * 	delete anything related to the community from three database
	 * 		-	community
	 * 		-	community member
	 * 		- 	community owner	
	 * @param community	community we want to delete
	 */
	public void deleteCommunity(Community community) {
			communityOwnerRepository.deleteByCommunityName(community.getName());
			communityMemberRepository.deleteByCommunityName(community.getName());
			communityRepository.deleteByName(community.getName());
			
			
	}
	
	/**
	 * 	extract the communities name from the community object
	 * 	for privacy concern
	 * @param communities
	 * @return
	 */
	public List<String> extractCommunityNames(List<Community> communities){
		List<String> names = new ArrayList<String>();
		for(Community community:communities) {
			names.add(community.getName());
		}
		return names;
	}
	
	/**
	 * 	extract the communities name from the community object
	 * 	for privacy concern
	 * @param communities
	 * @return
	 */
	public List<String> extractCommunityNamesFromCommunityMembers(List<CommunityMember> communities){
		List<String> names = new ArrayList<String>();
		for(CommunityMember community:communities) {
			names.add(community.getCommunityName());
		}
		return names;
	}
	
	/**
	 * update the member of number by counting number in the community
	 * @param community
	 */
	public void updateNumOfMembers(Community community) {
		community.setNumOfMembers(communityMemberRepository.countByCommunityName(community.getName()));
		communityRepository.save(community);
	}
	
	
	/**
	 * update the member of posts by counting number in the community
	 * @param community
	 */
	public void updateNumOfPosts(Community community) {
		community.setNumOfPosts(postRepository.countByCommunity(community.getName()));
		communityRepository.save(community)	;
	}
	
	public List<Community> ownedCommunitiesToCommunities(List<CommunityOwner> communityOwners){
		List<Community> res = new ArrayList<Community>(); 
		for(CommunityOwner communityOwner: communityOwners) {
			Optional<Community> communityOption = communityRepository.findByName(communityOwner.getCommunityName());
			
			//	for redundancy if community is deleted
			//	in some weird situation
			if(communityOption.isPresent()) {
				res.add(communityOption.get());
			}
		}
		
		return res;
	}
	
	/**
	 * transfer the list 
	 * @param communityMembers
	 * @return
	 */
	public List<Community> joinedCommunitiesToCommunities(List<CommunityMember> communityMembers){
		List<Community> res = new ArrayList<Community>(); 
		for(CommunityMember communityMember: communityMembers) {
			Optional<Community> communityOption = communityRepository.findByName(communityMember.getCommunityName());
			
			//	for redundancy if community is deleted
			//	in some weird situation
			if(communityOption.isPresent()) {
				res.add(communityOption.get());
			}
		}
		
		return res;
	}
	
	/**
	 * transfer the list of Object CommunityMember to String communityName
	 * @param communityMembers
	 * @return
	 */
	private List<String> communityMembersToCommunitiesNames(List<CommunityMember> communityMembers){
		List<String> res = new ArrayList<String>(); 
		for(CommunityMember communityMember: communityMembers) {
			Optional<Community> communityOption = communityRepository.findByName(communityMember.getCommunityName());
			
			//	for redundancy if community is deleted
			//	in some weird situation
			if(communityOption.isPresent()) {
				res.add(communityMember.getCommunityName());
			}
		}
		
		return res;
	}
	
	private List<String> communityMemberToMembersIds(List<CommunityMember> members){
		List<String> res = new ArrayList<String>();
		for(CommunityMember member:members) {
			res.add(member.getUserId());
		}
		return res;
	}
	
	
	
	
	/**
	 * 	get list of members inside the the communities
	 * @return
	 */
	public List<String> getMemberLists(Community community){
		return this.communityMemberToMembersIds(
				communityMemberRepository.findByCommunityName(
						community.getName()));
	}
	
	/**
	 * get user's joined communities names list
	 * @return
	 */
	public List<String> getJoinedCommunitiesNames(User user){
		return this.communityMembersToCommunitiesNames(
				communityMemberRepository.findByUserId(user.getId()));
	}

}
