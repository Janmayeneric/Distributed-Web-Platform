package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import relation.Following;

public interface FollowingRepository extends MongoRepository<Following, String>{
	
	// find user's follower
	List<Following> findByUserId(String userId);
	
	// find who is user following
	List<Following> findByFollowerId(String followerId);
	
	//	delete the following relation if the user is delete(to his follower)
	int deleteByUserId(String userId);
	
	//	delete the following relation if the user is delete(to his following)
	int deleteByFollowerId(String followerId);
	
	// 	check if that relation exists
	boolean existsByUserIdAndFollowerId(String userId, String followerId);
	
	void deleteByUserIdAndFollowerId(String userId, String followerId);
	

}
