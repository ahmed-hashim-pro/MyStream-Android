package com.medoapps.www.onlinequran.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.medoapps.www.onlinequran.model.Message;
import com.medoapps.www.onlinequran.model.Status;

import java.util.HashMap;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {


    public String id;
    public String username;
    public String email;
    public String firstname;
    public String lastname;
    public String photourl;
    public String avata;
    public Boolean CanPost;
    public Boolean ProfileCompleted;
    public UserTypes UserType;
    public Status status;
    public Message message;
    public Long createdAt;
    public Long updatedAt;
    public String continent_code;
    public String continent_name;
    public String country_code;
    public String country_name;
    public String region_code;
    public String region_name;
    public String city;
    public String FCMToken;
    public Boolean isSubscribedPremium;





    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }


    public User(String id, String username, String email, String firstname, String lastname, String photourl, String avata, Boolean canPost, Boolean profileCompleted, UserTypes userType, Status status, Message message, Long createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photourl = photourl;
        this.avata = avata;
        CanPost = canPost;
        ProfileCompleted = profileCompleted;
        UserType = userType;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public User(String username, String email ) {
        this.username = username;
        this.email = email;

    }


    public User(String username, String email ,String firstname ,String lastname ,String photourl,String avata ,UserTypes UserType , Boolean CanPost, Boolean ProfileCompleted) {
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photourl = photourl;
        this.avata = avata ;
        this.UserType = UserType ;
        this.CanPost = CanPost ;
        this.ProfileCompleted = ProfileCompleted ;
    }

    public User(String id, String username, String email, String firstname, String lastname, String photourl, String avata, Boolean canPost, Boolean profileCompleted, UserTypes userType, Long createdAt, Long updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photourl = photourl;
        this.avata = avata;
        CanPost = canPost;
        ProfileCompleted = profileCompleted;
        UserType = userType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public User(String id, String username, String email, String firstname, String lastname, String photourl, String avata, Boolean canPost, Boolean profileCompleted, UserTypes userType, Long updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photourl = photourl;
        this.avata = avata;
        CanPost = canPost;
        ProfileCompleted = profileCompleted;
        UserType = userType;
        this.updatedAt = updatedAt;
    }



    @Exclude
    public Map<String, Object> toMapAll() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("username", username);
        result.put("email", email);
        result.put("firstname", firstname);
        result.put("lastname", lastname);
        result.put("photourl", photourl);
        result.put("avata", avata);
        result.put("CanPost", CanPost);
        result.put("ProfileCompleted", ProfileCompleted);
        result.put("UserType", UserType);
        result.put("status", status);
        result.put("message", message);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        result.put("continent_code", continent_code);
        result.put("continent_name", continent_name);
        result.put("country_code", country_code);
        result.put("country_name", country_name);
        result.put("region_code", region_code);
        result.put("region_name", region_name);
        result.put("city", city);
        result.put("FCMToken", FCMToken);
        result.put("isSubscribedPremium", isSubscribedPremium);

        return result;
    }

    @Exclude
    public Map<String, Object> toMapUserInformation() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("photourl", photourl);
        result.put("avata", avata);
        result.put("CanPost", CanPost);
        result.put("ProfileCompleted", ProfileCompleted);
        result.put("UserType", UserType);
        result.put("updatedAt", updatedAt);

        return result;
    }



}
// [END blog_user_class]
