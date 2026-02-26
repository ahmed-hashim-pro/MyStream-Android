package com.medoapps.www.onlinequran.service;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BillingService {

    private static final String TAG = "HashimBillingService";

    Context context;
    Activity activity;
    private BillingClient billingClient;
    List<String> skuList = new ArrayList<>();
    private DatabaseReference mDatabase;

    SkuDetails skuDetails;
    public BillingService(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: dsadsada" +snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            Log.d(TAG, "onPurchasesUpdated: " + billingResult);
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    Log.d(TAG, "onPurchasesUpdated: " + purchase);
                    handlePurchaseForNonConsumable(purchase);

                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }

        }
    };



    public void startConnection(){
        if (context == null)
            return;


        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "onBillingSetupFinished: " );

                    querySkuDetails();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private void querySkuDetails(){
        skuList.add("support_mystream_1996");
        skuList.add("support_mystream_1996");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {

                        // Process the result.
                        Log.d(TAG, "onSkuDetailsResponse: " + skuDetailsList);
                        skuDetails = (SkuDetails) skuDetailsList.get(0);
                        Log.d(TAG, "onSkuDetailsResponse: " + skuDetails.hashCode());
                        Log.d(TAG, "onSkuDetailsResponse: " + skuDetails.getDescription());

                        launchBillingFlow(skuDetails);
                    }
                });
    }

    private void launchBillingFlow(SkuDetails skuDetails){
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();

        if (responseCode == BillingClient.BillingResponseCode.OK){
            Log.d(TAG, "launchBillingFlow: responseCode" + BillingClient.BillingResponseCode.OK);
            // success

        }

    }


    private void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.
//        Purchase purchase = purchase;

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);


    }
    private void handlePurchaseForNonConsumable(Purchase purchase) {

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    Log.d(TAG, "onAcknowledgePurchaseResponse: ");
                    mDatabase.child("isSubscribedPremium").setValue(true);
                }
            }
        };
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }


    }

    public void FetchingPurchases(){

        /**
         * https://developer.android.com/google/play/billing/integrate#fetch
         * To handle these situations, be sure that your app calls BillingClient.queryPurchasesAsync()
         * in your onResume() method to ensure that all purchases are successfully processed
         * as described in processing purchases.
         */
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS,new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                Log.d(TAG, "onQueryPurchasesResponse: billingResult " + billingResult);
                Log.d(TAG, "onQueryPurchasesResponse: List<Purchase> " + list);
            }
        });
    }

    private String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser()!= null){

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else {
            return "guest_mode";
        }
    }
}
