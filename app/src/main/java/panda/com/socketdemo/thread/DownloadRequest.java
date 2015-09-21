package panda.com.socketdemo.thread;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import panda.com.socketdemo.listener.DownloadProgressListner;
import panda.com.socketdemo.model.Uri;
import panda.com.socketdemo.service.FileService;
import panda.com.socketdemo.utils.ResponseUtil;

/**
 * 文件下载的请求socket
 *
 * Created by Administrator on 2015/9/21:9:48.
 */
public class DownloadRequest {

    private Context mContext;
    private File mSaveFile;         //本地保存文件
    private String mDownloadurl;    //下载路径
    private int mFileSize=0;        //文件大小
    private int mDownloadSize=0;    //已经下载的文件大小
    private FileService fileService; //下载记录操作的业务对象
    private DownloadThread[]  threads;
    private int block;              //每条线程下载的数据长度
    private Map<Integer, Integer> data=new ConcurrentHashMap<>();//缓存各线程下载的长度


    /**
     * 构造函数
     * <b>方法描述：</b><br/>
     * @param context
     *          调用该类的上下文context
     * @param downloadurl
     *          文件下载的路径
     * @param savedirFile
     *          本地保存的文件
     * @param threadNum
     *          需要启动的线程数
     */
    public DownloadRequest(Context context,String downloadurl,File savedirFile,int threadNum){
        try {
            Log.i("DownloadRequest","DownloadRequest下载请求启动");
            Log.i("DownloadRequest","此次请求将会开启" + threadNum + "个下载子线程");
            this.mDownloadurl=downloadurl;
//            URL url = new URL(downloadurl);
            Uri uri = new Uri(downloadurl);
            threads = new DownloadThread[threadNum];
            this.mContext = context;

            // 建立socket连接
            Socket socket = new Socket(uri.getHost(), uri.getPort());
            socket.setSoTimeout(5000);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            InputStream inputStream = socket.getInputStream();

            // 封装请求头
            writer.println("GET " + uri.getUrl() + " HTTP/1.1\r");
            writer.println("Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r");
            writer.println("Accept-Language: zh-CN\r");
            writer.println("Charset: UTF-8");
            writer.println("Connection: Keep-Alive");

            writer.flush();

            // 字节流缓冲区
            int max = 1024*1024;
            byte[] inByte = new byte[max];
            String str;
            int count;
            StringBuilder builder1 = new StringBuilder();
            ArrayList<byte[]> array = new ArrayList<>();
            while ((count = inputStream.read(inByte)) != -1) {
                array.add(inByte);
                builder1.append(new String(inByte, 0, count));
            }
            str = new String(array.get(0));

            // 关闭输入输出流和socket连接
            inputStream.close();
            writer.close();
            socket.close();

            // 处理响应的字符串,并得到响应头
            ResponseUtil util = new ResponseUtil(str);
            int responseCode = (int) util.getResponseCode()[1];

            if(responseCode == 200){
                mFileSize = util.getResponseLength();
                block = ((mFileSize % threads.length) == 0) ? (mFileSize / threads.length) : (mFileSize / threads.length + 1);//计算每条线程下载的数据长度
                String fileName = downloadurl.substring(downloadurl.lastIndexOf("/") + 1);//获得文件名
                this.mSaveFile = new File(savedirFile, fileName);
                fileService = new FileService(context);
                Map<Integer, Integer> logdata = fileService.getData(downloadurl);
                if(logdata.size() > 0){//如果存在下载记录
                    for(Map.Entry<Integer, Integer> entry: logdata.entrySet()){
                        data.put(entry.getKey(), entry.getValue());//把各条线程已经下载的数据长度放入data中。
                    }
                }
                if(data.size() == threads.length){//计算所有线程已经下载的数据总长度
                    for(int i = 0; i < threads.length; i++){
                        mDownloadSize += data.get(i+1);
                    }
                    Log.i("DownloadRequest", "已经下载的数据长度:" + mDownloadSize);
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 开始下载文件
     * <b>方法描述：</b><br/>
     * @param listner
     *          下载进度条监听器,
     * @return
     *          返回下载的长度大小
     */
    public int download(DownloadProgressListner listner){
        try{
            RandomAccessFile accessFile = new RandomAccessFile(mSaveFile, "rw");
            if(mFileSize > 0){
                accessFile.setLength(mFileSize);
            }
            accessFile.close();
            //TODO 这个url要删除
//            URL url = new URL(mDownloadurl);
            Uri uri = new Uri(mDownloadurl);
            if(data.size() != threads.length){  //如果原先下载的线程数和现在下载的线程数不一致
                this.data.clear();  //对原来的map集合清空
                for(int i = 0; i < threads.length; i++){
                    data.put(i + 1, 0); //初始化每天线程已经下载的数据长度为0
                }
                mDownloadSize = 0;
            }
            for(int i=0;i<threads.length;i++){//开启线程进行下载
                int downloadlength=this.data.get(i+1);//给当前的第i个线程赋下载长度。
                if(downloadlength < block && mDownloadSize < mFileSize){//判断线程是否已完成下载，负责继续下载
                    this.threads[i]=new DownloadThread(this, block, mSaveFile, i+1, uri, this.data.get(i+1));//线程Id从1开始
                    this.threads[i].setPriority(7);
                    this.threads[i].start();
                }else{
                    this.threads[i]=null;
                }
            }
            fileService.delete(mDownloadurl);
            fileService.save(mDownloadurl, data);
            boolean notFinish = true;//下载未完成。
            while(notFinish){
                Thread.sleep(900);
                for(int i=0;i<threads.length;i++){
                    if(this.threads[i]!=null && !this.threads[i].isfinish()){
                        notFinish=true;
                        if(threads[i].getDownloadLength()==-1){//如果下载失败，再重新下载。
                            this.threads[i] = new DownloadThread(this, block, mSaveFile, i+1, uri, this.data.get(i+1));
                            this.threads[i].setPriority(7);
                            this.threads[i].start();
                        }

                    }
                }
                if(listner != null){
                    listner.onDownloadSize(mDownloadSize);
                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return mDownloadSize;
    }
    /**
     * 更新指定线程最后下载的位置 方法前要加上synchronized
     * <b>方法描述：</b><br/>
     * @param threadId
     *          指定线程的id
     * @param downlength
     *          最后下载的长度
     */
    public synchronized  void update(int threadId,int downlength){
        this.data.put(threadId, downlength);
        fileService.update(this.mDownloadurl, threadId, downlength);
    }
    /**
     * 累计已经下载大小
     * <b>方法描述：</b><br/>
     * @param size
     *          当前下载的大小
     */
    public synchronized void  append(int size){
        this.mDownloadSize += size;
    }

}