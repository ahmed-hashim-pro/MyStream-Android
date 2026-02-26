package com.medoapps.www.onlinequran;

/**
 * Created by ASUS S550C on 18/01/2015.
 */
public class managment {
    public static String serverNumber(String EnglishName) {
        String ArabicName = "11";
        if (EnglishName.equals("shatri"))
            ArabicName = "11";
        else if (EnglishName.equals("ahmad_huth"))
            ArabicName = "8";
        else if (EnglishName.equals("hawashi"))
            ArabicName = "11";
        else if (EnglishName.equals("trabulsi"))
            ArabicName = "10";
        else if (EnglishName.equals("ajm"))
            ArabicName = "10";
        else if (EnglishName.equals("trablsi"))
            ArabicName = "10";
        else if (EnglishName.equals("saud"))
            ArabicName = "11";
        else if (EnglishName.equals("saber"))
            ArabicName = "8";
        else if (EnglishName.equals("Aamer"))
            ArabicName = "10";
        else if (EnglishName.equals("ahmad_nu"))
            ArabicName = "11";
        else if (EnglishName.equals("akil"))
            ArabicName = "12";
        else if (EnglishName.equals("akrm"))
            ArabicName = "9";
        else if (EnglishName.equals("akdr"))
            ArabicName = "6";
        else if (EnglishName.equals("IbrahemSadan"))
            ArabicName = "10";
        else if (EnglishName.equals("abkr"))
            ArabicName = "6";
        else if (EnglishName.equals("jbreen"))
            ArabicName = "6";
        else if (EnglishName.equals("jormy"))
            ArabicName = "11";
        else if (EnglishName.equals("ibrahim_dosri_warsh"))
            ArabicName = "10";
        else if (EnglishName.equals("3siri"))
            ArabicName = "6";
        else if (EnglishName.equals("zamri"))
            ArabicName = "12";
        else if (EnglishName.equals("3zazi"))
            ArabicName = "8";
        else if (EnglishName.equals("dokali"))
            ArabicName = "7";

        else if (EnglishName.equals("alzain"))
            ArabicName = "9";
        else if (EnglishName.equals("omran"))
            ArabicName = "9";
        else if (EnglishName.equals("koshi"))
            ArabicName = "11";
        else if (EnglishName.equals("fateh"))
            ArabicName = "6";
        else if (EnglishName.equals("qari"))
            ArabicName = "11";
            ///new qura
        else if (EnglishName.equals("twfeeq")) ArabicName = "6";
        else if (EnglishName.equals("jamal")) ArabicName = "6";
        else if (EnglishName.equals("jaman")) ArabicName = "6";
        else if (EnglishName.equals("hatem")) ArabicName = "11";
        else if (EnglishName.equals("qht")) ArabicName = "10";
        else if (EnglishName.equals("mohna")) ArabicName = "11";
        else if (EnglishName.equals("kafi")) ArabicName = "11";
        else if (EnglishName.equals("tnjy")) ArabicName = "12";
        else if (EnglishName.equals("hamza")) ArabicName = "9";
        else if (EnglishName.equals("else ifrad")) ArabicName = "12";
        else if (EnglishName.equals("zaki")) ArabicName = "9";
        else if (EnglishName.equals("sami_dosr")) ArabicName = "8";
        else if (EnglishName.equals("s_gmd")) ArabicName = "7";
        else if (EnglishName.equals("shur")) ArabicName = "7";
        else if (EnglishName.equals("shl")) ArabicName = "6";
        else if (EnglishName.equals("sayed")) ArabicName = "12";
        else if (EnglishName.equals("taher")) ArabicName = "12";
        else if (EnglishName.equals("hkm")) ArabicName = "12";
        else if (EnglishName.equals("sahood")) ArabicName = "8";
        else if (EnglishName.equals("s_bud")) ArabicName = "6";
        else if (EnglishName.equals("salah_hashim_m")) ArabicName = "12";
        else if (EnglishName.equals("bu_khtr")) ArabicName = "8";
        else if (EnglishName.equals("tareq")) ArabicName = "10";
        else if (EnglishName.equals("a_klb")) ArabicName = "8";
        else if (EnglishName.equals("ryan")) ArabicName = "8";
        else if (EnglishName.equals("thubti")) ArabicName = "6";
        else if (EnglishName.equals("bari")) ArabicName = "12";
        else if (EnglishName.equals("bari_molm")) ArabicName = "10";
        else if (EnglishName.equals("basit")) ArabicName = "7";
        else if (EnglishName.equals("basit_warsh")) ArabicName = "10";
        else if (EnglishName.equals("basit_mjwd")) ArabicName = "13";
        else if (EnglishName.equals("sds")) ArabicName = "11";
        else if (EnglishName.equals("soufi_klf")) ArabicName = "9";
        else if (EnglishName.equals("soufi")) ArabicName = "9";
        else if (EnglishName.equals("a_ahmed")) ArabicName = "11";
        else if (EnglishName.equals("brmi")) ArabicName = "8";
        else if (EnglishName.equals("Abdullahk")) ArabicName = "10";
        else if (EnglishName.equals("mtrod")) ArabicName = "8";
        else if (EnglishName.equals("bsfr")) ArabicName = "6";
        else if (EnglishName.equals("kyat")) ArabicName = "12";
        else if (EnglishName.equals("jhn")) ArabicName = "13";
        else if (EnglishName.equals("mohsin_harthi")) ArabicName = "6";
        else if (EnglishName.equals("obk")) ArabicName = "12";
        else if (EnglishName.equals("qasm")) ArabicName = "8";
        else if (EnglishName.equals("kanakeri")) ArabicName = "6";
        else if (EnglishName.equals("wdod")) ArabicName = "8";
        else if (EnglishName.equals("abo_hashim")) ArabicName = "9";
        else if (EnglishName.equals("huthelse ifi_qalon")) ArabicName = "9";
        else if (EnglishName.equals("hthfi")) ArabicName = "9";
        else if (EnglishName.equals("a_jbr")) ArabicName = "11";
        else if (EnglishName.equals("hajjaj")) ArabicName = "9";
        else if (EnglishName.equals("hafz")) ArabicName = "6";
        else if (EnglishName.equals("frs_a")) ArabicName = "8";
        else if (EnglishName.equals("lafi")) ArabicName = "6";
        else if (EnglishName.equals("zaml")) ArabicName = "9";
        else if (EnglishName.equals("shaibat")) ArabicName = "9";
        else if (EnglishName.equals("maher_m")) ArabicName = "12";
        else if (EnglishName.equals("maher")) ArabicName = "12";
        else if (EnglishName.equals("shaksh")) ArabicName = "10";
        else if (EnglishName.equals("ayyub")) ArabicName = "8";
        else if (EnglishName.equals("braak")) ArabicName = "13";
        else if (EnglishName.equals("tblawi")) ArabicName = "12";
        else if (EnglishName.equals("mhsny")) ArabicName = "11";
        else if (EnglishName.equals("monshed")) ArabicName = "10";
        else if (EnglishName.equals("jbrl")) ArabicName = "8";
        else if (EnglishName.equals("rashad")) ArabicName = "10";
        else if (EnglishName.equals("shah")) ArabicName = "12";
        else if (EnglishName.equals("minsh")) ArabicName = "10";
        else if (EnglishName.equals("minsh_mjwd")) ArabicName = "11";
        else if (EnglishName.equals("abdullah_dori")) ArabicName = "12";
        else if (EnglishName.equals("khan")) ArabicName = "6";
        else if (EnglishName.equals("mrelse ifai")) ArabicName = "11";
        else if (EnglishName.equals("sheimy")) ArabicName = "10";
        else if (EnglishName.equals("husr")) ArabicName = "13";
        else if (EnglishName.equals("bna_mjwd")) ArabicName = "8";
        else if (EnglishName.equals("afs")) ArabicName = "8";
        else if (EnglishName.equals("mustafa")) ArabicName = "8";
        else if (EnglishName.equals("lahoni")) ArabicName = "6";
        else if (EnglishName.equals("ra3ad")) ArabicName = "8";
        else if (EnglishName.equals("harthi")) ArabicName = "8";
        else if (EnglishName.equals("muftah_thakwan")) ArabicName = "11";
        else if (EnglishName.equals("bilal")) ArabicName = "11";
        else if (EnglishName.equals("qtm")) ArabicName = "6";
        else if (EnglishName.equals("nabil")) ArabicName = "9";
        else if (EnglishName.equals("namh")) ArabicName = "8";
        else if (EnglishName.equals("hani")) ArabicName = "8";
        else if (EnglishName.equals("waleed")) ArabicName = "9";
        else if (EnglishName.equals("yasser")) ArabicName = "11";
        else if (EnglishName.equals("qurashi")) ArabicName = "9";
        else if (EnglishName.equals("mzroyee")) ArabicName = "9";
        else if (EnglishName.equals("yahya")) ArabicName = "12";
        else if (EnglishName.equals("yousef")) ArabicName = "9";
        else if (EnglishName.equals("noah")) ArabicName = "8";
        return ArabicName;


    }

    public static ListBeginEndAya autherRanageDetermine(String EnglishName) {
        ListBeginEndAya authLengther = new ListBeginEndAya();

        authLengther.beginR = 0;
        authLengther.endread = 114;
        if (EnglishName.length() == 0) {
            authLengther.beginR = 0;
            authLengther.endread = 114;
        } else if (EnglishName.equals("saud")) {
            authLengther.beginR = 84;
            authLengther.endread = 114;
        } else if (EnglishName.equals("akil")) {
            authLengther.beginR = 49;
            authLengther.endread = 56;
        } else if (EnglishName.equals("jbreen")) {
            authLengther.beginR = 18;
            authLengther.endread = 114;
        } else if (EnglishName.equals("jormy")) {
            authLengther.beginR = 39;
            authLengther.endread = 41;
        } else if (EnglishName.equals("3zazi")) {
            authLengther.beginR = 57;
            authLengther.endread = 114;
        } else if (EnglishName.equals("hamza")) {
            authLengther.beginR = 1;
            authLengther.endread = 114;

        } else if (EnglishName.equals("sami_dosr")) {
            authLengther.beginR = 28;
            authLengther.endread = 114;

        } else if (EnglishName.equals("brmi")) {
            authLengther.beginR = 48;
            authLengther.endread = 114;

        } else if (EnglishName.equals("abo_hashim")) {
            authLengther.beginR = 49;
            authLengther.endread = 58;

        } else if (EnglishName.equals("lafi")) {
            authLengther.beginR = 29;
            authLengther.endread = 114;

        } else if (EnglishName.equals("zaml")) {
            authLengther.beginR = 12;
            authLengther.endread = 114;

        } else if (EnglishName.equals("shaibat")) {
            authLengther.beginR = 77;
            authLengther.endread = 114;

        } else if (EnglishName.equals("braak")) {
            authLengther.beginR = 49;
            authLengther.endread = 114;

        } else if (EnglishName.equals("maher_m")) {
            authLengther.beginR = 77;
            authLengther.endread = 114;

        } else if (EnglishName.equals("islam")) {

            authLengther.separatesAya = new int[]{12, 16, 17, 25, 30, 31, 40, 41, 43, 49, 52, 53, 54, 55, 58, 63, 65, 66, 67, 69, 71, 75, 78, 84, 87};
        } else if (EnglishName.equals("nufais")) {

            authLengther.separatesAya = new int[]{0, 1, 2, 7, 11, 12, 13, 17, 20, 25, 28, 31, 34, 36, 37, 38, 93, 40, 43, 45, 47, 49, 50, 51, 52, 55, 56, 59, 61, 66, 69, 70, 71, 72, 73, 74, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113};
        } else if (EnglishName.equals("malaysia/akil")) {

            authLengther.separatesAya = new int[]{49, 50, 51, 55};
        } else if (EnglishName.equals("IbrahemSadan")) {
            authLengther.separatesAya = new int[]{0, 5, 18, 19};

        } else if (EnglishName.equals("3siri")) {
            authLengther.separatesAya = new int[]{0, 2, 3, 4, 7, 11, 12, 13, 16, 17, 18, 19, 20, 22, 26, 33, 35, 37, 50, 52, 53, 55, 66, 68, 69, 74, 75};

        } else if (EnglishName.equals("malaysia/zamri")) {
            authLengther.separatesAya = new int[]{31, 43, 54, 55, 60, 66, 75};

        } else if (EnglishName.equals("zilaie")) {
            authLengther.separatesAya = new int[]{0, 18, 61, 53, 66, 96, 72, 90};

        } else if (EnglishName.equals("alshaik")) {
            authLengther.separatesAya = new int[]{12, 13, 21, 31, 37, 43, 44, 48, 49, 77, 78, 79, 80, 81, 84};

        } else if (EnglishName.equals("hamad")) {
            authLengther.separatesAya = new int[]{0, 11, 12, 42, 43};

        } else if (EnglishName.equals("hamad")) {
            authLengther.separatesAya = new int[]{0, 5, 8, 13, 20, 24, 29, 41, 49, 51, 52, 53, 57, 58, 59, 60, 64, 66, 67, 68, 69, 70, 76, 84, 85, 87, 90, 92};

        } else if (EnglishName.equals("whabi")) {
            authLengther.separatesAya = new int[]{11, 12, 13, 15, 17, 23, 24, 28, 29, 30, 31};

        } else if (EnglishName.equals("malaysia/rziah")) {
            authLengther.separatesAya = new int[]{2, 7, 32, 34};

        } else if (EnglishName.equals("malaysia/rogiah")) {
            authLengther.separatesAya = new int[]{35};

        } else if (EnglishName.equals("shakoor")) {
            authLengther.separatesAya = new int[]{0, 2, 9, 12, 13, 22, 25, 28, 34, 38, 39, 41, 42, 46, 47, 48, 49, 50, 56, 57, 58, 59, 62, 67, 68, 69, 70, 71, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113};

        } else if (EnglishName.equals("shakoor")) {
            authLengther.separatesAya = new int[]{31, 35, 43, 55, 66, 75, 84};

        } else if (EnglishName.equals("shakoor")) {
            authLengther.separatesAya = new int[]{31, 35, 43, 55, 66, 75, 84};
        } else if (EnglishName.equals("malaysia/mamat")) {
            authLengther.separatesAya = new int[]{2,13,20,21};
        } else if (EnglishName.equals("sami_hsn")) {
            authLengther.separatesAya = new int[]{0,18,19,23,25,26,31,33,54,80,81,85,86,87,90,91,92,93,94,966,101,103,104,108,109,110,111};
        }



        return authLengther;


    }
}
