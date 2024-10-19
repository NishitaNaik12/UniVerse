package com.example.universe;

public class Post {
    private String userId;
    private String content; // For text posts
    private String imageUrl; // For image posts
    private String caption;  // For image captions
    private long timestamp;

    public Post() {
        // Required for Firebase
    }

    public Post(String userId, String content, String imageUrl, String caption, long timestamp) {
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
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
}

