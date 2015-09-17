package panda.com.socketdemo.thread;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import panda.com.socketdemo.utils.ResponseUtil;
import panda.com.socketdemo.utils.UrlUtil;

/**
 * 用于socket连接的Http请求线程
 *
 * Created by Administrator on 2015/9/17:14:46.
 */
public class HttpRequest extends Thread {

    private Socket mSocket;
    private OutputStream mWriter;
    private BufferedReader mReader;

    private String mUri;
    private String mAgreement;
    private String mHost;
    private String mUrl;
    private int mPort;
    private int mMethod;

    private String mMimeType;
    private int mRepCode;
    private String mRepDescript;

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
    private HttpRequest(String uri) {
        mUri = uri;
        mMethod = GET;
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
    private HttpRequest(String uri, int method) {
        mUri = uri;
        mMethod = method;
        resolveUri();
    }

    /**
     * 针对传入的uri进行处理,从uri中获取相应的服务器地址等信息
     */
    private void resolveUri() {
        UrlUtil util = new UrlUtil(mUri);
        mAgreement = util.getAgreement();
        mHost = util.getHost();
        mUrl = util.getAddress();
        mPort = util.getPort();

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
        Message msg = new Message();
        Log.i("HttpRequest", "HttpRequest线程开启");
        // 建立socket连接
        try {
            // 设置true,是将返回结果以字节流形式获取
            mSocket = new Socket(mHost, mPort);

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

//            mWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            mWriter = mSocket.getOutputStream();

            // 拼装Http请求头
            StringBuilder builder = new StringBuilder();
            builder.append(mMethod + " " + mUrl + " HTTP/1.1\r\n");
            builder.append("Host: " + mHost + "\r\n");
            builder.append("Connection: keep-alive\r\n");
            builder.append("\r\n");

            byte[] requestHeader = builder.toString().getBytes("UTF-8");

            // 将请求头发送
            mWriter.write(requestHeader);
            mWriter.flush();

            // 读取响应消息
            if ( mSocket.isInputShutdown( ) )
            {
                Log.e("HttpRequest", "socket输入流被关闭,无法读取响应消息");
                return;
            }
            mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

            // 读取socket返回的字节流
            // 修正了读取的方法,避免出现字节丢失
            String str;
            String mString = "";
            while ((str = mReader.readLine()) != null) {
                mString += str;
                mString += "\n";
                Log.i("mReadThread", str);
            }

            // 读取完毕、关闭输入输出流、以及关闭socket连接
            mWriter.close();
            mReader.close();
            mSocket.close();

            // 处理响应的字符串
            ResponseUtil util = new ResponseUtil(mString);
            Object[] objects = util.getResponseCode();
            mMimeType = util.getMimeType();
//            mCode = util.getResponseStrBody();
            Log.i("HttpRequest", "服务器响应码:" + objects[1].toString());
            Log.i("HttpRequest", "服务器响应码描述:" + objects[2].toString());
            Log.i("HttpRequest", "响应的Conent-Type:" + mMimeType);
            Log.i("HttpRequest", "");

//            msg.what = DISPLAY;
//            mHandler.sendMessage(msg);

            Log.i("synchronized", Thread.currentThread().getName());
            Log.i("mThread running thread", Thread.activeCount() + Thread.currentThread().getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
