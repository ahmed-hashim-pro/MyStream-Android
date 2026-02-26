package com.medoapps.www.onlinequran.util;

import static java.text.DateFormat.getDateTimeInstance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.messaging.FirebaseMessaging;
import com.medoapps.www.onlinequran.BuildConfig;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.SettingSaved;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SeparateFunctions {


    private static final String TAG = "SeparateFunctions";
    private Context context;

    public SeparateFunctions(Context context) {
        this.context = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void onShareBy() {



        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = this.context.getString(R.string.sharemessage) + "  https://rebrand.ly/notfof70d";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        this.context.startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }

    public void generateAppShareLink(Activity activity){
        Uri imageUri = Uri.parse(context.getString(R.string.dynamicLinkShareImage));

        SeparateFunctions separateFunctions = new SeparateFunctions(context);
        separateFunctions.createDynamicLink(activity,"welcome",context.getString(R.string.app_name),context.getString(R.string.sharemessage),imageUri).addOnCompleteListener( activity, new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();


                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = context.getString(R.string.sharemessage) +"  "+ shortLink;
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));


                } else {
                    Log.d(TAG, "createDynamicLink 2: " +task);
                    // Error
                }
            }});
    }

    public void showNativeDialog(String title, String Message, final Runnable func){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(Message);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
//                showRewardedVideo();
                func.run();

            }
        });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    public void showNewCustomDialog(String title, String Message,String PositiveText,String NegativeText, final Runnable func , int icon){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View myView = inflater.inflate(R.layout.custom_dialog, null);

        TextView dialogTitle = myView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = myView.findViewById(R.id.dialogDescription);
        ImageView dialogIcon = myView.findViewById(R.id.dialogIcon);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,R.style.CustomAlertDialog);

        alertDialog.setView(myView)
                // Add action buttons
                .setPositiveButton(PositiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        func.run();
                    }
                })
                .setNegativeButton(NegativeText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        LoginDialogFragment.this.getDialog().cancel();
                    }
                });

        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogIcon.setImageResource(icon);

        if(!((Activity) context).isFinishing())
        {
            AlertDialog alert11 = alertDialog.create();
            alert11.show();

//        alertDialog.show();
            Button b = alert11.getButton(DialogInterface.BUTTON_POSITIVE);
            b.setBackgroundColor(context.getResources().getColor(R.color.purple));
            b.setTextColor(context.getResources().getColor(R.color.toolbarTextColor));
        }


    }
    public void showNewCustomWithFinishDialog(String title, String Message,String PositiveText,String NegativeText, final Runnable func, final Runnable negativeFunc , int icon){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View myView = inflater.inflate(R.layout.custom_dialog, null);

        TextView dialogTitle = myView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = myView.findViewById(R.id.dialogDescription);
        ImageView dialogIcon = myView.findViewById(R.id.dialogIcon);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,R.style.CustomAlertDialog);

        alertDialog.setView(myView)
                // Add action buttons
//                .setCustomTitle(dialogtitle)
                .setPositiveButton(PositiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        func.run();
                    }
                })
                .setNegativeButton(NegativeText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        LoginDialogFragment.this.getDialog().cancel();
                        negativeFunc.run();
                    }
                });

        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogIcon.setImageResource(icon);

        AlertDialog alert11 = alertDialog.create();
        alert11.show();

//        alertDialog.show();
        Button b = alert11.getButton(DialogInterface.BUTTON_POSITIVE);
        b.setBackgroundColor(context.getResources().getColor(R.color.purple));
        b.setTextColor(context.getResources().getColor(R.color.toolbarTextColor));
    }

    public void showSharingDialog(String title, String Message,String PositiveText,String NegativeText, final Runnable func , int icon){


        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View myView = inflater.inflate(R.layout.custom_dialog, null);

            TextView dialogTitle = myView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = myView.findViewById(R.id.dialogDescription);
            ImageView dialogIcon = myView.findViewById(R.id.dialogIcon);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,R.style.CustomAlertDialog);

            alertDialog.setView(myView)
                    // Add action buttons
    //                .setCustomTitle(dialogtitle)
                    .setPositiveButton(PositiveText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // sign in the user ...
                            func.run();
                        }
                    })
                    .setNegativeButton(NegativeText, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
    //                        negativeFunc.run();
                        }
                    });

            dialogTitle.setText(title);
            dialogMessage.setText(Message);
            dialogIcon.setImageResource(icon);

            AlertDialog alert11 = alertDialog.create();
            alert11.show();

//        alertDialog.show();
            Button b = alert11.getButton(DialogInterface.BUTTON_POSITIVE);
            b.setBackgroundColor(context.getResources().getColor(R.color.purple));
            b.setTextColor(context.getResources().getColor(R.color.toolbarTextColor));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }
    @Nullable
    public File getAppSpecificDownloadStorageDir(Context context,Activity activity) {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
//        File file = new File(context.getExternalFilesDir(null), albumName);
//        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC), albumName);
//        File file = new File(Environment.getExternalStoragePublicDirectory("//albumName"),"//SampleFolder");
        /*File file = new File(Environment.getExternalStorageDirectory().getPath()+ albumName);


        if (file == null || !file.mkdirs()) {
            Log.e(TAG, "Directory not created");
            file.mkdirs();
        }
        Log.d(TAG, String.valueOf(file));
        Log.d(TAG, "getAppSpecificAlbumStorageDir: " + Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
//        Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
*/

         File dir;
        if (Build.VERSION_CODES.Q > Build.VERSION.SDK_INT) {
            dir = new File(Environment.getExternalStorageDirectory()+ "/" + "MyStream","AhmedHashim_"+"/");

        } else {

//            dir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getPath() + "//MyStream//AhmedHashim_");
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),   "MyStream/AhmedHashim_");
//            dir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC),   "MyStream/AhmedHashim_");

            if (!dir.exists()){
                dir.mkdirs();
            }

        }



        Log.d(TAG, String.valueOf(dir));

        return dir;
    }
    public Uri getAudioCollection(){
        Uri audioCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollection = MediaStore.Audio.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        return audioCollection;
    }

    public void changeAppThemeGlobally(){
        SettingSaved settingSaved = new SettingSaved(context);
        settingSaved.LoadData();
        switch (SettingSaved.currentThemeMode ){
            case AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;


            case AppCompatDelegate.MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;

        }

    }

    public void openRationgIntent(){
        Uri uri = Uri.parse("market://details?id=" + SettingSaved.APPURL);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + SettingSaved.APPURL)));
        }
        SettingSaved.IsRated = 1;
        SettingSaved sv = new SettingSaved(context);
        sv.SaveData();
    }
    public void rateAppInAppReview(Activity activity){
        ReviewManager manager = ReviewManagerFactory.create(context);
//        ReviewManager manager = new FakeReviewManager(context);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d(TAG, "rateAppInAppReview: "+task.getResult());
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                launchReviewFlow(manager,reviewInfo,activity);
            } else {
                // There was some problem, log or handle the error code.
//                @ReviewErrorCode int reviewErrorCode = ((TaskException) task.getException()).getErrorCode();
            }
        });
    }

    public void launchReviewFlow(ReviewManager manager, ReviewInfo reviewInfo, Activity activity){

        Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
        flow.addOnCompleteListener(task -> {
            Log.d(TAG, "launchReviewFlow: "+task);
            // The flow has finished. The API does not indicate whether the user
            // reviewed or not, or even whether the review dialog was shown. Thus, no
            // matter the result, we continue our app flow.
        });
    }

    public com.google.android.gms.tasks.Task<ShortDynamicLink> createDynamicLink(Activity activity, String endPoint,String ImageUrl,String socialDescription,Uri socialImageUrl){

        int VERSION_CODE;
        try {
            VERSION_CODE = BuildConfig.VERSION_CODE;
        } catch (Exception e) {
            VERSION_CODE = 40;
            e.printStackTrace();
        }

        com.google.android.gms.tasks.Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://online-quran-3b07c.web.app/"+endPoint))
//                .setLink(Uri.parse("https://www.mystream.com/"+endPoint))
                .setDomainUriPrefix("https://mystream.page.link")
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(ImageUrl)
                                .setDescription(socialDescription)
                                .setImageUrl(socialImageUrl)
                                .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.medoapps.www.onlinequran").setMinimumVersion(VERSION_CODE).build())



                // Set parameters
                // ...
                .buildShortDynamicLink(3)
                /*.addOnCompleteListener(activity, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.d(TAG, "createDynamicLink 2: " +shortLink);
                            Log.d(TAG, "createDynamicLink 3: " +flowchartLink);
                        } else {
                            Log.d(TAG, "createDynamicLink 2: " +task);
                            // Error
                            // ...
                        }
                    }

                })*/
                ;


        return shortLinkTask;
//        shortLinkTask.getResult();
    }

    public static String getLocaleStringResource(Locale requestedLocale, int resourceId, Context context) {
        String result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // use latest api
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(requestedLocale);
            result = context.createConfigurationContext(config).getText(resourceId).toString();
        }
        else { // support older android versions
            Resources resources = context.getResources();
            Configuration conf = resources.getConfiguration();
            Locale savedLocale = conf.locale;
            conf.locale = requestedLocale;
            resources.updateConfiguration(conf, null);

            // retrieve resources from desired locale
            result = resources.getString(resourceId);

            // restore original locale
            conf.locale = savedLocale;
            resources.updateConfiguration(conf, null);
        }

        return result;
    }

    public Long getTimeStamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.getTime();
    }


    public static String getTimeDateFromTimeStamp(long timestamp){
        try{
            DateFormat dateFormat = getDateTimeInstance();
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "date";
        }
    }

    public com.google.android.gms.tasks.Task<String> getCurrentFCMToken(){
        final String[] token = new String[1];
       return FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
           @Override
           public void onComplete(@NonNull com.google.android.gms.tasks.Task<String> task) {
               if (!task.isSuccessful()) {
                   Log.w(TAG, "FCM registration token failed", task.getException());
                   return;
               }

               // Get new FCM registration token
               Log.d(TAG, "onCreate: " +task.getResult());


           }
       });



    }
    public  String randomName() {
        char[] chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray();
        StringBuilder sb1 = new StringBuilder();
        Random random1 = new Random();
        for (int i = 0; i < 9; i++)
        {
            char c1 = chars1[random1.nextInt(chars1.length)];
            sb1.append(c1);
        }
        return sb1.toString();
    }

}
