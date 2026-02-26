package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.classes.YouTubeConfig;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.PostType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.models.UserTypes;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class NewYouTubePostActivity extends BaseActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "NewYouTubePostActivity";
    EditText Youtubeurl;
    TextView videoTitle;
    TextView videoDesc;
    private ImageButton backBTN;
    Button checkbutton;
    Button addbutton;
    ImageView checkSign;
    String YoutubeVideoId;
    String title;
    String description;
    String thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_you_tube_post);




        backBTN = (ImageButton) findViewById(R.id.backBTN);
        checkbutton = (Button) findViewById(R.id.checkbutton);
        addbutton = (Button) findViewById(R.id.addbutton);
        checkSign = (ImageView) findViewById(R.id.checkSign);

        Youtubeurl = (EditText) findViewById(R.id.Youtubeurl);
        videoTitle = (TextView) findViewById(R.id.videoTitle);
        videoDesc = (TextView) findViewById(R.id.videoDesc);
        checkSign.setVisibility(View.GONE);
        addbutton.setEnabled(false);

        checkrecieveShareDAta();
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        checkbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUrl();
            }
        });
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_database_ref]
    }

    void checkUrl(){
        String urlText = Youtubeurl.getText().toString();
        if (urlText.contains("youtu.be/")){
            String[] splittxt = urlText.split("youtu.be/");
            Uri uri=Uri.parse(urlText);
            Log.d("TAGretertre", splittxt[1]);
            getYoutubeVideoInfo(splittxt[1]);


//                    https://www.youtube.com/watch?v=M-P4QBt-FWw&list=RDiOxzG3jjFkY&index=10&ab_channel=AlanWalker
        }else if(urlText.contains("youtube.com/watch")){
            String[] splittxt = urlText.split("v=");
            Log.d("TAGretertre", splittxt[1]);

            String[] splittxt2 = splittxt[1].split("&");
            Log.d("TAGretertre", splittxt2[0]);

            getYoutubeVideoInfo(splittxt2[0]);

        }

    }
    void checkrecieveShareDAta(){
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
//                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
//                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }
    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
            Youtubeurl.setText(sharedText);
            checkUrl();
            // Update UI to reflect text being shared
        }
    }
    private void getYoutubeVideoInfo(String youtubeVideoId){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.googleapis.com/youtube/v3/videos?part=id%2C+snippet&id="+youtubeVideoId+"&key=" + YouTubeConfig.getInfoAPI_KEY();


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                Log.d("onResponsehhhhh", response);
                        JSONObject snippet= null;
                        JSONObject thumbnails= null;
                        try {
                            snippet = new JSONObject(response.toString())
                                    .getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                            thumbnails = new JSONObject(response.toString())
                                    .getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String thumbnailsHigh=thumbnails.getString("url");
                            videoTitle.setText(snippet.getString("title"));
                            videoDesc.setText(snippet.getString("description"));
                            checkSign.setVisibility(View.VISIBLE);
                            YoutubeVideoId = youtubeVideoId;
                            title = snippet.getString("title");
                            description = snippet.getString("description");
                            thumbnail = thumbnailsHigh;
                            addbutton.setEnabled(true);
//
                            Log.d("onResponsehhhhh", snippet.getString("title"));

                        } catch (JSONException e) {
                            Log.d("onResponsehhhhh", e.toString());

                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                checkSign.setVisibility(View.GONE);
                YoutubeVideoId = null;
                addbutton.setEnabled(false);



            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void submitPost() {
        try {

            if (YoutubeVideoId == null)
                return;
            /*if (chicFileUpload==false){
                Toast.makeText(this, getString(R.string.Upload_file_recquired), Toast.LENGTH_SHORT).show();
                return;
            }
            if (chickThumbUpload==false){
                Toast.makeText(this, getString(R.string.Upload_Thumb), Toast.LENGTH_SHORT).show();
                return;
            }*/
            Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

            // [START single_value_read]
            final String userId = getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
//                            attach_url = mDownloadUrl.toString();
//                            Thumb_Url = mDownloadUrlThumb.toString();
                            // [START_EXCLUDE]
                            if (user == null) {
                                // User is null, error out
                                Log.e(TAG, "User " + userId + " is unexpectedly null");
                                Toast.makeText(NewYouTubePostActivity.this,
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            }else if(user.UserType != UserTypes.Admin){
                                Toast.makeText(getApplicationContext(), "You can not post , it is for admins only", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // Write new post
                                writeNewPost(userId, user.firstname+" "+user.lastname, title, description,user.photourl,null,null,thumbnail,YoutubeVideoId);
                            }

                            // Finish this Activity, back to the stream
//                            setEditingEnabled(true);
//                            hideProgressDialoghere();
                            finish();
                            // [END_EXCLUDE]
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // [START_EXCLUDE]
//                            setEditingEnabled(true);
                            // [END_EXCLUDE]
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "can not posting", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // [END single_value_read]
    }

    private void writeNewPost(String userId, String username, String title, String body , String profilePhoto,String attachment,String Upload_Type ,String Thumb_Url,String YouTubeVideoId ) {
        try {
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Long Date = timestamp.getTime();
            String key = mDatabase.child("youtube-posts").push().getKey();
            Post post = new Post(key,userId, username, title, body , profilePhoto,attachment,Upload_Type, PostType.YouTube,YouTubeVideoId,Thumb_Url, Date);
            Map<String, Object> postValues = post.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/youtube-posts/" + key, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

            mDatabase.updateChildren(childUpdates);
            increasePostsCount(FirebaseDatabase.getInstance().getReference().child("GlobalVariable").child("YoutubePostsCount"));
        } catch (Exception e) {
            Toast.makeText(this, "error in weite post", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void increasePostsCount(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }
                int p = mutableData.getValue(int.class);

                // Set value and report transaction success
                mutableData.setValue(p + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
            }
        });
    }

}