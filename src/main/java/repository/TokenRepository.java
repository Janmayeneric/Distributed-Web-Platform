package repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;


import object.Token;


public interface TokenRepository extends MongoRepository<Token,String>{
	
	Optional<Token> findByUserId(String userId);
	
	void deleteByKey(String key);
	
	Optional<Token> findByKey(String key);
	
	
	
}
