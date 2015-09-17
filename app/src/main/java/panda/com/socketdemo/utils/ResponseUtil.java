package panda.com.socketdemo.utils;

/**
 * 请求头响应处理类
 *
 * Created by Administrator on 2015/9/15:11:14.
 */
public class ResponseUtil {

    private String mResStr; // 字符串类型的响应
    private String[] mResStrArray; // 响应内容按换行符切割得到的数组

    /**
     * 响应头信息的名称
     */
    private final static String CONTENT_TYPE = "Content-Type";
    private final static String CONTENT_LENGTH = "Content-Length";
    private final static String SERVER = "Server";
    private final static String CONNECTION = "Connection";
    private final static String DATE = "Date";
    private final static String ETAG = "ETag";

    /**
     * 构造函数,初始化字符串类型的响应结果
     * @param response 响应结果
     */
    public ResponseUtil (String response) {
        mResStr = response;
        mResStrArray = splitStrBySpace(mResStr, "\n");
    }

    /**
     * 获取响应的http版本、响应码、和响应码简要描述
     *
     * @return Object数组,其中object[0]是String类型,保存http请求版本<br/>
     *                       object[1]是int类型,保存响应码<br/>
     *                       object[2]是String类型,保存响应码简要描述
     */
    public Object[] getResponseCode() {
        Object[] objects = new Object[3];
        objects[0] = "";
        objects[1] = 123;
        objects[2] = "";
        // 响应的字符串不为空
        if (mResStr != null) {
            // 响应头的第一行为响应状态和请求的http版本
            String state = mResStrArray[0];
            String str[] = splitStrBySpace(state, " ");
            if (str != null && str.length >= 3) {
                // 第一个字符串为http请求版本
                objects[0] = str[0];
                // 第二个字符串为请求响应码
                objects[1] = Integer.parseInt(str[1]);
                // 第三个字符串是请求响应码的简要描述
                for (int i = 2; i < str.length; i ++) {
                    objects[2] = objects[2] + str[i] + " ";
                }
            }
        }
        return objects;
    }

    /**
     * 获取响应的mime-Type
     *
     * @return 响应头存在mime-type(content-type),则返回mime-type,否则返回null<br/>
     *         mime-type的类型,String类型
     */
    public String getMimeType() {
        String mimeType;
        if (mResStr != null) {
            for (String str : mResStrArray){
                if (str.contains(CONTENT_TYPE)) {
                    String[] cache = splitStrBySpace(str, ": ");
                    mimeType = cache[1];
                    return mimeType;
                }
            }
        }
        return null;
    }

    /**
     * 获取响应内容的长度
     *
     * @return 响应头如果存在content-length,则返回long类型的长度 <br/>
     *         否则返回0
     */
    public long getResponseLength() {
        long length = 0;
        if (mResStr != null) {
            for (String str : mResStrArray){
                if (str.contains(CONTENT_LENGTH)) {
                    String[] cache = splitStrBySpace(str, ": ");
                    length = Long.parseLong(cache[1]);
                    return length;
                }
            }
        }
        return 0;
    }

    /**
     * 获取响应头中的server信息
     * @return 响应头如果存在Server参数，则返回server的值 <br/>
     *          否则返回null <br/>
     */
    public String getServer() {
        String server;
        if (mResStr != null) {
            for (String str : mResStrArray){
                if (str.contains(SERVER)) {
                    String[] cache = splitStrBySpace(str, ": ");
                    server = cache[1];
                    return server;
                }
            }
        }
        return null;
    }

    /**
     * 获取响应头中的日期时间
     *
     * @return 响应头如果存在Date参数，则返回Date的值 <br/>
     *          否则返回null <br/>
     */
    public String getDate() {
        String date;
        if (mResStr != null) {
            for (String str : mResStrArray){
                if (str.contains(CONTENT_LENGTH)) {
                    String[] cache = splitStrBySpace(str, ": ");
                    date = cache[1];
                    return date;
                }
            }
        }
        return null;
    }

    /**
     * 获取响应头中的ETag
     *
     * @return 响应头如果存在ETag参数，则返回ETag的值 <br/>
     *          否则返回null <br/>
     */
    public String getETag() {
        String eTag;
        if (mResStr != null) {
            for (String str : mResStrArray){
                if (str.contains(ETAG)) {
                    String[] cache = splitStrBySpace(str, ": ");
                    eTag = cache[1];
                    return eTag;
                }
            }
        }
        return null;
    }

    /**
     * 获取响应报文的body部分,由于http报文形式为 header + 空行 + body形式，
     * 故我们在获取报文主体时,只要将报文中按第一个空行将报文切割开即可
     *
     * @return 如果报文主体不为空,则返回报文体（String类型）、否则返回null
     */
    public String getResponseStrBody() {
        int i = 0;
        for (String str : mResStrArray) {
            if (str.equals("")) {
                break;
            }
            i ++;
        }
        String cache = "";
        for (int j = i; j < mResStrArray.length; j++) {
            cache += mResStrArray[j];
        }
        return cache;
    }

    /**
     * 将str字符串按split分割
     * @param str 要分割的字符串
     * @param split 分隔符
     * @return 字符数组
     */
    private String[] splitStrBySpace(String str, String split) {
        String[] result = str.split(split);        return result;
    }
}
