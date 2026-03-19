package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveList extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView title;
    GridView lVRecites;
    public ArrayList<OtherCategory> listCategory = new ArrayList<OtherCategory>();

    public LiveList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LiveList.
     */
    // TODO: Rename and change types and number of parameters
    public static LiveList newInstance(String param1, String param2) {
        LiveList fragment = new LiveList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_live_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lVRecites = (GridView) getView().findViewById(R.id.listView);
        title = (TextView) getView().findViewById(R.id.title);
        OtherCategoryListLanguageClass lc = new OtherCategoryListLanguageClass(getContext());
        listCategory = lc.LiveStreamList();

        lVRecites.setAdapter(new VivzAdapter(listCategory));
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
            if ("reciter".equals(getResources().getString(R.string.sharePart3))){
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
            View entireCard = myView.findViewById(R.id.entireCardOtherCategory);
            LnaguageClass lc = new LnaguageClass(getContext());
            itemtxt = lc.SetTextFont(itemtxt,"");

            final OtherCategory temp = listCategoryLocal.get(position);
            itemtxt.setText(temp.title);
            icon.setImageResource(temp.ImgDrawable);

            entireCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), LiveStreamPlayer.class);
                    intent.putExtra("Title", temp.title);
                    intent.putExtra("LiveUrl", temp.liveUrl);
                    startActivity(intent);
                }
            });

            return myView;


        }


    }
}