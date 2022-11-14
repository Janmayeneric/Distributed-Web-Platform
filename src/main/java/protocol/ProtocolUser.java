package protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import object.User;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "userList", itemRelation = "user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolUser {

    public String id;
    public String username;
    public long createdTime;
    public String description;
    public String profileImageURL;

    public ProtocolUser() {}

    public ProtocolUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.createdTime = System.currentTimeMillis();
        this.description = "";
        this.profileImageURL = "";
    }

    public User toUser() {
        User user = new User(this.username, null);
        user.setId(this.id);
        return user;
    }

}
