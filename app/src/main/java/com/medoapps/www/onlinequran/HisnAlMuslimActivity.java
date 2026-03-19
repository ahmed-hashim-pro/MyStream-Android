package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HisnAlMuslimActivity extends AppCompatActivity {

    private List<HisnSection> allSections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hisn_al_muslim);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("حصن المسلم");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        RecyclerView recyclerView = findViewById(R.id.recycler_hisn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allSections = buildAllCategories();
        recyclerView.setAdapter(new HisnAdapter(buildDisplayList()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ──────────────────────────────────────────────
    // Model classes
    // ──────────────────────────────────────────────

    static class DuaItem {
        String arabicText;
        String source;

        DuaItem(String arabicText, String source) {
            this.arabicText = arabicText;
            this.source = source;
        }
    }

    static class HisnSection {
        String title;
        boolean expanded;
        List<DuaItem> duas;

        HisnSection(String title) {
            this.title = title;
            this.expanded = false;
            this.duas = new ArrayList<>();
        }

        void addDua(String arabicText, String source) {
            duas.add(new DuaItem(arabicText, source));
        }
    }

    // Wrapper for display list
    static class DisplayItem {
        static final int TYPE_HEADER = 0;
        static final int TYPE_DUA = 1;

        int type;
        HisnSection section;  // for headers
        DuaItem dua;           // for duas

        DisplayItem(HisnSection section) {
            this.type = TYPE_HEADER;
            this.section = section;
        }

        DisplayItem(DuaItem dua) {
            this.type = TYPE_DUA;
            this.dua = dua;
        }
    }

    // ──────────────────────────────────────────────
    // Build flat display list from sections
    // ──────────────────────────────────────────────

    private List<DisplayItem> buildDisplayList() {
        List<DisplayItem> display = new ArrayList<>();
        for (HisnSection section : allSections) {
            display.add(new DisplayItem(section));
            if (section.expanded) {
                for (DuaItem dua : section.duas) {
                    display.add(new DisplayItem(dua));
                }
            }
        }
        return display;
    }

    // ──────────────────────────────────────────────
    // All 30 categories with authentic duas
    // ──────────────────────────────────────────────

    public static String[] getRandomDua() {
        List<HisnSection> sections = buildAllCategoriesStatic();
        List<String[]> allDuas = new ArrayList<>();
        for (HisnSection section : sections) {
            for (DuaItem dua : section.duas) {
                allDuas.add(new String[]{ section.title, dua.arabicText, dua.source });
            }
        }
        int index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % allDuas.size();
        return allDuas.get(index);
    }

    private List<HisnSection> buildAllCategories() {
        return buildAllCategoriesStatic();
    }

    private static List<HisnSection> buildAllCategoriesStatic() {
        List<HisnSection> sections = new ArrayList<>();

        // 1. أذكار الاستيقاظ
        HisnSection s1 = new HisnSection("أذكار الاستيقاظ من النوم");
        s1.addDua(
                "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                "رواه البخاري");
        s1.addDua(
                "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ، رَبِّ اغْفِرْ لِي",
                "رواه البخاري");
        s1.addDua(
                "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ",
                "رواه الترمذي");
        sections.add(s1);

        // 2. دعاء لبس الثوب
        HisnSection s2 = new HisnSection("دعاء لبس الثوب");
        s2.addDua(
                "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
                "رواه أبو داود والترمذي");
        s2.addDua(
                "اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ كَسَوْتَنِيهِ، أَسْأَلُكَ مِنْ خَيْرِهِ وَخَيْرِ مَا صُنِعَ لَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّهِ وَشَرِّ مَا صُنِعَ لَهُ",
                "رواه أبو داود والترمذي");
        sections.add(s2);

        // 3. دعاء الخلاء
        HisnSection s3 = new HisnSection("دعاء دخول الخلاء والخروج منه");
        s3.addDua(
                "بِسْمِ اللَّهِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
                "رواه البخاري ومسلم");
        s3.addDua(
                "غُفْرَانَكَ",
                "رواه أبو داود والترمذي");
        sections.add(s3);

        // 4. أذكار الوضوء
        HisnSection s4 = new HisnSection("أذكار الوضوء");
        s4.addDua(
                "بِسْمِ اللَّهِ",
                "رواه أبو داود والترمذي");
        s4.addDua(
                "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                "رواه مسلم");
        s4.addDua(
                "اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ",
                "رواه الترمذي");
        sections.add(s4);

        // 5. دعاء الخروج من المنزل
        HisnSection s5 = new HisnSection("دعاء الخروج من المنزل");
        s5.addDua(
                "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                "رواه أبو داود والترمذي");
        s5.addDua(
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أَضِلَّ أَوْ أُضَلَّ، أَوْ أَزِلَّ أَوْ أُزَلَّ، أَوْ أَظْلِمَ أَوْ أُظْلَمَ، أَوْ أَجْهَلَ أَوْ يُجْهَلَ عَلَيَّ",
                "رواه أبو داود والترمذي والنسائي");
        sections.add(s5);

        // 6. دعاء دخول المسجد
        HisnSection s6 = new HisnSection("دعاء دخول المسجد");
        s6.addDua(
                "أَعُوذُ بِاللَّهِ الْعَظِيمِ، وَبِوَجْهِهِ الْكَرِيمِ، وَسُلْطَانِهِ الْقَدِيمِ، مِنَ الشَّيْطَانِ الرَّجِيمِ",
                "رواه أبو داود");
        s6.addDua(
                "بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                "رواه مسلم");
        s6.addDua(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ (عند الخروج)",
                "رواه مسلم");
        sections.add(s6);

        // 7. دعاء الاستفتاح
        HisnSection s7 = new HisnSection("دعاء الاستفتاح");
        s7.addDua(
                "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكَ، وَلَا إِلَهَ غَيْرُكَ",
                "رواه أبو داود والترمذي");
        s7.addDua(
                "اللَّهُمَّ بَاعِدْ بَيْنِي وَبَيْنَ خَطَايَايَ كَمَا بَاعَدْتَ بَيْنَ الْمَشْرِقِ وَالْمَغْرِبِ، اللَّهُمَّ نَقِّنِي مِنْ خَطَايَايَ كَمَا يُنَقَّى الثَّوْبُ الْأَبْيَضُ مِنَ الدَّنَسِ، اللَّهُمَّ اغْسِلْنِي مِنْ خَطَايَايَ بِالثَّلْجِ وَالْمَاءِ وَالْبَرَدِ",
                "رواه البخاري ومسلم");
        sections.add(s7);

        // 8. دعاء الركوع
        HisnSection s8 = new HisnSection("دعاء الركوع");
        s8.addDua(
                "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                "رواه مسلم وأبو داود");
        s8.addDua(
                "سُبْحَانَكَ اللَّهُمَّ رَبَّنَا وَبِحَمْدِكَ، اللَّهُمَّ اغْفِرْ لِي",
                "رواه البخاري ومسلم");
        s8.addDua(
                "سُبُّوحٌ قُدُّوسٌ، رَبُّ الْمَلَائِكَةِ وَالرُّوحِ",
                "رواه مسلم");
        sections.add(s8);

        // 9. دعاء الرفع من الركوع
        HisnSection s9 = new HisnSection("دعاء الرفع من الركوع");
        s9.addDua(
                "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ",
                "رواه البخاري ومسلم");
        s9.addDua(
                "رَبَّنَا وَلَكَ الْحَمْدُ، حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ",
                "رواه البخاري");
        s9.addDua(
                "رَبَّنَا لَكَ الْحَمْدُ مِلْءَ السَّمَاوَاتِ وَمِلْءَ الْأَرْضِ وَمِلْءَ مَا شِئْتَ مِنْ شَيْءٍ بَعْدُ",
                "رواه مسلم");
        sections.add(s9);

        // 10. دعاء السجود
        HisnSection s10 = new HisnSection("دعاء السجود");
        s10.addDua(
                "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                "رواه مسلم وأبو داود");
        s10.addDua(
                "سُبْحَانَكَ اللَّهُمَّ رَبَّنَا وَبِحَمْدِكَ، اللَّهُمَّ اغْفِرْ لِي",
                "رواه البخاري ومسلم");
        s10.addDua(
                "اللَّهُمَّ إِنِّي أَعُوذُ بِرِضَاكَ مِنْ سَخَطِكَ، وَبِمُعَافَاتِكَ مِنْ عُقُوبَتِكَ، وَأَعُوذُ بِكَ مِنْكَ، لَا أُحْصِي ثَنَاءً عَلَيْكَ أَنْتَ كَمَا أَثْنَيْتَ عَلَى نَفْسِكَ",
                "رواه مسلم");
        sections.add(s10);

        // 11. دعاء بين السجدتين
        HisnSection s11 = new HisnSection("دعاء بين السجدتين");
        s11.addDua(
                "رَبِّ اغْفِرْ لِي، رَبِّ اغْفِرْ لِي",
                "رواه أبو داود وابن ماجه");
        s11.addDua(
                "اللَّهُمَّ اغْفِرْ لِي وَارْحَمْنِي وَاهْدِنِي وَاجْبُرْنِي وَعَافِنِي وَارْزُقْنِي وَارْفَعْنِي",
                "رواه أبو داود والترمذي وابن ماجه");
        sections.add(s11);

        // 12. دعاء التشهد
        HisnSection s12 = new HisnSection("دعاء التشهد");
        s12.addDua(
                "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                "رواه البخاري ومسلم");
        sections.add(s12);

        // 13. الصلاة على النبي
        HisnSection s13 = new HisnSection("الصلاة على النبي بعد التشهد");
        s13.addDua(
                "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
                "رواه البخاري");
        sections.add(s13);

        // 14. دعاء بعد التشهد الأخير وقبل السلام
        HisnSection s14 = new HisnSection("دعاء بعد التشهد الأخير قبل السلام");
        s14.addDua(
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، وَمِنْ عَذَابِ جَهَنَّمَ، وَمِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ، وَمِنْ شَرِّ فِتْنَةِ الْمَسِيحِ الدَّجَّالِ",
                "رواه البخاري ومسلم");
        s14.addDua(
                "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ",
                "رواه البخاري ومسلم");
        sections.add(s14);

        // 15. أذكار بعد السلام من الصلاة
        HisnSection s15 = new HisnSection("أذكار بعد السلام من الصلاة");
        s15.addDua(
                "أَسْتَغْفِرُ اللَّهَ (ثلاثًا)، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
                "رواه مسلم");
        s15.addDua(
                "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
                "رواه البخاري ومسلم");
        s15.addDua(
                "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، لَا إِلَهَ إِلَّا اللَّهُ، وَلَا نَعْبُدُ إِلَّا إِيَّاهُ، لَهُ النِّعْمَةُ وَلَهُ الْفَضْلُ وَلَهُ الثَّنَاءُ الْحَسَنُ، لَا إِلَهَ إِلَّا اللَّهُ مُخْلِصِينَ لَهُ الدِّينَ وَلَوْ كَرِهَ الْكَافِرُونَ",
                "رواه مسلم");
        sections.add(s15);

        // 16. دعاء الاستخارة
        HisnSection s16 = new HisnSection("دعاء الاستخارة");
        s16.addDua(
                "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الْأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الْأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ ثُمَّ أَرْضِنِي بِهِ",
                "رواه البخاري");
        sections.add(s16);

        // 17. أذكار الصباح والمساء
        HisnSection s17 = new HisnSection("أذكار الصباح والمساء");
        s17.addDua(
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "رواه أبو داود");
        s17.addDua(
                "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                "رواه الترمذي");
        s17.addDua(
                "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                "رواه البخاري");
        s17.addDua(
                "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ (ثلاث مرات)",
                "رواه أبو داود والترمذي");
        s17.addDua(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ (ثلاث مرات)",
                "رواه أبو داود وابن ماجه");
        sections.add(s17);

        // 18. أذكار النوم
        HisnSection s18 = new HisnSection("أذكار النوم");
        s18.addDua(
                "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                "رواه البخاري");
        s18.addDua(
                "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
                "رواه البخاري ومسلم");
        s18.addDua(
                "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَوَجَّهْتُ وَجْهِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَغْبَةً وَرَهْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ، وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ",
                "رواه البخاري ومسلم");
        s18.addDua(
                "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ (ثلاث مرات)",
                "رواه أبو داود والترمذي");
        sections.add(s18);

        // 19. دعاء القنوت
        HisnSection s19 = new HisnSection("دعاء القنوت");
        s19.addDua(
                "اللَّهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ، فَإِنَّكَ تَقْضِي وَلَا يُقْضَى عَلَيْكَ، إِنَّهُ لَا يَذِلُّ مَنْ وَالَيْتَ، وَلَا يَعِزُّ مَنْ عَادَيْتَ، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ",
                "رواه أبو داود والترمذي والنسائي");
        s19.addDua(
                "اللَّهُمَّ إِنِّي أَعُوذُ بِرِضَاكَ مِنْ سَخَطِكَ، وَبِمُعَافَاتِكَ مِنْ عُقُوبَتِكَ، وَأَعُوذُ بِكَ مِنْكَ، لَا أُحْصِي ثَنَاءً عَلَيْكَ أَنْتَ كَمَا أَثْنَيْتَ عَلَى نَفْسِكَ",
                "رواه مسلم");
        sections.add(s19);

        // 20. دعاء دخول السوق
        HisnSection s20 = new HisnSection("دعاء دخول السوق");
        s20.addDua(
                "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ، وَهُوَ حَيٌّ لَا يَمُوتُ، بِيَدِهِ الْخَيْرُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "رواه الترمذي");
        sections.add(s20);

        // 21. دعاء السفر
        HisnSection s21 = new HisnSection("دعاء السفر");
        s21.addDua(
                "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ، اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ وَالْخَلِيفَةُ فِي الْأَهْلِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ وَعْثَاءِ السَّفَرِ وَكَآبَةِ الْمَنْظَرِ وَسُوءِ الْمُنْقَلَبِ فِي الْمَالِ وَالْأَهْلِ",
                "رواه مسلم");
        s21.addDua(
                "آيِبُونَ تَائِبُونَ عَابِدُونَ لِرَبِّنَا حَامِدُونَ (عند العودة من السفر)",
                "رواه مسلم");
        sections.add(s21);

        // 22. دعاء الطعام
        HisnSection s22 = new HisnSection("دعاء الطعام");
        s22.addDua(
                "بِسْمِ اللَّهِ (وإن نسي في أوله فليقل: بِسْمِ اللَّهِ فِي أَوَّلِهِ وَآخِرِهِ)",
                "رواه أبو داود والترمذي");
        s22.addDua(
                "اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ، بِسْمِ اللَّهِ",
                "رواه ابن السني");
        sections.add(s22);

        // 23. دعاء الفراغ من الطعام
        HisnSection s23 = new HisnSection("دعاء الفراغ من الطعام");
        s23.addDua(
                "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
                "رواه أبو داود والترمذي");
        s23.addDua(
                "الْحَمْدُ لِلَّهِ حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ، غَيْرَ مَكْفِيٍّ وَلَا مُوَدَّعٍ وَلَا مُسْتَغْنًى عَنْهُ رَبَّنَا",
                "رواه البخاري");
        sections.add(s23);

        // 24. دعاء المريض
        HisnSection s24 = new HisnSection("دعاء عيادة المريض");
        s24.addDua(
                "لَا بَأْسَ طَهُورٌ إِنْ شَاءَ اللَّهُ",
                "رواه البخاري");
        s24.addDua(
                "أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ (سبع مرات)",
                "رواه أبو داود والترمذي");
        s24.addDua(
                "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ، اشْفِهِ وَأَنْتَ الشَّافِي لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
                "رواه البخاري ومسلم");
        sections.add(s24);

        // 25. دعاء تعزية أهل الميت
        HisnSection s25 = new HisnSection("دعاء تعزية أهل الميت");
        s25.addDua(
                "إِنَّ لِلَّهِ مَا أَخَذَ، وَلَهُ مَا أَعْطَى، وَكُلُّ شَيْءٍ عِنْدَهُ بِأَجَلٍ مُسَمَّى، فَلْتَصْبِرْ وَلْتَحْتَسِبْ",
                "رواه البخاري ومسلم");
        s25.addDua(
                "أَعْظَمَ اللَّهُ أَجْرَكَ، وَأَحْسَنَ عَزَاءَكَ، وَغَفَرَ لِمَيِّتِكَ",
                "ذكره النووي في الأذكار");
        s25.addDua(
                "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ، اللَّهُمَّ أْجُرْنِي فِي مُصِيبَتِي وَأَخْلِفْ لِي خَيْرًا مِنْهَا",
                "رواه مسلم");
        sections.add(s25);

        // 26. دعاء الريح
        HisnSection s26 = new HisnSection("دعاء الريح");
        s26.addDua(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا وَخَيْرَ مَا فِيهَا وَخَيْرَ مَا أُرْسِلَتْ بِهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ مَا فِيهَا وَشَرِّ مَا أُرْسِلَتْ بِهِ",
                "رواه مسلم");
        s26.addDua(
                "اللَّهُمَّ اجْعَلْهَا رِيَاحًا وَلَا تَجْعَلْهَا رِيحًا",
                "ذكره ابن القيم في زاد المعاد");
        sections.add(s26);

        // 27. دعاء المطر
        HisnSection s27 = new HisnSection("دعاء المطر");
        s27.addDua(
                "اللَّهُمَّ صَيِّبًا نَافِعًا",
                "رواه البخاري");
        s27.addDua(
                "مُطِرْنَا بِفَضْلِ اللَّهِ وَرَحْمَتِهِ",
                "رواه البخاري ومسلم");
        s27.addDua(
                "اللَّهُمَّ حَوَالَيْنَا وَلَا عَلَيْنَا، اللَّهُمَّ عَلَى الْآكَامِ وَالظِّرَابِ وَبُطُونِ الْأَوْدِيَةِ وَمَنَابِتِ الشَّجَرِ",
                "رواه البخاري ومسلم");
        sections.add(s27);

        // 28. دعاء الكرب
        HisnSection s28 = new HisnSection("دعاء الكرب");
        s28.addDua(
                "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
                "رواه البخاري ومسلم");
        s28.addDua(
                "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ، وَأَصْلِحْ لِي شَأْنِي كُلَّهُ، لَا إِلَهَ إِلَّا أَنْتَ",
                "رواه أبو داود");
        s28.addDua(
                "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                "سورة الأنبياء: ٨٧");
        sections.add(s28);

        // 29. دعاء لقاء العدو
        HisnSection s29 = new HisnSection("دعاء لقاء العدو وذي السلطان");
        s29.addDua(
                "اللَّهُمَّ إِنَّا نَجْعَلُكَ فِي نُحُورِهِمْ، وَنَعُوذُ بِكَ مِنْ شُرُورِهِمْ",
                "رواه أبو داود");
        s29.addDua(
                "اللَّهُمَّ أَنْتَ عَضُدِي وَأَنْتَ نَصِيرِي، بِكَ أَحُولُ وَبِكَ أَصُولُ وَبِكَ أُقَاتِلُ",
                "رواه أبو داود والترمذي");
        s29.addDua(
                "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                "رواه البخاري");
        sections.add(s29);

        // 30. دعاء ختم القرآن
        HisnSection s30 = new HisnSection("دعاء ختم القرآن الكريم");
        s30.addDua(
                "اللَّهُمَّ ارْحَمْنِي بِالْقُرْآنِ وَاجْعَلْهُ لِي إِمَامًا وَنُورًا وَهُدًى وَرَحْمَةً",
                "ذكره النووي في التبيان");
        s30.addDua(
                "اللَّهُمَّ ذَكِّرْنِي مِنْهُ مَا نَسِيتُ، وَعَلِّمْنِي مِنْهُ مَا جَهِلْتُ، وَارْزُقْنِي تِلَاوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ، وَاجْعَلْهُ لِي حُجَّةً يَا رَبَّ الْعَالَمِينَ",
                "ذكره النووي في التبيان");
        s30.addDua(
                "اللَّهُمَّ اجْعَلِ الْقُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجَلَاءَ حُزْنِي، وَذَهَابَ هَمِّي",
                "رواه أحمد");
        sections.add(s30);

        return sections;
    }

    // ──────────────────────────────────────────────
    // RecyclerView Adapter
    // ──────────────────────────────────────────────

    class HisnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_DUA = 1;
        private List<DisplayItem> items;

        HisnAdapter(List<DisplayItem> items) {
            this.items = items;
        }

        void updateItems(List<DisplayItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View v = inflater.inflate(R.layout.item_hisn_header, parent, false);
                return new HeaderVH(v);
            } else {
                View v = inflater.inflate(R.layout.item_hisn_dua, parent, false);
                return new DuaVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            DisplayItem displayItem = items.get(position);
            if (holder instanceof HeaderVH) {
                HeaderVH hvh = (HeaderVH) holder;
                HisnSection section = displayItem.section;
                hvh.title.setText(section.title);
                hvh.arrow.setRotation(section.expanded ? 0 : 180);
                hvh.itemView.setOnClickListener(v -> {
                    section.expanded = !section.expanded;
                    hvh.arrow.animate().rotation(section.expanded ? 0 : 180).setDuration(200).start();
                    updateItems(buildDisplayList());
                });
            } else if (holder instanceof DuaVH) {
                DuaVH dvh = (DuaVH) holder;
                DuaItem dua = displayItem.dua;
                dvh.duaText.setText(dua.arabicText);
                dvh.duaSource.setText(dua.source);
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
                title = v.findViewById(R.id.tv_hisn_header);
                arrow = v.findViewById(R.id.iv_hisn_expand_arrow);
            }
        }

        class DuaVH extends RecyclerView.ViewHolder {
            TextView duaText, duaSource;

            DuaVH(View v) {
                super(v);
                duaText = v.findViewById(R.id.tv_hisn_dua_text);
                duaSource = v.findViewById(R.id.tv_hisn_dua_source);
            }
        }
    }
}
