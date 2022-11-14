package object;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;;


@Entity
public class Community {
	
	private @Id @GeneratedValue String id;
	private String name;
	private String topic;
	private String genre;
	private long numOfMembers;
	private long numOfPosts;
	
	public Community() {
		
	}
	
	public Community(String name, String topic, String genre) {
		this.name = name;
		this.topic = topic;
		this.genre = genre;
	}

	public String getId() {
		return this.id;
	}
	
	public long getNumOfMembers() {
		return this.numOfMembers;
	}
	
	public long getNumOfPosts() {
		return this.numOfPosts;
	}
	
	public void setNumOfPosts(int numOfPosts) {
		this.numOfPosts = numOfPosts;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getTopic() {
		return this.topic;
	}
	
	public String getGenre() {
		return this.genre;
	}
	
	public void setNumOfMembers(long num) {
		this.numOfMembers = num;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setId(String id) {
		this.id = id;
	}
}
