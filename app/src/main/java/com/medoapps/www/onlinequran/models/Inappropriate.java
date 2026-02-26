package com.medoapps.www.onlinequran.models;

import com.google.firebase.database.IgnoreExtraProperties;


// [START comment_class]
@IgnoreExtraProperties
public class Inappropriate {

    public String id;
    public String uid;
    public InappropriateType inappropriateType;
    public String commentId;
    public String postId;
    public String authorName;
    public String autherPhoto;
    public Boolean isReviewed;
    public Boolean isDeleted;

    public Inappropriate() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Inappropriate(String id, String uid, InappropriateType inappropriateType, String commentId, String postId, String authorName, String autherPhoto) {
        this.id = id;
        this.uid = uid;
        this.inappropriateType = inappropriateType;
        this.commentId = commentId;
        this.postId = postId;
        this.authorName = authorName;
        this.autherPhoto = autherPhoto;
    }

    public Inappropriate(String id, String uid, InappropriateType inappropriateType, String commentId, String postId, String authorName, String autherPhoto, Boolean isReviewed, Boolean isDeleted) {
        this.id = id;
        this.uid = uid;
        this.inappropriateType = inappropriateType;
        this.commentId = commentId;
        this.postId = postId;
        this.authorName = authorName;
        this.autherPhoto = autherPhoto;
        this.isReviewed = isReviewed;
        this.isDeleted = isDeleted;
    }
}
// [END comment_class]
