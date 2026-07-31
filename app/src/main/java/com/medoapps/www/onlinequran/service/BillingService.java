package com.medoapps.www.onlinequran.service;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;

/**
 * Google Play subscription for "support My Stream".
 *
 * Written against the ProductDetails API. Billing 8 removed SkuDetails outright
 * (SkuDetailsParams, querySkuDetailsAsync, BillingClient.SkuType), so the old
 * implementation could not merely be version-bumped.
 *
 * Ownership: each instance builds its own BillingClient and must be released with
 * {@link #release()} — the connection is a bound service, and leaking one per
 * Activity.onResume() is what the previous version did.
 */
public class BillingService {

    private static final String TAG = "HashimBillingService";

    /** The single subscription product this app sells. */
    private static final String PRODUCT_ID = "support_mystream_1996";

    private final Context context;
    private final Activity activity;
    private final DatabaseReference mDatabase;
    private BillingClient billingClient;

    public BillingService(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());

        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                // Billing 8 requires the product types that may go PENDING to be declared
                // up front; the no-argument enablePendingPurchases() is gone.
                .enablePendingPurchases(PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build())
                .build();
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        Log.d(TAG, "onPurchasesUpdated: " + billingResult);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchaseForNonConsumable(purchase);
            }
        }
    };

    /**
     * Connect, then start the purchase flow for the subscription. Called from the
     * "subscribe" card, so launching the flow on success preserves the previous behaviour.
     */
    public void startConnection() {
        connect(this::queryProductDetailsThenLaunch);
    }

    /**
     * Query existing purchases. Unlike the previous version this connects first — a
     * queryPurchasesAsync on a client that was never connected always fails with
     * SERVICE_DISCONNECTED, so the old onResume() call could never have returned anything.
     */
    public void FetchingPurchases() {
        connect(this::queryPurchases);
    }

    private void connect(Runnable onReady) {
        if (context == null || billingClient == null) return;
        if (billingClient.isReady()) {
            onReady.run();
            return;
        }
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    onReady.run();
                } else {
                    Log.d(TAG, "onBillingSetupFinished: not OK " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Left to the next request to reconnect, as before.
            }
        });
    }

    private void queryProductDetailsThenLaunch() {
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(PRODUCT_ID)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, result) -> {
            List<ProductDetails> list = result.getProductDetailsList();
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK
                    || list == null || list.isEmpty()) {
                // The old code indexed get(0) unconditionally and crashed whenever the
                // product was unavailable — unreleased, wrong package, or no Play account.
                Log.d(TAG, "queryProductDetails: nothing returned for " + PRODUCT_ID);
                return;
            }
            launchBillingFlow(list.get(0));
        });
    }

    private void launchBillingFlow(ProductDetails productDetails) {
        String offerToken = firstOfferToken(productDetails);
        if (offerToken == null) {
            Log.d(TAG, "launchBillingFlow: no subscription offer on " + productDetails.getProductId());
            return;
        }
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                // Subscriptions must name the offer being bought; this is
                                // the parameter that has no equivalent in the SkuDetails API.
                                .setOfferToken(offerToken)
                                .build()))
                .build();
        BillingResult result = billingClient.launchBillingFlow(activity, billingFlowParams);
        Log.d(TAG, "launchBillingFlow: " + result.getResponseCode());
    }

    @Nullable
    private static String firstOfferToken(ProductDetails details) {
        List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
        if (offers == null || offers.isEmpty()) return null;
        return offers.get(0).getOfferToken();
    }

    private void handlePurchaseForNonConsumable(Purchase purchase) {
        AcknowledgePurchaseResponseListener listener = billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "onAcknowledgePurchaseResponse: ok");
                mDatabase.child("isSubscribedPremium").setValue(true);
            }
        };
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                && !purchase.isAcknowledged()) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(params, listener);
        }
    }

    private void queryPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                                         @NonNull List<Purchase> list) {
                        Log.d(TAG, "onQueryPurchasesResponse: " + billingResult + " " + list);
                        // Re-acknowledge anything Play already granted but the app never
                        // confirmed — Play refunds an unacknowledged purchase after 3 days.
                        for (Purchase purchase : list) {
                            handlePurchaseForNonConsumable(purchase);
                        }
                    }
                });
    }

    /** Ends the Play connection. Safe to call more than once. */
    public void release() {
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }

    private String getUid() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest_mode";
    }
}
