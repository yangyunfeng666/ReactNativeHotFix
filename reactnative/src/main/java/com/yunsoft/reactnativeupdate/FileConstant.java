package com.yunsoft.reactnativeupdate;

import android.app.Application;
import android.os.Environment;

import java.io.File;

/**
 * Author: yangyunfeng
 * Date: 公元2018-5-30 16:27
 * Description:this is FileConstant
 */

public class FileConstant {

    private static FileConstant fileConstant;
    private static String patckageName;

    public static void init(Application application) {
        patckageName = application.getPackageName();
        fileConstant = getInstance();
    }

    public static FileConstant getInstance() {
        if (fileConstant == null) {
            synchronized (FileConstant.class) {
                if (fileConstant == null) {
                    fileConstant = new FileConstant();
                }
            }
        }
        return fileConstant;
    }

    /**
     * bundle文件名
     */
    public static final String JS_BUNDLE_LOCAL_FILE = "index.android.bundle";

    /**
     * zip的文件名
     */
    public static final String ZIP_NAME = "hotfix";

    /**
     * 临时存放资源文件的目录
     */
    public static final String FUTURE_NAME = "future";

    /**
     * 网络下载的zip包名称
     */
    public static final String NET_ZIP_FILE_NAME = "bundle";

    /**
     * 版本号与文件名称的分隔符  xxxx_index.android.bundle
     */
    public static final String SPLEX = "_";

    /**
     * 第一次解压zip后的文件目录
     */
    public final String JS_PATCH_LOCAL_FOLDER = Environment.getExternalStorageDirectory().toString()
            + File.separator + patckageName;

    /**
     * zip文件
     */
    public final String JS_PATCH_LOCAL_PATH = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + ".zip";


//    /**
//     * 合并后的bundle文件保存路径
//     */
//    public static final String JS_BUNDLE_LOCAL_PATH = JS_PATCH_LOCAL_FOLDER + File.separator +ZIP_NAME+ File.separator  + JS_BUNDLE_LOCAL_FILE;

    /**
     * 合并后的bundle文件保存路径
     */
    public final String JS_BUNDLE_LOCAL_PATH = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator;

    /**
     * 第二次更新后的合并后的bundle保存路径
     */
    public final String JS_BUNDLE_FUTURE_LOCAL_PATH = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + FUTURE_NAME + File.separator + JS_BUNDLE_LOCAL_FILE;

    /**
     * 合并后的bundle文件保存路径
     */
//    public static final String JS_BUNDLE_LOCAL_PATH = JS_PATCH_LOCAL_FOLDER +"/"+ZIP_NAME+"/";

    /**
     * 除第一次外，未来解压zip后的文件目录
     */
    public final String FUTURE_JS_PATCH_LOCAL_FOLDER = JS_PATCH_LOCAL_FOLDER + File.separator + "future";

    public final String FUTURE_PAT_PATH = FUTURE_JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + "bundle.pat";
    //jsbundle 加载的图片文件
    public final String DRAWABLE_PATH = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + "drawable-mdpi";

    //解压后网络图片的地址
    public final String FUTURE_DRAWABLE_PATH = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + FUTURE_NAME + File.separator + NET_ZIP_FILE_NAME + File.separator + "drawable-mdpi";
    /**
     * 解压后.pat文件目录
     */
    public final String JS_PATCH_LOCAL_FILE = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + FUTURE_NAME + File.separator + NET_ZIP_FILE_NAME + File.separator + "bundle.pat";

    /**
     * 解压后.pat文件目录
     */
    public final String ALL_UPDATE_JS_LOCAL_FILE = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + FUTURE_NAME + File.separator + NET_ZIP_FILE_NAME + File.separator + JS_BUNDLE_LOCAL_FILE;


    //bundle.zip 文件到解压的目录  /wan/future 文件夹  解压后文件有/wan/future/bundle/ drawable-xdpi index.bundle
    public final String LOCAL_FOLDER = JS_PATCH_LOCAL_FOLDER + File.separator + ZIP_NAME + File.separator + FUTURE_NAME;

    /**
     * 下载URL
     */
    public final String JS_BUNDLE_REMOTE_URL = "http://oleeed73x.bkt.clouddn.com/" + ZIP_NAME + ".zip";

}
