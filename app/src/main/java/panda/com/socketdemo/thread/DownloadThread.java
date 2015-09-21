package panda.com.socketdemo.thread;

import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;

import panda.com.socketdemo.model.Uri;

/**
 * 下载的线程
 * 文件被分成多个子线程下载,一个文件的现在将多次创建该线程
 *
 * Created by Administrator on 2015/9/21:9:42.
 */
public class DownloadThread extends Thread {

    private int mBlock;         // 当前线程负责文件下载的长度
    private File mSaveFile;     // 文件保存名
    private int mThreadID;      // 当前线程的id
    private Uri mDownUri;       // 文件下载路径
    private int mDownLength;    // 已经下载的大小(字节长度)
    private DownloadRequest mDownRequest;
    public boolean mFinish = false;    // 当前线程是否下载完毕

    public DownloadThread(DownloadRequest downRequest,int block, File saveFile, int threadId, Uri downUrl,
                          int downLength) {
        super();
        this.mBlock = block;
        this.mSaveFile = saveFile;
        this.mThreadID = threadId;
        this.mDownUri = downUrl;
        this.mDownLength = downLength;
        this.mDownRequest = downRequest;
    }

    @Override
    public void run() {
        Log.i("DownloadThread", "第--" + mThreadID + "--个下载子线程开启");
        if(mDownLength < mBlock) {
            try {
                Socket socket = new Socket(mDownUri.getHost(), mDownUri.getPort());
                socket.setSoTimeout(5000);

                // 线程开始下载的位置
                int startPos = mBlock * (mThreadID - 1) + mDownLength;
                // 线程结束下载的位置
                int endPos = mBlock * mThreadID - 1;
                Log.i("DownloadThread", "第--" + mThreadID + "--个开始负责的下载位置为:" + startPos + "-" + endPos );

                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                InputStream inputStream = socket.getInputStream();
                // 拼装请求头
                writer.println("GET " + mDownUri.getUrl() + " HTTP/1.1\r");
                writer.println("Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r");
                writer.println("Accept-Language: zh-CN\r");
                writer.println("Referer: " + mDownUri.toString() + "\r");
                writer.println("Charset: UTF-8\r");
                writer.println("Range: bytes=" + startPos + "-" + endPos + "\r");
                writer.println("Connection: Keep-Alive\r");
                writer.println("\r");

                writer.flush();

                byte[] buffer = new byte[1024];
                int len;
                RandomAccessFile accessFile = new RandomAccessFile(mSaveFile, "rwd");
                accessFile.seek(startPos);
                while ((len = inputStream.read(buffer)) != -1) {
                    accessFile.write(buffer, 0, len);
                    mDownLength += len;
                    mDownRequest.update(this.mThreadID, len);
                    mDownRequest.append(len);
                }

                // 关闭文件流、输入输出流
                accessFile.close();
                inputStream.close();
                writer.close();
                socket.close();
                mFinish = true;
                Log.i("DownloadThread", "第--" + mThreadID + "--个子线程下载完成");
            } catch (Exception e) {
                Log.e("DownloadThread", "第--" + mThreadID + "--个子线程下载异常");
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得当前线程已经下载的大小
     * <b>方法描述：</b><br/>
     * @return
     *          返回已经下载的长度
     */
    public long getDownloadLength(){
        return mDownLength;
    }

    /**
     * 获取是否下载完毕
     *
     * @return
     *          如果下载完毕,返回 {@code true}
     *          否则返回 {@code false}
     */
    public boolean isfinish(){
        return mFinish;
    }
}
