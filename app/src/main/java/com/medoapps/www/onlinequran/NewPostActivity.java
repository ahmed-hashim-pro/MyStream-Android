package com.medoapps.www.onlinequran;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.medoapps.www.onlinequran.util.AppBottomSheet;
import androidx.core.content.IntentCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.PostType;
import com.medoapps.www.onlinequran.models.User;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends BaseActivity  {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private static final int RC_TAKE_PICTURE = 101;


    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mTitleField;
    private EditText mBodyField;
    private ImageView UpDone;
    private ImageView ThumbDone;
    private FloatingActionButton mSubmitButton;
    private static final String KEY_AudioVideo_URI = "key_audiovideo_uri";
    private static final String KEY_DOWNLOAD_URL_AudioVideo = "key_download_url_audiovideo";

    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private String attach_url =null;
    private String Upload_Type = null;
    private String Thumb_Url = null;
    private boolean chicFileUpload = false;
    private boolean chickThumbUpload = false;
    private String title = "";
    private String body = "";

    private static final int RC_TAKE_Thumb = 102;

    private static final String KEY_Thumb_URI = "key_thumb_uri";
    private static final String KEY_DOWNLOAD_URL_Thumb = "key_download_url_thumb";

    private BroadcastReceiver mBroadcastReceiverThumb;

    private ImageView preview ;
    private Uri mDownloadUrlThumb = null;
    private Uri mThumbUri = null;
    //private RewardedVideoAd mRewardedVideoAd;
    private static final String AD_UNIT_ID = "ca-app-pub-9350633918697995/7533398267";
    private static final String APP_ID = " ca-app-pub-9350633918697995~2524775865";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        // Initialize the Mobile Ads SDK.
//        //MobileAds.initialize(this, getString(R.string.ad_APP_ID));
        /*mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);*/
        loadRewardedVideoAd();
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_database_ref]

        mTitleField = findViewById(R.id.field_title);
        mBodyField = findViewById(R.id.field_body);
        mSubmitButton = findViewById(R.id.fab_submit_post);
        UpDone = findViewById(R.id.up_done);
        ThumbDone = findViewById(R.id.up_done2);
        preview = findViewById(R.id.previewThumb);



        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_AudioVideo_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL_AudioVideo);
            mThumbUri = savedInstanceState.getParcelable(KEY_Thumb_URI);
            mDownloadUrlThumb = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL_Thumb);
        }
        onNewIntent(getIntent());

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);


                switch (intent.getAction()) {

                    case MyUploadPostAudioVideo.UPLOAD_COMPLETED_VideoAudio:
                        //hideProgressDialoghere();

                        onUploadResultIntent(intent);

                        chicFileUpload=true;
                        //uploadThumbFromUri(mThumbUri);

                        if (mDownloadUrl==null&&mDownloadUrlThumb==null){
                            Toast.makeText(context, "no url", Toast.LENGTH_SHORT).show();
                        }else{
                        submitPost();}
                        //attach_url = mDownloadUrl.toString();
                        Toast.makeText(context, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                        break;
                    case MyUploadPostAudioVideo.UPLOAD_ERROR_VideoAudio:
                        Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                        //onUploadResultIntent(intent);
                        break;
                }
            }
        };

        // Local broadcast receiver
        mBroadcastReceiverThumb = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);


                switch (intent.getAction()) {

                    case MyUploadThumb.UPLOAD_COMPLETED_Thumb:
                        //hideProgressDialoghere();

                        onUploadThumbResultIntent(intent);
                        chickThumbUpload=true;


                        //Thumb_Url = mDownloadUrlThumb.toString();
                        Toast.makeText(context, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                        uploadFromUri(mFileUri);

                        break;
                    case MyUploadThumb.UPLOAD_ERROR_Thumb:
                        onUploadThumbResultIntent(intent);
                        break;
                }
            }
        };

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
        if (intent.hasExtra(MyUploadPostAudioVideo.EXTRA_DOWNLOAD_URL_VideoAudio)) {
            onUploadResultIntent(intent);
        }

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadThumb.EXTRA_DOWNLOAD_URL_Thumb)) {
            onUploadThumbResultIntent(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        //updateUI(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyDownloadPostAudioVideo.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadPostAudioVideo.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiverThumb, MyDownloadThumb.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiverThumb, MyUploadThumb.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverThumb);
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
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();
                ContentResolver cr = this.getContentResolver();
                String mime = cr.getType(mFileUri);
                Toast.makeText(this, mime, Toast.LENGTH_SHORT).show();
                if (mFileUri != null) {
                    if (mFileUri.toString().contains("video")) {
                        if (mime.contains("webm")) {

                            mFileUri = null;
                            Toast.makeText(this, "Does not support webm files", Toast.LENGTH_SHORT).show();
                        } else {
                            UpDone.setVisibility(View.VISIBLE);
                            Toast.makeText(this, mFileUri.toString(), Toast.LENGTH_SHORT).show();
                            //uploadFromUri(mFileUri);
                            Upload_Type = "video";
                            SettingSaved.Upload_Type = "video";
                            SettingSaved settingSaved = new SettingSaved(this);
                            settingSaved.SaveData();
                        }
                    } else {
                        //uploadFromUri(mFileUri);
                        UpDone.setVisibility(View.VISIBLE);
                        Upload_Type = "audio";
                        SettingSaved.Upload_Type = "audio";
                        SettingSaved settingSaved = new SettingSaved(this);
                        settingSaved.SaveData();
                    }
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking file failed.", Toast.LENGTH_SHORT).show();
            }
        }
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
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        updateUI(mAuth.getCurrentUser());
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

        // Save the File URI
        mThumbUri = fileUri;

        // Clear the last download, if any
        updateUI(mAuth.getCurrentUser());
        mDownloadUrlThumb = null;

        // Start MyUploadThumb to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadThumb.class)
                .putExtra(MyUploadThumb.EXTRA_Thumb_URI, fileUri)
                .setAction(MyUploadThumb.ACTION_UPLOAD_Thumb));


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



    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadPostAudioVideo with a success or failure
        mDownloadUrl = IntentCompat.getParcelableExtra(intent, MyUploadPostAudioVideo.EXTRA_DOWNLOAD_URL_VideoAudio, Uri.class);
        mFileUri = IntentCompat.getParcelableExtra(intent, MyUploadPostAudioVideo.EXTRA_VideoAudio_URI, Uri.class);
        //Toast.makeText(this, mDownloadUrl.toString(), Toast.LENGTH_SHORT).show();


        updateUI(mAuth.getCurrentUser());
    }
    private void onUploadThumbResultIntent(Intent intent) {
        // Got a new intent from MyUploadThumb with a success or failure
        mDownloadUrlThumb = IntentCompat.getParcelableExtra(intent, MyUploadThumb.EXTRA_DOWNLOAD_URL_Thumb, Uri.class);
        mThumbUri = IntentCompat.getParcelableExtra(intent, MyUploadThumb.EXTRA_Thumb_URI, Uri.class);
        //Toast.makeText(this, mDownloadUrlThumb.toString(), Toast.LENGTH_SHORT).show();

        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {
        // Signed in or Signed out
        if (user != null) {
            //findViewById(R.id.layout_signin).setVisibility(View.GONE);
            //findViewById(R.id.layout_storage).setVisibility(View.VISIBLE);
        } else {
            //findViewById(R.id.layout_signin).setVisibility(View.VISIBLE);
            //findViewById(R.id.layout_storage).setVisibility(View.GONE);
        }

        // Download URL and Download button
        if (mDownloadUrl != null) {
           // ((TextView) findViewById(R.id.picture_download_uri)).setText(mDownloadUrl.toString());
           // findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
        } else {
           // ((TextView) findViewById(R.id.picture_download_uri)).setText(null);
           // findViewById(R.id.layout_download).setVisibility(View.GONE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            updateUI(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void submitPost() {
        try {
           /* final String title = mTitleField.getText().toString();
            final String body = mBodyField.getText().toString();


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

            if (chicFileUpload==false){
                Toast.makeText(this, getString(R.string.Upload_file_recquired), Toast.LENGTH_SHORT).show();
                return;
            }
            if (chickThumbUpload==false){
                Toast.makeText(this, getString(R.string.Upload_Thumb), Toast.LENGTH_SHORT).show();
                return;
            }*/
            // Disable button so there are no multi-posts
            //setEditingEnabled(false);

            if (chicFileUpload==false){
                Toast.makeText(this, getString(R.string.Upload_file_recquired), Toast.LENGTH_SHORT).show();
                return;
            }
            if (chickThumbUpload==false){
                Toast.makeText(this, getString(R.string.Upload_Thumb), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

            // [START single_value_read]
            final String userId = getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            attach_url = mDownloadUrl.toString();
                            Thumb_Url = mDownloadUrlThumb.toString();
                            // [START_EXCLUDE]
                            if (user == null) {
                                // User is null, error out
                                Log.e(TAG, "User " + userId + " is unexpectedly null");
                                Toast.makeText(NewPostActivity.this,
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Write new post
                               writeNewPost(userId, user.firstname+" "+user.lastname, title, body,user.photourl,attach_url,Upload_Type,Thumb_Url);
                            }

                            // Finish this Activity, back to the stream
                            setEditingEnabled(true);
                            hideProgressDialoghere();
                            finish();
                            // [END_EXCLUDE]
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // [START_EXCLUDE]
                            setEditingEnabled(true);
                            // [END_EXCLUDE]
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "can not posting", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // [END single_value_read]
    }

    public void UploadAll(){

        showRewardedVideo();

        title=mTitleField.getText().toString();
        body=mBodyField.getText().toString();
         SettingSaved.title=mTitleField.getText().toString();
         SettingSaved.body=mBodyField.getText().toString();
        SettingSaved.mDownloadUrl=null;
        SettingSaved.mDownloadUrlThumb=null;
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

        if (mFileUri==null){
            Toast.makeText(this, getString(R.string.Upload_file_recquired), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mThumbUri==null){
            Toast.makeText(this, getString(R.string.Upload_Thumb), Toast.LENGTH_SHORT).show();
            return;
        }
        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        uploadThumbFromUri(mThumbUri);
        uploadFromUri(mFileUri);
        finish();




    }

    @SuppressLint("RestrictedApi")
    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body , String profilePhoto,String attachment,String Upload_Type ,String Thumb_Url ) {
        try {
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Long Date = timestamp.getTime();
            String key = mDatabase.child("posts").push().getKey();
            Post post = new Post(key,userId, username, title, body , profilePhoto,attachment,Upload_Type, PostType.Local,null,Thumb_Url, Date);
            Map<String, Object> postValues = post.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + key, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

            mDatabase.updateChildren(childUpdates);
        } catch (Exception e) {
            Toast.makeText(this, "error in weite post", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //String[] mimetypes = {"audio/*", "video/*|text/*"};
       // intent.putExtra(Intent.EXTRA_MIME_TYPES, intent);

        startActivityForResult(intent, RC_TAKE_Thumb);

    }








    private void loadRewardedVideoAd() {
        /*if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(getString(R.string.Video_ad_unit_id), new AdRequest.Builder().build());
        }*/
    }

    private void showRewardedVideo() {
    /*    /*if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }*/
    }
    // [END write_fan_out]
}
