package panda.com.socketdemo.thread;

import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;

import panda.com.socketdemo.model.Uri;
import panda.com.socketdemo.utils.ResponseUtil;

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
    private boolean mFinish = false;    // 当前线程是否下载完毕

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
//                socket.setSoTimeout(5000);

                // 线程开始下载的位置
                int startPos = mBlock * (mThreadID - 1) + mDownLength;
                // 线程结束下载的位置
                int endPos = mBlock * mThreadID - 1;
                Log.i("DownloadThread", "第--" + mThreadID + "--个开始负责的下载位置为:" + startPos + "-" + endPos );

                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                InputStream inputStream = socket.getInputStream();
                // 拼装请求头
                writer.println("GET " + mDownUri.getUrl() + " HTTP/1.1\r");
                writer.println("Host: " + mDownUri.getHost());
                writer.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r");
                writer.println("Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3\r");
                writer.println("Referer: " + mDownUri.toString() + "\r");
//                writer.println("Charset: UTF-8\r");
                writer.println("Range: bytes=" + startPos + "-" + endPos + "\r");
                writer.println("Connection: keep-alive\r");
                writer.println("\r");

                writer.flush();

                byte[] buffer = new byte[1024*1024];
                int len;
                RandomAccessFile accessFile = new RandomAccessFile(mSaveFile, "rwd");
                int current = 0;
                String header = null;
                boolean isHeader = false;
                int responseCode = -1;

                accessFile.seek(startPos);
                while ((len = inputStream.read(buffer)) != -1) {
                    // 截取请求头、以及响应body
                    for (int i = 0; i < buffer.length; i ++) {
                        if (buffer[i] == '\r' && buffer[i+1] == '\n'
                                && buffer[i+2] == '\r' && buffer[i+3] == '\n') {
                            current = i;
                            isHeader = true;
                            break;
                        }
                    }
                    // 获取字节流当中的响应头
                    if (isHeader) {
                        isHeader = false;
                        header = new String(buffer, 0, current);
                        Log.i("DownloadThread", "第" + mThreadID + "个线程的header:\n" + header);
                        Log.i("DownloadThread", "第" + mThreadID + "个线程的current:\n" + current);
                        ResponseUtil util = new ResponseUtil(header, ResponseUtil.TYPE_HEADER);
                        responseCode = (int) util.getResponseCode()[1];
                        if (responseCode == 206) {
                            accessFile.write(buffer, current + 4, len);
                            mDownLength += (len-(current+4));
                            mDownRequest.update(this.mThreadID, len-(current+4));
                        } else {
                            Log.e("DownloadThread", "线程" + mThreadID + "出现HTTP请求错误,HTTP响应码为:" + responseCode);
                        }
                    } else {
                        // 如果响应头已经存在,则改部分缓冲字节流为body
                        if (header != null) {
                            if (responseCode == 206) {
                                accessFile.write(buffer, 0, len);
                                mDownLength += len;
                                mDownRequest.update(this.mThreadID, len);
                            } else {
                                Log.e("DownloadThread", "线程" + mThreadID + "出现HTTP请求错误,HTTP响应码为:" + responseCode);
                            }
                        }
                    }
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
