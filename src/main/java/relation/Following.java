package relation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

import org.springframework.data.annotation.Id;

@Entity
public class Following {
	private @Id @GeneratedValue String id;
	
	//	someone be followed
	private String userId;
	
	//	follower
	private String followerId;
	
	public Following(){
		
	}
	
	public Following(String followed, String following) {
		this.userId = followed;
		this.followerId = following;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setFollower(String followerId) {
		this.followerId = followerId; 
	}
	
	public String getUserId() {
		return this.userId;
	}
	
	public String getFollowerId() {
		return this.followerId;
	}
	
}
