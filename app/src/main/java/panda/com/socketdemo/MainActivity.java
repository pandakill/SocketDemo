package panda.com.socketdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import panda.com.socketdemo.model.Uri;
import panda.com.socketdemo.thread.HttpRequest;


public class MainActivity extends Activity {

    private Socket mSocket;

    private BufferedReader mReader;
    private PrintWriter mWriter;

    private WebView mBrowser;
    private TextView mUrlTv;
    private Button mEnterBtn;
    private Button mRefreshBtn;

    private ProgressDialog mProDialog;

    private String mCode;
    private String mMimeType;

    /**
     * 默认打开网址
     */
    private static String HOST = "www.haosou.com";
    private static int POST = 80;
    private static String URL = "/";

    private Uri mUri;
    private Thread mThread;

    private final static int CONNECT = 100; // 连接成功代码
    private final static int BOND    = 200; // 响应开始代码
    private final static int HANDLED = 300; // 字节流处理完毕代码
    private final static int DISPLAY = 222; // 源码加载完毕,可以将源码显示

    private final static String CHAR_SET = "utf-8"; // 编码

    // 消息处理
    @SuppressWarnings("discall")
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
                mCode = (String) msg.obj;
                mBrowser.loadData(mCode, "text/html; charset=UTF-8", CHAR_SET);
                // 唤起刷新按钮
                mRefreshBtn.setEnabled(true);
                mProDialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化webview控件
        mBrowser = (WebView) findViewById(R.id.browser);

        // 初始化刷新按钮空间
        mRefreshBtn = (Button) findViewById(R.id.btn_refresh);
        mRefreshBtn.setEnabled(false);

        // 初始化url输入框
        mUrlTv = (TextView) findViewById(R.id.tv_url);
        mEnterBtn = (Button) findViewById(R.id.btn_enter);

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

        // 新建并启动线程,连接socket
        mUri = new Uri(HOST + ":" + POST + URL);
        mThread = new HttpRequest(mUri, mHandler);
        mThread.start();
        mProDialog = ProgressDialog.show(MainActivity.this, "正在加载中...", ("正在打开:" + HOST + URL));

        // 刷新按钮的监听器
        mRefreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProDialog = ProgressDialog.show(MainActivity.this, "正在加载中...", ("正在打开:" + HOST + URL));
                mRefreshBtn.setEnabled(false);
                Log.i("mOnClick", mThread.getState() + "");
                Log.i("new之前的线程数：", Thread.activeCount()+"");
                // 线程执行完毕会死掉、所以在点击刷新时、重新新建一个线程
                mThread = new HttpRequest(mUri, mHandler);
                mThread.start();
                Log.i("new之后的线程数：", Thread.activeCount()+"");
            }
        });

        mEnterBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProDialog = ProgressDialog.show(MainActivity.this, "正在加载中...", ("正在打开:" + HOST + URL));
                String url = mUrlTv.getText().toString();
                mRefreshBtn.setEnabled(false);
                if (url.equals("")) {
                    Toast.makeText(MainActivity.this, "请输入地址", Toast.LENGTH_SHORT).show();
                } else {
                    mUri = new Uri(url);
                    Uri uri = new Uri(url);
                    if (mUri.getAgreement().equals("http") && mUri.getHost() != null) {
                        HOST = mUri.getHost();
                        POST = mUri.getPort();
                        URL  = mUri.getUrl();
                        mThread = new HttpRequest(uri, mHandler);
                        mThread.start();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果mThread线程没有关闭,则在activity销毁时关闭线程
        if (mThread != null) {
            mThread.interrupt();
        }
        // 如果mSocket还在连接,则在销毁时关闭socket连接
        if (mSocket.isConnected()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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