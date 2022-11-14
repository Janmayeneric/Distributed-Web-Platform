package object;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Token {
	
	private @Id @GeneratedValue String id; // auto generated an id for mongoDB to use
	private String userId;
    private String key;
    private long expiry;

    public Token() {
        // required by MongoDB
    }

    public Token(String userId,String key, long expiry) {
        this.key = key;
        this.expiry = expiry;
        this.userId = userId;
    }
    
    public String getUserId() {
    	return this.userId;
    }
    
    public void setUserId(String userId) {
    	this.userId = userId;
    }

    public String getKey() {
        return this.key;
    }
    

    public void setToken(String key) {
        this.key = key;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

}
