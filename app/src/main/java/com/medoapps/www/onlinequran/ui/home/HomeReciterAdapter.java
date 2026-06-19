package com.medoapps.www.onlinequran.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeReciterAdapter extends RecyclerView.Adapter<HomeReciterAdapter.VH> {

    public interface OnReciterClick { void onClick(Post post); }

    private final List<Post> items = new ArrayList<>();
    private OnReciterClick clickListener;

    public void setOnReciterClick(OnReciterClick l) { this.clickListener = l; }

    public void submit(List<Post> posts) {
        items.clear();
        if (posts != null) items.addAll(posts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_reciter, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Post p = items.get(position);
        h.name.setText(p.title != null ? p.title : p.author);
        Glide.with(h.image.getContext())
                .load(p.Thumb_Url)
                .placeholder(R.mipmap.ic_launcher_new_transparent9)
                .into(h.image);
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(p);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.reciter_image);
            name = v.findViewById(R.id.reciter_name);
        }
    }
}
