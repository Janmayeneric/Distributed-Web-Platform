package protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "forumList", itemRelation = "forum")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolForum {

    public static String ID = "00000000-0000-0000-0000-000000000000";

    public String id;
    public String forumName;

    public ProtocolForum() {
        this.id = ID;
        this.forumName = "All";
    }

}
