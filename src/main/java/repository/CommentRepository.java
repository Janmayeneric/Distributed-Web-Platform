package repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import object.Comment;

public interface CommentRepository extends MongoRepository<Comment,String>{
	
	// find the comment inside the post
	// but not the comment of the comment
	List<Comment> findByPostIDAndParentIDIsNullOrderByTimeOfCreationAsc(String postID);
	
	// find the comment inside the comment
	List<Comment> findByParentIDOrderByTimeOfCreationAsc(String parentID);
	
	boolean existsById(String id);

	List<Comment> findByUserId(String id);

}
