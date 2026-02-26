package com.medoapps.www.onlinequran.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class Reporting {

    public String id;
    public String uid;
    public ReportingType reportingType;
    public String commentId;
    public String postId;
    public String authorName;
    public String autherPhoto;
    public String text;
    public Long createdAt;
    public Boolean isReviewed;
    public Boolean isDeleted;


    public Reporting() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Reporting(String id, String uid, ReportingType reportingType, String commentId, String postId, String authorName, String autherPhoto, String text, Long createdAt) {
        this.id = id;
        this.uid = uid;
        this.reportingType = reportingType;
        this.commentId = commentId;
        this.postId = postId;
        this.authorName = authorName;
        this.autherPhoto = autherPhoto;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Reporting(String id, String uid, ReportingType reportingType, String commentId, String postId, String authorName, String autherPhoto, String text, Boolean isReviewed, Boolean isDeleted) {
        this.id = id;
        this.uid = uid;
        this.reportingType = reportingType;
        this.commentId = commentId;
        this.postId = postId;
        this.authorName = authorName;
        this.autherPhoto = autherPhoto;
        this.text = text;
        this.isReviewed = isReviewed;
        this.isDeleted = isDeleted;
    }
}
// [END comment_class]
