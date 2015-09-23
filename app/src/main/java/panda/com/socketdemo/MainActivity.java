package panda.com.socketdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import panda.com.socketdemo.listener.DownloadProgressListner;
import panda.com.socketdemo.model.Uri;
import panda.com.socketdemo.thread.DownloadRequest;
import panda.com.socketdemo.thread.HttpRequest;


public class MainActivity extends Activity {

    private WebView mBrowser;
    private TextView mUrlTv;
    private Button mEnterBtn;
    private Button mRefreshBtn;
    private Button mDownBtn;
    private LinearLayout mDownArea;
    private ProgressBar mProgressBar;
    private TextView mProgressText;

    private ProgressDialog mProDialog;

    private String mCode;

    private Thread mDownload;

    /**
     * 默认打开网址
     */
    private static String HOST = "www.haosou.com";
    private static int PORT = 80;
    private static String URL = "/";

    private Uri mUri;
    private Thread mThread;

    private final static int CONNECT = 100;     // 连接成功代码
    private final static int BOND    = 200;     // 响应开始代码
    private final static int HANDLED = 300;     // 字节流处理完毕代码
    private final static int DISPLAY = 222;     // 源码加载完毕,可以将源码显示
    private final static int DOWNLOAD = 400;    // 下载代码
    private final static int DOWNLOAD_ERROR = 444; // 下载错误代码

    private final static String CHAR_SET = "utf-8"; // 编码
    private final static int ONE_MB = 1024*1024; // 1MB占的字节数
    private final static int THREAD_COUNT = 3; // 下载的子线程数

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
            if (msg.what == DOWNLOAD) {
                int size = msg.getData().getInt("size");
                int downloaded = msg.getData().getInt("downloaded");
                mProgressBar.setMax(size);
                mProgressBar.setProgress(downloaded);
                float num = (float)mProgressBar.getProgress()/(float)mProgressBar.getMax();
                int result = (int)(num*100);
                float allSize = size/ONE_MB;
                mProgressText.setText("文件总共:" + (String.format("%.2f", allSize))
                        + "Mb 已经下载:"+ String.format("%.2f", ((float)downloaded/ONE_MB)) + "Mb  下载进度:" + result+"%");
                if(mProgressBar.getProgress() == mProgressBar.getMax()){
                    Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();
                    mDownload.interrupt();
                }
            }
            if (msg.what == DOWNLOAD_ERROR) {
                int errorCode = msg.getData().getInt("errorCode");
                Toast.makeText(getApplicationContext(), "HTTP请求出错,HTTP错误响应代码为：" + errorCode, Toast.LENGTH_SHORT).show();
                mProgressText.setText("HTTP请求出错,HTTP错误响应代码为：" + errorCode);
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

        // 初始化下载进度栏
        mDownBtn = (Button) findViewById(R.id.btn_download);
        mDownArea = (LinearLayout) findViewById(R.id.ll_download);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressText = (TextView) findViewById(R.id.tv_progress);

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
        mUri = new Uri(HOST + ":" + PORT + URL);
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
                Log.i("new之后的线程数：", Thread.activeCount() + "");
            }
        });

        // 跳转按钮的监听器
        mEnterBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mUrlTv.getText().toString();
                mProDialog = ProgressDialog.show(MainActivity.this, "正在加载中...", ("正在打开:" + url));
                mBrowser.setVisibility(View.VISIBLE);
                mDownArea.setVisibility(View.GONE);
                mRefreshBtn.setEnabled(false);
                if (url.equals("")) {
                    Toast.makeText(MainActivity.this, "请输入地址", Toast.LENGTH_SHORT).show();
                } else {
                    mUri = new Uri(url);
                    Uri uri = new Uri(url);
                    if (mUri.getAgreement().equals("http") && mUri.getHost() != null) {
                        HOST = mUri.getHost();
                        PORT = mUri.getPort();
                        URL  = mUri.getUrl();
                        mThread = new HttpRequest(uri, mHandler);
                        mThread.start();
                    } else {
                        Toast.makeText(MainActivity.this, "不支持的HTTP协议或者地址输入有误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 下载按钮的点击监听器
        mDownBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = mUrlTv.getText().toString();
                mDownArea.setVisibility(View.VISIBLE);
                mBrowser.setVisibility(View.GONE);
                mProgressBar.setProgress(0);
                mProgressText.setText("");
                mRefreshBtn.setEnabled(false);
                // 判断sd卡是否支持写入操作
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File savedir = Environment.getExternalStorageDirectory();
                    download(path, savedir);
                } else {
                    Toast.makeText(getApplicationContext(), "sd卡不存在或者写保护", Toast.LENGTH_SHORT).show();
                    mProgressText.setText("sd卡不存在或者写保护");
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

    private DownloadTask task;

    /**
     * 由于用户的输入事件(点击button, 触摸屏幕....)是由主线程负责处理的，如果主线程处于工作状态，
     * 此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无响应”错误。
     * 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件，
     * 导致“应用无响应”错误的出现。耗时的工作应该在子线程里执行。
     */
    private void download(String path, File savedir) {
        task=new DownloadTask(path,savedir);
        mDownload = new Thread(task);
        mDownload.setName("download");
        mDownload.start();
    }

    /**
     * 下载的Runnable,用于启动DownloadRequest线程
     */
    private class DownloadTask implements Runnable{
        private String path;
        private File savedir;
        public DownloadTask(String path, File savedir) {
            super();
            this.path = path;
            this.savedir = savedir;
        }
        @Override
        public void run() {
            // 默认启动三个线程进行下载
            DownloadRequest fileDownLoader=new DownloadRequest(getApplicationContext(), path, savedir, THREAD_COUNT, mHandler);
            fileDownLoader.download(new DownloadProgressListner() {
                @Override
                public void onDownloadSize(int size, int current) {
                    Message msMessage = new Message();
                    msMessage.what = DOWNLOAD;
                    msMessage.getData().putInt("size", size);
                    msMessage.getData().putInt("downloaded", current);
                    mHandler.sendMessage(msMessage);
                }
            });
        }
    }
}