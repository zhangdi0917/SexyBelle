package com.jesson.android.utils;

import java.security.MessageDigest;

/**
 * Created by zhangdi on 14-2-12.
 */
public class MD5 {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String encode(String origin) {
        String md5 = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md5 = byteArrayToHexString(md.digest(origin.getBytes("UTF-8")));
        } catch (Exception ex) {

        }
        return md5;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder(512);
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n >>> 4 & 0xf;
        int d2 = n & 0xf;
        return hexDigits[d1] + hexDigits[d2];
    }
}
