package com.medoapps.www.onlinequran;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.models.User;

public class UserPage extends AppCompatActivity {

    private static final String TAG = "UserPage";

    public ImageView userImage;
    public ImageView userImageFit;
    public TextView userName;
    public TextView userEmail;
    public Button ubdateInfo;
    private DatabaseReference mUserReference;
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private Uri mDownloadUrlUserPhoto = null;
    private Uri mFileUriUserPhoto = null;
    private static final String KEY_UserPhoto_URI = "key_UserPhoto_uri";
    private static final String KEY_DOWNLOAD_URL_UserPhoto = "key_download_url_UserPhoto";

    private BroadcastReceiver mBroadcastReceiverUserPhoto;
    private ProgressDialog mProgressDialogUserPhoto;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    private static final int RC_TAKE_PICTURE = 190;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
// Initialize Firebase Auth
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());
        userImage = findViewById(R.id.user);
        userImageFit = findViewById(R.id.userfit);
        userName = findViewById(R.id.name_user);
        userEmail = findViewById(R.id.email_user);
        ubdateInfo = findViewById(R.id.ubdateInfobtn);


        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent down = new Intent(UserPage.this, UserPhotoView.class);
                startActivity(down);
            }
        });
        ubdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent down = new Intent(UserPage.this, UbdateUserInfo.class);
                startActivity(down);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                startActivity(new Intent(UserPage.this, NewPostActivity.class));
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {


                    new MyPosts()

            };
            private final String[] mFragmentNames = new String[] {


                    getString(R.string.heading_my_posts)

            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);

        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
                hideProgressDialog();

                switch (intent.getAction()) {

                    case MyUploadService.UPLOAD_COMPLETED_UserPhoto:

                        onUploadResultIntent(intent);
                        Toast.makeText(context, "User Photo Set", Toast.LENGTH_SHORT).show();

                        break;
                    case MyUploadService.UPLOAD_ERROR_UserPhoto:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };
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
        mDownloadUrlUserPhoto = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto);
        mFileUriUserPhoto = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI_UserPhoto);

        if (mDownloadUrlUserPhoto!=null){
            String u2 = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10205?alt=media&token=52f537ce-fb27-4aaa-9ba5-412b9e71d7f5";

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

                String email = url.email;

                String ProfileUrl = url.photourl;
                //Toast.makeText(PostDetailActivity.this, id, Toast.LENGTH_SHORT).show();
                String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                try {
                    Glide.with(getApplicationContext()).load(ProfileUrl).into(userImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Glide.with(getApplicationContext()).load(ProfileUrl).into(userImageFit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                userName.setText(url.firstname+" "+url.lastname);
                userEmail.setText(email);

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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);
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
            mProgressDialogUserPhoto.setIndeterminate(true);
        }

        mProgressDialogUserPhoto.setMessage(caption);
        mProgressDialogUserPhoto.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialogUserPhoto != null && mProgressDialogUserPhoto.isShowing()) {
            mProgressDialogUserPhoto.dismiss();
        }
    }

}
