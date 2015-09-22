package panda.com.socketdemo.utils;

/**
 * 文件名工具类
 * 通过该工具类可以设置
 *
 * Created by Administrator on 2015/9/22:10:17.
 */
public class FileNameUtil {

    private MimeTypeUtil mMimeTypeUtil;

    private String fillName;    // 文件全名，包括扩展名
    private String fileName;    // 文件名，不包括扩展名
    private String mimeType;    // 文件的mime-type
    private String extensions;  // 文件的扩展名

    /**
     * 构造函数,传入文件名和文件的类型
     *
     * @param name
     *          文件名（不包含扩展名）
     * @param mimeType
     *          文件的mime-type，一般从http报文中获取到
     */
    public FileNameUtil (String name, String mimeType) {
        this.fileName = name;
        this.mimeType = mimeType;
        mMimeTypeUtil = new MimeTypeUtil();
    }

    /**
     * 获取文件全名，包括扩展名
     * @return
     *      文件全名
     */
    public String getFillName() {
        extensions = mMimeTypeUtil.getExtensions(mimeType);
        fillName = fileName + extensions;
        return fillName;
    }
}
