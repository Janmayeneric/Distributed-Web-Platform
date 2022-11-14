package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import relation.CommunityMember;

public interface MemberRepository extends MongoRepository<CommunityMember,String> {
	
	// 	find community members
	List<CommunityMember> findByCommunityName(String communityName);
	
	//	find random member from the community, use for owner switching
	CommunityMember findFirst1ByCommunityName(String communityName);
	
	//	delete all communities followed by deleted member
	int deleteByUserId(String UserId); 
	
	//	delete all members belong to deleted community
	int deleteByCommunityName(String communityName);
	
	int deleteByCommunityNameAndUserId(String communityName, String UserId);
	
	//	find the user's following communities in ascending order 
	List<CommunityMember> findByUserIdOrderByCommunityNameAsc(String userId);
	
	boolean existsByCommunityNameAndUserId(String communityName, String userId);
	
	//	check if the community still have any members
	boolean existsByCommunityName(String communityName);
	
	List<CommunityMember> findByUserId(String userId);
	
	// count all members in the community
	long countByCommunityName(String communityName);
}
