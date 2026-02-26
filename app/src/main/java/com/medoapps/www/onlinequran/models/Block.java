package com.medoapps.www.onlinequran.models;

import com.google.firebase.database.IgnoreExtraProperties;


// [START comment_class]
@IgnoreExtraProperties
public class Block {

    public String id;
    public String uid;
    public String blockedUserId;
    public String commentId;
    public String postId;
    public Boolean isReviewed;
    public Boolean isDeleted;

    public Block() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Block(String id, String uid, String blockedUserId, String commentId, String postId, Boolean isReviewed, Boolean isDeleted) {
        this.id = id;
        this.uid = uid;
        this.blockedUserId = blockedUserId;
        this.commentId = commentId;
        this.postId = postId;
        this.isReviewed = isReviewed;
        this.isDeleted = isDeleted;
    }
}
// [END comment_class]
