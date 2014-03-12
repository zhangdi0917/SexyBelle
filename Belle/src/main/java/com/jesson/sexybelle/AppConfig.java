package com.jesson.sexybelle;

/**
 * Created by zhangdi on 14-3-7.
 */
public class AppConfig {

    public static final boolean DEBUG = false;

    public static final int SERIES_MODE = 1;

    public static final boolean TEST_GDT_AD = false;

    public static final String GDT_AD_APPID;

    public static final String GDT_AD_APPWALL_POSID;

    public static final String GDT_AD_BANNER_POSID;

    public static final String GDT_AD_SPLASH_POSID;

    public static final String GDT_AD_INTERSTITIAL_POSID;

    static {
        if (TEST_GDT_AD) {
            GDT_AD_APPID = "1101152570";
            GDT_AD_APPWALL_POSID = "9007479624379698465";
            GDT_AD_BANNER_POSID = "9079537218417626401";
            GDT_AD_SPLASH_POSID = "8863364436303842593";
            GDT_AD_INTERSTITIAL_POSID = "8935422030341770529";
        } else {
            GDT_AD_APPID = "1101252680";
            GDT_AD_APPWALL_POSID = "9079537207559444033";
            GDT_AD_BANNER_POSID = "9007479613521516097";
            GDT_AD_SPLASH_POSID = "8863364425445660225";
            GDT_AD_INTERSTITIAL_POSID = "8935422019483588161";
        }
    }

}
