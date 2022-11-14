package repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import object.Community;

public interface CommunityRepository extends MongoRepository<Community,String>{
	
	Optional<Community> findByName(String name);
	
	List<Community> findByGenre(String genre);
	
	// return all the letter that contain the letter starting with exp
	List<Community> findByGenreStartingWith(String exp);
	List<Community> findByNameStartingWith(String exp);
	List<Community> findByTopicStartingWith(String exp);
	
	//List<Community> findTop5ByOrderByNumOfMembersDesc();
	
	
	void deleteByName(String name);
	
	List<Community> findFirst5ByNameNotOrderByNumOfMembersDesc(String except);
	
	boolean existsByName(String name);
}
