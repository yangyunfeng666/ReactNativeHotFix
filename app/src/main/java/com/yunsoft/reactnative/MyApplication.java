package com.yunsoft.reactnative;

import android.app.Application;
import android.util.Log;

import com.RNFetchBlob.RNFetchBlobPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.imagepicker.ImagePickerPackage;
import com.kye.reactnativeupdate.FileConstant;
import com.kye.reactnativeupdate.ReactNativeUpdate;
import com.oblongmana.webviewfileuploadandroid.AndroidWebViewPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Author: yangyunfeng
 * Date: 公元2018-5-29 17:27
 * Description:this is MyApplication
 */

public class MyApplication extends Application  {


    private static MyApplication getInstance;

    public static MyApplication getInstance() {
        return getInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //这里设置图片资源最终的打包文件是drawable-xhdpi（默认是这个），这个文件是你所有图片打包的目录，有且只有唯一,
        //不然你更新图片资源，会找不到文件
        ReactNativeUpdate.init(this, FileConstant.DrwableXhdpi);
        getInstance = this;
    }
}