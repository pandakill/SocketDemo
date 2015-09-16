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

    /**
     * 百度:www.baidu.com 端口:443
     * qq杀毒:m.qq.com 端口:80
     */
    private final static String HOST = "m.qq.com";
    private final static int POST = 80;

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
                // 这里的请求头一定要注意,报文格式的结束符是\r\n
                // 这里纠结了一个下午,一直把\r写成了\\r！！！罪过啊。。。
                mWriter.println("GET /wap/index.jsp HTTP/1.1\r");
                mWriter.println("Host: m.qq.com\r");
                mWriter.println("Connection: keep-alive\r");
                mWriter.println("\r");

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

                // 读取完毕、关闭输入输出流、以及关闭socket连接
                mWriter.close();
                mReader.close();
                mSocket.close();

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