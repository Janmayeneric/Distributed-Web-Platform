package repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import object.User;



public interface UserRepository extends MongoRepository<User,String>{
	
	// find account by name
	Optional<User> findByUsername(String username);
	
	boolean existsByUsername(String username);
	
	// return all user except specific user id
	List<User> findByIdNot(String id);
	
	void deleteById(String id);
	
	void deleteByUsername(String username);
	
	// search the name with the letter but except the login user himself
	// warning search is username, but except must be the login user's id due to mongodb limit
	// it is not case sensitive
	List<User> findByUsernameStartingWithIgnoreCaseAndIdNot(String search, String except);
	
	
}