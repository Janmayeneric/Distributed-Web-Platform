package object;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Comment {
    
	private @Id @GeneratedValue String id; // auto generated an id for mongoDB to use
	private String username;
	private String userId;
    private String postID;	// post it belong to
    private String parentID; //	parent of comment
    private String content;
    private long timeOfCreation; //	time when comment created
    
    // this comment is return to the front end
    // not for storage
    private List<Comment> comments;

    public Comment() {
        // required by MongoDB
    }

    public Comment(String username, String userId, long timeOfCreation, String postID, String parentID, String content) {
        this.username = username;
        this.userId = userId;
        this.timeOfCreation = timeOfCreation;
        this.postID = postID;
        this.parentID = parentID;
        this.content = content;
    }
    
    //Getters
    public String getId() {
		return this.id;
	}

    public String getUsername(){
        return this.username;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setTimeOfCreation(long timeOfCreation) {
    	this.timeOfCreation = timeOfCreation;
    }

    public long getTimeOfCreation(){
        return this.timeOfCreation;
    }

    public String getPostID(){
        return this.postID;
    }

    public String getParentID(){
        return this.parentID;
    }

    public String getContent(){
        return this.content;
    }
    
    public List<Comment> getComments(){
    	return this.comments;
    }

    //Setters
    public void setId(String id) {
		this.id = id;
	}

    public void setUsername(String username){
        this.username = username;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPostID(String postID){
        this.postID = postID;
    }

    public void setParentID(String parentID){
        this.parentID = parentID;
    }

    public void setContent(String content){
        this.content = content;
    }
    
    public void setComments(List<Comment> comments) {
    	this.comments = comments;
    }
    
}
