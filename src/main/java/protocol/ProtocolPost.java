package protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import object.Post;
import org.springframework.hateoas.server.core.Relation;

import java.util.ArrayList;
import java.util.List;

@Relation(collectionRelation = "postList", itemRelation = "post")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolPost {

    public String id;
    public String postTitle;
    public String postContents;
    public long createdTime;
    public long modifiedTime;
    public String userId;
    public String username;
    public String subforumId;
    public int downvotes;
    public int upvotes;
    public String postType;
    public List<ProtocolVote> _userVotes;

    public ProtocolPost() {}

    public ProtocolPost(Post post) {
        this.id = post.getId();
        this.postTitle = post.getTitle();
        this.postContents = post.getContent();
        this.createdTime = post.getlastestUpdateTime();
        this.modifiedTime = post.getlastestUpdateTime();
        this.userId = post.getUserId();
        this.username = post.getUsername();
        this.subforumId = post.getCommunity();
        this.downvotes = post.getVotesDown();
        this.upvotes = post.getVotesUp();
        this.postType = "text";
        this._userVotes = new ArrayList<>();
    }

    public Post toPost(String server) {
        Post post = new Post(userId, username, subforumId, postTitle, postContents, createdTime, null, modifiedTime, null);

        post.setId(id);
        post.setServer(server);

        for (int i = 0; i < downvotes; ++i) post.addVoteDown();
        for (int i = 0; i < upvotes; ++i) post.addVoteUp();

        return post;
    }

}
