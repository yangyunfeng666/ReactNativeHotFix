package com.kye.reactnativeupdate.hotupdate;

import android.content.Context;
import android.os.Handler;
import android.os.Message;


import com.kye.reactnativeupdate.FileConstant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

/**
 * 热修复
 * Created by Song on 2017/5/12.
 */
public class HotUpdate {

    public final static int UNZIP_SUCCESS = 10001;

    public static void handleZIP(final Context context, final String newVersion, final String olderVersion, final boolean allUpdate, final Handler handler) {

        // 开启单独线程，解压，合并。
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (allUpdate) {//如果是全量更新
//                    ACache.get(context).put(FileConstant.FIRST_UPDATE,true);
                    // 解压到根目录
                    FileUtils.decompression(FileConstant.getInstance().LOCAL_FOLDER);
                    mergeAllUpdate(newVersion);
                } else {
                    //如果是第一次更新 这里以是否存在以前的版本的jsbundle文件为判断依据
                    boolean result = true;
                    File file = new File(FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH+olderVersion+FileConstant.SPLEX+FileConstant.JS_BUNDLE_LOCAL_FILE);
                    if(file!=null&&file.exists()){
                        result = false;
                    }
//                    boolean result = (Boolean) ACache.get(context).getAsObject(FileConstant.FIRST_UPDATE);
                    if (result) {//如果是第一次更新
//                        Log.e("show", "result" + result);
                        // 解压到根目录
                        FileUtils.decompression(FileConstant.getInstance().LOCAL_FOLDER);
                        //com.yunsoft.mvpdemp/bundle.pat drawable-mdpi
                        // 合并
                        mergePatAndAsset(context,newVersion);
                        //判断是否有图片
                    } else { //如果有文件 那么
//                        Log.e("show", "result" + result);
                        // 解压到future目录
                        FileUtils.decompression(FileConstant.getInstance().LOCAL_FOLDER);
                        // 合并
                        mergePatAndBundle(newVersion,olderVersion);
                    }
                }
                // 删除ZIP压缩包
                FileUtils.traversalFile(FileConstant.getInstance().LOCAL_FOLDER);
                FileUtils.deleteFile(FileConstant.getInstance().JS_PATCH_LOCAL_PATH);
                Message message = new Message();
                message.what = UNZIP_SUCCESS;
                message.obj = newVersion;
                handler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 与Asset资源目录下的bundle进行合并 然后在生成xxx_index.android.bundle文件
     */
    private static void mergePatAndAsset(Context context,String newVersion) {
        // 1.解析Asset目录下的bundle文件
        String assetsBundle = FileUtils.getJsBundleFromAssets(context);
        // 2.解解压后的bundle当前目录下.pat文件
        String patcheStr = FileUtils.getStringFromPat(FileConstant.getInstance().JS_PATCH_LOCAL_FILE);
        // 3.合并 /wan/xxxx_index.android.bundle 文件
        merge(patcheStr,assetsBundle,FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH+newVersion+FileConstant.SPLEX+FileConstant.JS_BUNDLE_LOCAL_FILE);
        File file = new File(FileConstant.getInstance().FUTURE_DRAWABLE_PATH);
        //如果有网络图片
        if(file!=null&&file.exists()) {
            //拷贝图片文件
            FileUtils.copyPatchImgs(FileConstant.getInstance().FUTURE_DRAWABLE_PATH, FileConstant.getInstance().DRAWABLE_PATH);
        }
    }

    /**
     * 与SD卡下的bundle进行合并
     */
    private static void mergePatAndBundle(String newVersion,String oldVersion) {
        // 1.解析sd卡目录下的bunlde war/xxx_index.android.bundle文件
        String assetsBundle = FileUtils.getJsBundleFromSDCard(FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH+oldVersion+FileConstant.SPLEX+FileConstant.JS_BUNDLE_LOCAL_FILE);
        // 2.解析最新下发的.pat文件字符串
        String patcheStr = FileUtils.getStringFromPat(FileConstant.getInstance().JS_PATCH_LOCAL_FILE);
        // 3.合并
        merge(patcheStr,assetsBundle,FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH+newVersion+FileConstant.SPLEX+FileConstant.JS_BUNDLE_LOCAL_FILE);
        //4.删除原来的jsbundle文件，然后把临时合并文件拷贝过来
//        FileUtils.deleteFile(FileConstant.JS_BUNDLE_LOCAL_PATH);
        //5.拷贝合并jsbundle文件到目的文件
       // FileUtils.copyFile(FileConstant.JS_BUNDLE_FUTURE_LOCAL_PATH,FileConstant.JS_BUNDLE_LOCAL_PATH);
        //6.拷贝图片文件到
        File file = new File(FileConstant.getInstance().FUTURE_DRAWABLE_PATH);
        //如果有网络图片
        if(file!=null&&file.exists()) {
            //拷贝图片文件
            FileUtils.copyPatchImgs(FileConstant.getInstance().FUTURE_DRAWABLE_PATH, FileConstant.getInstance().DRAWABLE_PATH);
        }
    }

    /**
     * 全量更新 拷贝 jsbundle文件到某个目录 然后在拷贝/war/xxxjsbundle 和图片到/war/drawable-mdpi 目录
     */
    private static void mergeAllUpdate(String newVersion) {

        //4 删除原来的jsbundle文件，然后把临时合并文件拷贝过来
//        FileUtils.deleteFile(FileConstant.JS_BUNDLE_LOCAL_PATH);
        //5 拷贝合并jsbundle文件到目的文件
        //如果有以前的文件删除
        File oldFile = new File(FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH+newVersion+FileConstant.SPLEX+FileConstant.JS_BUNDLE_LOCAL_FILE);
        if(oldFile!=null&&!oldFile.exists()){
            oldFile.delete();
        }
        FileUtils.copyFile(FileConstant.getInstance().ALL_UPDATE_JS_LOCAL_FILE,FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH+newVersion+FileConstant.SPLEX+FileConstant.JS_BUNDLE_LOCAL_FILE);
        //6 拷贝图片文件到
        File file = new File(FileConstant.getInstance().FUTURE_DRAWABLE_PATH);
//        Log.e("show","FUTURE_DRAWABLE_PATH:"+FileConstant.FUTURE_DRAWABLE_PATH);
        //如果有网络图片
        if(file!=null&&file.exists()) {
            //拷贝图片文件
//            Log.e("show","haveImage");
            FileUtils.copyPatchImgs(FileConstant.getInstance().FUTURE_DRAWABLE_PATH, FileConstant.getInstance().DRAWABLE_PATH);
        }else{
//            Log.e("show"," not haveImage");
        }
    }

    /**
     * 合并,生成新的bundle文件
     */
    private static void merge(String patcheStr, String bundle,String savebundleFilePath) {

        // 3.初始化 dmp
        diff_match_patch dmp = new diff_match_patch();
        // 4.转换pat
        LinkedList<diff_match_patch.Patch> pathes = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(patcheStr);
        // 5.pat与bundle合并，生成新的bundle
        Object[] bundleArray = dmp.patch_apply(pathes,bundle);
        // 6.保存新的bundle文件
        try {
            Writer writer = new FileWriter(savebundleFilePath);
            String newBundle = (String) bundleArray[0];
            writer.write(newBundle);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
