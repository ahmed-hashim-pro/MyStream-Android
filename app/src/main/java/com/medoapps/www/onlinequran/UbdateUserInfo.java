package com.medoapps.www.onlinequran;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UbdateUserInfo extends AppCompatActivity {

    private static final String TAG = "UbdateUserInfo";

    private EditText FirstName;
    private EditText LastName;
    private ProgressDialog mProgressDialogUserPhoto;

    String first_name = null;
    String last_name =null;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubdate_user_info);


        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirstName = findViewById(R.id.first_name_ubdate);
        LastName = findViewById(R.id.last_name_ubdate);
    }

    public void saveinformation(View view) {

        Log.d(TAG, "Ubdate_Info");
        if (!validateForm()) {
            return;
        }
        showProgressDialog(getString(R.string.get_information));

        first_name = FirstName.getText().toString();
        last_name = LastName.getText().toString();

        mDatabase.child("users").child(getUid()).child("firstname").setValue(first_name);
        mDatabase.child("users").child(getUid()).child("lastname").setValue(last_name);
        finish();

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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
