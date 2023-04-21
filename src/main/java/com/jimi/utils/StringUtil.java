package com.jimi.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    // 加密字符
    public static String maskStr(String oriStr) {
        int length = oriStr.length();
        if (StringUtils.isBlank(oriStr)) {
            return "";
        } else if (length <= 3) {
            return oriStr.charAt(0) + getText(oriStr.substring(1));
        } else if (length >= 4 && length <= 6) {
            String middleChars = oriStr.substring((length-2)/2, (length+2)/2);
            return getText(oriStr.substring(0, (length - 2) / 2)) + middleChars + getText(oriStr.substring((length + 2) / 2));
        } else {
            String start = oriStr.substring(0, 2);
            String end = oriStr.substring(length - 2);
//            char middle = oriStr.charAt(length/2);
//            return start + getText(oriStr.substring(2, length/2)) + middle + getText(oriStr.substring((length+2)/2, length-2)) + end;
            return start + "*" + oriStr.charAt(3) + getText(oriStr.substring(4, length - 2)) + end;
        }
    }

    public static String getText(String str) {
        String text = "";
        for (int i = 0; i < str.length(); i++) {
            text += "*";
        }
        return text;
    }

    public static void main(String[] args) {
        System.out.println(maskStr("123")); // 1**
        System.out.println(maskStr("1234")); // *23*
        System.out.println(maskStr("12345")); // *23**
        System.out.println(maskStr("123456")); // **34**
        System.out.println(maskStr("1234567")); // 12*4*67
        System.out.println(maskStr("12345678")); // 12*4**78
        System.out.println(maskStr("123456789")); // 12*4***89
        System.out.println(maskStr("123456789a")); // 12*4****9a

    }
}
