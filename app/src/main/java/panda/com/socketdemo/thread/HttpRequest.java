package panda.com.socketdemo.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import panda.com.socketdemo.model.Uri;
import panda.com.socketdemo.utils.ResponseUtil;

/**
 * 用于socket连接的Http请求线程
 *
 * Created by Administrator on 2015/9/17:14:46.
 */
public class HttpRequest extends Thread {

    private Handler mHandler;
    private Message mMsg;

    private Socket mSocket;
    private PrintWriter mWriter;

    private Uri mUri;
    private String mAgreement;
    private String mHost;
    private String mUrl;
    private int mPort;
    private String mMethod;

    private String mMimeType;

    /**
     * http的请求连接方法
     */
    public final static int POST = 0;
    public final static int GET = 1;

    /**
     * 带有只有uri地址{@code uri}的构造函数
     * 调用该构造函数,则默认方法http连接方法为{@code GET}
     *
     * @param uri
     *          服务器地址
     */
    public HttpRequest(Uri uri, Handler handler) {
        mUri = uri;
        mHandler = handler;
        mMethod = "GET";
        resolveUri();
    }

    /**
     * 带有uri地址{@code uri}和请求方法{@code method}的构造函数
     *
     * @param uri
     *          服务器地址
     * @param method
     *          http请求方法
     */
    public HttpRequest(Uri uri, int method) {
        mUri = uri;
        mMethod = (method == GET) ? "GET" : "POST";
        resolveUri();
    }

    /**
     * 针对传入的uri进行处理,从uri中获取相应的服务器地址等信息
     */
    private void resolveUri() {
        mAgreement = mUri.getAgreement();
        mHost = mUri.getHost();
        mUrl = mUri.getUrl();
        mPort = mUri.getPort();

        if (! mAgreement.equals("http")) {
            Log.e("HttpRequest", "HttpRequest类不支持该协议:" + mAgreement);
        }
        if (mHost == null) {
            Log.e("HttpRequest", "服务器地址异常");
            Log.e("HttpRequest", "您传入的uri为:" + mUri);
            Log.e("HttpRequest", "解析得到的服务器地址为:" + mHost);
        }
    }

    @Override
    public void run() {
        mMsg = new Message();
        Log.i("HttpRequest", "HttpRequest线程开启");
        // 建立socket连接
        try {
            // 设置true,是将返回结果以字节流形式获取
            mSocket = new Socket(mUri.getHost(), mUri.getPort());

            // 处理连接异常
            if ( ! mSocket.isConnected() )
            {
                Log.e("HttpRequest", "socket未连接");
                return;
            }

            // 发送请求消息
            if ( mSocket.isOutputShutdown( ) )
            {
                Log.e("HttpRequest", "socket输出流被关闭,无法发送请求");
                return;
            }
            Log.i("HttpRequest", mSocket.getLocalAddress() + "");

            mWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));

            // 拼装Http请求头
            mWriter.println(mMethod + " " + mUrl + " HTTP/1.1\r");
            mWriter.println("Host: " + mHost + "\r");
            mWriter.println("Connection: keep-alive\r");
            mWriter.println("\r");

            mWriter.flush();

            // 读取响应消息
            if ( mSocket.isInputShutdown( ) )
            {
                Log.e("HttpRequest", "socket输入流被关闭,无法读取响应消息");
                return;
            }

            // 读取socket返回的字节流
            String str;
            InputStream in = mSocket.getInputStream();

            // 字节流缓冲区
            int max = 1024*1024;
            byte[] inByte = new byte[max];
            int count;
            StringBuilder builder1 = new StringBuilder();
            ArrayList<byte[]> array = new ArrayList<>();
            while ((count = in.read(inByte)) != -1) {
                array.add(inByte);
                builder1.append(new String(inByte, 0, count));
            }
            str = builder1.toString();

            in.close();

            // 读取完毕、关闭输入输出流、以及关闭socket连接
            mWriter.close();
            mSocket.close();

            // 处理响应的字符串
            ResponseUtil util = new ResponseUtil(str);
            Object[] objects = util.getResponseCode();
            // 如果响应码为200,则打印html至webview控件中
            mMimeType = util.getMimeType();
            mMsg.obj = util.getResponseHtml();
            if (((int)objects[1] == 200) && mMimeType.contains("text/html")) {
                mMsg.what = 222;
                mHandler.sendMessage(mMsg);
            } else {
                Log.i("==========================================", str);
            }

            Log.i("HttpRequest", "服务器响应码:" + objects[1].toString());
            Log.i("HttpRequest", "服务器响应码描述:" + objects[2].toString());
            Log.i("HttpRequest", "响应的Conent-Type:" + mMimeType);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
