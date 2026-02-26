package com.medoapps.www.onlinequran.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class Comment {

    public String id;
    public String uid;
    public String postId;
    public PostType postType;
    public String author;
    public String text;
    public String autherPhoto;
    public String[] oldText;
    public Boolean isEdited;
    public Boolean isReported;
    public Boolean isDeleted;
    public int reportsNumber;


    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String uid, String author, String text , String autherPhoto) {
        this.uid = uid;
        this.author = author;
        this.text = text;
        this.autherPhoto = autherPhoto;
    }


    public Comment(String id, String uid, String postId, PostType postType, String author, String text, String autherPhoto) {
        this.id = id;
        this.uid = uid;
        this.postId = postId;
        this.postType = postType;
        this.author = author;
        this.text = text;
        this.autherPhoto = autherPhoto;
    }


    public Comment(String id, String uid, String postId, PostType postType, String author, String text, String autherPhoto, String[] oldText, Boolean isEdited, Boolean isReported, Boolean isDeleted, int reportsNumber) {
        this.id = id;
        this.uid = uid;
        this.postId = postId;
        this.postType = postType;
        this.author = author;
        this.text = text;
        this.autherPhoto = autherPhoto;
        this.oldText = oldText;
        this.isEdited = isEdited;
        this.isReported = isReported;
        this.isDeleted = isDeleted;
        this.reportsNumber = reportsNumber;
    }
}
// [END comment_class]
