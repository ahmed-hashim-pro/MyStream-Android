package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AthkarActivity extends AppCompatActivity {

    private static final String FAVORITES_PREFS = "athkar_favorites";
    private static final String FAVORITES_KEY = "favorite_texts";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athkar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.morning_athkar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        recyclerView = findViewById(R.id.recycler_athkar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allSections = buildSections(getAthkarList());
        recyclerView.setAdapter(new AthkarAdapter(buildDisplayList()));
    }

    private Set<String> getFavorites() {
        return new HashSet<>(getSharedPreferences(FAVORITES_PREFS, MODE_PRIVATE)
                .getStringSet(FAVORITES_KEY, new HashSet<>()));
    }

    private void toggleFavorite(String text) {
        SharedPreferences prefs = getSharedPreferences(FAVORITES_PREFS, MODE_PRIVATE);
        Set<String> favorites = getFavorites();
        if (favorites.contains(text)) {
            favorites.remove(text);
        } else {
            favorites.add(text);
        }
        prefs.edit().putStringSet(FAVORITES_KEY, favorites).apply();
    }

    private boolean isFavorite(String text) {
        return getFavorites().contains(text);
    }

    private List<AthkarItem> getAthkarList() {
        List<AthkarItem> list = new ArrayList<>();

        list.add(new AthkarItem("أذكار الصباح", true));

        list.add(new AthkarItem(
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَـهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "سُبْحَانَ اللهِ وَبِحَمْدِهِ",
                "100 مرة", false));

        list.add(new AthkarItem(
                "لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "100 مرة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "بِسْمِ اللهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "رَضِيتُ بِاللهِ رَبًّا، وَبِالإِسْلامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
                "3 مرات", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "حَسْبِيَ اللَّهُ لاَ إِلَـهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                "7 مرات", false));

        list.add(new AthkarItem(
                "أَسْتَغْفِرُ اللهَ وَأَتُوبُ إِلَيْهِ",
                "100 مرة", false));

        // ===== أذكار المساء =====
        list.add(new AthkarItem("أذكار المساء", true));

        list.add(new AthkarItem(
                "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ للهِ، وَالْحَمْدُ للهِ، لاَ إِلَهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَـهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "أَعُوذُ بِكَلِمَاتِ اللهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "بِسْمِ اللهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "رَضِيتُ بِاللهِ رَبًّا، وَبِالإِسْلامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
                "3 مرات", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "حَسْبِيَ اللَّهُ لاَ إِلَـهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                "7 مرات", false));

        list.add(new AthkarItem(
                "سُبْحَانَ اللهِ وَبِحَمْدِهِ",
                "100 مرة", false));

        list.add(new AthkarItem(
                "لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "100 مرة", false));

        list.add(new AthkarItem(
                "أَسْتَغْفِرُ اللهَ وَأَتُوبُ إِلَيْهِ",
                "100 مرة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَأَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ، وَأَعُوذُ بِكَ مِنَ الْجُبْنِ وَالْبُخْلِ، وَأَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ وَقَهْرِ الرِّجَالِ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلاَ تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                "مرة واحدة", false));

        // ===== أذكار بعد الصلاة =====
        list.add(new AthkarItem("أذكار بعد الصلاة", true));

        list.add(new AthkarItem(
                "أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "لاَ إِلَهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لاَ مَانِعَ لِمَا أَعْطَيْتَ، وَلاَ مُعْطِيَ لِمَا مَنَعْتَ، وَلاَ يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "سُبْحَانَ اللهِ",
                "33 مرة", false));

        list.add(new AthkarItem(
                "الْحَمْدُ للهِ",
                "33 مرة", false));

        list.add(new AthkarItem(
                "اللهُ أَكْبَرُ",
                "33 مرة", false));

        list.add(new AthkarItem(
                "لاَ إِلَهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "آية الكرسي: اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "قُلْ هُوَ اللَّهُ أَحَدٌ، اللَّهُ الصَّمَدُ، لَمْ يَلِدْ وَلَمْ يُولَدْ، وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ، مِن شَرِّ مَا خَلَقَ، وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ، وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ، وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "قُلْ أَعُوذُ بِرَبِّ النَّاسِ، مَلِكِ النَّاسِ، إِلَهِ النَّاسِ، مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ، الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ، مِنَ الْجِنَّةِ وَالنَّاسِ",
                "3 مرات", false));

        // ===== أذكار النوم =====
        list.add(new AthkarItem("أذكار النوم", true));

        list.add(new AthkarItem(
                "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
                "3 مرات", false));

        list.add(new AthkarItem(
                "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ إِنَّكَ خَلَقْتَ نَفْسِي وَأَنْتَ تَوَفَّاهَا، لَكَ مَمَاتُهَا وَمَحْيَاهَا، إِنْ أَحْيَيْتَهَا فَاحْفَظْهَا، وَإِنْ أَمَتَّهَا فَاغْفِرْ لَهَا، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَافِيَةَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَوَجَّهْتُ وَجْهِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَغْبَةً وَرَهْبَةً إِلَيْكَ، لاَ مَلْجَأَ وَلاَ مَنْجَا مِنْكَ إِلاَّ إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ، وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "سُبْحَانَ اللهِ",
                "33 مرة", false));

        list.add(new AthkarItem(
                "الْحَمْدُ للهِ",
                "33 مرة", false));

        list.add(new AthkarItem(
                "اللهُ أَكْبَرُ",
                "34 مرة", false));

        list.add(new AthkarItem(
                "آية الكرسي: اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "سورة الإخلاص والمعوذتين (تقرأ ثم تنفث في كفيك وتمسح بهما جسدك)",
                "3 مرات", false));

        // ===== أذكار الاستيقاظ =====
        list.add(new AthkarItem("أذكار الاستيقاظ من النوم", true));

        list.add(new AthkarItem(
                "الْحَمْدُ للهِ الَّذِي أَحْيَانَا بَعْدَمَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "لاَ إِلَهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، سُبْحَانَ اللهِ، وَالْحَمْدُ للهِ، وَلاَ إِلَهَ إِلاَّ اللهُ، وَاللهُ أَكْبَرُ، وَلاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللهِ الْعَلِيِّ الْعَظِيمِ",
                "مرة واحدة", false));

        // ===== أذكار متنوعة =====
        list.add(new AthkarItem("أذكار متنوعة", true));

        list.add(new AthkarItem(
                "دعاء دخول المسجد: اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء الخروج من المسجد: اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء دخول المنزل: بِسْمِ اللهِ وَلَجْنَا، وَبِسْمِ اللهِ خَرَجْنَا، وَعَلَى اللهِ رَبِّنَا تَوَكَّلْنَا",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء الخروج من المنزل: بِسْمِ اللهِ، تَوَكَّلْتُ عَلَى اللهِ، وَلاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللهِ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء الركوب: بِسْمِ اللهِ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنقَلِبُونَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء السفر: اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء لبس الثوب: الحَمْدُ للهِ الَّذِي كَسَانِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلاَ قُوَّةٍ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء دخول الخلاء: اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "دعاء الخروج من الخلاء: غُفْرَانَكَ",
                "مرة واحدة", false));

        list.add(new AthkarItem(
                "الصلاة على النبي ﷺ",
                "10 مرات", false));

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

    private List<AthkarItem> buildSections(List<AthkarItem> flatList) {
        List<AthkarItem> sections = new ArrayList<>();
        AthkarItem currentSection = null;
        for (AthkarItem item : flatList) {
            if (item.isHeader) {
                currentSection = item;
                currentSection.expanded = false;
                sections.add(currentSection);
            } else if (currentSection != null) {
                currentSection.children.add(item);
            }
        }
        return sections;
    }

    private static int parseCount(String countStr) {
        if (countStr == null || countStr.isEmpty()) return 1;
        if (countStr.contains("واحدة")) return 1;
        Matcher matcher = Pattern.compile("\\d+").matcher(countStr);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 1;
    }

    // Inner model class
    static class AthkarItem {
        String text;
        String count;
        boolean isHeader;
        int remainingCount;
        boolean expanded = true; // for headers only
        List<AthkarItem> children; // for headers only

        AthkarItem(String text, boolean isHeader) {
            this.text = text;
            this.isHeader = isHeader;
            this.remainingCount = 0;
            if (isHeader) this.children = new ArrayList<>();
        }

        AthkarItem(String text, String count, boolean isHeader) {
            this.text = text;
            this.count = count;
            this.isHeader = isHeader;
            this.remainingCount = parseCount(count);
        }
    }

    // Build flat display list from sections
    private List<AthkarItem> allSections;

    private List<AthkarItem> buildDisplayList() {
        List<AthkarItem> display = new ArrayList<>();

        // Add favorites section at top if there are any
        Set<String> favorites = getFavorites();
        if (!favorites.isEmpty()) {
            AthkarItem favHeader = new AthkarItem("المفضلة ⭐", true);
            favHeader.expanded = true;
            display.add(favHeader);
            for (AthkarItem section : allSections) {
                if (section.children != null) {
                    for (AthkarItem child : section.children) {
                        if (favorites.contains(child.text)) {
                            display.add(new AthkarItem(child.text, child.count, false));
                        }
                    }
                }
            }
        }

        for (AthkarItem section : allSections) {
            display.add(section);
            if (section.expanded && section.children != null) {
                display.addAll(section.children);
            }
        }
        return display;
    }

    private void rebuildWithFavorites() {
        AthkarAdapter adapter = (AthkarAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateItems(buildDisplayList());
        }
    }

    // RecyclerView Adapter
    class AthkarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private List<AthkarItem> items;

        AthkarAdapter(List<AthkarItem> items) {
            this.items = items;
        }

        void updateItems(List<AthkarItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
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
                View v = inflater.inflate(R.layout.item_athkar_header, parent, false);
                return new HeaderVH(v);
            } else {
                View v = inflater.inflate(R.layout.item_athkar, parent, false);
                return new ItemVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AthkarItem item = items.get(position);
            if (holder instanceof HeaderVH) {
                HeaderVH hvh = (HeaderVH) holder;
                hvh.title.setText(item.text);
                // Rotate arrow: 0 = expanded (up), 180 = collapsed (down)
                hvh.arrow.setRotation(item.expanded ? 0 : 180);
                hvh.itemView.setOnClickListener(v -> {
                    item.expanded = !item.expanded;
                    hvh.arrow.animate().rotation(item.expanded ? 0 : 180).setDuration(200).start();
                    updateItems(buildDisplayList());
                });
            } else if (holder instanceof ItemVH) {
                ItemVH ivh = (ItemVH) holder;
                ivh.text.setText(item.text);

                // Favorite button
                ivh.btnFavorite.setImageResource(isFavorite(item.text)
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off);
                ivh.btnFavorite.setOnClickListener(v -> {
                    toggleFavorite(item.text);
                    ivh.btnFavorite.setImageResource(isFavorite(item.text)
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off);
                    rebuildWithFavorites();
                });

                if (item.remainingCount <= 0) {
                    ivh.count.setText("\u2713");
                    ivh.card.setCardBackgroundColor(
                            ContextCompat.getColor(ivh.itemView.getContext(), R.color.gold_accent_faint));
                } else {
                    ivh.count.setText(item.count != null ? item.count : "");
                    ivh.card.setCardBackgroundColor(
                            ContextCompat.getColor(ivh.itemView.getContext(), R.color.background_card));
                }

                ivh.card.setOnClickListener(v -> {
                    if (item.remainingCount <= 0) return;

                    item.remainingCount--;

                    Vibrator vibrator = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        int duration = item.remainingCount <= 0 ? 20 : 10;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(duration);
                        }
                    }

                    if (item.remainingCount <= 0) {
                        ivh.count.setText("\u2713");
                        ivh.card.setCardBackgroundColor(
                                ContextCompat.getColor(v.getContext(), R.color.gold_accent_faint));
                    } else {
                        ivh.count.setText("\u0628\u0627\u0642\u064A: " + item.remainingCount);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HeaderVH extends RecyclerView.ViewHolder {
            TextView title;
            ImageView arrow;
            HeaderVH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_header);
                arrow = v.findViewById(R.id.iv_expand_arrow);
            }
        }

        class ItemVH extends RecyclerView.ViewHolder {
            TextView text, count;
            MaterialCardView card;
            ImageView btnFavorite;
            ItemVH(View v) {
                super(v);
                text = v.findViewById(R.id.tv_athkar_text);
                count = v.findViewById(R.id.tv_athkar_count);
                card = (MaterialCardView) v;
                btnFavorite = v.findViewById(R.id.btn_favorite);
            }
        }
    }
}
