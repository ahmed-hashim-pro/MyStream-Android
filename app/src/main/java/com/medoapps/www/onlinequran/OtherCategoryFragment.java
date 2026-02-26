package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.R.id.adView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.HashSet;

//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardItem;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;


public class OtherCategoryFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    //banner add
    private static final String TAG = "RecitesName";
    private AdView mAdView;
    //private InterstitialAd mInterstitialAd;
    //private RewardedVideoAd mAd;
    static OtherCategoryFragment instance4;
    private PreferenceManager prefManager;
    public static final String PAGE_TITLE = "Tab2";

    TextView recitelisttxt;
    Boolean new_ubdate =false;
    FirebaseRemoteConfig mFirebaseRemoteConfig;


    public ArrayList<OtherCategory> listCategory = new ArrayList<OtherCategory>();
    GridView lVRecites;

    String RecitesName="";
    String RecitesAYA="";
    String Rewayat="";
    String RealRecitesName="";
    SearchView searchView;

    // Remote Config keys
    private static final String LOADING_PHRASE_CONFIG_KEY = "loading_phrase";
    private static final String urlUpdate = "url";
    private static final String chicUpdate = "new_ubdate";
    private static final String new_ubdate2 = "new_ubdate2";
    private static final String url2 = "url2";
    private static final String url3 = "url3";
    private static final String new_ubdate3 = "new_ubdate3";
    private static final String ubdated_version = "ubdated_version";


    public static OtherCategoryFragment newInstance() {
        OtherCategoryFragment fragment = new OtherCategoryFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_other_category_fragment, container, false);

        // load setting informatin if we have
        SettingSaved settingSaved=new SettingSaved(getContext());
        settingSaved.LoadData();

        instance4 =this;
        recitelisttxt = (TextView) view.findViewById(R.id.recitelisttxt);
        SettingSaved.IsOpen =  1;//App Is Opened
        SettingSaved.SounlLoad=1;//sound load
        //load banner ad
        mAdView = (AdView) view.findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }



        });

        lVRecites = (GridView) view.findViewById(R.id.listView);
        loadItems();

        if(SettingSaved.OnTimeAds==false) {
            if(SettingSaved.IsRated==1){
                SettingSaved.OnTimeAds=true;
            }

        }



        return view;
    }

    private void loadItems(){
        OtherCategoryListLanguageClass lc = new OtherCategoryListLanguageClass(getContext());
        listCategory = lc.CategoryList();

        lVRecites.setAdapter(new VivzAdapter(listCategory));
        Log.d(TAG, "loadItems: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems();
    }


    private ArrayList<String> getRewayahList(){
        ArrayList<String> mylist = new ArrayList<String>();
//        String[] items = new String[listCategory.size()];
        mylist.add(getString(R.string.All));

        int position = 0;
        for (OtherCategory listCategoryitem : listCategory) {
//            items[position] = listCategoryitem.Rewayat;
            mylist.add(listCategoryitem.title);
            position++;

        }
        HashSet hs = new HashSet();
        hs.addAll(mylist);
        mylist.clear();
        mylist.addAll(hs);
        return mylist;
    }

    private void laodRadio(){
        Intent intent= new Intent( getActivity(),NewQuranPlayer.class);
        intent.putExtra("RecitesName",RecitesName);
        intent.putExtra("Rewayat",Rewayat);
        intent.putExtra("RealRecitesName",Rewayat);
        intent.putExtra("RecitesAYA",RecitesAYA);
        intent.putExtra("IsRadio",true);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ArrayList<OtherCategory> listCategorytemp = new ArrayList<OtherCategory>();
        adapterView.getItemAtPosition(i);
        String rewayah = adapterView.getItemAtPosition(i).toString();
        for (OtherCategory listCategoryitem : listCategory) {
            if (rewayah == getString(R.string.All)){
                listCategorytemp.add(listCategoryitem);
            }else if (listCategoryitem.title.contains(rewayah)) {
                listCategorytemp.add(listCategoryitem);

            }


        }
        lVRecites .setAdapter(new VivzAdapter(listCategorytemp));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    class VivzAdapter extends BaseAdapter {

        ArrayList<OtherCategory> listCategoryLocal;

        VivzAdapter(ArrayList<OtherCategory> listCategory) {

            listCategoryLocal = new ArrayList<OtherCategory>();
            listCategoryLocal = listCategory;

        }


        @Override
        public int getCount() {
            return listCategoryLocal.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        private void onShareBy( String soura,String reciter) {

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
//            String shareBody = getResources().getString(R.string.sharemessage) + "  https://rebrand.ly/notfof70d";
            String shareBody = "";
            if (getResources().getString(R.string.sharePart3) == "reciter"){
                shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ "."+ " "+ getResources().getString(R.string.sharePart4) + " " + getResources().getString(R.string.shareURL);

            }else{
                shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " " +"."+ " "+ getResources().getString(R.string.sharePart4) + " " + getResources().getString(R.string.shareURL);

            }
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "My Stream");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater mInflater = getActivity().getLayoutInflater();
            View myView = mInflater.inflate(R.layout.other_ticket, null);

            ImageView icon = (ImageView) myView.findViewById(R.id.icon);
            TextView itemtxt = (TextView) myView.findViewById(R.id.itemtxt);
            ImageButton buttonShare = (ImageButton) myView.findViewById(R.id.buttonShare);
            LinearLayout entireCard = (LinearLayout) myView.findViewById(R.id.entireCardOtherCategory);
            LnaguageClass lc = new LnaguageClass(getContext());
            itemtxt = lc.SetTextFont(itemtxt,"");

            final OtherCategory temp = listCategoryLocal.get(position);
            itemtxt.setText(temp.title);
            icon.setImageResource(temp.ImgDrawable);


            entireCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*Intent intent = new Intent(getContext(), VideoWallDemoActivity.class);
                    startActivity(intent);*/


                    if (temp.fragment!=null){
                        LiveList newFragment = new LiveList();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.EntireLayoutCategory, newFragment, "liveListFragment")
                                .addToBackStack("liveListFragmentBAck")
                                .commit();

                    }else if (temp.activity != null){
                        Intent intent = new Intent(getContext(), temp.activity);
//                        intent.putExtra("Title", temp.title);
//                        intent.putExtra("LiveUrl", temp.liveUrl);
                        startActivity(intent);


                    }
//                    LiveList nextFrag= new LiveList();


                }
            });
            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onShareBy(temp.title,temp.title);


                }
            });

            return myView;


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


}
