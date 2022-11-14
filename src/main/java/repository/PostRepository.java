package repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;


import object.Post;


public interface PostRepository extends MongoRepository<Post,String>{
	
	Optional<Post> findById(String id);
	
	void deleteById(String id);
	
	void deleteByCommunity(String community);
	
	List<Post> findByUsername(String username);
	
	// find the list of comment in one subcommittee
	// order is descending by their latest update time
	List<Post> findByCommunityOrderByLatestUpdateTimeAsc(String community);
	
	List<Post> findByUserIdOrderByLatestUpdateTimeAsc(String userId);
	
	int countByCommunity(String community);
	
	List<Post> findByUserIdInOrderByLatestUpdateTimeAsc(List<String> userIds);
	
	// find the list of posts by
	// user id (1)
	//	list of communities
	List<Post> findByUserIdInOrCommunityInOrderByLatestUpdateTimeAsc(List<String> userIds, List<String> community);
}