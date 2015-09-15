package panda.com.socketdemo.utils;

import android.util.Log;

import java.util.ArrayList;

/**
 * 请求头响应处理类
 *
 * Created by Administrator on 2015/9/15:11:14.
 */
public class ResponseUtil {

    private String mResStr; // 字符串类型的响应
    private String[] mResStrArray; // 响应内容按换行符切割得到的数组

    /**
     * 构造函数,初始化字符串类型的响应结果
     * @param response 响应结果
     */
    public ResponseUtil (String response) {
        mResStr = response;
        mResStrArray = mResStr.split("\n");
    }

    public Object[] getResponseCode() {
        Object[] objects = new Object[3];
        objects[0] = "";
        objects[1] = 123;
        objects[2] = "";
        if (mResStr != null) {
            // 响应头的第一行为响应状态和请求的http版本
            String state = mResStrArray[0];
            String str[] = splitStrBySpace(state, " ");
            // 第一个字符串为http请求版本
            objects[0] = str[0];
            // 第二个字符串为请求响应码
            objects[1] = Integer.parseInt(str[1]);
            // 第三个字符串是请求响应码的简要描述
            for (int i = 2; i < str.length; i ++) {
                objects[2] = objects[2] + str[i] + " ";
            }
        }
        return objects;
    }

    /**
     * 将str字符串按split分割
     * @param str 要分割的字符串
     * @param split 分隔符
     * @return 字符数组
     */
    private String[] splitStrBySpace(String str, String split) {
        String[] result = str.split(split);
        return result;
    }
}
