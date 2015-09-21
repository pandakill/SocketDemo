package panda.com.socketdemo.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 请求头响应处理类
 *
 * Created by Administrator on 2015/9/15:11:14.
 */
public class ResponseUtil {

    /**
     * 字符类型时的变量
     */
    private String      mResStr;            // 字符串类型的响应
    private List<String> mResStrArray;       // 响应内容按换行符切割得到的数组

    /**
     * 字节类型时的变量
     */
    private byte[]      mResByte;           // 字节类型的响应
    private List<String>    mResHeader;         // 响应头
    private byte[]      mResBodyByte;       // 字节类型的响应体

    private String      mResBodyStr;        // 字符类型的响应体

    /**
     * 响应头的响应信息
     */
    private String mContentType;    // 响应头里Content-type的内容
    private int mContentLength;    // 响应头里Content-length的长度
    private Object[] mResLine;      // 响应行

    /**
     * 响应头信息的名称
     */
    private final static String CONTENT_TYPE = "Content-Type";
    private final static String CONTENT_LENGTH = "Content-Length";
    private final static String LOCATION = "Location";
    private final static int END = 0; //响应体的结束符,如果Content-length不存在的话

    /**
     * 构造函数,初始化字符串类型的响应结果
     * @param response
     *          字符串类型的响应结果
     */
    public ResponseUtil (String response) {
        mResStr = response;
        mResStrArray = splitStrBySpace(mResStr, "\n");
        getResHeader();
    }

    /**
     * 构造函数,初始化字节类型的相应结果
     *
     * @param response
     *          字节类型的相应结果
     */
    public ResponseUtil (byte[] response) {
        mResByte = response;
        getResHeader();
    }

    /**
     * 获取请求头信息
     * 1、如果传入的响应消息是 {@code mResStr},则响应头可以通过 {@code mRestrArray} 来分割得到{@code mResHeader}
     * 2、如果传入的响应消息是 {@code mResByte},则响应头要通过对字节数组进行分割,得到字节数组型的响应头{@code header},
     *    然后再将{@code header}转为字符串,再切割成字符串数组{@code mResHeader}
     *
     * @return 如果响应头不为空,则返回响应头信息,以字符串数组的形式
     *         字符串数组的第一行为响应行信息
     *         其余每一行字符串表示每一个响应头信息
     */
    public List<String> getResHeader() {
        mResHeader = new ArrayList<>();
        if (mResStr != null) {
            for (String str : mResStrArray) {
                if (str.equals("\r\n") || str.equals("\r") || str.equals("\n")){
                    break;
                }
                mResHeader.add(str);
            }
        } else if (mResByte != null) {
            // 用于存储响应头的字节数组
            byte[] header = new byte[1024];
            for (int i = 0; i < mResByte.length; i ++) {
                if (mResByte[i] == '\r' && mResByte[i+1] == '\n'
                        && mResByte[i+2] == '\r' && mResByte[i+3] == '\n') {
                    break;
                }
                header[i] = mResByte[i];
            }
            String cache = new String(header);
            mResHeader = splitStrBySpace(cache, "\n");
        }
        return mResHeader;
    }

    /**
     * 获取字节类型的响应体
     * 响应体的长度由响应头中的{@code Content-length}决定,如果响应头中没有包含{@code Content-length}
     * 则在响应体内前四位为响应体长度,并且以{@code END}为结束符号
     *
     * @return 返回字节类型的响应体
     */
    public byte[] getResBodyByte() {
        if (getResponseLength() != -1) {
            mResBodyByte = new byte[(int) getResponseLength()];
        } else {

        }
        return null;
    }

    /**
     * 获取响应的http版本、响应码、和响应码简要描述
     *
     * @return Object数组,其中 {@code object[0]} 是String类型,保存http请求版本<br/>
     *                        {@code object[1]} 是int类型,保存响应码<br/>
     *                        {@code object[2]} 是String类型,保存响应码简要描述
     */
    public Object[] getResponseCode() {
        mResLine = new Object[3];
        mResLine[0] = "";
        mResLine[1] = 123;
        mResLine[2] = "";
        // 响应头不为空
        if (mResHeader != null) {
            // 响应头的第一行为响应状态和请求的http版本
            String state = mResHeader.get(0);
            List<String> str = splitStrBySpace(state, " ");
            if (str != null && str.size() >= 3) {
                // 第一个字符串为http请求版本
                mResLine[0] = str.get(0);
                // 第二个字符串为请求响应码
                mResLine[1] = Integer.parseInt(str.get(1));
                // 第三个字符串是请求响应码的简要描述
                for (int i = 2; i < str.size(); i ++) {
                    mResLine[2] = mResLine[2] + str.get(i) + " ";
                }
            }
        }
        return mResLine;
    }

    /**
     * 获取响应的mime-Type
     *
     * @return 响应头存在mime-type(content-type),则返回mime-type,否则返回{@code null}<br/>
     *         mime-type的类型,String类型
     */
    public String getMimeType() {
        if (mResHeader != null) {
            for (String str : mResHeader){
                if (str.contains(CONTENT_TYPE)) {
                    List<String> cache = splitStrBySpace(str, ": ");
                    mContentType = cache.get(1);
                    return mContentType;
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
    public int getResponseLength() {
        if (mResHeader != null) {
            for (String str : mResHeader){
                if (str.contains(CONTENT_LENGTH)) {
                    List<String> cache = splitStrBySpace(str, ": ");
                    String num = cache.get(1);
                    if (num.contains("\r") ) {
                        num = splitStrBySpace(num, "\r").get(0);
                    } else if(num.contains("\n")){
                        num = splitStrBySpace(num, "\n").get(0);
                    } else if (num.contains("\r\n")) {
                        num = splitStrBySpace(num, "\r\n").get(0);
                    }
                    mContentLength = Integer.parseInt(num);
                    return mContentLength;
                }
            }
        }
        return -1;
    }

    /**
     * 获取响应报文的body部分,由于http报文形式为 header + 空行 + body形式，
     * 故我们在获取报文主体时,只要将报文中按第一个空行将报文切割开即可
     * 截止至{2015-9-18},该方法只能用于得到 {@code Content-type}为text/html的响应体
     *
     * @return 如果报文主体不为空,则返回报文体（String类型）、否则返回 {@code null}
     */
    public String getResponseHtml() {
        // 如果响应的Content-type是text/html,则返回String
        if (getMimeType().contains("text/html")){
            int i;
            int count;
            if (getResponseLength() != -1) {
                i = 0;
                count = mResStrArray.size();
            } else {
                i = 2;
                count = mResStrArray.size() - 2;
            }
            for (String str : mResStrArray) {
                if (str.equals("\r\n") || str.equals("\r") || str.equals("\n")) {
                    break;
                }
                i ++;
            }
            mResBodyStr = "";
            for (int j = i; j < count; j++) {
                mResBodyStr += mResStrArray.get(j);
            }
        }
        return mResBodyStr;
    }

    /**
     * 将str字符串按split分割
     * @param str
     *          要分割的字符串
     * @param split
     *          分隔符
     * @return 字符数组
     */
    private List<String> splitStrBySpace(String str, String split) {
        String[] result = str.split(split);
        List<String> cache = new ArrayList<>();
        Collections.addAll(cache, result);
        return cache;
    }
}
