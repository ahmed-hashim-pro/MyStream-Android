package com.medoapps.www.onlinequran;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.content.IntentCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.models.User;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class UserPhotoView extends AppCompatActivity {

    private static final String TAG = "UserPhotoView";
    private ImageView userImage;
    private Button ChangePhoto;
    private static final int RC_TAKE_PICTURE = 200;
    private BroadcastReceiver mBroadcastReceiverUserPhoto;
    public ProgressDialog mProgressDialogUserPhoto;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Uri mDownloadUrlUserPhoto = null;
    private Uri mFileUriUserPhoto = null;
    private static final String KEY_UserPhoto_URI = "key_UserPhoto_uri";
    private static final String KEY_DOWNLOAD_URL_UserPhoto = "key_download_url_UserPhoto";
    private DatabaseReference mUserReference;


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_photo_view);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        userImage = findViewById(R.id.fullscreen_content);
        ChangePhoto = findViewById(R.id.ubdate_button);
        ChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.ubdate_button).setOnTouchListener(mDelayHideTouchListener);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUriUserPhoto = savedInstanceState.getParcelable(KEY_UserPhoto_URI);
            mDownloadUrlUserPhoto = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL_UserPhoto);
        }
        onNewIntent(getIntent());

        // Local broadcast receiver
        mBroadcastReceiverUserPhoto = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);


                switch (intent.getAction()) {

                    case MyUploadService.UPLOAD_COMPLETED_UserPhoto:

                        onUploadResultIntent(intent);
                        hideProgressDialog();
                        Toast.makeText(context, "User Photo Set", Toast.LENGTH_SHORT).show();

                        break;
                    case MyUploadService.UPLOAD_ERROR_UserPhoto:
                        hideProgressDialog();
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };
    }
    @Override
    protected void onStart() {
        super.onStart();


        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiverUserPhoto, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiverUserPhoto, MyUploadService.getIntentFilter());
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                User url = dataSnapshot.getValue(User.class);

                String id = url.email;

                String ProfileUrl = url.photourl;
                //Toast.makeText(PostDetailActivity.this, id, Toast.LENGTH_SHORT).show();
                String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                try {
                    Glide.with(getApplicationContext()).load(ProfileUrl).into(userImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        };
        mUserReference.addValueEventListener(userListener);

    }

    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto)) {
            onUploadResultIntent(intent);
        }

    }
    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrlUserPhoto = IntentCompat.getParcelableExtra(intent, MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto, Uri.class);
        mFileUriUserPhoto = IntentCompat.getParcelableExtra(intent, MyUploadService.EXTRA_FILE_URI_UserPhoto, Uri.class);

        if (mDownloadUrlUserPhoto!=null){
            String u2 = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10205?alt=media&token=52f537ce-fb27-4aaa-9ba5-412b9e71d7f5";
            hideProgressDialog();
            try {
                Glide.with(getApplicationContext())
                        .load(mDownloadUrlUserPhoto.toString())
                        .into(userImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mDatabase.child("users").child(getUid()).child("photourl").setValue(mDownloadUrlUserPhoto.toString());
        }

        //updateUI(mAuth.getCurrentUser());
    }


    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUriUserPhoto = data.getData();

                if (mFileUriUserPhoto != null) {
                    uploadFromUri(mFileUriUserPhoto);
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUriUserPhoto = fileUri;

        // Clear the last download, if any
        //updateUI(mAuth.getCurrentUser());
        mDownloadUrlUserPhoto = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI_UserPhoto, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD_UserPhoto));

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_uploading));
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialogUserPhoto == null) {
            mProgressDialogUserPhoto = new ProgressDialog(this);
            mProgressDialogUserPhoto.setCancelable(false);
            mProgressDialogUserPhoto.setIndeterminate(true);

        }

        mProgressDialogUserPhoto.setMessage(caption);
        mProgressDialogUserPhoto.show();
    }
    private void hideProgressDialog() {
        mProgressDialogUserPhoto.dismiss();
        if ( mProgressDialogUserPhoto.isShowing()) {
            mProgressDialogUserPhoto.dismiss();
        }
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
