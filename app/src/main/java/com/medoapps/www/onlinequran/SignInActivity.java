package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.R.id.adView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.medoapps.www.onlinequran.util.AppBottomSheet;
import androidx.core.content.IntentCompat;
import androidx.transition.Slide;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.data.SharedPreferenceHelper;
import com.medoapps.www.onlinequran.data.StaticConfig;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.models.UserTypes;
import com.medoapps.www.onlinequran.service.AuthService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.HashMap;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private AdView mAdView;
    private static final int RC_TAKE_PICTURE = 101;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private int RC_SIGN_IN = 0;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private BroadcastReceiver mBroadcastReceiver;


    private String firstName;
    private String lastName;
    private TextView explain;
    private TextInputLayout mEmailField;
    private TextInputLayout mPasswordField;

    private Button mSignInButton;
    private Button mSignUpButton;
    private Button button_change_mode;
    private ProgressDialog mProgressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    private LinearLayout checkPolicyLayout;
    private CheckBox policyAcceptCheck;
    private Button termsButton, privacyButton;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    Button emailSignInTap;
    LinearLayout email_Login_Container;
    LinearLayout bigger_container;
    Boolean isEmail_Login_ContainerShow = false;
    private enum pageMode{
        SIGN_IN ,
        SIGN_UP
    }

    pageMode currentActiveMode = pageMode.SIGN_IN;
    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(getApplicationContext());
        setContentView(R.layout.activity_sign_in);
        authService = new AuthService(SignInActivity.this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        checkIsAnonymousSignIn();
        checkUserSignin();
        // Views
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);
        mSignInButton = findViewById(R.id.button_sign_in);
        mSignUpButton = findViewById(R.id.button_sign_up);
        button_change_mode = findViewById(R.id.button_change_mode);
        checkPolicyLayout = findViewById(R.id.checkPolicyLayout);
        policyAcceptCheck = findViewById(R.id.policyAcceptCheck);
        termsButton = findViewById(R.id.termsButton);
        privacyButton = findViewById(R.id.privacyButton);
        explain = findViewById(R.id.textexolain);
        emailSignInTap = findViewById(R.id.emailSignInTap);
        email_Login_Container = findViewById(R.id.email_Login_Container);
        bigger_container = findViewById(R.id.bigger_container);
//        explain.setText(getString(R.string.explain_signin));

        email_Login_Container.setVisibility(View.GONE);
        mSignUpButton.setVisibility(View.GONE);
        checkPolicyLayout.setVisibility(View.GONE);

        // Click listeners
        mSignInButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
        button_change_mode.setOnClickListener(this);
        emailSignInTap.setOnClickListener(this);
        termsButton.setOnClickListener(this);
        privacyButton.setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        mEmailField.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Perform your Actions here.
//                    signIn();
                }
                return handled;
            }
        });

        mPasswordField.getEditText().setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    signIn();
                    return true;
                }
                return false;
            }
        });
        mPasswordField.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    //Perform your Actions here.
                    signIn();
                }
                return handled;
            }
        });



//load banner ad
        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }



        });

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.buttonFacebookLogin);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    public void checkIsAnonymousSignIn(){
        if(authService.isAnonymousSignIn()){
            startMainActivity();
        }
    }


    /*@Override
    public void onBackPressed() {
        // Move the task containing the MainActivity to the back of the activity stack, instead of
        // destroying it. Therefore, MainActivity will be shown when the user switches back to the app.
        moveTaskToBack(true);
    }*/

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto)) {
            onUploadResultIntent(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

//        checkUserSignin();

    }
    private void startReminderService(){
        try {
            startService(new Intent(SignInActivity.this, QuranListenTimerService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkUserSignin(){
        // Check auth on Activity start
        StorageUtil storageUtil = new StorageUtil(this);
        Boolean ISProfileCompleted = storageUtil.loadProfileCompleted();

        if (mAuth.getCurrentUser() != null) {

            StaticConfig.UID = getUid();
            if (ISProfileCompleted){
                startMainActivity();
            }else{
                showProgressDialog();

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //User Exists , No Need To add new data.
                            //Get previous data from firebase. It will take previous data as soon as possible..

//                        User user = (User) dataSnapshot.getChildren("users/" + getUid());
                            User user =  dataSnapshot.getValue(User.class);
                            hideProgressDialog();
                            if (user.ProfileCompleted != null && user.ProfileCompleted == true){
                                storageUtil.storeProfileCompleted(true);
                                startMainActivity();
                            }else if (user.ProfileCompleted != null && user.ProfileCompleted == false){
                                storageUtil.storeProfileCompleted(false);
                                startMainActivity();
                            }else{
                                storageUtil.storeProfileCompleted(true);
                                startMainActivity();
                            }

                        } else {
//                        hideProgressDialog();
//                        onAuthSuccess(task.getResult().getUser());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


            //onAuthSuccess(mAuth.getCurrentUser());
        }
    }
    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //check if user is already in database or not
                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
                            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        //User Exists , No Need To add new data.
                                        //Get previous data from firebase. It will take previous data as soon as possible..

                                        hideProgressDialog();
                                        saveUserInfo();
                                        checkUserSignin();
//                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                        //startActivity(new Intent(SignInActivity.this, UserInformation.class));
//                                        finish();
                                        return;
                                    } else {
                                        hideProgressDialog();
//                                        Toast.makeText(SignInActivity.this, "add data to database", Toast.LENGTH_SHORT).show();
                                        onAuthSuccess(task.getResult().getUser());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");


                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
                            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        //User Exists , No Need To add new data.
                                        //Get previous data from firebase. It will take previous data as soon as possible..

                                        hideProgressDialog();
                                        saveUserInfo();
                                        checkUserSignin();

//                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                        //startActivity(new Intent(SignInActivity.this, UserInformation.class));
//                                        finish();
                                        return;
                                    } else {
                                        hideProgressDialog();
                                        onAuthSuccess(task.getResult().getUser());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            hideProgressDialog();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getEditText().getText().toString();
        String password = mPasswordField.getEditText().getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());


                        if (task.isSuccessful()) {
                            hideProgressDialog();
                            // Go to MainActivity
                            saveUserInfo();
                            checkUserSignin();

//                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            //startActivity(new Intent(SignInActivity.this, UserInformation.class));
//                            finish();
                        } else {
                            hideProgressDialog();
                            Toast.makeText(SignInActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getEditText().getText().toString();
        String password = mPasswordField.getEditText().getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());


                        if (task.isSuccessful()) {
                            hideProgressDialog();
                            onAuthSuccessEmailAndPassword(task.getResult().getUser());

                        } else {
                            hideProgressDialog();
                            Toast.makeText(SignInActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onAuthSuccessEmailAndPassword(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());
        String profilephoto ="https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/ic_action_account_circle_40.png?alt=media&token=93fe3dc5-f9a0-462c-ba57-4e77847baff6";

        if (username.contains(" ")) {
            firstName= username.split(" ")[0];
            lastName= username.split(" ")[1];
        } else {
            firstName= username;
            lastName= "";
        }
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail(),firstName,lastName,profilephoto);


        // Go to MainActivity
        //startActivity(new Intent(SignInActivity.this, MainActivity.class));
        startMainActivity();
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        String profilephoto = user.getPhotoUrl().toString();
        String DisplayName = user.getDisplayName();
        if (DisplayName.contains(" ")) {
            firstName= DisplayName.split(" ")[0];
            lastName= DisplayName.split(" ")[1];
        } else {
            firstName= DisplayName;
            lastName= "";
        }
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail(),firstName,lastName,profilephoto);

        // Go to MainActivity
        //startActivity(new Intent(SignInActivity.this, MainActivity.class));
        startMainActivity();
    }

    private void startUserInformation(){
        startActivity(new Intent(SignInActivity.this, UserInformation.class));
        finish();
    }
    private void startMainActivity(){
        startReminderService();
        // Go to MainA+ctivity
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }
    private String usernameFromEmail(String email) {
        if (email != null){
            if ( email.contains("@")) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();

                if (mFileUri != null) {
                    uploadFromUri(mFileUri);
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                showProgressDialog();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        /*// Clear the last download, if any
        updateUI(mAuth.getCurrentUser());
        mDownloadUrl = null;*/

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI_UserPhoto, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD_UserPhoto));

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_uploading));
    }


    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getEditText().getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getEditText().getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        if (currentActiveMode == pageMode.SIGN_UP){
            if (!policyAcceptCheck.isChecked()) {
                policyAcceptCheck.setError("Required");
                result = false;
            } else {
                policyAcceptCheck.setError(null);
            }
        }




        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email,String FirstName,String LastName,String profilephoto ) {

        Long Date = new SeparateFunctions(SignInActivity.this).getTimeStamp();
        User user = new User(userId,name, email,FirstName,LastName,profilephoto,StaticConfig.STR_DEFAULT_BASE64,false,false , UserTypes.Listener,Date,Date);

        mDatabase.child("users").child(userId).setValue(user);
        increaseUsersCount(FirebaseDatabase.getInstance().getReference().child("GlobalVariable").child("UsersCount"));

    }

    private void increaseUsersCount(DatabaseReference postRef) {
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


    // [END basic_write]
    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("Image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);
    }
    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = IntentCompat.getParcelableExtra(intent, MyUploadService.EXTRA_DOWNLOAD_URL_UserPhoto, Uri.class);
        mFileUri = IntentCompat.getParcelableExtra(intent, MyUploadService.EXTRA_FILE_URI_UserPhoto, Uri.class);

        //updateUI(mAuth.getCurrentUser());
    }

    private void showMessageDialog(String title, String message) {
        AppBottomSheet.showMessage(this, title, message);
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

    public void hideProgressDialoghere() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
            signIn();
        } else if (i == R.id.button_sign_up) {
            signUp();
        }
        else if (i == R.id.sign_in_button) {
            signIngoogle();
        }
        else if (i == R.id.emailSignInTap){
            toggleFade();
        }
        else if (i == R.id.button_change_mode){
            changeMode();
        }
        else if (i == R.id.termsButton){
            goToTerms();
        }
        else if (i == R.id.privacyButton){
            goToPrivacyPolicy();
        }

    }

    private void changeMode(){
        if (currentActiveMode == pageMode.SIGN_IN )
        {
            currentActiveMode = pageMode.SIGN_UP;
            mSignUpButton.setVisibility(View.VISIBLE);
            checkPolicyLayout.setVisibility(View.VISIBLE);
            mSignInButton.setVisibility(View.GONE);
            button_change_mode.setText(getString(R.string.sign_in));
            emailSignInTap.setText(getString(R.string.sign_up_with_email));


        }else{

            currentActiveMode = pageMode.SIGN_IN;
            mSignUpButton.setVisibility(View.GONE);
            checkPolicyLayout.setVisibility(View.GONE);
            mSignInButton.setVisibility(View.VISIBLE);
            button_change_mode.setText(getString(R.string.sign_up));
            emailSignInTap.setText(getString(R.string.sign_in_with_email));

        }


    }
    private void goToTerms(){

        Intent intent = new Intent(SignInActivity.this, MyWebView.class);
        intent.putExtra("url", "https://online-quran-3b07c.web.app/terms-of-service");

        startActivity(intent);
    }
    private void goToPrivacyPolicy(){

        Intent intent = new Intent(SignInActivity.this, MyWebView.class);
        intent.putExtra("url", "https://online-quran-3b07c.web.app/privacy");

        startActivity(intent);
    }



    private void toggle() {
        Transition transition = new Fade();

        transition.setDuration(900);
        transition.addTarget(R.id.email_Login_Container);

        TransitionManager.beginDelayedTransition(bigger_container, transition);
        email_Login_Container.setVisibility(View.VISIBLE);
    }
    private void toggleFade() {
        Slide transition = new Slide(Gravity.BOTTOM);

        transition.setDuration(900);
        transition.addTarget(R.id.email_Login_Container);
        TransitionManager.beginDelayedTransition(bigger_container);
        email_Login_Container.setVisibility(!isEmail_Login_ContainerShow ? View.VISIBLE : View.GONE);

        emailSignInTap.setCompoundDrawablesWithIntrinsicBounds(isEmail_Login_ContainerShow ?R.drawable.outline_email_24:0, 0, !isEmail_Login_ContainerShow ?R.drawable.outline_expand_less_24:R.drawable.outline_expand_more_24, 0);

        emailSignInTap.setBackgroundColor(getResources().getColor(!isEmail_Login_ContainerShow ?R.color.grey_500:R.color.colorPrimaryDark));
        isEmail_Login_ContainerShow = !isEmail_Login_ContainerShow;
    }


    private void signIngoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    void saveUserInfo() {
        StaticConfig.UID = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                User userInfo = new User();
                userInfo.firstname = (String) hashUser.get("firstnamef");
                userInfo.email = (String) hashUser.get("email");
                userInfo.avata = (String) hashUser.get("avata");
                SharedPreferenceHelper.getInstance(SignInActivity.this).saveUserInfo(userInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // TODO ; add phone number auth
}
