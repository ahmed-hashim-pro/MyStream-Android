package com.medoapps.www.onlinequran.models;

import android.os.Build;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;


import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {

    public String id;
    public String uid;
    public String author;
    public String title;
    public String body;
    public String attachment;
    public int starCount = 0;
    public int viewCount = 0;
    public int foundedCount = 0;
    public  String profilePhoto;
    public String Upload_Type;
    public PostType postType;
    public String YouTubeVideoId;
    public String Thumb_Url;
    public Boolean isDeleted;
    public Map<String, Boolean> stars = new HashMap<>();
    public Map<String, Boolean> founded = new HashMap<>();
    public Long createdAt;
    public boolean isReported;
    public int reportsNumber;





    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String id,String uid, String author, String title, String body,String profilePhoto,String attachment ,String Upload_Type ,PostType postType,String YouTubeVideoId,String Thumb_Url,Long createdAt ) {
        this.id = id;
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.profilePhoto = profilePhoto;
        this.attachment = attachment;
        this.Upload_Type = Upload_Type;
        this.postType = postType;
        this.YouTubeVideoId = YouTubeVideoId;
        this.Thumb_Url = Thumb_Url;
        this.createdAt = createdAt;
    }

    public Post(String id, String uid, String author, String title, String body, String attachment, int starCount, int viewCount, int foundedCount, String profilePhoto, String upload_Type, PostType postType, String youTubeVideoId, String thumb_Url, Boolean isDeleted, Map<String, Boolean> stars, Map<String, Boolean> founded, Long createdAt, boolean isReported, int reportsNumber) {
        this.id = id;
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.attachment = attachment;
        this.starCount = starCount;
        this.viewCount = viewCount;
        this.foundedCount = foundedCount;
        this.profilePhoto = profilePhoto;
        Upload_Type = upload_Type;
        this.postType = postType;
        YouTubeVideoId = youTubeVideoId;
        Thumb_Url = thumb_Url;
        this.isDeleted = isDeleted;
        this.stars = stars;
        this.founded = founded;
        this.createdAt = createdAt;
        this.isReported = isReported;
        this.reportsNumber = reportsNumber;
    }

    public Post(String uid, String author, String title, String body, String profilePhoto, String attachment , String Upload_Type , String Thumb_Url, Boolean isDeleted ) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.profilePhoto = profilePhoto;
        this.attachment = attachment;
        this.Upload_Type = Upload_Type;
        this.Thumb_Url = Thumb_Url;
        this.isDeleted = isDeleted;
    }

    public Post(String uid, String author, String title, String body,String profilePhoto ,String Thumb_Url ) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.profilePhoto = profilePhoto;
        this.Thumb_Url = Thumb_Url;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("profilePhoto", profilePhoto);
        result.put("starCount", starCount);
        result.put("viewCount", viewCount);
        result.put("foundedCount",foundedCount);
        result.put("stars", stars);
        result.put("founded",founded);
        result.put("attachment",attachment);
        result.put("Upload_Type",Upload_Type);
        result.put("postType",postType);
        result.put("YouTubeVideoId",YouTubeVideoId);
        result.put("Thumb_Url",Thumb_Url);
        result.put("createdAt",createdAt);

        return result;
    }

    @Exclude
    public Post fromMap(Map<String, Object> map) {
//        HashMap<String, Object> result = new HashMap<>();
        Post model = new Post();

        model.id = (String) map.get("id");
        model.uid = (String) map.get("uid");
        model.author = (String) map.get("author");
        model.title = (String) map.get("title");
        model.body = (String) map.get("body");
        model.profilePhoto = (String) map.get("profilePhoto");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            model.starCount = Math.toIntExact((Long) map.get("starCount"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            model.viewCount = Math.toIntExact(((Long) map.get("viewCount")));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            model.foundedCount = Math.toIntExact((Long) map.get("foundedCount"));
        }
        model.stars = (Map<String, Boolean>) map.get("stars");
        model.founded = (Map<String, Boolean>) map.get("founded");
        model.attachment = (String) map.get("attachment");
        model.Upload_Type = (String) map.get("Upload_Type");
        model.postType = PostType.valueOf((String) map.get("postType"));
        model.YouTubeVideoId = (String) map.get("YouTubeVideoId");
        model.Thumb_Url = (String) map.get("Thumb_Url");
        model.createdAt = (Long) map.get("createdAt");

        return model;
    }


    // [END post_to_map]

}
// [END post_class]
