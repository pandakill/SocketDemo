package panda.com.socketdemo;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class MainActivity extends Activity {

    private Socket mSocket;

    private BufferedReader mReader;
    private PrintWriter mWriter;
    private BufferedInputStream bis;
    private InputStream in;

    private final static String HOST = "180.97.33.107"; // 百度的ip地址 180.97.33.107
    private final static int POST = 80; //百度的端口 80

    private final static int CONNECT = 100; // 连接成功代码
    private final static int BOND    = 200; // 响应开始代码
    private final static int HANDLED = 300; // 字节流处理完毕代码

    // 线程处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CONNECT) {
                Log.i("mHandler", "连接成功");
            }
            if (msg.what == BOND) {
                mReadThread.start();
                Log.i("mHandler", "响应开始");
            }
            if (msg.what == HANDLED) {
                Log.i("mHandler", "字节流处理完毕");
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
                mSocket = new Socket();

                SocketAddress address = new InetSocketAddress(HOST, POST);

                mSocket.connect(address, 3000);
                System.out.println("mThread:" + mSocket.getLocalAddress());

                mWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                bis = new BufferedInputStream(mSocket.getInputStream());

                in = mSocket.getInputStream();

                // 拼装请求头
                mWriter.println("GET HTTP/1.1");
                mWriter.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                mWriter.println("Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                mWriter.println("Accept-Encoding: gzip, deflate");

                mWriter.flush();

                if (mSocket.isConnected()) {
                    msg.what = CONNECT;
                    Log.i("mThread", "连接成功--isConnected");
                } else {
                    Log.i("mThread", "连接失败--isConnected");
                }
                if (mSocket.isBound()) {
                    msg.what = BOND;
                    Log.i("mThread", "isBound");
                } else {
                    Log.i("mThread", "isBond else");
                }

                // 线程发送消息至handler
                mHandler.sendMessage(msg);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    // 新建线程2,用于处理socket响应
    private Thread mReadThread = new Thread(new Runnable() {
        Message msg = new Message();
        @Override
        public void run() {
            Log.i("mReadThread", "处理字节流线程开启");
            try {
                // 设定读取的字节数
                byte buffer[] = new byte[512];

                while (in.read(buffer) != -1) {
                    Log.i("mReadThread", "buffer=" + new String(buffer));
                }

//                String line = mReader.readLine();
//                Log.i("mReadThread", "read完毕");
//                if (line != null) {
//                    Log.i("mReadThread", line);
//                } else {
//                    Log.i("mReadThread", "line is null");
//                }
                msg.what = HANDLED;
                mHandler.sendMessage(msg);

                // TODO 其他字节流读取方法
//                // 读取输入流
//                if (in == null) {
//                    Log.i("mReadThread", "in is null");
//                }
//                buffer = (byte) mReader.read();
//                Log.i("mReadThread", "buffer.length=" + new String(String.valueOf(buffer)));

//                String str = "null";
//                while(true)
//                {
//                    int size=0;
//                    byte[] tmp=new byte[5];
//                    in.read(tmp);
//                    size=tmp[3]*256+tmp[4];
//                    byte buf[]=new byte[size];
//                    in.read(buf);
//                    str=new String(buf);
//                    Log.i("mReadThread", "buffer.length=" + str);
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}