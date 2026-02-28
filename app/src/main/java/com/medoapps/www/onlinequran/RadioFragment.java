package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;
import static com.medoapps.www.onlinequran.R.id.adView;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.models.ReportType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.service.ReportService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardItem;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;


public class RadioFragment extends Fragment implements  AdapterView.OnItemSelectedListener {
    //banner add
    private static final String TAG = "RecitesName";
    private AdView mAdView;
    //private InterstitialAd mInterstitialAd;
    //private RewardedVideoAd mAd;
    static RadioFragment instance4;
    private PreferenceManager prefManager;
    public static final String PAGE_TITLE = "Tab2";

    TextView recitelisttxt;
    Boolean new_ubdate =false;
    FirebaseRemoteConfig mFirebaseRemoteConfig;


    public ArrayList<AuthorClass> listrecites = new ArrayList<AuthorClass>();
    RecyclerView lVRecites;

    String RecitesName="";
    String RecitesAYA="";
    String Rewayat="";
    String RealRecitesName="";
    SearchView searchView;

    View AyaNameView;
    View AyaImage;
    // Remote Config keys
    private static final String LOADING_PHRASE_CONFIG_KEY = "loading_phrase";
    private static final String urlUpdate = "url";
    private static final String chicUpdate = "new_ubdate";
    private static final String new_ubdate2 = "new_ubdate2";
    private static final String url2 = "url2";
    private static final String url3 = "url3";
    private static final String new_ubdate3 = "new_ubdate3";
    private static final String ubdated_version = "ubdated_version";

    private boolean isSubscribedPremium;

    private List<Object> recyclerViewItems = new ArrayList<>();
    public static final int ITEMS_PER_AD = 20;
    String surahName ;

    public static RadioFragment newInstance() {
        RadioFragment fragment = new RadioFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_radio_fragment, container, false);
    }

    public void searchManager(){
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) getView().findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recitelisttxt.setVisibility(View.VISIBLE);

                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recitelisttxt.setVisibility(View.GONE);

            }
        });
        //final Context co=this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                List<Object> recyclerViewItemstemp = new ArrayList<>();
                for (Object listrecitesitem : recyclerViewItems) {
                    if (listrecitesitem instanceof AuthorClass){
                        AuthorClass kk = (AuthorClass) listrecitesitem;

                        if (kk.RealName.contains(newText)) {
                            recyclerViewItemstemp.add(listrecitesitem);
                        }
                    }
                }

                addBannerAds(recyclerViewItemstemp);
                loadBannerAds(recyclerViewItemstemp);
                lVRecites .setAdapter(new VivzAdapter(recyclerViewItemstemp));
                return false;
            }
        });
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettingSaved settingSaved=new SettingSaved(getContext());
        settingSaved.LoadData();
        instance4 =this;
        recitelisttxt = (TextView) getView().findViewById(R.id.recitelisttxt);

        searchManager();

        chicLastRecite();

        loadad();//to load ads full screen


        SettingSaved.IsOpen =  1;//App Is Opened
        SettingSaved.SounlLoad=1;//sound load
        //load banner ad
        mAdView = (AdView) getView().findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }



        });


        lVRecites = (RecyclerView) getView().findViewById(R.id.listView);
//get list of recites
        RadioLanguageClass lc = new RadioLanguageClass();
        listrecites = lc.AutherList();

//        List<String> carArray = Arrays.asList(getRewayahList());
        Spinner spinner = (Spinner) getView().findViewById(R.id.rewayah_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter =new  ArrayAdapter<String>(getActivity(),R.layout.spinner_dropdown_item,
                getRewayahList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(getString(R.string.All));
        spinner.setSelection(spinnerPosition);


        for (int i = 0; i < listrecites.size(); ++i) {

            AuthorClass temp = listrecites.get(i);

            recyclerViewItems.add(temp);
        }


        addBannerAds(recyclerViewItems);
        loadBannerAds(recyclerViewItems);
        lVRecites.setHasFixedSize(true);
        lVRecites.setLayoutManager(new LinearLayoutManager(getContext()));
        lVRecites.setAdapter(new VivzAdapter(recyclerViewItems));

        if(SettingSaved.OnTimeAds==false) {
            if(SettingSaved.IsRated==1){
                SettingSaved.OnTimeAds=true;
            }

        }

    }

    /**
     * Adds banner ads to the items list.
     * @param recyclerViewItems
     */
    private void addBannerAds(List<Object> recyclerViewItems) {
        if (SettingSaved.isSubscribedPremium)
            return;
        // Loop through the items array and place a new banner ad in every ith position in
        // the items List.
        for (int i = 5; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {
            try {
                final AdView adView = new AdView(requireContext());
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                recyclerViewItems.add(i, adView);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Sets up and loads the banner ads.
     * @param recyclerViewItems
     */
    private void loadBannerAds(List<Object> recyclerViewItems) {
        if (SettingSaved.isSubscribedPremium)
            return;
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically
        // in sequence).
        loadBannerAd(5,recyclerViewItems);
    }

    /**
     * Loads the banner ads in the items list.
     */
    private void loadBannerAd(final int index, List<Object> recyclerViewItems) {

        if (index >= recyclerViewItems.size()) {
            return;
        }

        Object item = recyclerViewItems.get(index);
        if (!(item instanceof AdView)) {
            return;
            /*throw new ClassCastException("Expected item at index " + index + " to be a banner ad"
                    + " ad.");*/
        }

        final AdView adView = (AdView) item;

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        // The previous banner ad loaded successfully, call this method again to
                        // load the next ad in the items list.
                        loadBannerAd(index + ITEMS_PER_AD, recyclerViewItems);
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // The previous banner ad failed to load. Call this method again to load
                        // the next ad in the items list.
                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Log.e(
                                "MainActivity",
                                "The previous banner ad failed to load with error: "
                                        + error
                                        + ". Attempting to"
                                        + " load the next banner ad in the items list.");
                        loadBannerAd(index + ITEMS_PER_AD, recyclerViewItems);
                    }
                });

        // Load the banner ad.
        adView.loadAd(new AdRequest.Builder().build());
    }
    private ArrayList<String> getRewayahList(){
        ArrayList<String> mylist = new ArrayList<String>();
//        String[] items = new String[listrecites.size()];
//        mylist.add(getString(R.string.All));

        int position = 0;
        for (AuthorClass listrecitesitem : listrecites) {
//            items[position] = listrecitesitem.Rewayat;
            if (listrecitesitem.Rewayat != ""){
                mylist.add(listrecitesitem.Rewayat);
            }
            position++;


        }
        HashSet hs = new HashSet();
        hs.addAll(mylist);
        mylist.clear();
        mylist.addAll(hs);
        mylist.add(0,getString(R.string.All));
        return mylist;
    }
    private void laodRadio(){
        Intent intent= new Intent( getActivity(),NewQuranPlayer.class);
        intent.putExtra("RecitesName",RecitesName);
        intent.putExtra("Rewayat",Rewayat);
        intent.putExtra("RealRecitesName",Rewayat);
        intent.putExtra("RecitesAYA",RecitesAYA);
        intent.putExtra("IsRadio",true);
//        Log.d(TAG, "laodRadio: " + intent.getExtras());


        View ayaNameView = AyaNameView;
//        View reciteNameView = ActivityReciter;
        View ayaImage = AyaImage;
        String transitionNameReciter = "shared_reciter";
        String transitionNameSurah = "shared_surah";
        String transitionNameSurahImage = "shared_surah_image";


        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (AyaNameView != null && AyaImage != null){
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                        Pair.create(ayaNameView, transitionNameSurah),
                        Pair.create(ayaImage, transitionNameSurahImage)

                );
//                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), ayaNameView, transitionNameSurah);
                startActivity(intent, options.toBundle());
            }else{
                startActivity(intent);
            }

        }else{

            startActivity(intent);
        }
        createReport(null);
    }

    private void createReport(String shareUrl){
        ReportType reportType;
        if (shareUrl!= null){
            reportType = ReportType.ShareRadio;
        }else{
            reportType = ReportType.OpenRadio;

        }
        if (getUid().equals("guest_mode"))
            return;

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                Long Date = new SeparateFunctions(getContext()).getTimeStamp();
//                Report report = new Report("",user.id,user.username,user.photourl, reportType,RecitesAYA,surahName,shareUrl,"",Date,"");
                Report report = new Report("",user.id,user.username,user.photourl, reportType,RecitesAYA,surahName,shareUrl,"",Date,0);
                ReportService reportService = new ReportService(getActivity(),getContext());
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser()!= null){

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else {
            return "guest_mode";
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ArrayList<AuthorClass> listrecitestemp = new ArrayList<AuthorClass>();
        List<Object> recyclerViewItemstemp = new ArrayList<>();

        adapterView.getItemAtPosition(i);
        String rewayah = adapterView.getItemAtPosition(i).toString();


        for (Object listrecitesitem : recyclerViewItems) {
            if ( listrecitesitem instanceof AuthorClass) {
                AuthorClass kk = (AuthorClass) listrecitesitem;
                if (rewayah == getString(R.string.All)){
                    recyclerViewItemstemp.add(listrecitesitem);
                }else if (kk.Rewayat.contains(rewayah)) {
                    recyclerViewItemstemp.add(listrecitesitem);

                }
            }

        }
        addBannerAds(recyclerViewItemstemp);
        loadBannerAds(recyclerViewItemstemp);
        lVRecites .setAdapter(new VivzAdapter(recyclerViewItemstemp));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    class VivzAdapter extends RecyclerView.Adapter<VivzAdapter.ViewHolder> {

//        ArrayList<AuthorClass> listrecitesLocal;
        private List<Object> listrecitesLocalobject;

        VivzAdapter(ArrayList<AuthorClass> listrecites) {

//            listrecitesLocal = new ArrayList<AuthorClass>();
//            listrecitesLocal = listrecites;

        }

        public VivzAdapter(List<Object> recyclerViewItems) {
            listrecitesLocalobject = recyclerViewItems;

        }






        @NonNull
        @Override
        public VivzAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.radio_ticket, parent, false);
            VivzAdapter.ViewHolder viewHolder = new VivzAdapter.ViewHolder(listItem);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull VivzAdapter.ViewHolder holder,  int position) {

            if (listrecitesLocalobject.get(position) instanceof AdView){
                AdView adView = (AdView) listrecitesLocalobject.get(position);

                if (listrecitesLocalobject.get(position) instanceof AdView){
                    Log.d("sfssdfsddsf", "The interstitial wasn't loaded yet.");

                }


                if(adView.getParent() != null) {
                    ((ViewGroup)adView.getParent()).removeView(adView); // <- fix
                }
                holder.cardview.addView(adView); //  <==========  ERROR IN THIS LINE DURING 2ND RUN
                holder.cardview.setVisibility(View.VISIBLE);
                holder.cardContent.setVisibility(View.GONE);

                holder.entireCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                holder.buttonShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });


            }
            else{
                final AuthorClass temp = (AuthorClass) listrecitesLocalobject.get(position);
                LnaguageClass lc = new LnaguageClass(getContext());
                holder.txtRecitesName = lc.SetTextFont(holder.txtRecitesName,"");
                holder.txtRadioRewayah = lc.SetTextFont(holder.txtRadioRewayah,"");
                holder.txtRecitesName.setText(temp.RealName);
                holder.txtRadioRewayah.setText(temp.Rewayat);

                holder.cardview.setVisibility(View.GONE);
                holder.cardContent.setVisibility(View.VISIBLE);

                holder.txtRecitesName.setSelected(true);
                holder.txtRadioRewayah.setSelected(true);
                holder.entireCard.setLayoutDirection(SettingSaved.LanguageSelect==1?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);

                holder.entireCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (position <listrecites.size()){
                            if (temp.ServerName == listrecites.get(position).ServerName){
                                RecitesName = temp.ServerName;
                                Rewayat = temp.Rewayat;
                                RealRecitesName = temp.RealName;
                                RecitesAYA=String.valueOf(position);// ServerName;
                                surahName = temp.RealName;

                                AyaNameView = holder.txtRecitesName;

                                AyaImage = holder.imgchannel;
                                laodRadio();
                            }else {
                                for(int i = 0; i < listrecites.size(); i++) {
                                    if (listrecites.get(i).ServerName == temp.ServerName) {
                                        RecitesName = temp.ServerName;
                                        Rewayat = temp.Rewayat;
                                        RealRecitesName = temp.RealName;
                                        RecitesAYA=String.valueOf(i);// ServerName;
                                        surahName = listrecites.get(i).RealName;

                                        laodRadio();
                                        break;
                                    }
                                    System.out.println("Current index is: " + i);
                                }



                            }
                        }else{
                            for(int i = 0; i < listrecites.size(); i++) {
                                if (listrecites.get(i).ServerName == temp.ServerName) {
                                    RecitesName = temp.ServerName;
                                    Rewayat = temp.Rewayat;
                                    RealRecitesName = temp.RealName;
                                    RecitesAYA=String.valueOf(i);// ServerName;
                                    surahName = listrecites.get(i).RealName;

                                    laodRadio();
                                    break;
                                }
                                System.out.println("Current index is: " + i);
                            }
                        }


                    }
                });
                holder.buttonShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onShareBy(temp.RealName,RealRecitesName,temp.ServerName);

                    }
                });
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return listrecitesLocalobject.size();
        }

        private void onShareBy( String soura,String reciter,String ServerName) {


            Uri imageUri = Uri.parse(getString(R.string.dynamicLinkRadioShareImage));
            String title = "";
            String description = "";
            if (SettingSaved.LanguageSelect == 2){
                String englishTitle = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("en"), R.string.sharePart3, getContext());
                String englishDescription = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("en"), R.string.dynamicLinkShareTitle, getContext());
                String radioStation = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("en"), R.string.radiostation, getContext());


                String nameWithoutRadio;
                if (soura.contains("Radio")){
                    nameWithoutRadio = soura.split("Radio")[1];

                }else{
                    nameWithoutRadio = soura;
                }
                title = englishTitle + nameWithoutRadio+"'s " + radioStation;
                description = englishDescription;
            }else {
                String arabicTitle = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("ar"), R.string.sharePart3, getContext());
                String arabicDescription = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("ar"), R.string.dynamicLinkShareTitle, getContext());
                String radioStation = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("ar"), R.string.radiostation, getContext());

                String nameWithoutRadio;
                if (soura.contains("إذاعة")){
                    nameWithoutRadio = soura.split("إذاعة")[1];
                }else{
                    nameWithoutRadio = soura;
                }
                title = radioStation+ arabicTitle + nameWithoutRadio ;
                description = arabicDescription;
            }

//            Log.d(TAG, "createDynamicLink 2: " +title);
//            Log.d(TAG, "createDynamicLink 2: " +description);
//            Log.d(TAG, "createDynamicLink 2: " +"radio/"+ServerName);

            SeparateFunctions separateFunctions = new SeparateFunctions(getActivity());
            separateFunctions.createDynamicLink(getActivity(),"radio/"+ServerName,title,description,imageUri).addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<ShortDynamicLink> task) {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();
                        Uri flowchartLink = task.getResult().getPreviewLink();
                        Log.d(TAG, "createDynamicLink 2: " +shortLink);

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
//            String shareBody = getResources().getString(R.string.sharemessage) + "  https://rebrand.ly/notfof70d";
                        String shareBody = "";
                        if (getResources().getString(R.string.sharePart3) == "reciter"){
                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ "."+ " "+ getResources().getString(R.string.sharePart4) + " " + shortLink.toString();

                        }else{
                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " " +"."+ " "+ getResources().getString(R.string.sharePart4) + " " + shortLink.toString();

                        }
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));

                        createReport(shortLink.toString());

                    } else {
                        Log.d(TAG, "createDynamicLink 2: " +task);
                        // Error
                        // ...
                    }
                }

            });



        }




        public  class ViewHolder extends RecyclerView.ViewHolder {



            TextView txtRecitesName ;
            TextView txtRadioRewayah ;
            ImageButton buttonShare ;
            LinearLayout entireCard ;
            CardView cardview ;
            CardView cardContent;

            ImageView imgchannel;
            public ViewHolder(View itemView) {
                super(itemView);



                txtRecitesName = (TextView) itemView.findViewById(R.id.txtRecitesName);
                txtRadioRewayah = (TextView) itemView.findViewById(R.id.txtRadioRewayah);
                buttonShare = (ImageButton) itemView.findViewById(R.id.buttonShare);
                entireCard = (LinearLayout) itemView.findViewById(R.id.entireCard);
                cardview =(CardView) itemView.findViewById( R.id.cardviewad);
                cardContent =(CardView) itemView.findViewById( R.id.cardContent);
                imgchannel =(ImageView) itemView.findViewById( R.id.imgchannel);

            }

        }

    }

    //get access to mailbox
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    laodRadio();
                } else {
                    // Permission Denied
                    laodRadio();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Required to reward the user.

    public void loadad(){

        /*/*mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });*/

    }

    public void  prepareAd(){

        /*mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());*/
    }
    @Override
    public void onDestroy() {
        SettingSaved.IsOpen =  0;//App Is Opened
        SettingSaved.SounlLoad= 0;//Sound not load
        super.onDestroy();
    }

    private void DisplayDownload() {
        if ((int) Build.VERSION.SDK_INT >= 23)
        {
            if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                    (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                    ( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)||
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)
            {

                requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        REQUEST_CODE_ASK_PERMISSIONS);


                return;
            }

        }
        //permisons

        Intent down = new Intent(getActivity(), Downloads.class);
        startActivity(down);
    }

    void reminder(){


// load setting informatin if we have
        SettingSaved settingSaved = new SettingSaved(getActivity());
        settingSaved.LoadData();
        //start notification every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SettingSaved.selectedHour);
        calendar.set(Calendar.MINUTE, SettingSaved.selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0,intent1, PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Toast.makeText(getActivity(), R.string.reminder_started, Toast.LENGTH_SHORT).show();
    }

    public void chicLastRecite(){
        SettingSaved settingSaved = new SettingSaved(getActivity());
                settingSaved.LoadData();

        if (SettingSaved.FinalRecite!=""){
            //chec for ubdate
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            // Setting Dialog Title
            alertDialog.setTitle(R.string.lastrecite);

            // Setting Dialog Message
            alertDialog.setMessage(R.string.history);

            // Setting Icon to Dialog
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    // Write your code here to invoke YES event
                    Intent intent= new Intent( getActivity(),managerdb.class);
                    intent.putExtra("RecitesName", SettingSaved.FinalRecite);
                    intent.putExtra("RecitesAYA", SettingSaved.FinalAya);
                    intent.putExtra("Rewayat", SettingSaved.FinalRewayat);
                    intent.putExtra("RealRecitesName", SettingSaved.FinalRealRecitesName);
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
            });

            // Setting Negative "NO" Button
            alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to invoke NO event

                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();


        }
    }

}
