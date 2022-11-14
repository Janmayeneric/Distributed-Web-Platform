package object;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Post {
	
	private @Id @GeneratedValue String id; // auto generated an id for mongoDB to use
	private String username;
	private String community;
    private String title;
    private String content;
    private long timeOfCreation;
    private String photoId;
    private long latestUpdateTime;
    private String userId;
    private String server;
    
    //	comment for the posting
    private List<Comment> comments;

    private int votesUp;
    private int votesDown;

    public Post() {
        // required by MongoDB
    }

    public Post(String userId, String username, String community, String title, String content, long timeOfCreation, List<Comment> comments,long latestUpdateTime,String photoId) {
        this.userId = userId;
        this.username = username;
        this.community = community;
        this.title = title;
        this.content = content;
        this.timeOfCreation = timeOfCreation;
        this.latestUpdateTime = latestUpdateTime;
        this.photoId = photoId;
        this.votesUp = 0;
        this.votesDown = 0;

    }
    
    // for the propagationController
    public Post(String userId, String community, String title, String content, long timeOfCreation, List<Comment> comments) {
    	this.userId = userId;
    	this.community = community;
    	this.title = title;
    	this.content = content;
    	this.timeOfCreation = timeOfCreation;
    	updateTime();
    }
    
    //Getters
    public String getId() {
		return this.id;
	}
    
    public List<Comment> getComments(){
    	return this.comments;
    }

    public String getUsername(){
        return this.username;
    }
    
    public int getVotesUp() {
        return this.votesUp;
    }

    public int getVotesDown() {
        return this.votesDown;
    }

    public int getNetVotes() {
        return this.votesUp - this.votesDown;
    }

    public float getVoteRatio() {
        return (float)this.votesUp / (float)this.votesDown;
    }

    public String getServer() {
        return this.server;
    }

    public void addVoteUp() {
        ++this.votesUp;
    }

    public void removeVoteUp() {
        --this.votesUp;
    }

    public void addVoteDown() {
        ++this.votesDown;
    }

    public void removeVoteDown() {
        --this.votesDown;
    }


    

    public String getCommunity(){
        return this.community;
    }
    
    public long getlastestUpdateTime() {
    	return this.latestUpdateTime;
    }

    public String getTitle(){
        return this.title;
    }

    public String getContent(){
        return this.content;
    }

    public String getUserId() {
    	return this.userId;
    }
    public long getTimeOfCreation(){
        return this.timeOfCreation;
    }
    
    public String getPhotoId() {
    	return this.photoId;
    }

    //Setters
    public void setId(String id) {
		this.id = id;
	}
    
    public void setUserId(String userId) {
    	this.userId = userId;
    }

    public void setUsername(String username){
        this.username = username;
    }
    
    public void setComments(List<Comment> comments) {
    	this.comments = comments;
    }
   

    public void setCommunity(String community){
        this.community = community;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setServer(String server) {
        this.server = server;
    }
    
    /**
     * this function is used to show last time the post is updated
     * create, new comment, will refresh the time
     */
    public void updateTime() {
    	this.latestUpdateTime = System.currentTimeMillis();
    }

    public void setlastestUpdateTime(long lastestUpdateTime) {
    	this.latestUpdateTime = lastestUpdateTime;
    }
    
    public void setTimeOfCreation(long timeOfCreation){
        this.timeOfCreation  = timeOfCreation;
    }
    
    public void setphotoID(String photoID) {
    	this.photoId = photoID;
    }
    
 

}
