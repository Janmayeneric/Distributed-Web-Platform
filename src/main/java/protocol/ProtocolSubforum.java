package protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import object.Community;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "subforumList", itemRelation = "subforum")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolSubforum {

    public String id;
    public String subforumName;
    public String forumId;

    public ProtocolSubforum() {}

    public ProtocolSubforum(Community community) {
        this.id = community.getId();
        this.subforumName = community.getName();
        this.forumId = ProtocolForum.ID;
    }

    public Community toCommunity() {
        Community community = new Community(subforumName, "", "");
        community.setId(this.id);
        return community;
    }

}
