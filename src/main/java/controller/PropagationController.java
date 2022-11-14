package controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.*;
import object.Comment;
import object.Community;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bson.internal.Base64;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import protocol.*;
import repository.CommentRepository;
import repository.CommunityRepository;
import repository.PostRepository;
import repository.UserRepository;
import object.Post;
import object.User;

@EnableScheduling
@RestController
@RequestMapping("/api")
public class PropagationController {

    private static PropagationController instance;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;

    private String publicKey;
    private PrivateKey privateKey;

    private final List<String> serverList;

    private static final Pattern keyIdRegex = Pattern.compile("keyId=(.+);");
    private static final Pattern keyRegex = Pattern.compile("sig\\d+=:([a-zA-Z0-9+/=]+):");

    PrintStream log;

    public PropagationController(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository, CommunityRepository communityRepository) throws Exception {
        instance = this;

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.communityRepository = communityRepository;

        this.serverList = readServerList();
        this.loadKeys();

        this.log = new PrintStream(new FileOutputStream("protocol.log"));
    }

    public static PropagationController getInstance() {
        return instance;
    }

    private static List<String> readServerList() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("./server_list.txt"));

        List<String> result = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) result.add(line);
        }

        return result;
    }

    private static String readAll(InputStream in) {
        return new Scanner(in).useDelimiter("\\A").next();
    }

    private void loadPrivateKey() throws Exception {
        PEMParser parser = new PEMParser(new FileReader("./private"));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = parser.readObject();
        this.privateKey = converter.getKeyPair((PEMKeyPair) object).getPrivate();
    }

    private static PublicKey loadPublicKey(String input) throws IOException {
        PEMParser parser = new PEMParser(new StringReader(input));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = parser.readObject();
        return converter.getPublicKey((SubjectPublicKeyInfo)object);
    }

    private void loadKeys() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        this.publicKey = readAll(new FileInputStream("public"));
        this.loadPrivateKey();
    }

    private static Signature makeSignature(Object key, String requestTarget, String date, String userId) throws Exception {
        Signature sig = Signature.getInstance("SHA512withRSAandMGF1");
        sig.setParameter(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 20, 1));

        if (key instanceof PrivateKey) {
            sig.initSign((PrivateKey)key);
        } else {
            sig.initVerify((PublicKey)key);
        }

        String content = "*request-target: " + requestTarget + "\ncurrent-date: " + date + "\nuser-id: " + userId;
        sig.update(content.getBytes());

        return sig;
    }

    public String protocolRequest(String host, String method, String target) throws Exception {
        return protocolRequest(host, method, target, "-1", null);
    }

    public String protocolRequest(String host, String method, String target, String userId, Object postData) throws Exception {
        String date = Long.toString(System.currentTimeMillis());

        String signature = Base64.encode(makeSignature(privateKey, method.toLowerCase() + ' ' + target, date, userId).sign());

        URL url = new URL(host + target);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty("signature-input", "sig1=(*request-target, current-date, user-id); "
                + "keyId=https://cs3099user-b10.host.cs.st-andrews.ac.uk/api/key; alg=RSASSA-PSS-SHA512");
        connection.setRequestProperty("signature", "sig1=:" + signature + ":");
        connection.setRequestProperty("current-date", date);
        connection.setRequestProperty("user-id", userId);

        try {
            if (postData != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                new ObjectMapper().writeValue(connection.getOutputStream(), postData);
            }

            return readAll(connection.getInputStream());
        } catch (IOException e) {
            return readAll(connection.getErrorStream());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class UserList {
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Inner {
            public List<ProtocolUser> userList;
        }
        public Inner _embedded;

        public List<ProtocolUser> get() {
            return _embedded.userList;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ForumList {
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Inner {
            public List<ProtocolForum> forumList;
        }
        public Inner _embedded;

        public List<ProtocolForum> get() {
            return _embedded.forumList;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SubforumList {
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Inner {
            public List<ProtocolSubforum> subforumList;
        }
        public Inner _embedded;

        public List<ProtocolSubforum> get() {
            return _embedded.subforumList;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PostList {
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Inner {
            public List<ProtocolPost> postList;
        }
        public Inner _embedded;

        public List<ProtocolPost> get() {
            return _embedded.postList;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class CommentList {
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Inner {
            public List<ProtocolComment> commentList;
        }
        public Inner _embedded;

        public List<ProtocolComment> get() {
            return _embedded.commentList;
        }
    }

    void assembleComments(String server, String postId, List<ProtocolComment> source, String parentId) throws Exception {
        for (ProtocolComment pc : source) {
            Comment comment = pc.toComment();
            comment.setParentID(parentId);

            String json = protocolRequest(server, "GET", "/api/comments/" + comment.getId() + "/comments");
            List<ProtocolComment> children = new ObjectMapper().readValue(json, CommentList.class).get();

            assembleComments(server, postId, children, comment.getId());

            commentRepository.save(comment);
        }
    }

    void assemblePost(String server, ProtocolPost source, String prefix) throws Exception {
        Post post = source.toPost(server);

        post.setTitle(prefix + post.getTitle());
        post.setCommunity("global");

        String json = protocolRequest(server, "GET", "/api/posts/" + post.getId() + "/comments");
        List<ProtocolComment> comments = new ObjectMapper().readValue(json, CommentList.class).get();

        assembleComments(server, post.getId(), comments, null);

        postRepository.save(post);
    }

    private void updateServer(String server) throws Exception {
        String json = protocolRequest(server, "GET", "/api/users");
        new ObjectMapper().readValue(json, UserList.class).get().stream()
                .map(ProtocolUser::toUser)
                .forEach(userRepository::save);

        json = protocolRequest(server, "GET", "/api/forums");
        List<ProtocolForum> forums = new ObjectMapper().readValue(json, ForumList.class).get();

        for (ProtocolForum forum : forums) {
            json = protocolRequest(server, "GET", "/api/forums/" + forum.id + "/subforums");
            List<ProtocolSubforum> subforums = new ObjectMapper().readValue(json, SubforumList.class).get();

            for (ProtocolSubforum sub : subforums) {
                String prefix = '[' + forum.forumName + '/' + sub.subforumName + "] ";

                json = protocolRequest(server, "GET", "/api/subforums/" + sub.id + "/posts");
                List<ProtocolPost> posts = new ObjectMapper().readValue(json, PostList.class).get();

                for (ProtocolPost post : posts) {
                    assemblePost(server, post, prefix);
                }
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    public void update() throws Exception {
        for (String server : serverList) try {
            updateServer(server);
            log.println("Successfully updated from " + server);
        } catch (Exception e) {
            log.println("Error getting updates from " + server);
        }
        log.flush();
    }

    static class SendComment {
        public SendComment(String commentContent, String userId, String username) {
            this.commentContent = commentContent;
            this.userId = userId;
            this.username = username;
        }

        public String commentContent;
        public String userId;
        public String username;
    }

    Comment postComment(String server, Comment comment) throws Exception {
        SendComment send = new SendComment(comment.getContent(), comment.getUserId(), comment.getUsername());

        String endpoint = comment.getParentID() == null ?
                "/api/posts/" + comment.getPostID() + "/comments" :
                "/api/comments/" + comment.getParentID() + "/comments";
        String json = protocolRequest(server, "POST", endpoint, comment.getUserId(), send);

        return new ObjectMapper().readValue(json, ProtocolComment.class).toComment();
    }

    static class SendVote {
        public SendVote(Boolean vote) {
            this.isUpvote = vote;
        }

        public Boolean isUpvote;
    }

    void sendVote(String server, String postId, String userId, Boolean vote) throws Exception {
        protocolRequest(server, "PUT", "/api/posts/" + postId + "/vote", userId, new SendVote(vote));
    }

    private static String getHeaderValue(HttpHeaders headers, String key) {
        List<String> values = headers.get(key);
        if (values == null) throw new InvalidSignatureException("No header value: " + key);
        return values.get(0);
    }

    static class PublicKeyInput {
        public String key;
        public String get() {
            return key;
        }
    }

    private String verifySignature(HttpHeaders headers, String request) {
        String sigInput = getHeaderValue(headers, "signature-input");
        String signature = getHeaderValue(headers, "signature");
        String date = getHeaderValue(headers, "current-date");
        String userId = getHeaderValue(headers, "user-id");

        Matcher m = keyRegex.matcher(signature);
        if (!m.matches()) throw new InvalidSignatureException("Invalid sig");
        signature = m.group(1);

        m = keyIdRegex.matcher(sigInput);
        if (!m.find()) throw new InvalidSignatureException("Invalid keyId");

        try {
            PublicKeyInput keyInput = new ObjectMapper().readValue(new URL(m.group(1)), PublicKeyInput.class);
            PublicKey key = loadPublicKey(keyInput.get());

            Signature verifier = makeSignature(key, request, date, userId);
            if (verifier.verify(Base64.decode(signature))) return userId;
        } catch (Exception e) {
            throw new InvalidSignatureException(e.getMessage());
        }

        throw new InvalidSignatureException("Failed to verify");
    }

    private EntityModel<ProtocolUser> toModel(User user) {
        return EntityModel.of(new ProtocolUser(user),
                linkTo(methodOn(PropagationController.class).getUser(null, user.getId())).withSelfRel(),
                linkTo(methodOn(PropagationController.class).getUsers(null)).withRel("users"));
    }

    private EntityModel<ProtocolPost> toModel(Post post) {
        return EntityModel.of(new ProtocolPost(post),
                linkTo(methodOn(PropagationController.class).getPost(null, post.getId())).withSelfRel(),
                linkTo(methodOn(PropagationController.class).getForum(null, ProtocolForum.ID)).withRel("forum"),
                linkTo(methodOn(PropagationController.class).getPosts(null, post.getCommunity())).withRel("subforum"),
                linkTo(methodOn(PropagationController.class).getUser(null, post.getUserId())).withRel("user"));
    }

    private EntityModel<ProtocolForum> toModel(ProtocolForum forum) {
        return EntityModel.of(forum,
                linkTo(methodOn(PropagationController.class).getForum(null, forum.id)).withSelfRel(),
                linkTo(methodOn(PropagationController.class).getForums(null)).withRel("forums"),
                linkTo(methodOn(PropagationController.class).getCommunities(null, forum.id)).withRel("subforums"));
    }

    private EntityModel<ProtocolSubforum> toModel(Community community) {
        return EntityModel.of(new ProtocolSubforum(community),
                linkTo(methodOn(PropagationController.class).getCommunity(null, community.getId())).withSelfRel(),
                linkTo(methodOn(PropagationController.class).getForum(null, ProtocolForum.ID)).withRel("form"),
                linkTo(methodOn(PropagationController.class).getPosts(null, community.getId())).withRel("posts"));
    }

    private EntityModel<ProtocolComment> toModel(Comment comment, Post post) {
        return EntityModel.of(new ProtocolComment(comment),
                linkTo(methodOn(PropagationController.class).getComment(null, comment.getId())).withSelfRel(),
                linkTo(methodOn(PropagationController.class).getCommunity(null, post.getCommunity())).withRel("subforum"),
                linkTo(methodOn(PropagationController.class).getForum(null, ProtocolForum.ID)).withRel("forum"),
                linkTo(methodOn(PropagationController.class).getUser(null, comment.getUserId())).withRel("user"),
                linkTo(methodOn(PropagationController.class).getChildComments(null, comment.getId())).withRel("childComments"));
    }

    @GetMapping("/users/{id}")
    public EntityModel<ProtocolUser> getUser(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/users/" + id);

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return toModel(user);
    }

    @GetMapping("/users/{id}/posts")
    public CollectionModel<EntityModel<ProtocolPost>> getUserPosts(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/users/" + id + "/posts");

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        List<EntityModel<ProtocolPost>> posts = postRepository.findByUsername(user.getUsername()).stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(posts, linkTo(methodOn(PropagationController.class).getUserPosts(null, id)).withSelfRel());
    }

    @GetMapping("/users/{id}/comments")
    public CollectionModel<EntityModel<ProtocolComment>> getUserComments(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/users/" + id + "/comments");

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        List<Comment> comments = commentRepository.findByUserId(user.getId());

        List<EntityModel<ProtocolComment>> result = new ArrayList<>();
        for (Comment comment : comments) try {
            Post post = postRepository.findById(comment.getPostID()).orElseThrow(() -> new PostNotFoundException(comment.getPostID()));
            result.add(toModel(comment, post));
        } catch (Exception ignored) {}

        return CollectionModel.of(result, linkTo(methodOn(PropagationController.class).getUserComments(null, id)).withSelfRel());
    }

    @GetMapping("/users")
    public CollectionModel<EntityModel<ProtocolUser>> getUsers(@RequestHeader HttpHeaders headers) {
        this.verifySignature(headers, "get /api/users");

        List<EntityModel<ProtocolUser>> users = userRepository.findAll().stream()
                .filter(user -> user.getPassword() != null)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(PropagationController.class).getUsers(null)).withSelfRel());
    }

    @GetMapping("/forums/{id}")
    public EntityModel<ProtocolForum> getForum(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/forums/" + id);

        if (!id.equals(ProtocolForum.ID)) throw new CommunityNotFoundException(id);

        return toModel(new ProtocolForum());
    }

    @GetMapping("/forums")
    public CollectionModel<EntityModel<ProtocolForum>> getForums(@RequestHeader HttpHeaders headers) {
        this.verifySignature(headers, "get /api/forums");

        List<EntityModel<ProtocolForum>> forums = new ArrayList<>();
        forums.add(toModel(new ProtocolForum()));

        return CollectionModel.of(forums, linkTo(methodOn(PropagationController.class).getForums(null)).withSelfRel());
    }

    @GetMapping("/subforums/{id}")
    public EntityModel<ProtocolSubforum> getCommunity(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/subforums/" + id);

        Community community = communityRepository.findById(id).orElseThrow(() -> new CommunityNotFoundException(id));
        return toModel(community);
    }

    @GetMapping("/forums/{id}/subforums")
    public CollectionModel<EntityModel<ProtocolSubforum>> getCommunities(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/forums/" + id + "/subforums");

        List<EntityModel<ProtocolSubforum>> communities = communityRepository.findAll().stream()
                .filter(community -> !community.getName().equals("global"))
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(communities, linkTo(methodOn(PropagationController.class).getCommunities(null, id)).withSelfRel());
    }

    static class VoteRequest {
        public boolean isUpvote;
    }

    @PutMapping("/posts/{id}/vote")
    public void votePost(@RequestHeader HttpHeaders headers, @PathVariable String id, @RequestBody VoteRequest value) {
        String userId = this.verifySignature(headers, "put /api/posts/" + id + "/vote");

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        if (value == null) {
            if (user.removeVoteUp(id)) post.removeVoteUp();
            if (user.removeVoteDown(id)) post.removeVoteDown();
        } else if (value.isUpvote) {
            if (user.addVoteUp(id)) post.addVoteUp();
        } else {
            if (user.addVoteDown(id)) post.addVoteDown();
        }

        userRepository.save(user);
        postRepository.save(post);
    }

    @GetMapping("/posts/{id}")
    public EntityModel<ProtocolPost> getPost(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/posts/" + id);

        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        return toModel(post);
    }

    @PutMapping("/posts/{id}")
    public EntityModel<ProtocolPost> updatePost(@RequestHeader HttpHeaders headers, @PathVariable String id, @RequestBody ProtocolPost pp) {
        this.verifySignature(headers, "put /api/posts/" + id);

        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        post.setTitle(pp.postTitle);
        post.setContent(pp.postContents);

        post = postRepository.save(post);
        return toModel(post);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "delete /api/posts/" + id);
        postRepository.deleteById(id);
    }

    @GetMapping("/subforums/{id}/posts")
    public CollectionModel<EntityModel<ProtocolPost>> getPosts(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/subforums/" + id + "/posts");

        Community community = communityRepository.findById(id).orElseThrow(() -> new CommunityNotFoundException(id));

        List<EntityModel<ProtocolPost>> posts = postRepository.findAll().stream()
                .filter(post -> post.getCommunity().equals(community.getName()))
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(posts, linkTo(methodOn(PropagationController.class).getPosts(null, id)).withSelfRel());
    }

    @PostMapping("/subforums/{id}/posts")
    public EntityModel<ProtocolPost> createPost(@RequestHeader HttpHeaders headers, @PathVariable String id, @RequestBody ProtocolPost pp) {
        this.verifySignature(headers, "post /api/subforums/" + id + "/posts");

        Community community = communityRepository.findById(id).orElseThrow(() -> new CommunityNotFoundException(id));

        Post post = pp.toPost(null);

        long time = System.currentTimeMillis();
        post.setTimeOfCreation(time);
        post.setlastestUpdateTime(time);
        post.setCommunity(community.getName());

        post = postRepository.save(post);
        return toModel(post);
    }

    @GetMapping("/comments/{id}")
    public EntityModel<ProtocolComment> getComment(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/comments/" + id);

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Post post = postRepository.findById(comment.getPostID()).orElseThrow(() -> new PostNotFoundException(comment.getPostID()));

        return toModel(comment, post);
    }

    @PutMapping("/comments/{id}")
    public EntityModel<ProtocolComment> updateComment(@RequestHeader HttpHeaders headers, @PathVariable String id, @RequestBody ProtocolComment updated) {
        this.verifySignature(headers, "put /api/comments/" + id);

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Post post = postRepository.findById(comment.getPostID()).orElseThrow(() -> new PostNotFoundException(comment.getPostID()));

        comment.setContent(updated.commentContent);

        commentRepository.save(comment);
        return toModel(comment, post);
    }

    @DeleteMapping("/comments/{id}")
    public void deleteComment(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "delete /api/comments/" + id);

        commentRepository.deleteById(id);
    }

    @GetMapping("/posts/{id}/comments")
    public CollectionModel<EntityModel<ProtocolComment>> getComments(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/posts/" + id + "/comments");

        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        List<Comment> comments = commentRepository.findByPostIDAndParentIDIsNullOrderByTimeOfCreationAsc(id);
        List<EntityModel<ProtocolComment>> result = comments.stream()
                .map(comment -> toModel(comment, post))
                .collect(Collectors.toList());

        return CollectionModel.of(result, linkTo(methodOn(PropagationController.class).getComments(null, id)).withSelfRel());
    }

    @PostMapping("/posts/{id}/comments")
    public EntityModel<ProtocolComment> createComment(@RequestHeader HttpHeaders headers, @RequestBody ProtocolComment pc, @PathVariable String id) {
        this.verifySignature(headers, "post /api/posts/" + id + "/comments");

        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        Comment comment = pc.toComment();
        comment.setPostID(id);
        comment.setTimeOfCreation(System.currentTimeMillis());
        comment = commentRepository.save(comment);

        return toModel(comment, post);
    }

    @PutMapping("/comments/{id}/vote")
    public ResponseEntity<?> voteComment() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/comments/{id}/comments")
    public CollectionModel<EntityModel<ProtocolComment>> getChildComments(@RequestHeader HttpHeaders headers, @PathVariable String id) {
        this.verifySignature(headers, "get /api/comments/" + id + "/comments");

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Post post = postRepository.findById(comment.getPostID()).orElseThrow(() -> new PostNotFoundException(comment.getPostID()));

        List<Comment> comments = commentRepository.findByParentIDOrderByTimeOfCreationAsc(id);
        List<EntityModel<ProtocolComment>> result = comments.stream()
                .map(c -> toModel(c, post))
                .collect(Collectors.toList());

        return CollectionModel.of(result, linkTo(methodOn(PropagationController.class).getChildComments(null, id)).withSelfRel());
    }

    @PostMapping("/comments/{id}/comments")
    public EntityModel<ProtocolComment> createChildComment(@RequestHeader HttpHeaders headers, @PathVariable String id, @RequestBody ProtocolComment pc) {
        this.verifySignature(headers, "post /api/comments/" + id + "/comments");

        Comment parent = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Post post = postRepository.findById(parent.getPostID()).orElseThrow(() -> new PostNotFoundException(parent.getPostID()));

        Comment comment = pc.toComment();
        comment.setPostID(post.getId());
        comment.setParentID(id);
        comment.setTimeOfCreation(System.currentTimeMillis());
        comment = commentRepository.save(comment);

        return toModel(comment, post);
    }

    @GetMapping(value = "/key", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPublicKey() {
        return this.publicKey;
    }

}
