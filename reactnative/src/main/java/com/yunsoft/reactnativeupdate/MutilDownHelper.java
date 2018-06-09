package com.yunsoft.reactnativeupdate;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Author: yangyunfeng
 * Date: 公元2018-6-7 12:06
 * Description:this is MutilDownHelper
 */

public class MutilDownHelper {
    private static int blockSize;
    private int currentRunThreadCount ;
    /**
     *
     * @param downUrl 下载地址
     * @param storePath 存储地址
     * @param threadCount 线程池大小
     * @param version 下载版本
     * @return
     */
    public int  load(String downUrl, String storePath, int threadCount, String version) {
        HttpURLConnection connection = null;
        currentRunThreadCount = threadCount;
        try {
            URL url = new URL(downUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10 * 1000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            if (code == 200) {
                int fileLength = connection.getContentLength();
                RandomAccessFile randomFile = new RandomAccessFile(new File(storePath), "rw");
                randomFile.setLength(fileLength);
                randomFile.close();
                blockSize = fileLength / threadCount;
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
                List<DownLoadThread> downLoadThreads = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    int startBlock = i * blockSize;
                    int endBlock = (i + 1) * blockSize - 1;
                    if (i == threadCount - 1) {//如果是最后一个线程，下载完
                        endBlock = fileLength - 1;
                    }
                    downLoadThreads.add(new DownLoadThread(i, startBlock, endBlock, downUrl, storePath, version));
                }
                try {
                    List<Future<Integer>> futures = executorService.invokeAll(downLoadThreads);
                    for (Future<Integer> future : futures) {
                        if (future.get() == 1) {//这里会等待 阻塞线程 1是成功的标识
                            currentRunThreadCount = currentRunThreadCount - 1;//还没有完成的进程
                        }
                    }
                    if (currentRunThreadCount == 0) {
                        return 1;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return 0;
    }


    public static class DownLoadThread implements Callable<Integer> {

        private int threadId;
        private int startBlock;
        private int endBlock;

        private String downUrl;
        private String storePath;
        private String version;//解决下载中断，不同版本的切换问题


        public DownLoadThread(int i, int startBlock, int endBlock, String url, String storePath, String version) {
            this.threadId = i;
            this.startBlock = startBlock;
            this.endBlock = endBlock;
            this.downUrl = url;
            this.storePath = storePath;
            this.version = version;
        }

        @Override
        public Integer call() {
            try {
                URL url = new URL(downUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(10 * 1000);
                File file = new File(storePath.substring(0, storePath.lastIndexOf("/")), version + "_" + threadId + ".txt");
                RandomAccessFile downLoadAss = null;
                if (file != null && file.exists()) {
                    downLoadAss = new RandomAccessFile(file, "rwd");
                    String lastPositon = downLoadAss.readLine();
                    if (null == lastPositon || "".equals(lastPositon)) {
                        this.startBlock = startBlock;
                    } else {
                        if (lastPositon != null && !"".equals(lastPositon)) {
                            startBlock = Integer.parseInt(lastPositon) - 1;
                        }
                    }
                } else {
                    downLoadAss = new RandomAccessFile(file, "rwd");
                }
                con.setRequestProperty("Range", "bytes=" + startBlock + "-" + endBlock);
                if (con.getResponseCode() == 206) {//请求部分成果
                    InputStream input = con.getInputStream();
                    RandomAccessFile randomAccessFile = new RandomAccessFile(new File(storePath), "rwd");
                    randomAccessFile.seek(startBlock);
                    byte[] bytes = new byte[1024 * 4];
                    int length = -1;
                    int total = 0;
                    while ((length = input.read(bytes)) != -1) {
                        randomAccessFile.write(bytes, 0, length);
                        total += length;
                        downLoadAss.seek(0);
                        downLoadAss.write(String.valueOf(startBlock + total).getBytes("UTF-8"));
                    }
                    downLoadAss.close();
                    randomAccessFile.close();
                    input.close();
//                    Log.e("show", "线程" + threadId + "下载关闭");
                    File f = new File(storePath.substring(0, storePath.lastIndexOf("/")), version + "_" + threadId + ".txt");
                    f.delete();//删除记录下载的文件
                    return 1;
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return 0;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return 0;
            }
            return 1;

        }

    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
