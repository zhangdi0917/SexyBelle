package com.jesson.android.utils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EmojiFilter {

    /**
     * 通过对文本当中的UTF-8字符进行手动的解析达到识别并转换UTF-8中没有 的表情符号的目的。<BR/>
     * <BR/>
     * UTF-8的编码有如下特点：<BR/>
     * 如果一个字符是1字节（即ascii码），则该字符的格式为: 0xxxxxxx<BR/>
     * 如果一个字符是2字节，则该字符的编码格式为：110xxxxx 10xxxxxx<BR/>
     * 如果一个字符是3字节，则该字符的编码格式为：1110xxxx 10xxxxxx 10xxxxxx<BR/>
     * <BR/>
     * 目前sql无法解析的UTF-8表情编码为四个字节，即格式为：<BR/>
     * 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx<BR/>
     * <BR/>
     * 所以本方法过滤emoji的方式就是逐个读取utf-8字符，判断该字符的字节数<BR/>
     * 若小于4字节则直接转换成string保留<BR/>
     * 若为4字节则将其编码成为:<BR/>
     * <BR/>
     * \:xxxxxxxx (如\:f09f8f86)<BR/>
     * <BR/>
     * 的格式，其中x代表1位16进制字符<BR/>
     * 长度大于4字节的暂未处理，有无必要待确定。<BR/>
     * <BR/>
     *
     * @param text 需要过滤的字符串
     * @return 过滤后的字符串
     */
    public static String emojiFilter(String text) {

        if (text == null) return new String();
        byte[] b = text.getBytes(Charset.forName("UTF-8"));
        StringBuffer hs = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {

            if ((b[n] & 0XFF) >= 0XFC && (b[n] & 0XFF) < 0XFE && n < b.length - 5) {
                stmp = new String(b, n, 6, Charset.forName("UTF-8"));
                n = n + 5;
            } else if ((b[n] & 0XFF) >= 0XF8 && (b[n] & 0XFF) < 0XFC && n < b.length - 4) {
                stmp = new String(b, n, 5, Charset.forName("UTF-8"));
                n = n + 4;
            } else if ((b[n] & 0XFF) >= 0XF0 && (b[n] & 0XFF) < 0XF8 && n < b.length - 3) {
                stmp = convertToString(b[n], b[n + 1], b[n + 2], b[n + 3]);
                n = n + 3;
            } else if ((b[n] & 0XFF) >= 0XE0 && (b[n] & 0XFF) < 0XF0 && n < b.length - 2) {
                stmp = new String(b, n, 3, Charset.forName("UTF-8"));
                n = n + 2;
            } else if ((b[n] & 0XFF) >= 0XC0 && (b[n] & 0XFF) < 0XE0 && n < b.length - 1) {
                stmp = new String(b, n, 2, Charset.forName("UTF-8"));
                n++;
            } else if ((b[n] & 0XFF) < 0X80) {
                stmp = new String(b, n, 1, Charset.forName("UTF-8"));
            } else {//残缺字节时忽略
                continue;
            }

            hs.append(stmp);
        }
        return hs.toString();
    }

    private static String convertToString(byte a, byte b, byte c, byte d) {
        String result = "\\:" + (Integer.toHexString(a & 0XFF)) + (Integer.toHexString(b & 0XFF))
                + (Integer.toHexString(c & 0XFF)) + (Integer.toHexString(d & 0XFF));
        return result;
    }

    /**
     * 通过分析固定格式：<BR/>
     * \:xxxxxxxx (如\:f09f8f86)<BR/>
     * 将该内容转化为4字节的utf-8表情字符<BR/>
     * <BR/>
     * 利用正则表达式进行匹配后逐一处理并替换<BR/>
     * <BR/>
     *
     * @param str 经过emojiFilter过滤的字符串
     * @return 还原后的字符串
     */
    public static String recoverToEmoji(String str) throws PatternSyntaxException {
        if (str == null) return new String();

        String regEx = "(\\\\:)([0-9a-fA-F]{8})";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String temp = m.group(2);
            temp = new String(hexStringToBytes(temp), Charset.forName("UTF-8"));
            m.appendReplacement(sb, temp);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String emojiKiller(String text) {

        if (text == null) return new String();
        byte[] b = text.getBytes(Charset.forName("UTF-8"));
        StringBuffer hs = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {

            if ((b[n] & 0XFF) >= 0XFC && (b[n] & 0XFF) < 0XFE && n < b.length - 5) {
                stmp = new String(b, n, 6, Charset.forName("UTF-8"));
                n = n + 5;
            } else if ((b[n] & 0XFF) >= 0XF8 && (b[n] & 0XFF) < 0XFC && n < b.length - 4) {
                stmp = new String(b, n, 5, Charset.forName("UTF-8"));
                n = n + 4;
            } else if ((b[n] & 0XFF) >= 0XF0 && (b[n] & 0XFF) < 0XF8 && n < b.length - 3) {
                stmp = "";
                n = n + 3;
            } else if ((b[n] & 0XFF) >= 0XE0 && (b[n] & 0XFF) < 0XF0 && n < b.length - 2) {
                stmp = new String(b, n, 3, Charset.forName("UTF-8"));
                n = n + 2;
            } else if ((b[n] & 0XFF) >= 0XC0 && (b[n] & 0XFF) < 0XE0 && n < b.length - 1) {
                stmp = new String(b, n, 2, Charset.forName("UTF-8"));
                n++;
            } else if ((b[n] & 0XFF) < 0X80) {
                stmp = new String(b, n, 1, Charset.forName("UTF-8"));
            } else {//残缺字节时忽略
                continue;
            }

            hs.append(stmp);
        }
        return hs.toString();
    }

    private static String killStringSpace(String str) {
        String[] array = str.split(" ");
        StringBuilder sb = new StringBuilder("");
        System.out.println("array size is:" + array.length);
        if (array != null) {
            for (String s : array) {
                if (!s.trim().equals("")) {
                    sb.append(" ").append(s.trim());
                }

            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String str = "                  ";
        String str1 = "asdfasd    sdfasdfas dfasdfasd  fasdfadsfad  dfasdfa    ";
        String str2 = "    dfa  ";
        String str3 = "   fadfasdf";
        String str4 = "fasdfa   ";

        System.out.println("str is:" + killStringSpace(str));
        System.out.println("str1 is:" + killStringSpace(str1));
        System.out.println("str2 is:" + killStringSpace(str2));
        System.out.println("str3 is:" + killStringSpace(str3));
        System.out.println("str4 is:" + killStringSpace(str4));
    }

}
