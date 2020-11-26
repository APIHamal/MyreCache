package com.lizhengpeng.myrecache.util;

import java.util.regex.Pattern;

/**
 * 字符串操作工具类
 * @author lizhengpeng
 */
public class StringUtil {

    /**
     * 判断字符串是否未空
     * @param text
     * @return
     */
    public static boolean isEmpty(String text){
        return text == null || text.trim().length() == 0;
    }

    /**
     * 判断字符串是否是一个数字
     * @param text
     * @return
     */
    public static boolean isNumber(String text){
        if(isEmpty(text)){
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        return pattern.matcher(text).matches();
    }

}
