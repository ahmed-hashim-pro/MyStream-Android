package com.medoapps.www.onlinequran;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.medoapps.www.onlinequran.util.AppBottomSheet;
import androidx.core.content.IntentCompat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.User;

import java.util.HashMap;
import java.util.Map;

public class EditPost extends BaseActivity {
    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";
    private String mPostKey;
    private String mUserKey;
    private DatabaseReference mPostReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ValueEventListener mPostListener;


    private EditText mTitleField;
    private EditText mBodyField;
    private ImageView UpDone;
    private ImageView ThumbDone;
    private FloatingActionButton mSubmitButton;
    private ImageView preview ;
    private  String thumb_url ;
    private  String file_uri ;

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_TAKE_Thumb = 102;




    private static final String KEY_AudioVideo_URI = "key_audiovideo_uri";
    private static final String KEY_DOWNLOAD_URL_AudioVideo = "key_download_url_audiovideo";

    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private String attach_url =null;
    private String Upload_Type = null;
    private String Thumb_Url = null;
    private boolean chicFileUpload = false;
    private boolean chickThumbUpload = false;
    private String title = "";
    private String body = "";


    private static final String KEY_Thumb_URI = "key_thumb_uri";
    private static final String KEY_DOWNLOAD_URL_Thumb = "key_download_url_thumb";



    private Uri mDownloadUrlThumb = null;
    private Uri mThumbUri = null;
//    //private RewardedVideoAd mRewardedVideoAd;
    private static final String AD_UNIT_ID = "ca-app-pub-9350633918697995/7533398267";
    private static final String APP_ID = " ca-app-pub-9350633918697995~2524775865";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);


        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mUserKey = getIntent().getStringExtra(EXTRA_USER_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserKey);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mTitleField = findViewById(R.id.field_title_edit);
        mBodyField = findViewById(R.id.field_body_edit);
        mSubmitButton = findViewById(R.id.fab_submit_post_edit);
        UpDone = findViewById(R.id.up_done_edit);
        ThumbDone = findViewById(R.id.up_done2_edit);
        preview = findViewById(R.id.previewThumb_edit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadAll();
            }
        });

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);



        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadThumb.EXTRA_DOWNLOAD_URL_Thumb)) {
            onUploadThumbResultIntent(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                //Toast.makeText(PostDetailActivity.this, mUserKey, Toast.LENGTH_SHORT).show();
                // [START_EXCLUDE]
                mTitleField.setText(post.title);
                mBodyField.setText(post.body);

                thumb_url = post.Thumb_Url;
                file_uri = post.attachment;


                try {
                    Glide.with(getApplicationContext())
                            .load(thumb_url)
                            .into(preview);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        };


        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                User url = dataSnapshot.getValue(User.class);

                String id = url.email;

                String ProfileUrl = url.photourl;
                //Toast.makeText(PostDetailActivity.this, id, Toast.LENGTH_SHORT).show();
                String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                // Glide.with(PostDetailActivity.this).load(ProfileUrl).into(mProfileView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        mUserReference.addValueEventListener(userListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;


    }


    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_AudioVideo_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL_AudioVideo, mDownloadUrl);
        out.putParcelable(KEY_Thumb_URI, mThumbUri);
        out.putParcelable(KEY_DOWNLOAD_URL_Thumb, mDownloadUrlThumb);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        try {
            if (requestCode == RC_TAKE_Thumb) {
                if (resultCode == RESULT_OK) {
                    mThumbUri = data.getData();

                    if (mThumbUri != null) {
                        ThumbDone.setVisibility(View.VISIBLE);
                        preview.setImageURI(mThumbUri);
                        Toast.makeText(this, mFileUri.toString(), Toast.LENGTH_SHORT).show();

                        //uploadThumbFromUri(mThumbUri);
                    } else {
                        Log.w(TAG, "File URI is null");
                    }
                } else {
                    Toast.makeText(this, "Taking Thumbnail failed.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();
                ContentResolver cr = this.getContentResolver();
                String mime = cr.getType(mFileUri);
                Toast.makeText(this, mime, Toast.LENGTH_SHORT).show();
                if (mFileUri != null) {
                    if (mFileUri.toString().contains("video"))
                    {
                        if(mime.contains("webm")){

                            mFileUri=null;
                            Toast.makeText(this, "Does not support webm files", Toast.LENGTH_SHORT).show();
                        }else {
                            UpDone.setVisibility(View.VISIBLE);
                            Toast.makeText(this, mFileUri.toString(), Toast.LENGTH_SHORT).show();
                            //uploadFromUri(mFileUri);
                            Upload_Type = "video";
                            SettingSaved.Upload_Type = "video";
                            SettingSaved settingSaved = new SettingSaved(this);
                            settingSaved.SaveData();
                        }
                    }else
                    {
                    //uploadFromUri(mFileUri);
                        UpDone.setVisibility(View.VISIBLE);
                        Upload_Type = "audio";
                        SettingSaved.Upload_Type=  "audio";
                        SettingSaved settingSaved=new SettingSaved(this);
                        settingSaved.SaveData();
                    }
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking file failed.", Toast.LENGTH_SHORT).show();
            }
        }*/

    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mDownloadUrl = null;

        // Start MyUploadPostAudioVideo to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadPostAudioVideo.class)
                .putExtra(MyUploadPostAudioVideo.EXTRA_VideoAudio_URI, fileUri)
                .setAction(MyUploadPostAudioVideo.ACTION_UPLOAD_VideoAudio));

        // Show loading spinner
        //showProgressDialog(getString(R.string.progress_uploading));
    }
    private void uploadThumbFromUri(Uri fileUri) {
        Log.d(TAG, "uploadThumbFromUri:src:" + fileUri.toString());

        try {
            // Save the File URI
            mThumbUri = fileUri;

            // Clear the last download, if any
            //updateUI(mAuth.getCurrentUser());
            mDownloadUrlThumb = null;

            // Start MyUploadThumb to upload the file, so that the file is uploaded
            // even if this Activity is killed or put in the background
            startService(new Intent(this, MyUploadThumb.class)
                    .putExtra(MyUploadThumb.EXTRA_Thumb_URI, fileUri)
                    .setAction(MyUploadThumb.ACTION_UPLOAD_Thumb));
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Show loading spinner
        //showProgressDialog(getString(R.string.progress_uploading));
    }

    private void beginDownload() {
        // Get path
        String path = "photos/" + mFileUri.getLastPathSegment();

        // Kick off MyDownloadPostAudioVideo to download the file
        Intent intent = new Intent(this, MyDownloadPostAudioVideo.class)
                .putExtra(MyDownloadPostAudioVideo.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadPostAudioVideo.ACTION_DOWNLOAD);
        startService(intent);

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_downloading));
    }



    private void onUploadThumbResultIntent(Intent intent) {
        try {
            // Got a new intent from MyUploadThumb with a success or failure
            mDownloadUrlThumb = IntentCompat.getParcelableExtra(intent, MyUploadThumb.EXTRA_DOWNLOAD_URL_Thumb, Uri.class);
            mThumbUri = IntentCompat.getParcelableExtra(intent, MyUploadThumb.EXTRA_Thumb_URI, Uri.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private void showMessageDialog(String title, String message) {
        AppBottomSheet.showMessage(this, title, message);
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

    private void hideProgressDialoghere() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }*/


    public void UploadAll(){


        try {



            title=mTitleField.getText().toString();
            body=mBodyField.getText().toString();
            SettingSaved.title=mTitleField.getText().toString();
            SettingSaved.body=mBodyField.getText().toString();
            SettingSaved.mDownloadUrl=null;
            SettingSaved.mDownloadUrlThumb=null;
            SettingSaved.editMode=true;
            SettingSaved.mPostKey = mPostKey;
            SettingSaved.thumb_url=thumb_url;
            SettingSaved.file_uri=file_uri;
            SettingSaved settingSaved=new SettingSaved(this);
            settingSaved.SaveData();


// Title is required
            if (TextUtils.isEmpty(title)) {
                mTitleField.setError(REQUIRED);
                return;
            }

            // Body is required
            if (TextUtils.isEmpty(body)) {
                mBodyField.setError(REQUIRED);
                return;
            }



            // Disable button so there are no multi-posts
            setEditingEnabled(false);
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            if (mThumbUri == null&&mFileUri== null){


                DatabaseReference globalPostRef = mDatabase.child("posts").child(mPostKey);
                DatabaseReference userPostRef = mDatabase.child("user-posts").child(getUid()).child(mPostKey);

                // Run two transactions
                onStarClicked(globalPostRef);
                onStarClicked(userPostRef);


            }else{

                if (mThumbUri == null){

                }else {

                    uploadThumbFromUri(mThumbUri);
                }
                if (mFileUri== null){

                }else{
                    uploadFromUri(mFileUri);
                }
            }

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                p.title = SettingSaved.title;
                p.body =  SettingSaved.body;
                p.Thumb_Url = thumb_url;
                p.attachment = file_uri;



                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    private void submitPost() {
        try {


            Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

            // [START single_value_read]
            final String userId = getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);

                            // [START_EXCLUDE]
                            if (user == null) {
                                // User is null, error out
                                Log.e(TAG, "User " + userId + " is unexpectedly null");
                                Toast.makeText(EditPost.this,
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Write new post
                                writeNewPost(userId, user.firstname+" "+user.lastname, SettingSaved.title, SettingSaved.body,user.photourl, thumb_url);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // [START_EXCLUDE]

                            // [END_EXCLUDE]
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "can not posting", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // [END single_value_read]
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body , String profilePhoto ,String Thumb_Url ) {
        try {
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            String key = mDatabase.child("posts").push().getKey();
            Post post = new Post(userId, username, title, body , profilePhoto,Thumb_Url);
            Map<String, Object> postValues = post.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + mPostKey, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + mPostKey, postValues);

            mDatabase.updateChildren(childUpdates);


        } catch (Exception e) {
            Toast.makeText(this, "error in weite post", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
//            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
//            mSubmitButton.setVisibility(View.GONE);
        }
    }

    public void ubload(View view) {

        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*|application/pdf|audio/*");
        String[] mimetypes = {"audio/*", "video/*|text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        startActivityForResult(intent, RC_TAKE_PICTURE);
    }
    public void ubload_thumb(View view) {
        Log.d(TAG, "launchCamera");

        try {
            // Pick an image from storage
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            //String[] mimetypes = {"audio/*", "video/*|text/*"};
            // intent.putExtra(Intent.EXTRA_MIME_TYPES, intent);

            startActivityForResult(intent, RC_TAKE_Thumb);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





}


