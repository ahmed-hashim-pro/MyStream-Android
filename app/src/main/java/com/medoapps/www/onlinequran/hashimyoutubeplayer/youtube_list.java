package com.medoapps.www.onlinequran.hashimyoutubeplayer;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.medoapps.www.onlinequran.OtherCategory;
import com.medoapps.www.onlinequran.OtherCategoryListLanguageClass;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.classes.YouTubeConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class youtube_list extends Fragment {

    public ArrayList<OtherCategory> listCategory = new ArrayList<OtherCategory>();
    private YouTubeThumbnailView thumbnailView;
    private YouTubeThumbnailLoader thumbnailLoader;



    public
    youtube_list() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube_list, container, false);
        // Inflate the layout for this fragment
        OtherCategoryListLanguageClass lc = new OtherCategoryListLanguageClass(getContext());
        listCategory = lc.YouTubeVideoList();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        MyListAdapter adapter = new MyListAdapter(listCategory);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
//add ItemDecoration
//        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(200));

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        return view;



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
        }
    }

    public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        private ArrayList<OtherCategory> listdata;



        YouTubeThumbnailView youTubeThumbnailView ;
        // RecyclerView recyclerView;
        public MyListAdapter(ArrayList<OtherCategory> listdata) {
            this.listdata = listdata;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.youtube_ticket, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int  position) {
             OtherCategory myListData = listdata.get(holder.getAdapterPosition());

//            getVideoInfo(myListData.youtubeVideoId);
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = "https://www.googleapis.com/youtube/v3/videos?part=id%2C+snippet&id="+myListData.youtubeVideoId+"&key=" + YouTubeConfig.getInfoAPI_KEY();


            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                    new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
//                Log.d("onResponsehhhhh", response);
                            JSONObject snippet= null;
                            JSONObject thumbnails= null;
                            try {
                                snippet = new JSONObject(response.toString())
                                        .getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                                thumbnails = new JSONObject(response.toString())
                                        .getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
//                                title[0] =snippet.getString("title");
//                                String description=snippet.getString("description");
                                String thumbnailsHigh=thumbnails.getString("url");
                                holder.title = snippet.getString("title");
                                holder.descriptiontxt = snippet.getString("description");

                                holder.textView.setText(snippet.getString("title"));
                                holder.description.setText(snippet.getString("description"));
//                                imageView.setImageDrawable(thumbnailsHigh);
//                                imageView.setImageURI(Uri.parse(thumbnailsHigh));
                                try {
                                    Glide.with(getContext())
                                            .load(thumbnailsHigh)
                                            .into(holder.imageView);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Log.d("onResponsehhhhh", snippet.getString("title"));
                                Log.d("onResponsehhhhh", thumbnailsHigh);

                            } catch (JSONException e) {
                                Log.d("onResponsehhhhh", e.toString());

                                e.printStackTrace();
                            }

                            // Display the first 500 characters of the response string.
//                        textView.setText("Response is: " + response.substring(0,500));
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("onErrorResponsehhhhhhhh", url);
                    Log.d("onErrorResponsehhhhhhhh", error.toString());

//                textView.setText("That didn't work!");
                }
            });

// Add the request to the RequestQueue.
            queue.add(stringRequest);


            /*youTubeThumbnailView.initialize(YouTubeConfig.getApiKey(), new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {
                    youTubeThumbnailLoader.setVideo(myListData.youtubeVideoId);


                    youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                        @Override
                        public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                            youTubeThumbnailLoader.release();
                        }

                        @Override
                        public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {

                        }
                    });
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });*/


            holder.linearlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("onClickResponsehhhhhhhh", holder.title);

                    Intent intent = new Intent(getContext(), YoutubePlayerViewActivity.class);
                        intent.putExtra("videoId", myListData.youtubeVideoId);
                        intent.putExtra("videoTitle", holder.title);
                        intent.putExtra("videoDescription", holder.descriptiontxt);
                    startActivity(intent);
                }
            });
        }

        public String[] getVideoInfo(String videoId){
             String[] title = {"",""};

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getContext());
            String url = "https://www.googleapis.com/youtube/v3/videos?part=id%2C+snippet&id="+videoId+"&key=" + YouTubeConfig.getInfoAPI_KEY();


            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                    new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
//                Log.d("onResponsehhhhh", response);
                            JSONObject snippet= null;
                            JSONObject thumbnails= null;
                            try {
                                snippet = new JSONObject(response.toString())
                                        .getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                                thumbnails = new JSONObject(response.toString())
                                        .getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                 title[0] =snippet.getString("title");
                                String description=snippet.getString("description");
                                String thumbnailsHigh=thumbnails.getString("url");

//                                textView.setText(snippet.getString("title"));
//                                imageView.setImageDrawable(thumbnailsHigh);
//                                imageView.setImageURI(Uri.parse(thumbnailsHigh));
                                /*Glide.with(getContext())
                                        .load(thumbnailsHigh)
                                        .into(imageView);*/

                                Log.d("onResponsehhhhh", title[0]);
                                Log.d("onResponsehhhhh", thumbnailsHigh);

                            } catch (JSONException e) {
                                Log.d("onResponsehhhhh", e.toString());

                                e.printStackTrace();
                            }

                            // Display the first 500 characters of the response string.
//                        textView.setText("Response is: " + response.substring(0,500));
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("onErrorResponsehhhhhhhh", url);
                    Log.d("onErrorResponsehhhhhhhh", error.toString());

//                textView.setText("That didn't work!");
                }
            });

// Add the request to the RequestQueue.
            queue.add(stringRequest);
            return title;

        }


        @Override
        public int getItemCount() {
            return listdata.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout linearlayout;
            public ImageView imageView;
            public TextView textView;
            public TextView description;
            String title = "";
            String descriptiontxt = "";
            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.imageView5);
                textView = (TextView) itemView.findViewById(R.id.textView13);
                description = (TextView) itemView.findViewById(R.id.description);
                linearlayout = (LinearLayout)itemView.findViewById(R.id.entireCard);
                youTubeThumbnailView = (YouTubeThumbnailView) itemView.findViewById(R.id.playerthu2);

            }
        }

        private final class ThumbnailListener implements
                YouTubeThumbnailLoader.OnThumbnailLoadedListener {

            @Override
            public void onThumbnailLoaded(YouTubeThumbnailView thumbnail, String videoId) {

//                flipNext();
//                imageView.setImageDrawable(null); // <--- added to force redraw of ImageView

//                imageView.setImageDrawable(thumbnail.getDrawable());
                /*Glide.with(getContext())
                        .asDrawable()
                        .thumbnail(Glide.with(getContext())
                        .asDrawable()
                        .load(thumbnail.getDrawable()))
                        .load(thumbnail.getDrawable()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (isFirstResource) {
                            return false; // thumbnail was not shown, do as usual
                        }
                        return true;
                        *//*return new DrawableCrossFadeFactory()
                                .build(DataSource.LOCAL, false) // force crossFade() even if coming from memory cache
                                .transition(resource, (Transition.ViewAdapter) target);*//*
                    }
                }).into(imageView);*/




            }

            @Override
            public void onThumbnailError(YouTubeThumbnailView thumbnail,
                                         YouTubeThumbnailLoader.ErrorReason reason) {
//                loadNextThumbnail();
            }

        }
    }
}