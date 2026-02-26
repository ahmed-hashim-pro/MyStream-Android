package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.IntentCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.PostType;
import com.medoapps.www.onlinequran.models.User;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to handle uploading files to Firebase Storage.
 */
public class MyUploadPostAudioVideo extends MyBaseTaskPostAudioVideo {

    private static final String TAG = "MyUploadPostAudioVideo";

    /** Intent Actions **/
    public static final String ACTION_UPLOAD_VideoAudio = "action_upload_VideoAudio";
    public static final String UPLOAD_COMPLETED_VideoAudio = "upload_completed_VideoAudio";
    public static final String UPLOAD_ERROR_VideoAudio = "upload_error_VideoAudio";

    /** Intent Extras **/
    public static final String EXTRA_VideoAudio_URI = "extra_VideoAudio_uri";
    public static final String EXTRA_DOWNLOAD_URL_VideoAudio = "extra_download_url_VideoAudio";

    // [START declare_ref]
    private StorageReference mStorageRef;
    // [END declare_ref]
    private DatabaseReference mDatabase;
    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD_VideoAudio.equals(intent.getAction())) {
            Uri fileUri = IntentCompat.getParcelableExtra(intent, EXTRA_VideoAudio_URI, Uri.class);
            uploadFromUri(fileUri);
        }

        return START_REDELIVER_INTENT;
    }

    // [START upload_from_uri]
    private void uploadFromUri(final Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START_EXCLUDE]
        taskStarted();
        showProgressNotification(getString(R.string.progress_uploading), 0, 0);
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("video_audio")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        showProgressNotification(getString(R.string.progress_uploading),
                                taskSnapshot.getBytesTransferred(),
                                taskSnapshot.getTotalByteCount());
                    }
                })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        Log.d(TAG, "uploadFromUri: upload success");

                        // Request the public download URL
                        return photoRef.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(@NonNull Uri downloadUri) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri: getDownloadUri success");

                        // [START_EXCLUDE]
                        //broadcastUploadFinished(downloadUri, fileUri);
                        SettingSaved.mDownloadUrl=downloadUri.toString();
                        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                        settingSaved.SaveData();

                        if (SettingSaved.editMode==false) {
                            if (SettingSaved.mDownloadUrlThumb == null) {
                                Toast.makeText(getApplicationContext(), "no url", Toast.LENGTH_SHORT).show();
                            } else {
                                submitPost();
                            }
                            showUploadFinishedNotification(downloadUri, fileUri);
                        }else {
                            DatabaseReference globalPostRef = mDatabase.child("posts").child(SettingSaved.mPostKey);
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(getUid()).child(SettingSaved.mPostKey);

                            // Run two transactions
                            onStarClicked(globalPostRef);
                            onStarClicked(userPostRef);
                            showUploadFinishedNotification(downloadUri, fileUri);
                            SettingSaved.editMode=false;
                            settingSaved.SaveData();
                        }
                        taskCompleted();
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        // [START_EXCLUDE]
                        broadcastUploadFinished(null, fileUri);
                        showUploadFinishedNotification(null, fileUri);
                        taskCompleted();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]
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
                p.attachment = SettingSaved.mDownloadUrl;



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
    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED_VideoAudio : UPLOAD_ERROR_VideoAudio;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL_VideoAudio, downloadUrl)
                .putExtra(EXTRA_VideoAudio_URI, fileUri);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to UserInformation
        Intent intent = new Intent(this, NewPostActivity.class)
                .putExtra(EXTRA_DOWNLOAD_URL_VideoAudio, downloadUrl)
                .putExtra(EXTRA_VideoAudio_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = downloadUrl != null;
        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
        showFinishedNotification(caption, intent, success);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED_VideoAudio);
        filter.addAction(UPLOAD_ERROR_VideoAudio);

        return filter;
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
                                Toast.makeText(MyUploadPostAudioVideo.this,
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Write new post
                                writeNewPost(userId, user.firstname+" "+user.lastname, SettingSaved.title, SettingSaved.body,user.photourl, SettingSaved.mDownloadUrl,SettingSaved.Upload_Type,SettingSaved.mDownloadUrlThumb);
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

}
