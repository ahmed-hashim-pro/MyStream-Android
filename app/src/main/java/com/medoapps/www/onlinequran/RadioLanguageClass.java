package com.medoapps.www.onlinequran;

/**
 * Created by Ahmed Hashim on 12/26/15.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ASUS S550C on 18/01/2015.
 */
public class RadioLanguageClass {
    private Context context;

    public RadioLanguageClass(Context context) {
        this.context = context;
    }
    public RadioLanguageClass() {

    }

    public  ArrayList<String> ServerFolderName = new ArrayList<String>();
    public   ArrayList<AuthorClass> AutherListInfo = new ArrayList<AuthorClass>();
    private   ArrayList<AuthorClass> ListAya = new ArrayList<AuthorClass>();
    public   ArrayList<AuthorClass> ListAyaRanage = new ArrayList<AuthorClass>();
    //public static int ISBackgroundMusic = 1; // 1 for is not, 20 for out back ground 3 for in background
   // public static boolean  Firstentry  = true;
    // for public folder select
    //public static String OtherFolderName;

    //public static MediaPlayer mp;
    //=============================

    @SuppressWarnings("deprecation")
    public void setLocale(Locale locale){
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
        } else{
            configuration.locale=locale;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            context.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }*/

        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(configuration);
        } else {
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }

    }
    public void setAppLocale(String localeCode){
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }
     public TextView SetTextFont(TextView tv,String type)
    {
        TextView newtv = tv;
        Typeface tf = null;
        switch(SettingSaved.LanguageSelect)
        {
            case 1:
//                tf = Typeface.createFromAsset(context.getAssets(),R.font.rocketfuel);
                tf = ResourcesCompat.getFont(context, R.font.droidkufi_kegular);
                newtv.setTextSize(15);

                newtv.setTypeface(tf);

                break;
            case 2:
                tf = ResourcesCompat.getFont(context, R.font.ajarsans_regular);
                newtv.setTextSize(20);
                newtv.setTypeface(tf);

                break;
            //   up so on

        }
        return newtv;
    }
    public static String avalible()
    {
        if (SettingSaved.LanguageSelect == 1)

            return ("من الهاتف");

        else
            return ("From phone");


    }
    public static String disavalible()
    {
        if (SettingSaved.LanguageSelect == 1)

            return ("بث مباشر");

        else
            return ("online");

    }



    public    ArrayList<AuthorClass> AutherList()
    {
        //120 read of quran
        AutherListInfo.clear();
        if (SettingSaved.LanguageSelect == 1)
        {
            AutherListInfo.add(new AuthorClass( "mohammed_siddiq_alminshawi_mojawwad",   " إذاعة محمد صديق المنشاوي" , "المصحف المجود","https://qurango.net/radio/mohammed_siddiq_alminshawi_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "addokali_mohammad_alalim",   "إذاعة الدوكالي محمد العالم" , "قالون عن نافع","https://qurango.net/radio/addokali_mohammad_alalim"));//**
            AutherListInfo.add(new AuthorClass( "ahmed_altrabulsi",   "إذاعة أحمد الطرابلسي" , "حفص عن عاصم","https://qurango.net/radio/ahmed_altrabulsi"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_khader_altarabulsi",   "إذاعة أحمد خضر الطرابلسي" , "قالون عن نافع","https://qurango.net/radio/ahmad_khader_altarabulsi"));//**
            AutherListInfo.add(new AuthorClass( "ahmed_amer",   "إذاعة أحمد عامر" , "حفص عن عاصم","https://qurango.net/radio/ahmed_amer"));//**
            AutherListInfo.add(new AuthorClass( "ibrahim_aldosari",   "إذاعة ابراهيم الدوسري" , "ورش عن نافع","https://qurango.net/radio/ibrahim_aldosari"));//**
            AutherListInfo.add(new AuthorClass( "alfateh_alzubair",   "إذاعة الفاتح محمد الزبير" , "الدوري عن أبي عمرو","https://qurango.net/radio/alfateh_alzubair"));//**
            AutherListInfo.add(new AuthorClass( "jamaan_alosaimi",   "إذاعة جمعان العصيمي" , "حفص عن عاصم","https://qurango.net/radio/jamaan_alosaimi"));//**
            AutherListInfo.add(new AuthorClass( "hatem_fareed_alwaer",   "إذاعة حاتم فريد الواعر" , "حفص عن عاصم","https://qurango.net/radio/hatem_fareed_alwaer"));//**
            AutherListInfo.add(new AuthorClass( "khalid_almohana",   "إذاعة خالد المهنا" , "حفص عن عاصم","https://qurango.net/radio/khalid_almohana"));//**
            AutherListInfo.add(new AuthorClass( "tareq_abdulgani_daawob",   "إذاعة طارق عبدالغني دعوب" , "قالون عن نافع","https://qurango.net/radio/tareq_abdulgani_daawob"));//**
            AutherListInfo.add(new AuthorClass( "adel_alkhalbany",   "إذاعة عادل الكلباني" , "حفص عن عاصم","https://qurango.net/radio/adel_alkhalbany"));//**
            AutherListInfo.add(new AuthorClass( "abdulrahman_almajed",   "إذاعة عبدالرحمن الماجد" , "حفص عن عاصم","https://qurango.net/radio/abdulrahman_almajed"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_alkandari",   "إذاعة عبدالله الكندري" , "حفص عن عاصم","https://qurango.net/radio/abdullah_alkandari"));//**
            AutherListInfo.add(new AuthorClass( "ali_jaber",   "إذاعة علي جابر" , "حفص عن عاصم","https://qurango.net/radio/ali_jaber"));//**
            AutherListInfo.add(new AuthorClass( "ali_hajjaj_alsouasi",   "إذاعة علي حجاج السويسي" , "حفص عن عاصم","https://qurango.net/radio/ali_hajjaj_alsouasi"));//**
            AutherListInfo.add(new AuthorClass( "emad_hafez",   "إذاعة عماد زهير حافظ" , "حفص عن عاصم","https://qurango.net/radio/emad_hafez"));//**
            AutherListInfo.add(new AuthorClass( "omar_alqazabri",   "إذاعة عمر القزابري" , "ورش عن نافع","https://qurango.net/radio/omar_alqazabri"));//**
            AutherListInfo.add(new AuthorClass( "fares_abbad",   "إذاعة فارس عباد" , "حفص عن عاصم","https://qurango.net/radio/fares_abbad"));//**
            AutherListInfo.add(new AuthorClass( "maher_al_meaqli",   "إذاعة ماهر المعيقلي" , "حفص عن عاصم","https://qurango.net/radio/maher_al_meaqli"));//**
            AutherListInfo.add(new AuthorClass( "maher_shakhashero",   "إذاعة ماهر شخاشيرو" , "حفص عن عاصم","https://qurango.net/radio/maher_shakhashero"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_ayyub",   "إذاعة محمد أيوب" , "حفص عن عاصم","https://qurango.net/radio/mohammed_ayyub"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_altablaway",   "إذاعة محمد الطبلاوي" , "حفص عن عاصم","https://qurango.net/radio/mohammad_altablaway"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_allohaidan",   "إذاعة محمد اللحيدان" , "حفص عن عاصم","https://qurango.net/radio/mohammed_allohaidan"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_jibreel",   "إذاعة محمد جبريل" , "حفص عن عاصم","https://qurango.net/radio/mohammed_jibreel"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_rashad_alshareef",   "إذاعة محمد رشاد الشريف" , "حفص عن عاصم","https://qurango.net/radio/mohammad_rashad_alshareef"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_saleh_alim_shah",   "إذاعة محمد صالح عالم شاه" , "حفص عن عاصم","https://qurango.net/radio/mohammad_saleh_alim_shah"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_siddiq_alminshawi",   "إذاعة محمد صديق المنشاوي" , "حفص عن عاصم","https://qurango.net/radio/mohammed_siddiq_alminshawi"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_alabdullah_albizi",   "إذاعة محمد عبدالحكيم سعيد العبدالله" , "البزي وقنبل عن ابن كثير","https://qurango.net/radio/mohammad_alabdullah_albizi"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_alabdullah_aldorai",   " إذاعة محمد عبدالحكيم سعيد العبدالله" , "الدوري عن الكسائي","https://qurango.net/radio/mohammad_alabdullah_aldorai"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_abdullkarem",   " إذاعة محمد عبدالكريم" , "حفص عن عاصم","https://qurango.net/radio/mohammad_abdullkarem"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_abdullkarem_alasbahani",   "إذاعة محمد عبدالكريم" , "ورش عن نافع من طريق أبي بكر الأصبهاني","https://qurango.net/radio/mohammad_abdullkarem_alasbahani"));//**
            AutherListInfo.add(new AuthorClass( "mahmood_al_rifai",   "إذاعة محمود الرفاعي" , "حفص عن عاصم","https://qurango.net/radio/mahmood_al_rifai"));//**
            AutherListInfo.add(new AuthorClass( "mahmood_alsheimy",   "إذاعة محمود الشيمي" , "الدوري عن الكسائي","https://qurango.net/radio/mahmood_alsheimy"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_khalil_alhussary",   "إذاعة محمود خليل الحصري" , "حفص عن عاصم","https://qurango.net/radio/mahmoud_khalil_alhussary"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_khalil_alhussary_mojawwad",   "إذاعة محمود خليل الحصري" , "المصحف المجود","https://qurango.net/radio/mahmoud_khalil_alhussary_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_khalil_alhussary_warsh",   "إذاعة محمود خليل الحصري" , "ورش عن نافع","https://qurango.net/radio/mahmoud_khalil_alhussary_warsh"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_ali__albanna",   "إذاعة محمود علي البنا" , "حفص عن عاصم"," https://qurango.net/radio/mahmoud_ali__albanna"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_ali__albanna_mojawwad",   "إذاعة محمود علي البنا" , "المصحف المجود","https://qurango.net/radio/mahmoud_ali__albanna_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "mishary_alafasi",   "إذاعة مشاري العفاسي" , "حفص عن عاصم","https://qurango.net/radio/mishary_alafasi"));//**
            AutherListInfo.add(new AuthorClass( "mustafa_ismail",   "إذاعة مصطفى إسماعيل" , "حفص عن عاصم","https://qurango.net/radio/mustafa_ismail"));//**
            AutherListInfo.add(new AuthorClass( "mustafa_allahoni",   "إذاعة مصطفى اللاهوني" , "حفص عن عاصم","https://qurango.net/radio/mustafa_allahoni"));//**
            AutherListInfo.add(new AuthorClass( "mustafa_raad_alazawy",   "إذاعة مصطفى رعد العزاوي" , "حفص عن عاصم","https://qurango.net/radio/mustafa_raad_alazawy"));//**
            AutherListInfo.add(new AuthorClass( "moeedh_alharthi",   "إذاعة معيض الحارثي" , "حفص عن عاصم","https://qurango.net/radio/moeedh_alharthi"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany_aldori_an_abi_amr",   "إذاعة مفتاح السلطني" , "الدوري عن أبي عمرو","https://qurango.net/radio/muftah_alsaltany_aldori_an_abi_amr"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany_aldorai",   "إذاعة مفتاح السلطني" , "الدوري عن الكسائي","https://qurango.net/radio/muftah_alsaltany_aldorai"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany",   "إذاعة مفتاح السلطني" , "حفص عن عاصم","https://qurango.net/radio/muftah_alsaltany"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany_ibn_thakwan_an_ibn_amr",   "إذاعة مفتاح السلطني" , "ابن ذكوان عن ابن عامر","https://qurango.net/radio/muftah_alsaltany_ibn_thakwan_an_ibn_amr"));//**
            AutherListInfo.add(new AuthorClass( "mousa_bilal",   "إذاعة موسى بلال" , "حفص عن عاصم","https://qurango.net/radio/mousa_bilal"));//**
            AutherListInfo.add(new AuthorClass( "nasser_alqatami",   " إذاعة ناصر القطامي" , "حفص عن عاصم","https://qurango.net/radio/nasser_alqatami"));//**
            AutherListInfo.add(new AuthorClass( "nabil_al_rifay",   " إذاعة نبيل الرفاعي" , "حفص عن عاصم","https://qurango.net/radio/nabil_al_rifay"));//**
            AutherListInfo.add(new AuthorClass( "neamah_alhassan",   "إذاعة نعمة الحسان" , "حفص عن عاصم","https://qurango.net/radio/neamah_alhassan"));//**
            AutherListInfo.add(new AuthorClass( "hani_arrifai",   "إذاعة هاني الرفاعي" , "حفص عن عاصم","https://qurango.net/radio/hani_arrifai"));//**
            AutherListInfo.add(new AuthorClass( "waleed_alnaehi",   "إذاعة وليد النائحي" , "قالون عن نافع من طريق أبي نشيط","https://qurango.net/radio/waleed_alnaehi"));//**
            AutherListInfo.add(new AuthorClass( "yasser_aldosari",   "إذاعة ياسر الدوسري" , "حفص عن عاصم","https://qurango.net/radio/yasser_aldosari"));//**
            AutherListInfo.add(new AuthorClass( "yasser_alqurashi",   "إذاعة ياسر القرشي" , "حفص عن عاصم","https://qurango.net/radio/yasser_alqurashi"));//**
            AutherListInfo.add(new AuthorClass( "yasser_almazroyee",   "إذاعة ياسر المزروعي" , "قراءة يعقوب الحضرمي بروايتي رويس وروح","https://qurango.net/radio/yasser_almazroyee"));//**
            AutherListInfo.add(new AuthorClass( "yahya_hawwa",   " إذاعة يحيى حوا" , "حفص عن عاصم","https://qurango.net/radio/yahya_hawwa"));//**
            AutherListInfo.add(new AuthorClass( "yousef_alshoaey",   "إذاعة يوسف الشويعي" , "حفص عن عاصم","https://qurango.net/radio/yousef_alshoaey"));//**
            AutherListInfo.add(new AuthorClass( "yousef_bin_noah_ahmad",   "إذاعة يوسف بن نوح أحمد" , "حفص عن عاصم","https://qurango.net/radio/yousef_bin_noah_ahmad"));//**
            AutherListInfo.add(new AuthorClass( "tarateel",   "---تراتيل قصيرة متميزة---" , "","https://qurango.net/radio/tarateel"));//**
            AutherListInfo.add(new AuthorClass( "sahabah",   "-إذاعة صور من حياة الصحابة رضوان الله عليهم-" , "","https://qurango.net/radio/sahabah"));//**
            AutherListInfo.add(new AuthorClass( "mix",   "-الإذاعة العامة - اذاعة متنوعة لمختلف القراء" , "","https://qurango.net/radio/mix"));//**
            AutherListInfo.add(new AuthorClass( "mukhtasartafsir",   "-المختصر في تفسير القرآن الكريم-" , "","https://qurango.net/radio/mukhtasartafsir"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_deban",   "أحمد ديبان" , "حفص عن عاصم","https://qurango.net/radio/ahmad_deban"));//**
            AutherListInfo.add(new AuthorClass( "athkar_sabah",   "أذكار الصباح" , "","https://qurango.net/radio/athkar_sabah"));//**
            AutherListInfo.add(new AuthorClass( "athkar_masa",   "أذكار المساء" , "","https://qurango.net/radio/athkar_masa"));//**
            AutherListInfo.add(new AuthorClass( "albaqarah",   "إذاعة ---سورة البقرة - لعدد من القراء---" , "","https://qurango.net/radio/albaqarah"));//**
            AutherListInfo.add(new AuthorClass( "tafseer",   "إذاعة --تفسير القران الكريم--" , "","https://qurango.net/radio/tafseer"));//**
            AutherListInfo.add(new AuthorClass( "salma",   "إذاعة -تلاوات خاشعة-" , "","https://qurango.net/radio/salma"));//**
            AutherListInfo.add(new AuthorClass( "sakeenah",   "إذاعة آيات السكينة" , "حفص عن عاصم","https://qurango.net/radio/sakeenah"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_alhawashi",   "إذاعة أحمد الحواشي" , "حفص عن عاصم","https://qurango.net/radio/ahmad_alhawashi"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_alajmy",   "إذاعة أحمد العجمي" , "حفص عن عاصم","https://qurango.net/radio/ahmad_alajmy"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_shaheen",   "إذاعة أحمد خليل شاهين" , "حفص عن عاصم","https://qurango.net/radio/ahmad_shaheen"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_saber",   "إذاعة أحمد صابر" , "حفص عن عاصم","https://qurango.net/radio/ahmad_saber"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_nauina",   "إذاعة أحمد نعينع" , "حفص عن عاصم","https://qurango.net/radio/ahmad_nauina"));//**
            AutherListInfo.add(new AuthorClass( "akram_alalaqmi",   "إذاعة أكرم العلاقمي" , "حفص عن عاصم","https://qurango.net/radio/akram_alalaqmi"));//**
            AutherListInfo.add(new AuthorClass( "ibrahim_alakdar",   "إذاعة إبراهيم الأخضر" , "حفص عن عاصم","https://qurango.net/radio/ibrahim_alakdar"));//**
            AutherListInfo.add(new AuthorClass( "idrees_abkr",   "إذاعة إدريس أبكر" , "حفص عن عاصم","https://qurango.net/radio/idrees_abkr"));//**
            AutherListInfo.add(new AuthorClass( "roqiah",   "إذاعة الرقية الشرعية" , "","https://qurango.net/radio/roqiah"));//**
            AutherListInfo.add(new AuthorClass( "alzain_mohammad_ahmad",   "إذاعة الزين محمد أحمد" , "حفص عن عاصم","https://qurango.net/radio/alzain_mohammad_ahmad"));//**
            AutherListInfo.add(new AuthorClass( "aloyoon_alkoshi",   "إذاعة العيون الكوشي" , "ورش عن نافع","https://qurango.net/radio/aloyoon_alkoshi"));//**
            AutherListInfo.add(new AuthorClass( "fatwa",   "إذاعة الفتاوى العامة" , "","https://qurango.net/radio/fatwa"));//**
            AutherListInfo.add(new AuthorClass( "alqaria_yassen",   "إذاعة القارئ ياسين" , "ورش عن نافع","https://qurango.net/radio/alqaria_yassen"));//**
            AutherListInfo.add(new AuthorClass( "eid",   "إذاعة تكبيرات العيد" , "","https://qurango.net/radio/eid"));//**
            AutherListInfo.add(new AuthorClass( "tawfeeq_assayegh",   "إذاعة توفيق الصايغ" , "حفص عن عاصم","https://qurango.net/radio/tawfeeq_assayegh"));//**
            AutherListInfo.add(new AuthorClass( "jamal_shaker_abdullah",   "إذاعة جمال شاكر عبدالله" , "حفص عن عاصم","https://qurango.net/radio/jamal_shaker_abdullah"));//**
            AutherListInfo.add(new AuthorClass( "khalid_aljileel",   "إذاعة خالد الجليل" , "حفص عن عاصم","https://qurango.net/radio/khalid_aljileel"));//**
            AutherListInfo.add(new AuthorClass( "khaled_alqahtani",   "إذاعة خالد القحطاني" , "حفص عن عاصم","https://qurango.net/radio/khaled_alqahtani"));//**
            AutherListInfo.add(new AuthorClass( "khalid_abdulkafi",   "إذاعة خالد عبدالكافي" , "حفص عن عاصم","https://qurango.net/radio/khalid_abdulkafi"));//**
            AutherListInfo.add(new AuthorClass( "khalifa_altunaiji",   "إذاعة خليفة الطنيجي" , "حفص عن عاصم","https://qurango.net/radio/khalifa_altunaiji"));//**
            AutherListInfo.add(new AuthorClass( "zaki_daghistani",   "إذاعة زكي داغستاني" , "حفص عن عاصم","https://qurango.net/radio/zaki_daghistani"));//**
            AutherListInfo.add(new AuthorClass( "saad_alghamdi",   "إذاعة سعد الغامدي" , "حفص عن عاصم","https://qurango.net/radio/saad_alghamdi"));//**
            AutherListInfo.add(new AuthorClass( "saud_alshuraim",   "إذاعة سعود الشريم" , "حفص عن عاصم","https://qurango.net/radio/saud_alshuraim"));//**
            AutherListInfo.add(new AuthorClass( "sahl_yassin",   "إذاعة سهل ياسين" , "حفص عن عاصم","https://qurango.net/radio/sahl_yassin"));//**
            AutherListInfo.add(new AuthorClass( "sayeed_ramadan",   "إذاعة سيد رمضان" , "حفص عن عاصم","https://qurango.net/radio/sayeed_ramadan"));//**
            AutherListInfo.add(new AuthorClass( "shaik_abu_bakr_al_shatri",   "إذاعة شيخ أبو بكر الشاطري" , "حفص عن عاصم","https://qurango.net/radio/shaik_abu_bakr_al_shatri"));//**
            AutherListInfo.add(new AuthorClass( "shirazad_taher",   "إذاعة شيرزاد عبدالرحمن طاهر" , "حفص عن عاصم","https://qurango.net/radio/shirazad_taher"));//**
            AutherListInfo.add(new AuthorClass( "saber_abdulhakm",   "إذاعة صابر عبدالحكم" , "حفص عن عاصم","https://qurango.net/radio/saber_abdulhakm"));//**
            AutherListInfo.add(new AuthorClass( "salah_albudair",   "إذاعة صلاح البدير" , "حفص عن عاصم","https://qurango.net/radio/salah_albudair"));//**
            AutherListInfo.add(new AuthorClass( "salah_alhashim",   "إذاعة صلاح الهاشم" , "حفص عن عاصم","https://qurango.net/radio/salah_alhashim"));//**
            AutherListInfo.add(new AuthorClass( "slaah_bukhatir",   "إذاعة صلاح بو خاطر" , "حفص عن عاصم","https://qurango.net/radio/slaah_bukhatir"));//**
            AutherListInfo.add(new AuthorClass( "adel_ryyan",   "إذاعة عادل ريان" , "حفص عن عاصم","https://qurango.net/radio/adel_ryyan"));//**
            AutherListInfo.add(new AuthorClass( "abdelbari_altoubayti",   "إذاعة عبدالبارئ الثبيتي" , "حفص عن عاصم","https://qurango.net/radio/abdelbari_altoubayti"));//**
            AutherListInfo.add(new AuthorClass( "abdulbari_mohammad",   "إذاعة عبدالبارئ محمد" , "حفص عن عاصم"," https://qurango.net/radio/abdulbari_mohammad"));//**
            AutherListInfo.add(new AuthorClass( "abdulbasit_abdulsamad_mojawwad",   "إذاعة عبدالباسط عبدالصمد" , "المصحف المجود","https://qurango.net/radio/abdulbasit_abdulsamad_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "abdulbasit_abdulsamad_warsh",   "إذاعة عبدالباسط عبدالصمد" , "ورش عن نافع","https://qurango.net/radio/abdulbasit_abdulsamad_warsh"));//**
            AutherListInfo.add(new AuthorClass( "abdulbasit_abdulsamad",   "إذاعة عبدالباسط عبدالصمد" , "حفص عن عاصم","https://qurango.net/radio/abdulbasit_abdulsamad"));//**
            AutherListInfo.add(new AuthorClass( "abdulrahman_alsudaes",   "إذاعة عبدالرحمن السديس" , "حفص عن عاصم","https://qurango.net/radio/abdulrahman_alsudaes"));//**
            AutherListInfo.add(new AuthorClass( "abdulrasheed_soufi_khalaf",   "إذاعة عبدالرشيد صوفي" , " خلف عن حمزة","https://qurango.net/radio/abdulrasheed_soufi_khalaf"));//**
            AutherListInfo.add(new AuthorClass( "abdulrasheed_soufi_assosi",   "إذاعة عبدالرشيد صوفي" , "السوسي عن أبي عمرو","https://qurango.net/radio/abdulrasheed_soufi_assosi"));//**
            AutherListInfo.add(new AuthorClass( "abdul_aziz_alahmad",   "إذاعة عبدالعزيز الأحمد" , "حفص عن عاصم","https://qurango.net/radio/abdul_aziz_alahmad"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_alkhalaf",   "إذاعة عبدالله الخلف" , "حفص عن عاصم","ttps://qurango.net/radio/abdullah_alkhalaf"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_almattrod",   "إذاعة عبدالله المطرود" , "حفص عن عاصم","https://qurango.net/radio/abdullah_almattrod"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_almousa",   "إذاعة عبدالله الموسى" , "حفص عن عاصم","https://qurango.net/radio/abdullah_almousa"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_basfer",   "إذاعة عبدالله بصفر" , "حفص عن عاصم","https://qurango.net/radio/abdullah_basfer"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_khayyat",   "إذاعة عبدالله خياط" , "حفص عن عاصم","https://qurango.net/radio/abdullah_khayyat"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_aljohany",   "إذاعة عبدالله عواد الجهني" , "حفص عن عاصم","https://qurango.net/radio/abdullah_aljohany"));//**
            AutherListInfo.add(new AuthorClass( "abdulmohsin_alharthy",   "إذاعة عبدالمحسن الحارثي" , "حفص عن عاصم","https://qurango.net/radio/abdulmohsin_alharthy"));//**
            AutherListInfo.add(new AuthorClass( "abdulmohsin_alobaikan",   "إذاعة عبدالمحسن العبيكان" , "حفص عن عاصم","https://qurango.net/radio/abdulmohsin_alobaikan"));//**
            AutherListInfo.add(new AuthorClass( "abdulmohsen_alqasim",   "إذاعة عبدالمحسن القاسم" , "حفص عن عاصم","https://qurango.net/radio/abdulmohsen_alqasim"));//**
            AutherListInfo.add(new AuthorClass( "abdulhadi_kanakeri",   "إذاعة عبدالهادي أحمد كناكري" , "حفص عن عاصم","https://qurango.net/radio/abdulhadi_kanakeri"));//**
            AutherListInfo.add(new AuthorClass( "abdulwadood_haneef",   "إذاعة عبدالودود حنيف" , "حفص عن عاصم","https://qurango.net/radio/abdulwadood_haneef"));//**
            AutherListInfo.add(new AuthorClass( "ali_alhuthaifi_qalon",   "إذاعة علي الحذيفي" , "قالون عن نافع","https://qurango.net/radio/ali_alhuthaifi_qalon"));//**
            AutherListInfo.add(new AuthorClass( "ali_alhuthaifi",   "إذاعة علي بن عبدالرحمن الحذيفي" , "حفص عن عاصم","https://qurango.net/radio/ali_alhuthaifi"));//**
            AutherListInfo.add(new AuthorClass( "ayyoub2",   "إذاعة محمد أيوب - قراءة متميزة-" , "حفص عن عاصم","https://qurango.net/radio/ayyoub2"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_osman_khan",   "إذاعة محمد عثمان خان" , "حفص عن عاصم","https://qurango.net/radio/mohammed_osman_khan"));//**
            AutherListInfo.add(new AuthorClass( "nasser_almajed",   "إذاعة ناصر الماجد" , "حفص عن عاصم","https://qurango.net/radio/nasser_almajed"));//**
            AutherListInfo.add(new AuthorClass( "hitham_aljadani",   "إذاعة هيثم الجدعاني" , "حفص عن عاصم","https://qurango.net/radio/hitham_aljadani"));//**
            AutherListInfo.add(new AuthorClass( "bandar_balilah",   "بندر بليله" , "حفص عن عاصم","https://qurango.net/radio/bandar_balilah"));//**
            AutherListInfo.add(new AuthorClass( "fi_zilal_alsiyra",   "في ظلال السيرة النبوية - 400 حلقة عن سيرة نبينا محمد صلى الله عليه وسلم" , "","https://qurango.net/radio/fi_zilal_alsiyra"));//**
            AutherListInfo.add(new AuthorClass( "majed_alzamel",   "ماجد الزامل" , "حفص عن عاصم","https://qurango.net/radio/majed_alzamel"));//**
            AutherListInfo.add(new AuthorClass( "nasser_alosfor",   "ناصر العصفور" , "حفص عن عاصم","https://qurango.net/radio/nasser_alosfor"));//**



        }





        else
        {
            AutherListInfo.add(new AuthorClass( "mohammed_siddiq_alminshawi_mojawwad",   " Radio Mohammed Siddiq Al-Minshawi" , "Almusshaf Al Mojawwad","https://qurango.net/radio/mohammed_siddiq_alminshawi_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "addokali_mohammad_alalim",   "Radio Addokali Mohammad Alalim" , "Rewayat Qalon A'n Nafi'","https://qurango.net/radio/addokali_mohammad_alalim"));//**
            AutherListInfo.add(new AuthorClass( "ahmed_altrabulsi",   "Radio Ahmed Al-trabulsi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmed_altrabulsi"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_khader_altarabulsi",   "Radio Ahmad Khader Al-Tarabulsi" , "Rewayat Qalon A'n Nafi'","https://qurango.net/radio/ahmad_khader_altarabulsi"));//**
            AutherListInfo.add(new AuthorClass( "ahmed_amer",   "Radio Ahmed Amer" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmed_amer"));//**
            AutherListInfo.add(new AuthorClass( "ibrahim_aldosari",   "Radio Ibrahim Aldosari" , "Rewayat Warsh A'n Nafi'","https://qurango.net/radio/ibrahim_aldosari"));//**
            AutherListInfo.add(new AuthorClass( "alfateh_alzubair",   "Radio Alfateh Alzubair" , "Rewayat Aldori A'n Abi Amr","https://qurango.net/radio/alfateh_alzubair"));//**
            AutherListInfo.add(new AuthorClass( "jamaan_alosaimi",   "Radio Jamaan Alosaimi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/jamaan_alosaimi"));//**
            AutherListInfo.add(new AuthorClass( "hatem_fareed_alwaer",   "Radio Hatem Fareed Alwaer" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/hatem_fareed_alwaer"));//**
            AutherListInfo.add(new AuthorClass( "khalid_almohana",   "Radio Khalid Almohana" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/khalid_almohana"));//**
            AutherListInfo.add(new AuthorClass( "tareq_abdulgani_daawob",   "Radio Tareq Abdulgani daawob" , "Rewayat Qalon A'n Nafi'","https://qurango.net/radio/tareq_abdulgani_daawob"));//**
            AutherListInfo.add(new AuthorClass( "adel_alkhalbany",   "Radio Adel Al-Khalbany" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/adel_alkhalbany"));//**
            AutherListInfo.add(new AuthorClass( "abdulrahman_almajed",   "Radio Abdulrahman Al-Majed" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulrahman_almajed"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_alkandari",   "Radio Abdullah Al-Kandari" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdullah_alkandari"));//**
            AutherListInfo.add(new AuthorClass( "ali_jaber",   "Radio Ali Jaber" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ali_jaber"));//**
            AutherListInfo.add(new AuthorClass( "ali_hajjaj_alsouasi",   "Radio Ali Hajjaj Alsouasi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ali_hajjaj_alsouasi"));//**
            AutherListInfo.add(new AuthorClass( "emad_hafez",   "Radio Emad Hafez" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/emad_hafez"));//**
            AutherListInfo.add(new AuthorClass( "omar_alqazabri",   "Radio Omar Al-Qazabri" , "Rewayat Warsh A'n Nafi'","https://qurango.net/radio/omar_alqazabri"));//**
            AutherListInfo.add(new AuthorClass( "fares_abbad",   "Radio Fares Abbad" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/fares_abbad"));//**
            AutherListInfo.add(new AuthorClass( "maher_al_meaqli",   "Radio Maher Al Meaqli" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/maher_al_meaqli"));//**
            AutherListInfo.add(new AuthorClass( "maher_shakhashero",   "Radio Maher Shakhashero" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/maher_shakhashero"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_ayyub",   "Radio Mohammed Ayyub" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammed_ayyub"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_altablaway",   "Radio Mohammad Al-Tablaway" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammad_altablaway"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_allohaidan",   "Radio Mohammed Al-Lohaidan" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammed_allohaidan"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_jibreel",   "Radio Mohammed Jibreel" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammed_jibreel"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_rashad_alshareef",   "Radio Mohammad Rashad Alshareef" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammad_rashad_alshareef"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_saleh_alim_shah",   "Radio Mohammad Saleh Alim Shah" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammad_saleh_alim_shah"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_siddiq_alminshawi",   "Radio Mohammed Siddiq Al-Minshawi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammed_siddiq_alminshawi"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_alabdullah_albizi",   "Radio Mohammad Al-Abdullah" , "البزي وقنبل عن ابن كثير","https://qurango.net/radio/mohammad_alabdullah_albizi"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_alabdullah_aldorai",   " Radio Mohammad Al-Abdullah" , "الدوري عن الكسائي","https://qurango.net/radio/mohammad_alabdullah_aldorai"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_abdullkarem",   " Radio Mohammad Abdullkarem" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammad_abdullkarem"));//**
            AutherListInfo.add(new AuthorClass( "mohammad_abdullkarem_alasbahani",   "Radio Mohammad Abdullkarem" , "Rewayat Warsh A'n Nafi' من طريق أبي بكر الأصبهاني","https://qurango.net/radio/mohammad_abdullkarem_alasbahani"));//**
            AutherListInfo.add(new AuthorClass( "mahmood_al_rifai",   "Radio Mahmood Al rifai" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mahmood_al_rifai"));//**
            AutherListInfo.add(new AuthorClass( "mahmood_alsheimy",   "Radio Mahmood AlSheimy" , "الدوري عن الكسائي","https://qurango.net/radio/mahmood_alsheimy"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_khalil_alhussary",   "Radio Mahmoud Khalil Al-Hussary" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mahmoud_khalil_alhussary"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_khalil_alhussary_mojawwad",   "Radio Mahmoud Khalil Al-Hussary" , "Almusshaf Al Mojawwad","https://qurango.net/radio/mahmoud_khalil_alhussary_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_khalil_alhussary_warsh",   "Radio Mahmoud Khalil Al-Hussary" , "Rewayat Warsh A'n Nafi'","https://qurango.net/radio/mahmoud_khalil_alhussary_warsh"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_ali__albanna",   "Radio Mahmoud Ali  Albanna" , "Rewayat Hafs A'n Assem"," https://qurango.net/radio/mahmoud_ali__albanna"));//**
            AutherListInfo.add(new AuthorClass( "mahmoud_ali__albanna_mojawwad",   "Radio Mahmoud Ali  Albanna" , "Almusshaf Al Mojawwad","https://qurango.net/radio/mahmoud_ali__albanna_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "mishary_alafasi",   "Radio Mishary Alafasi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mishary_alafasi"));//**
            AutherListInfo.add(new AuthorClass( "mustafa_ismail",   "Radio Mustafa Ismail" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mustafa_ismail"));//**
            AutherListInfo.add(new AuthorClass( "mustafa_allahoni",   "Radio Mustafa Al-Lahoni" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mustafa_allahoni"));//**
            AutherListInfo.add(new AuthorClass( "mustafa_raad_alazawy",   "Radio Mustafa raad Alazawy" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mustafa_raad_alazawy"));//**
            AutherListInfo.add(new AuthorClass( "moeedh_alharthi",   "Radio Moeedh Alharthi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/moeedh_alharthi"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany_aldori_an_abi_amr",   "إذاعة مفتاح السلطني" , "Rewayat Aldori A'n Abi Amr","https://qurango.net/radio/muftah_alsaltany_aldori_an_abi_amr"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany_aldorai",   "Radio Muftah Alsaltany" , "الدوري عن الكسائي","https://qurango.net/radio/muftah_alsaltany_aldorai"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany",   "Radio Muftah Alsaltany" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/muftah_alsaltany"));//**
            AutherListInfo.add(new AuthorClass( "muftah_alsaltany_ibn_thakwan_an_ibn_amr",   "Radio Muftah Alsaltany" , "ابن ذكوان عن ابن عامر","https://qurango.net/radio/muftah_alsaltany_ibn_thakwan_an_ibn_amr"));//**
            AutherListInfo.add(new AuthorClass( "mousa_bilal",   "Radio Mousa Bilal" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mousa_bilal"));//**
            AutherListInfo.add(new AuthorClass( "nasser_alqatami",   " Radio Nasser Alqatami" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/nasser_alqatami"));//**
            AutherListInfo.add(new AuthorClass( "nabil_al_rifay",   " Radio Nabil Al Rifay" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/nabil_al_rifay"));//**
            AutherListInfo.add(new AuthorClass( "neamah_alhassan",   "Radio Neamah Al-Hassan" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/neamah_alhassan"));//**
            AutherListInfo.add(new AuthorClass( "hani_arrifai",   "Radio Hani Arrifai" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/hani_arrifai"));//**
            AutherListInfo.add(new AuthorClass( "waleed_alnaehi",   "Radio Waleed Alnaehi" , "Rewayat Qalon A'n Nafi' من طريق أبي نشيط","https://qurango.net/radio/waleed_alnaehi"));//**
            AutherListInfo.add(new AuthorClass( "yasser_aldosari",   "Radio Yasser Al-Dosari" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/yasser_aldosari"));//**
            AutherListInfo.add(new AuthorClass( "yasser_alqurashi",   "Radio Yasser Al-Qurashi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/yasser_alqurashi"));//**
            AutherListInfo.add(new AuthorClass( "yasser_almazroyee",   "Radio Yasser Al-Mazroyee" , "قراءة يعقوب الحضرمي بروايتي رويس وروح","https://qurango.net/radio/yasser_almazroyee"));//**
            AutherListInfo.add(new AuthorClass( "yahya_hawwa",   " Radio Yahya Hawwa" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/yahya_hawwa"));//**
            AutherListInfo.add(new AuthorClass( "yousef_alshoaey",   "Radio Yousef Alshoaey" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/yousef_alshoaey"));//**
            AutherListInfo.add(new AuthorClass( "yousef_bin_noah_ahmad",   "Radio Yousef Bin Noah Ahmad" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/yousef_bin_noah_ahmad"));//**
            AutherListInfo.add(new AuthorClass( "tarateel",   "---Amazing short Recitations---" , "","https://qurango.net/radio/tarateel"));//**
            AutherListInfo.add(new AuthorClass( "sahabah",   "-إذاعة صور من حياة الصحابة رضوان الله عليهم-" , "","https://qurango.net/radio/sahabah"));//**
            AutherListInfo.add(new AuthorClass( "mix",   "-Main Radio-" , "","https://qurango.net/radio/mix"));//**
            AutherListInfo.add(new AuthorClass( "mukhtasartafsir",   "-المختصر في تفسير القرآن الكريم-" , "","https://qurango.net/radio/mukhtasartafsir"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_deban",   "Ahmad Deban" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmad_deban"));//**
            AutherListInfo.add(new AuthorClass( "athkar_sabah",   "أذكار الصباح" , "","https://qurango.net/radio/athkar_sabah"));//**
            AutherListInfo.add(new AuthorClass( "athkar_masa",   "أذكار المساء" , "","https://qurango.net/radio/athkar_masa"));//**
            AutherListInfo.add(new AuthorClass( "albaqarah",   "-Surah Al-Baqarah - Many Reciters-" , "","https://qurango.net/radio/albaqarah"));//**
            AutherListInfo.add(new AuthorClass( "tafseer",   "--Quran Tafseer--" , "","https://qurango.net/radio/tafseer"));//**
            AutherListInfo.add(new AuthorClass( "salma",   "-Beautiful Recitations-" , "","https://qurango.net/radio/salma"));//**
            AutherListInfo.add(new AuthorClass( "sakeenah",   "إذاعة آيات السكينة" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/sakeenah"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_alhawashi",   "Radio Ahmad Al-Hawashi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmad_alhawashi"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_alajmy",   "Radio Ahmad Al-Ajmy" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmad_alajmy"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_shaheen",   "Ahmad Shaheen" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmad_shaheen"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_saber",   "Radio Ahmad Saber" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmad_saber"));//**
            AutherListInfo.add(new AuthorClass( "ahmad_nauina",   "Radio Ahmad Nauina" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ahmad_nauina"));//**
            AutherListInfo.add(new AuthorClass( "akram_alalaqmi",   "Radio Akram Alalaqmi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/akram_alalaqmi"));//**
            AutherListInfo.add(new AuthorClass( "ibrahim_alakdar",   "Radio Ibrahim Al-Akdar" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ibrahim_alakdar"));//**
            AutherListInfo.add(new AuthorClass( "idrees_abkr",   "Radio Idrees Abkr" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/idrees_abkr"));//**
            AutherListInfo.add(new AuthorClass( "roqiah",   "إذاعة الرقية الشرعية" , "","https://qurango.net/radio/roqiah"));//**
            AutherListInfo.add(new AuthorClass( "alzain_mohammad_ahmad",   "Radio Alzain Mohammad Ahmad" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/alzain_mohammad_ahmad"));//**
            AutherListInfo.add(new AuthorClass( "aloyoon_alkoshi",   "Radio Aloyoon Al-Koshi" , "Rewayat Warsh A'n Nafi'","https://qurango.net/radio/aloyoon_alkoshi"));//**
            AutherListInfo.add(new AuthorClass( "fatwa",   "إذاعة الفتاوى العامة" , "","https://qurango.net/radio/fatwa"));//**
            AutherListInfo.add(new AuthorClass( "alqaria_yassen",   "Radio Al-Qaria Yassen" , "Rewayat Warsh A'n Nafi'","https://qurango.net/radio/alqaria_yassen"));//**
            AutherListInfo.add(new AuthorClass( "eid",   "إذاعة تكبيرات العيد" , "","https://qurango.net/radio/eid"));//**
            AutherListInfo.add(new AuthorClass( "tawfeeq_assayegh",   "Radio Tawfeeq As-Sayegh" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/tawfeeq_assayegh"));//**
            AutherListInfo.add(new AuthorClass( "jamal_shaker_abdullah",   "Radio Jamal Shaker Abdullah" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/jamal_shaker_abdullah"));//**
            AutherListInfo.add(new AuthorClass( "khalid_aljileel",   "Khalid Al-Jileel" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/khalid_aljileel"));//**
            AutherListInfo.add(new AuthorClass( "khaled_alqahtani",   "Radio Khaled Al-Qahtani" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/khaled_alqahtani"));//**
            AutherListInfo.add(new AuthorClass( "khalid_abdulkafi",   "Radio Khalid Abdulkafi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/khalid_abdulkafi"));//**
            AutherListInfo.add(new AuthorClass( "khalifa_altunaiji",   "Radio Khalifa Altunaiji" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/khalifa_altunaiji"));//**
            AutherListInfo.add(new AuthorClass( "zaki_daghistani",   "Radio Zaki Daghistani" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/zaki_daghistani"));//**
            AutherListInfo.add(new AuthorClass( "saad_alghamdi",   "Radio Saad Al-Ghamdi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/saad_alghamdi"));//**
            AutherListInfo.add(new AuthorClass( "saud_alshuraim",   "Radio Saud Al-Shuraim" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/saud_alshuraim"));//**
            AutherListInfo.add(new AuthorClass( "sahl_yassin",   "Radio Sahl Yassin" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/sahl_yassin"));//**
            AutherListInfo.add(new AuthorClass( "sayeed_ramadan",   "Radio Sayeed Ramadan" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/sayeed_ramadan"));//**
            AutherListInfo.add(new AuthorClass( "shaik_abu_bakr_al_shatri",   "Radio Shaik Abu Bakr Al Shatri" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/shaik_abu_bakr_al_shatri"));//**
            AutherListInfo.add(new AuthorClass( "shirazad_taher",   "Radio Shirazad Taher" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/shirazad_taher"));//**
            AutherListInfo.add(new AuthorClass( "saber_abdulhakm",   "Radio Saber Abdulhakm" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/saber_abdulhakm"));//**
            AutherListInfo.add(new AuthorClass( "salah_albudair",   "Radio Salah Albudair" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/salah_albudair"));//**
            AutherListInfo.add(new AuthorClass( "salah_alhashim",   "Radio Salah Alhashim" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/salah_alhashim"));//**
            AutherListInfo.add(new AuthorClass( "slaah_bukhatir",   "Radio Slaah Bukhatir" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/slaah_bukhatir"));//**
            AutherListInfo.add(new AuthorClass( "adel_ryyan",   "Radio Adel Ryyan" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/adel_ryyan"));//**
            AutherListInfo.add(new AuthorClass( "abdelbari_altoubayti",   "Radio Abdelbari Al-Toubayti" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdelbari_altoubayti"));//**
            AutherListInfo.add(new AuthorClass( "abdulbari_mohammad",   "Radio Abdulbari Mohammad" , "Rewayat Hafs A'n Assem"," https://qurango.net/radio/abdulbari_mohammad"));//**
            AutherListInfo.add(new AuthorClass( "abdulbasit_abdulsamad_mojawwad",   "Radio Abdulbasit Abdulsamad" , "Almusshaf Al Mojawwad","https://qurango.net/radio/abdulbasit_abdulsamad_mojawwad"));//**
            AutherListInfo.add(new AuthorClass( "abdulbasit_abdulsamad_warsh",   "إذاعة عبدالباسط عبدالصمد" , "Rewayat Warsh A'n Nafi'","https://qurango.net/radio/abdulbasit_abdulsamad_warsh"));//**
            AutherListInfo.add(new AuthorClass( "abdulbasit_abdulsamad",   "Radio Abdulbasit Abdulsamad" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulbasit_abdulsamad"));//**
            AutherListInfo.add(new AuthorClass( "abdulrahman_alsudaes",   "Radio Abdulrahman Alsudaes" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulrahman_alsudaes"));//**
            AutherListInfo.add(new AuthorClass( "abdulrasheed_soufi_khalaf",   "abdulrasheed_soufi_khalaf" , " خلف عن حمزة","https://qurango.net/radio/abdulrasheed_soufi_khalaf"));//**
            AutherListInfo.add(new AuthorClass( "abdulrasheed_soufi_assosi",   "Radio Abdulrasheed Soufi" , "السوسي عن أبي عمرو","https://qurango.net/radio/abdulrasheed_soufi_assosi"));//**
            AutherListInfo.add(new AuthorClass( "abdul_aziz_alahmad",   "Radio Abdul Aziz Al-Ahmad" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdul_aziz_alahmad"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_alkhalaf",   "Abdullah Al-Khalaf" , "Rewayat Hafs A'n Assem","ttps://qurango.net/radio/abdullah_alkhalaf"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_almattrod",   "Radio Abdullah Al-Mattrod" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdullah_almattrod"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_almousa",   "Abdullah Al-Mousa - Radio" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdullah_almousa"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_basfer",   "Radio Abdullah Basfer" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdullah_basfer"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_khayyat",   "Radio Abdullah Khayyat" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdullah_khayyat"));//**
            AutherListInfo.add(new AuthorClass( "abdullah_aljohany",   "Radio Abdullah Al-Johany" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdullah_aljohany"));//**
            AutherListInfo.add(new AuthorClass( "abdulmohsin_alharthy",   "Radio Abdulmohsin Al-Harthy" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulmohsin_alharthy"));//**
            AutherListInfo.add(new AuthorClass( "abdulmohsin_alobaikan",   "Radio Abdulmohsin Al-Obaikan" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulmohsin_alobaikan"));//**
            AutherListInfo.add(new AuthorClass( "abdulmohsen_alqasim",   "Radio Abdulmohsen Al-Qasim" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulmohsen_alqasim"));//**
            AutherListInfo.add(new AuthorClass( "abdulhadi_kanakeri",   "Radio Abdulhadi Kanakeri" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulhadi_kanakeri"));//**
            AutherListInfo.add(new AuthorClass( "abdulwadood_haneef",   "Radio Abdulwadood Haneef" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/abdulwadood_haneef"));//**
            AutherListInfo.add(new AuthorClass( "ali_alhuthaifi_qalon",   "Radio Ali Alhuthaifi" , "Rewayat Qalon A'n Nafi'","https://qurango.net/radio/ali_alhuthaifi_qalon"));//**
            AutherListInfo.add(new AuthorClass( "ali_alhuthaifi",   "Radio Ali Alhuthaifi" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ali_alhuthaifi"));//**
            AutherListInfo.add(new AuthorClass( "ayyoub2",   "إذاعة محمد أيوب - قراءة متميزة-" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/ayyoub2"));//**
            AutherListInfo.add(new AuthorClass( "mohammed_osman_khan",   "Radio Mohammed Osman Khan" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/mohammed_osman_khan"));//**
            AutherListInfo.add(new AuthorClass( "nasser_almajed",   "Nasser Almajed" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/nasser_almajed"));//**
            AutherListInfo.add(new AuthorClass( "hitham_aljadani",   "Haitham Aljudaany" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/hitham_aljadani"));//**
            AutherListInfo.add(new AuthorClass( "bandar_balilah",   "Bandar Balilah" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/bandar_balilah"));//**
            AutherListInfo.add(new AuthorClass( "fi_zilal_alsiyra",   "في ظلال السيرة النبوية - 400 حلقة عن سيرة نبينا محمد صلى الله عليه وسلم" , "","https://qurango.net/radio/fi_zilal_alsiyra"));//**
            AutherListInfo.add(new AuthorClass( "majed_alzamel",   "Majed Al-Zamil" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/majed_alzamel"));//**
            AutherListInfo.add(new AuthorClass( "nasser_alosfor",   "Nasser Alosfor" , "Rewayat Hafs A'n Assem","https://qurango.net/radio/nasser_alosfor"));//**

        }

        return (AutherListInfo);

    }
    public String serverNumber (String  EnglishName ){
        String ArabicName ="11";

        if (EnglishName.endsWith("islam"))
            ArabicName ="14";

        return ArabicName;


    }
    public ArrayList<AuthorClass> GuranAya(String ReciteName, String Rewayat)
    {

        ListAya.clear();
        if (SettingSaved.LanguageSelect == 1) {
            ListAya.add(new AuthorClass("001", " الفاتحة"));

        }
        else{
        //english aya
            ListAya.add(new AuthorClass("001", "Al-Fatihah "));

        }
        ListBeginEndAya ListRange = new ListBeginEndAya();
        ListRange = managment.autherRanageDetermine(ReciteName);
        ListAyaRanage.clear();
        //IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication();
        String AYAPAth;
        if (ListRange.separatesAya != null){
            // the separate aya system
            for (int i = 0; i < ListRange.separatesAya.length; i++) {
                try{
                    // Log.d("gdfgrgfdg", String.valueOf(ListRange.separatesAya[i]));
                    String folder_main = "My Stream";
                    AuthorClass ac = new AuthorClass();
                    ac = ListAya.get(ListRange.separatesAya[i]);
                    //String SDPath = Environment.getExternalStorageDirectory().getPath() + "/";
                    File SDPath =  new File(Environment.getExternalStorageDirectory()+ "/" + folder_main,"Medo_"+"/");
                    AYAPAth =SDPath+  ReciteName+ ac.ServerName +".mp3";

                    //    String[] fmyFilemyFileiles = isoStore.GetFileNames(RealServerFolder + ListAya[i].ServerName + ".mp3");
                    File myFile = new File(AYAPAth);
                    if (myFile.exists())
                        ListAyaRanage.add(new AuthorClass(ac.ServerName, ac.RealName,  avalible(),  AYAPAth,Rewayat  ));
                    else
                    {
                        if (Rewayat!= null){
                            AYAPAth ="http://server"+  serverNumber (ReciteName) + ".mp3quran.net/" +ReciteName + "/"+ Rewayat + "/" +  ac.ServerName + ".mp3";
                            // Log.d("gdfgrgfdg", AYAPAth);

                        }else {
                            AYAPAth ="http://server"+  serverNumber (ReciteName) + ".mp3quran.net/" +ReciteName + "/"+  ac.ServerName + ".mp3";
//                            // Log.d("gdfgrgfdg", AYAPAth);

                        }

                        //  AYAPAth = "http://www.quran.alrubaye.com/quran/" + LnaguageClass.RecitesName + "/" + ac.ServerName + ".mp3";
                        ListAyaRanage.add(new AuthorClass(ac.ServerName, ac.RealName, disavalible(), AYAPAth,Rewayat));
                    }


                }catch (Exception ex){}


            }
        }else{
            for (int i = ListRange.beginR; i < ListRange.endread; i++) {
                try{

                    String folder_main = "My Stream";
                    AuthorClass ac = new AuthorClass();
                    ac = ListAya.get(i);
                    //String SDPath = Environment.getExternalStorageDirectory().getPath() + "/";
                    File SDPath =  new File(Environment.getExternalStorageDirectory()+ "/" + folder_main,"Medo_"+"/");
                    AYAPAth =SDPath+  ReciteName+ ac.ServerName +".mp3";

                    //    String[] fmyFilemyFileiles = isoStore.GetFileNames(RealServerFolder + ListAya[i].ServerName + ".mp3");
                    File myFile = new File(AYAPAth);
                    if (myFile.exists())
                        ListAyaRanage.add(new AuthorClass(ac.ServerName, ac.RealName,  avalible(),  AYAPAth,Rewayat  ));
                    else
                    {
                        if (Rewayat!= null){
                            AYAPAth ="http://server"+  serverNumber (ReciteName) + ".mp3quran.net/" +ReciteName + "/"+ Rewayat + "/" +  ac.ServerName + ".mp3";
                            // Log.d("gdfgrgfdg", AYAPAth);

                        }else {
                            AYAPAth ="http://server"+  serverNumber (ReciteName) + ".mp3quran.net/" +ReciteName + "/"+  ac.ServerName + ".mp3";
                            // Log.d("gdfgrgfdg", AYAPAth);

                        }

                        //  AYAPAth = "http://www.quran.alrubaye.com/quran/" + LnaguageClass.RecitesName + "/" + ac.ServerName + ".mp3";
                        ListAyaRanage.add(new AuthorClass(ac.ServerName, ac.RealName, disavalible(), AYAPAth,Rewayat));
                    }


                }catch (Exception ex){}


            }
        }

        return(ListAyaRanage);
    }



}

