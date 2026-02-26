package com.medoapps.www.onlinequran.service;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.SettingSaved;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

public class ReportService {

    private DatabaseReference mDatabase;
    private SettingSaved settingSaved;
    private Activity activity;
    private Context context;



    public ReportService(Activity activity,Context context) {
        this.activity = activity;
        this.context = context;
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("reports").child(getUid());
        settingSaved = new SettingSaved(getApplicationContext());
        settingSaved.LoadData();

        if (SettingSaved.reportsNumberForShowSharingDialog == 4){
            showSharingDialog();
            SettingSaved.reportsNumberForShowSharingDialog ++ ;
            settingSaved.SaveData();
        }else{
            SettingSaved.reportsNumberForShowSharingDialog ++ ;
            settingSaved.SaveData();
        }
         if (!settingSaved.isUserCountAsReporter){
             this.mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange( DataSnapshot snapshot) {
                     if (!snapshot.exists()) {
                         increaseReportsCount(FirebaseDatabase.getInstance().getReference().child("GlobalVariable").child("ReportsCount"));
                         //store that the user is count as reporter in phone storage to decrease requests number
                         settingSaved.LoadData();
                         settingSaved.isUserCountAsReporter = true;
                         settingSaved.SaveData();

                     }else{
                         settingSaved.LoadData();
                         settingSaved.isUserCountAsReporter = true;
                         settingSaved.SaveData();
                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });

         }
    }
    private void increaseReportsCount(DatabaseReference postRef) {
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


    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    public void createReciteReport(Report report){
        String key = mDatabase.push().getKey();
        report.id = key;
        mDatabase.child(key).setValue(report);

    }

    public void createSurahReport(Report report){
        String key = mDatabase.push().getKey();
        report.id = key;
        mDatabase.child(key).setValue(report);

    }

    private void showSharingDialog(){

        new SeparateFunctions(context).showSharingDialog(context.getString(R.string.separeteapp),context.getString(R.string.shareDesc),context.getString(R.string.shareNow),context.getString(R.string.later),generateShareLingRunnable,R.drawable.outline_share_24_settings);

    }


    Runnable generateShareLingRunnable = new Runnable() {
        @Override
        public void run() {
            new SeparateFunctions(getApplicationContext()).generateAppShareLink(activity);
//            goToMarket();
        }
    };


}
