package panda.com.socketdemo.utils;

import android.util.Log;

/**
 * mime-type与扩展名互转的类
 *
 * Created by Administrator on 2015/9/22:10:37.
 */
public class MimeTypeUtil {

    private String mimeType;
    private String extensions;

    /**
     * 获取文件扩展名
     *
     * @param mimeType
     *          文件的mime-type类型
     * @return
     *          返回文件扩展名,
     */
    public String getExtensions(String mimeType) {

        this.mimeType = mimeType;

        if(mimeType != null) {
            switch (mimeType) {
                case "application/x-msdownload":
                    extensions = ".exe";
                    break;

                case "application/x-img":
                    extensions = ".img";
                    break;

                case "image/jpeg":
                    extensions = ".jpg";
                    break;

                case "image/png":
                    extensions = ".png";
                    break;

                case " audio/mp3":
                    extensions = ".mp3";
                    break;

                case "video/mpeg4":
                    extensions = ".mp4";
                    break;

                case "application/vnd.ms-powerpoint":
                    extensions = ".ppt";
                    break;

                case "application/x-ppt":
                    extensions = ".ppt";
                    break;

                case "application/msword":
                    extensions = ".doc";
                    break;

                case "application/vnd.android.package-archive":
                    extensions = ".apk";
                    break;

                case " text/html":
                    extensions = ".html";
                    break;

                default:
                    break;
            }
        }
        Log.i("MimeTypeUtil", "mime-type=" + mimeType + "extension=" + extensions);
        return extensions;
    }
}
