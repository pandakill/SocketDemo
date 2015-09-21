package panda.com.socketdemo.utils;

/**
 * url地址的工具类，用于处理url地址
 *
 * Created by Administrator on 2015/9/16:14:47.
 */
public class UrlUtil {

    private String mUrl;
    private String mUrlWithoutAgm;
    private String mAgreement;
    private String mHost;
    private int mPort;
    private String mAddress;

    /**
     * 构造函数、初始化url地址
     * @param url 传入的url地址
     */
    public UrlUtil (String url) {
        mUrl = url;
        setUrlAndAgreement();
    }

    public String getUrlWithoutAgm() {
        return mUrlWithoutAgm;
    }

    public String getAgreement() {
        return mAgreement;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public String getAddress() {
        return mAddress;
    }

    /**
     * 从一个标准的url地址,获取不包含协议的url地址和协议
     * 如果url地址中没有协议,则默认为http协议
     *
     * 示例：
     * 如果
     *      mUrl = http://www.baidu.com/index.php
     * 或者
     *      mUrl = www.baidu.com/index.php
     * 则
     *      mUrlWithoutAgm = www.baidu.com/index.php
     *      mAgreement = http
     */
    private void setUrlAndAgreement() {
        if (mUrl != null) {
            String cache[] = splitStrBySpace(mUrl, "://");
            mAgreement = "http";
            if (cache.length == 2) {
                // 获取不包含协议的url地址
                mUrlWithoutAgm = cache[1];
                // 获取协议
                mAgreement = cache[0];
            } else {
                mUrlWithoutAgm = cache[0];
            }
            // 调用方法获取服务器地址、端口号和UR地址
            setAddress();
        }
    }

    /**
     * 从url地中获取地址
     * 示例：
     * 如果
     *      mUrlWithoutAgm = www.baidu.com/index.php/1
     * 或者
     *      mUrlWithoutAgm = www.baidu.com:80/index.php/1
     * 或者
     *      mUrlWithoutAgm = www.baidu.com
     * 则
     *      mAddress = /index.php/1 或者 mAddress = /
     *      mHost = www.baidu.com
     *      mPort = 80
     */
    private void setAddress() {
        String cache[] = splitStrBySpace(mUrlWithoutAgm, "/");
        mAddress = "/";
        mPort = 80;
        if (cache != null) {
            if (cache.length > 1) {
                for (int i = 1; i < cache.length; i++) {
                    if (i == (cache.length - 1)) {
                        mAddress += cache[i];
                    } else {
                        mAddress += cache[i] + "/";
                    }
                }
            }
            String cache2[] = splitStrBySpace(cache[0], ":");
            mHost = cache2[0];
            if (cache2.length == 2) {
                mPort = Integer.parseInt(cache2[1]);
            }
        }
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