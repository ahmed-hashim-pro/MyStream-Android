package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DuaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.dua_collection);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        RecyclerView recyclerView = findViewById(R.id.recycler_dua);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DuaAdapter(getDuaList()));
    }

    private List<DuaItem> getDuaList() {
        List<DuaItem> list = new ArrayList<>();

        // أدعية الصباح والمساء
        list.add(new DuaItem("أدعية الصباح والمساء", true));

        list.add(new DuaItem(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
                "رواه ابن ماجه", false));

        list.add(new DuaItem(
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَأَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ",
                "رواه البخاري", false));

        // أدعية السفر
        list.add(new DuaItem("أدعية السفر", true));

        list.add(new DuaItem(
                "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى",
                "رواه مسلم", false));

        list.add(new DuaItem(
                "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
                "سورة الزخرف: 13-14", false));

        // أدعية الطعام
        list.add(new DuaItem("أدعية الطعام", true));

        list.add(new DuaItem(
                "اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ، بِسْمِ اللهِ",
                "رواه ابن السني", false));

        list.add(new DuaItem(
                "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلاَ قُوَّةٍ",
                "رواه الترمذي", false));

        // أدعية النوم
        list.add(new DuaItem("أدعية النوم", true));

        list.add(new DuaItem(
                "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                "رواه البخاري", false));

        list.add(new DuaItem(
                "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
                "رواه أبو داود", false));

        // أدعية الصلاة
        list.add(new DuaItem("أدعية الصلاة", true));

        list.add(new DuaItem(
                "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                "سورة البقرة: 201", false));

        list.add(new DuaItem(
                "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا وَلاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
                "متفق عليه", false));

        // أدعية متنوعة
        list.add(new DuaItem("أدعية متنوعة", true));

        list.add(new DuaItem(
                "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
                "سورة طه: 25-26", false));

        list.add(new DuaItem(
                "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
                "سورة الفرقان: 74", false));

        list.add(new DuaItem(
                "اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي",
                "رواه مسلم", false));

        return list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner model class
    static class DuaItem {
        String text;
        String reference;
        boolean isHeader;

        DuaItem(String text, boolean isHeader) {
            this.text = text;
            this.isHeader = isHeader;
        }

        DuaItem(String text, String reference, boolean isHeader) {
            this.text = text;
            this.reference = reference;
            this.isHeader = isHeader;
        }
    }

    // RecyclerView Adapter
    static class DuaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private final List<DuaItem> items;

        DuaAdapter(List<DuaItem> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View v = inflater.inflate(R.layout.item_dua_header, parent, false);
                return new HeaderVH(v);
            } else {
                View v = inflater.inflate(R.layout.item_dua, parent, false);
                return new ItemVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            DuaItem item = items.get(position);
            if (holder instanceof HeaderVH) {
                ((HeaderVH) holder).title.setText(item.text);
            } else if (holder instanceof ItemVH) {
                ((ItemVH) holder).text.setText(item.text);
                ((ItemVH) holder).reference.setText(item.reference != null ? item.reference : "");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class HeaderVH extends RecyclerView.ViewHolder {
            TextView title;
            HeaderVH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_header);
            }
        }

        static class ItemVH extends RecyclerView.ViewHolder {
            TextView text, reference;
            ItemVH(View v) {
                super(v);
                text = v.findViewById(R.id.tv_dua_text);
                reference = v.findViewById(R.id.tv_dua_reference);
            }
        }
    }
}
