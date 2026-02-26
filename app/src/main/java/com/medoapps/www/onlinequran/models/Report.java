package com.medoapps.www.onlinequran.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

// [START comment_class]
@IgnoreExtraProperties
public class Report {

    public String id;
    public String uid;
    public String userName;
    public String userPhoto;

    public ReportType reportType;

//    Reciter
    public String openReciterServer;
    public String openReciterName;

    public String shareReciterUrl;

//    Surah
    public String openSurahServer;
    public String openSurahName;

    public String shareSurahUrl;

//    Radio
    public String openRadioServer;
    public String openRadioName;

    public String shareRadioUrl;

//    YouTubePost
    public String openYouTubePostId;
    public String openYouTubePostTitle;
    public String shareYouTubePostUrl;
    public String starYouTubePostId;
    public String starYouTubePostTitle;
    public String unStarYouTubePostId;
    public String unStarYouTubePostTitle;

//    LocalPost
    public String openLocalPostId;
    public String openLocalPostTitle;
    public String shareLocalPostUrl;
    public String starLocalPostId;
    public String starLocalPostTitle;
    public String unStarLocalPostId;
    public String unStarLocalPostTitle;

//    commentYouTubePost
    public String commentYouTubePostCommentId;
    public String commentYouTubePostPostId;

//    commentLocalPost
    public String commentLocalPostCommentId;
    public String commentLocalPostPostId;

    public String text;
    public Long createdAt;
    public Boolean isDeleted;


    public Report() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String openReciterServer, String openReciterName, String shareReciterUrl, String text, Long createdAt) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.openReciterServer = openReciterServer;
        this.openReciterName = openReciterName;
        this.shareReciterUrl = shareReciterUrl;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String openSurahServer, String openSurahName, String shareSurahUrl,String openReciterServer, String openReciterName, String text, Long createdAt,String empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.openSurahServer = openSurahServer;
        this.openSurahName = openSurahName;
        this.shareSurahUrl = shareSurahUrl;
        this.openReciterServer = openReciterServer;
        this.openReciterName = openReciterName;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String openRadioServer, String openRadioName, String shareRadioUrl, String text, Long createdAt, int empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.openRadioServer = openRadioServer;
        this.openRadioName = openRadioName;
        this.shareRadioUrl = shareRadioUrl;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String openYouTubePostId, String openYouTubePostTitle, String shareYouTubePostUrl, String text, Long createdAt,Boolean empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.openYouTubePostId = openYouTubePostId;
        this.openYouTubePostTitle = openYouTubePostTitle;
        this.shareYouTubePostUrl = shareYouTubePostUrl;
        this.text = text;
        this.createdAt = createdAt;

    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String starYouTubePostId, String starYouTubePostTitle, String text, Long createdAt) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.starYouTubePostId = starYouTubePostId;
        this.starYouTubePostTitle = starYouTubePostTitle;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String unStarYouTubePostId, String unStarYouTubePostTitle, String text, Long createdAt,String[] empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.unStarYouTubePostId = unStarYouTubePostId;
        this.unStarYouTubePostTitle = unStarYouTubePostTitle;
        this.text = text;
        this.createdAt = createdAt;

    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String openLocalPostId, String openLocalPostTitle, String shareLocalPostUrl, String text, Long createdAt,int[] empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.openLocalPostId = openLocalPostId;
        this.openLocalPostTitle = openLocalPostTitle;
        this.shareLocalPostUrl = shareLocalPostUrl;
        this.text = text;
        this.createdAt = createdAt;

    }
    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String starLocalPostId, String starLocalPostTitle, String text, Long createdAt, boolean[] empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.starLocalPostId = starLocalPostId;
        this.starLocalPostTitle = starLocalPostTitle;
        this.text = text;
        this.createdAt = createdAt;

    }
    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String unStarLocalPostId, String unStarLocalPostTitle, String text, Long createdAt , HashMap empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.unStarLocalPostId = unStarLocalPostId;
        this.unStarLocalPostTitle = unStarLocalPostTitle;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType, String commentYouTubePostCommentId, String commentYouTubePostPostId, String text, Long createdAt,Long [] empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.commentYouTubePostCommentId = commentYouTubePostCommentId;
        this.commentYouTubePostPostId = commentYouTubePostPostId;
        this.text = text;
        this.createdAt = createdAt;
    }
    public Report(String id, String uid, String userName, String userPhoto, ReportType reportType,  String commentLocalPostCommentId, String commentLocalPostPostId, String text, Long createdAt ,Long empty) {
        this.id = id;
        this.uid = uid;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.reportType = reportType;
        this.commentLocalPostCommentId = commentLocalPostCommentId;
        this.commentLocalPostPostId = commentLocalPostPostId;
        this.text = text;
        this.createdAt = createdAt;
    }


}
// [END comment_class]
