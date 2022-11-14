package object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import enumeration.Identity;

/*
 * Entity of account itself, id is auto generated,
 * name and password is set by users
 */
@Entity
public class User {

	private @Id @GeneratedValue String id; // auto generated an id for mongoDB to use
	private String username;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	private Identity identity;

	private List<String> previousUsernames;

	private Set<String> votesUp;
	private Set<String> votesDown;

	private List<Notification> notifications;
	
	public User() {}
	
	public User(String username, String password){
		this.username = username;
		this.password = password;

		this.initialise();
	}
	
	public void initialise() {
		this.identity = Identity.USER;

		this.previousUsernames = new ArrayList<>();

		this.votesUp = new HashSet<>();
		this.votesDown = new HashSet<>();

		this.notifications = new ArrayList<>();
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getUsername() {
		return this.username;
	}

	@JsonIgnore
	public String getPassword() {
		return this.password;
	}
	
	public Identity getIdentity() {
		return this.identity;
	}

	public List<String> getPreviousUsernames() {
		return previousUsernames;
	}

	public Set<String> getVotesUp() {
		return votesUp;
	}
	

	public Set<String> getVotesDown() {
		return votesDown;
	}


	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setVotesUp(Set<String> votesUp) {
		this.votesUp = votesUp;
	}

	public void setVotesDown(Set<String> votesDown) {
		this.votesDown = votesDown;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public void setPreviousUsernames(List<String> previousUsernames) {
		this.previousUsernames = previousUsernames;
	}

	public void rename(String newName) {
		this.previousUsernames.add(this.username);
		this.username = newName;
	}

	public boolean addVoteUp(String id) {
		return votesUp.add(id);
	}

	public boolean removeVoteUp(String id) {
		return votesUp.remove(id);
	}

	public boolean addVoteDown(String id) {
		return votesDown.add(id);
	}

	public boolean removeVoteDown(String id) {
		return votesDown.remove(id);
	}

	public void addNotification(String content) {
		this.notifications.add(new Notification(content));
	}
	
}
