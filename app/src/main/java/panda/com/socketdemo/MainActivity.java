package panda.com.socketdemo;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import panda.com.socketdemo.utils.ResponseUtil;


public class MainActivity extends Activity {

    private Socket mSocket;

    private BufferedReader mReader;
    private PrintWriter mWriter;

    private WebView mBrowser;
    private String mString = "";
    private String mCode;
    private String mMimeType;

    private final static String HOST = "www.baidu.com"; // 百度的ip地址 180.97.33.107
    private final static int POST = 443; //百度的端口 80

    private final static int CONNECT = 100; // 连接成功代码
    private final static int BOND    = 200; // 响应开始代码
    private final static int HANDLED = 300; // 字节流处理完毕代码
    private final static int DISPLAY = 222; // 源码加载完毕,可以将源码显示

    private final static String CHAR_SET = "utf-8"; // 编码

    // 线程处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CONNECT) {
                Log.i("mHandler", "连接成功");
            }
            if (msg.what == BOND) {
//                mReadThread.start();
                Log.i("mHandler", "响应开始");
            }
            if (msg.what == HANDLED) {
                Log.i("mHandler", "字节流处理完毕");
            }
            if (msg.what == DISPLAY) {
                // 只有在mime-type后面加";charset=UTF-8"才可以解决乱码问题,在第三个参数设置并不能解决乱码
                mBrowser.loadData(mCode, mMimeType + "; charset=UTF-8", CHAR_SET);
            }
        }
    };

    // 新建线程1,用于连接socket
    private Thread mThread = new Thread(new Runnable() {
        Message msg = new Message();
        @Override
        public void run() {
            Log.i("mThread", "socket连接线程开启");
            // 建立socket连接
            try {
                // 设置true,是将返回结果以字节流形式获取
                mSocket = new Socket(HOST, POST);

                // 处理连接异常
                if ( ! mSocket.isConnected() )
                {
                    Log.e("mThread/error", "socket未连接");
                    return;
                }

                // 发送请求消息
                if ( mSocket.isOutputShutdown( ) )
                {
                    Log.e("mThread/error", "socket输出流被关闭,无法发送请求");
                    return;
                }

                System.out.println("mThread:" + mSocket.getLocalAddress());
                mWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));

                // 拼装请求头
                mWriter.println("Host: www.baidu.com");
                mWriter.println("Mozilla/5.0 (Linux; U; Android 4.3; zh-CN; SM-G7108V Build/JLS36C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/10.4.0.558 U3/0.8.0 Mobile Safari/534.30");
                mWriter.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                mWriter.println("Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                mWriter.println("Accept-Encoding: gzip, deflate");
                mWriter.println("Connection: keep-alive");

                mWriter.flush();

                // 读取响应消息
                if ( mSocket.isInputShutdown( ) )
                {
                    Log.e("mThread/error", "socket输入流被关闭,无法读取响应消息");
                    return;
                }
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                // 读取socket返回的字节流
                // 修正了读取的方法,避免出现字节丢失
                String str = null;
                while ((str = mReader.readLine()) != null) {
                    mString += str;
                    mString += "\n";
                    Log.i("mReadThread", str);
                }

                // 处理响应的字符串
                ResponseUtil util = new ResponseUtil(mString);
                Object[] objects = util.getResponseCode();
                mMimeType = util.getMimeType();
                mCode = util.getResponseStrBody();
                Log.i("mThread/code", objects[1].toString());
                Log.i("mThread/descripte", objects[2].toString());
                Log.i("mThread/mime-Type", mMimeType);
                Log.i("mThread/body", mCode);
                msg.what = DISPLAY;
                mHandler.sendMessage(msg);

            } catch (UnknownHostException ee){
                ee.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化webview控件
        mBrowser = (WebView) findViewById(R.id.browser);

        mBrowser.getSettings().setJavaScriptEnabled(true);
        mBrowser.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                setTitle("Loading");
                setProgress(newProgress * 100);
                if (newProgress == 100) {
                    setTitle(R.string.app_name);
                }
            }
        });

        // 启动线程,连接socket
        mThread.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果mThread线程没有关闭,则在activity销毁时关闭线程
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 如果按下返回键,则控制浏览器为后退
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mBrowser.canGoBack()) {
            mBrowser.goBack();
            return true;
        }
        return false;
    }
}