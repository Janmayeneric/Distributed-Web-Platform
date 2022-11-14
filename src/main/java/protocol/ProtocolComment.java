package protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import object.Comment;
import org.springframework.hateoas.server.core.Relation;

import java.util.ArrayList;
import java.util.List;

@Relation(collectionRelation = "commentList", itemRelation = "comment")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolComment {

    public String id;
    public String commentContent;
    public long createdTime;
    public long modifiedTime;
    public String userId;
    public String username;
    public String postId;
    public long downvotes;
    public long upvotes;
    public List<ProtocolVote> _userVotes;

    public ProtocolComment() {}

    public ProtocolComment(Comment comment) {
        this.id = comment.getId();
        this.commentContent = comment.getContent();
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = System.currentTimeMillis();
        this.userId = comment.getUserId();
        this.username = comment.getUsername();
        this.postId = comment.getPostID();
        this.downvotes = 0;
        this.upvotes = 0;
        this._userVotes = new ArrayList<>();
    }

    public Comment toComment() {
        Comment comment = new Comment(this.username, this.userId, this.createdTime, this.postId, null, this.commentContent);
        comment.setId(this.id);
        return comment;
    }

}
