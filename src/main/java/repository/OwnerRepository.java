package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import relation.CommunityOwner;




public interface OwnerRepository extends MongoRepository<CommunityOwner,String>{
	
		// find community owner
		String findUserIdBycommunityName(String communityName);
		
		// the owning communities list for a user
		List<CommunityOwner> findByUserId(String userId);
		
		//	delete the owner information when community is deleted
		int deleteByCommunityName(String communityName);
		
		//	delete the owner of the community
		int deleteByCommunityNameAndUserId(String communityName, String userId);
		
		//	find if user own that community
		boolean existsByCommunityNameAndUserId(String communityName, String userId);
		
}
