package com.example.universe;

import java.util.UUID;

public class Event {
    private String name;
    private String id;
    private String date;
    private String imageUrl;

    public Event() {
        this.id = UUID.randomUUID().toString();
    } // Needed for Firebase

    public Event(String name, String date, String imageUrl) {
        this.name = name;
        this.date = date;
        this.imageUrl = imageUrl;
        this.id = UUID.randomUUID().toString();;
    }
    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    public String getDate() {
        return date;
    }

//    public void setDate(String date) {
//        this.date = date;
//    }

    public String getImageUrl() {
        return imageUrl;
    }

//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }
}

