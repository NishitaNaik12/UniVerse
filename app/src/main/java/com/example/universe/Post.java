package com.example.universe;

import java.util.HashMap;
import java.util.Map;

public class Post {
    private String postId;
    private String userId;
    private String content; // For text posts
    private String imageUrl; // For image posts
    private String caption;  // For image captions
    private long timestamp;
    private int likeCount;
    private Map<String, Boolean> likes = new HashMap<>();
    private String userImageUrl;
    public Post() {
        // Required for Firebase
    }

    public Post(String userId, String content, String imageUrl, String caption, long timestamp) {
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
        this.userImageUrl = userImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }
    public String getUserImageUrl() { // Add this method
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) { // Add this method
        this.userImageUrl = userImageUrl;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }
}

