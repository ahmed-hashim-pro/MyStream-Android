/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.medoapps.www.onlinequran;

import com.medoapps.www.onlinequran.util.AppBottomSheet;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.data.StaticConfig;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.models.UserTypes;
import com.medoapps.www.onlinequran.util.ImageUtils;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Activity to upload and download photos from Firebase Storage.
 *
 * See {@link MyUploadService} for upload example.
 * See {@link MyDownloadService} for download example.
 */
public class UserInformation extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Storage#UserInformation";

    private static final int RC_TAKE_PICTURE = 180;

    private static final String KEY_UserPhoto_URI = "key_UserPhoto_uri";
    private static final String KEY_DOWNLOAD_URL_UserPhoto = "key_download_url_UserPhoto";

    private BroadcastReceiver mBroadcastReceiverUserPhoto;
    private ProgressDialog mProgressDialogUserPhoto;
    private FirebaseAuth mAuth;
    private EditText FirstName;
    private EditText LastName;
    private TextView useremail;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserReference;
    private ImageView userPhoto;

    private Uri mDownloadUrlUserPhoto = null;
    private Uri mFileUriUserPhoto = null;

    String first_name = null;
    String last_name =null;
    String photo_url =null;
    String firstPhotoUrl = "";
    private Context context;
    private User myAccount;
    private DatabaseReference userDB;
    private String avata = "default";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);


        // Initialize Firebase Auth
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());
        mAuth = FirebaseAuth.getInstance();




        // Click listeners
        // findViewById(R.id.profile_pic).setOnClickListener(this);
        findViewById(R.id.finish_sign_up).setOnClickListener(this);
        // Views
        FirstName = findViewById(R.id.first_name);
        LastName = findViewById(R.id.last_name);
        useremail = findViewById(R.id.useremail);
        userPhoto = findViewById(R.id.profile_pic);

        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });


        Objects.requireNonNull(useremail).setText(mAuth.getCurrentUser().getEmail());




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
    protected void onStart() {
        super.onStart();

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiverUserPhoto, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiverUserPhoto, MyUploadService.getIntentFilter());
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI


                myAccount = dataSnapshot.getValue(User.class);
                User url = dataSnapshot.getValue(User.class);

                FirstName.setText(url.firstname);
                LastName.setText(url.lastname);
                //String id = url.email;

                //to save profile photo if user doesnot upload photo
                firstPhotoUrl = url.photourl;
                String ProfileUrl = url.photourl;
                //Toast.makeText(PostDetailActivity.this, id, Toast.LENGTH_SHORT).show();
                //String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                try {
                    Glide.with(getApplicationContext()).load(ProfileUrl).into(userPhoto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //userName.setText(url.firstname+" "+url.lastname);

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

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto)) {
            onUploadResultIntent(intent);
        }

    }
    private void FinishSignUp(){
        Log.d(TAG, "finish_sign_up");
        if (!validateForm()) {
            return;
        }
        showProgressDialog(getString(R.string.get_information));

        first_name = FirstName.getText().toString();
        last_name = LastName.getText().toString();

        if (mDownloadUrlUserPhoto!=null){
            photo_url = mDownloadUrlUserPhoto.toString();
        }else {
            photo_url=firstPhotoUrl;

            DatabaseReference globalPostRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
            onStarClicked(globalPostRef);
            //photo_url = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/ic_action_account_circle_40.png?alt=media&token=93fe3dc5-f9a0-462c-ba57-4e77847baff6";
        }



        onAuthSuccess(mAuth.getCurrentUser());


    }
    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference userRef) {
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User p = mutableData.getValue(User.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }else {
                    User newUser = new User();
                    newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
                    p.avata = StaticConfig.STR_DEFAULT_BASE64;
                    avata=StaticConfig.STR_DEFAULT_BASE64;
                }



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

    private void startReminderService(){
        startService(new Intent(UserInformation.this, QuranListenTimerService.class));
    }
    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        completeInformation(user.getUid(), username, user.getEmail(),first_name,last_name,photo_url);

        StorageUtil storageUtil = new StorageUtil(this);
        storageUtil.storeProfileCompleted(true);
        startReminderService();
        // Go to MainA+ctivity
        startActivity(new Intent(UserInformation.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Go to MainA+ctivity
        startActivity(new Intent(UserInformation.this, MainActivity.class));
        finish();
    }

    // [START basic_write]
    private void completeInformation(String userId, String name, String email , String firstname, String lastname , String photourl) {
        Long Date = new SeparateFunctions(UserInformation.this).getTimeStamp();

        User user = new User(userId,name, email,firstname,lastname,photourl,avata,false,true, UserTypes.Listener,Date,Date);



        mDatabase.child("users").child(userId).child("username").setValue(name);
        mDatabase.child("users").child(userId).child("firstname").setValue(firstname);
        mDatabase.child("users").child(userId).child("lastname").setValue(lastname);
        mDatabase.child("users").child(userId).child("photourl").setValue(photourl);
        mDatabase.child("users").child(userId).child("ProfileCompleted").setValue(true);
        mDatabase.child("users").child(userId).child("updatedAt").setValue(Date);

    }
    private String usernameFromEmail(String email) {
        if (email != null){
            if (email.contains("@")) {
                return email.split("@")[0];
            } else {
                return email;
            }
        }else{
            String randomName = new SeparateFunctions(getApplicationContext()).randomName();
            return randomName;
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverUserPhoto);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_UserPhoto_URI, mFileUriUserPhoto);
        out.putParcelable(KEY_DOWNLOAD_URL_UserPhoto, mDownloadUrlUserPhoto);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {

                if (data == null) {
                    Toast.makeText(context, "no data", Toast.LENGTH_LONG).show();
                    return;
                }
                mFileUriUserPhoto = data.getData();

                if (mFileUriUserPhoto != null) {
                    uploadFromUri(mFileUriUserPhoto);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());

                        Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                        imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                        InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                        final Bitmap liteImage = ImageUtils.makeImageLite(is,
                                imgBitmap.getWidth(), imgBitmap.getHeight(),
                                ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                        final String imageBase64 = ImageUtils.encodeBase64(liteImage);
                        // myAccount.avata = imageBase64;


                        DatabaseReference globalPostRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
                        globalPostRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                User p = mutableData.getValue(User.class);
                                if (p == null) {
                                    return Transaction.success(mutableData);
                                } else {

                                    p.avata = imageBase64;
                                    avata = imageBase64;
                                }


                                // Set value and report transaction success
                                mutableData.setValue(p);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b,
                                                   DataSnapshot dataSnapshot) {

                                //SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(context);
                                // preferenceHelper.saveUserInfo(myAccount);

                                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                            }
                        });

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
        updateUI(mAuth.getCurrentUser());
        mDownloadUrlUserPhoto = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI_UserPhoto, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD_UserPhoto));

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_uploading));
    }

    private void beginDownload() {
        // Get path
        String path = "photos/" + mFileUriUserPhoto.getLastPathSegment();

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(this, MyDownloadService.class)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH_UserPhoto, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD_UserPhoto);
        startService(intent);

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_downloading));
    }

    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);
    }

    private void signInAnonymously() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
        showProgressDialog(getString(R.string.progress_auth));
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInAnonymously:SUCCESS");
                        hideProgressDialog();
                        updateUI(authResult.getUser());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                        hideProgressDialog();
                        updateUI(null);
                    }
                });
    }

    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrlUserPhoto = IntentCompat.getParcelableExtra(intent, MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto, Uri.class);
        mFileUriUserPhoto = IntentCompat.getParcelableExtra(intent, MyUploadService.EXTRA_FILE_URI_UserPhoto, Uri.class);

        if (mDownloadUrlUserPhoto!=null){
            String u2 = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10205?alt=media&token=52f537ce-fb27-4aaa-9ba5-412b9e71d7f5";

            try {
                Glide.with(getApplicationContext())
                        .load(mDownloadUrlUserPhoto.toString())
                        .into(userPhoto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        if (mDownloadUrlUserPhoto != null) {
            //((TextView) findViewById(R.id.picture_download_uri)).setText(mDownloadUrlUserPhoto.toString());
            //findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
        } else {
            //((TextView) findViewById(R.id.picture_download_uri)).setText(null);
            //findViewById(R.id.layout_download).setVisibility(View.GONE);
        }
    }

    private void showMessageDialog(String title, String message) {
        AppBottomSheet.showMessage(this, title, message);
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialogUserPhoto == null) {
            mProgressDialogUserPhoto = new ProgressDialog(this);
            mProgressDialogUserPhoto.setIndeterminate(true);
        }

        mProgressDialogUserPhoto.setMessage(caption);
        mProgressDialogUserPhoto.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialogUserPhoto != null && mProgressDialogUserPhoto.isShowing()) {
            mProgressDialogUserPhoto.dismiss();
        }
    }



    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(FirstName.getText().toString())) {
            FirstName.setError("Required");
            result = false;
        } else {
            FirstName.setError(null);
        }

        if (TextUtils.isEmpty(LastName.getText().toString())) {
            LastName.setError("Required");
            result = false;
        } else {
            LastName.setError(null);
        }


        return result;
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.finish_sign_up) {
            FinishSignUp();

        } else  {

        }
    }


}
