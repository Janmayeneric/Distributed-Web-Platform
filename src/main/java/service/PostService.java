package service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import object.Comment;
import object.Post;
import repository.CommentRepository;



import java.util.List;






@Service
public class PostService {

	
	@Autowired
	private CommentRepository commentRepository;

	
	// it will update the posts with the comments, for the list
	public List<Post> getComments(List<Post> posts){
		// for each posts update their comments
		posts.stream().forEach(post -> this.getComments(post));
		return posts;
	}
	
	
	
	// it will update the post with the comments
	public Post getComments(Post post) {
		List<Comment> comments= commentRepository
				.findByPostIDAndParentIDIsNullOrderByTimeOfCreationAsc(
						post.getId());
		comments = this.findSubComments(comments);
		post.setComments(comments);
		return post;
	}
	
	/**
	 * find the comment for each comment in the list
	 * @return
	 */
	private List<Comment> findSubComments(List<Comment> comments){
		
		// it is recursive
		// but if the comments list is null, the for loop will reject automatically
		//	so that is the base case
		for(Comment comment:comments) {
			List<Comment> subComments = commentRepository.findByParentIDOrderByTimeOfCreationAsc(comment.getId());
			this.findSubComments(subComments);
			comment.setComments(subComments);
		}
		return comments;
	}
	
}
