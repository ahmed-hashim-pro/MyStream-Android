package com.medoapps.www.onlinequran;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class MiniPlaylistAdapter extends RecyclerView.Adapter<MiniPlaylistAdapter.ViewHolder> {

    public interface OnSurahClickListener {
        void onSurahClick(int position);
    }

    private final ArrayList<HashMap<String, String>> surahList;
    private final Context context;
    private final OnSurahClickListener listener;
    private int currentPlayingIndex = -1;

    public MiniPlaylistAdapter(ArrayList<HashMap<String, String>> surahList, Context context, OnSurahClickListener listener) {
        this.surahList = surahList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mini_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> surah = surahList.get(position);

        holder.txtSurahNumber.setText(String.valueOf(position + 1));
        holder.txtSurahName.setText(surah.get("songTitle"));

        boolean isCurrent = position == currentPlayingIndex;

        if (isCurrent) {
            int goldColor = ContextCompat.getColor(context, R.color.gold_accent);
            holder.txtSurahNumber.setTextColor(goldColor);
            holder.txtSurahName.setTextColor(goldColor);
            holder.imgNowPlaying.setVisibility(View.VISIBLE);
        } else {
            holder.txtSurahNumber.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.txtSurahName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.imgNowPlaying.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSurahClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return surahList != null ? surahList.size() : 0;
    }

    public void setCurrentPlayingIndex(int newIndex) {
        int oldIndex = currentPlayingIndex;
        currentPlayingIndex = newIndex;
        if (oldIndex >= 0 && oldIndex < getItemCount()) {
            notifyItemChanged(oldIndex);
        }
        if (newIndex >= 0 && newIndex < getItemCount()) {
            notifyItemChanged(newIndex);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSurahNumber;
        TextView txtSurahName;
        ImageView imgNowPlaying;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSurahNumber = itemView.findViewById(R.id.txtSurahNumber);
            txtSurahName = itemView.findViewById(R.id.txtSurahName);
            imgNowPlaying = itemView.findViewById(R.id.imgNowPlaying);
        }
    }
}
