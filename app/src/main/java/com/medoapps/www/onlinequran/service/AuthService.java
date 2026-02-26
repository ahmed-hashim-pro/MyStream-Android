package com.medoapps.www.onlinequran.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CountDownLatch;

public class AuthService {
    Context context;

    private FirebaseAuth mAuth;
    private boolean isServiceStarted;
    private static final String TAG = "AnonymousAuth";

    public AuthService(Context context){
        this.context=context;

        if (!this.isServiceStarted){
            // Initialize Firebase Auth
            initFirebase();
            this.isServiceStarted = true;

        }
    }

    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();

    }

    public boolean isUserSignedIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }
    public boolean isAnonymousSignIn(){

        if (mAuth.getCurrentUser() != null){
            return mAuth.getCurrentUser().isAnonymous();
        }else{
            return false;
        }
    }

    public void asyncSignInAnonymously(){
        AsyncTaskRunner runner = new AsyncTaskRunner();
        //String sleepTime = time.getText().toString();
        runner.execute("3");

    }
    public boolean signInAnonymously(){
        final CountDownLatch[] latch = {new CountDownLatch(1)};
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("");
        progressDialog.show();
        final boolean[] inProgress = {true};
        mAuth.signInAnonymously()
                .addOnCompleteListener((Activity) this.context, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            inProgress[0] = false;
                            //signInCallback(user);
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            inProgress[0] = false;

                            //signInCallback(null);


                            throw new Error("not authed");
                            /*Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();*/
                            //updateUI(null);
                        }
                    }
                });

        if (inProgress[0]){

            try {
                //latch[0].await();
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
                progressDialog.dismiss();

            }
        }

        progressDialog.dismiss();
        return isAnonymousSignIn();



    }
    private boolean signInCallback(FirebaseUser user){

        try {
            //Toast.makeText(context, user.getUid(), Toast.LENGTH_SHORT).show();
            return user.getUid() != null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("not authed");

            //return false;
        }

    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                //int time = Integer.parseInt(params[0])*1000;
                signInAnonymously();
                CountDownLatch latch = new CountDownLatch(1);

                while (mAuth.getUid() == null){
                    latch.await();
                    Thread.sleep(1000);
                }
                resp = "Slept for " + params[0] + " seconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
           // finalResult.setText(result);
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context,
                    "ProgressDialog",
                    "Wait for  seconds");
        }


        @Override
        protected void onProgressUpdate(String... text) {
           // finalResult.setText(text[0]);

        }
    }
}
