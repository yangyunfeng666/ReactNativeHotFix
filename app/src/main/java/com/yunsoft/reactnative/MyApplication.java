package com.yunsoft.reactnative;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Author: yangyunfeng
 * Date: 公元2018-5-29 17:27
 * Description:this is MyApplication
 */

public class MyApplication extends Application implements ReactApplication {

    private String version;

    private static MyApplication getInstance;

    public void setVersion(String version) {
        this.version = version;
    }

    public static MyApplication getInstance() {
        return getInstance;
    }

    @Override
    public ReactNativeHost getReactNativeHost() {
        return new ReactNativeHost(this) {
            @Override
            public boolean getUseDeveloperSupport() {
                return BuildConfig.DEBUG;
            }

            @Override
            protected List<ReactPackage> getPackages() {
                List<ReactPackage> d = new ArrayList<>();
                d.add(new MainReactPackage());
                return d;
            }

            @Nullable
            @Override
            protected String getJSBundleFile() {
                //如果版本为空，方法本地bundle文件
                if ("".equals(version)) {
                    return super.getJSBundleFile();
                }
                //判断新版本的bundle文件时候存在
                File file = new File(FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH + version + FileConstant.SPLEX + FileConstant.JS_BUNDLE_LOCAL_FILE);
                if (file != null && file.exists()) {
                    return FileConstant.getInstance().JS_BUNDLE_LOCAL_PATH + version + FileConstant.SPLEX + FileConstant.JS_BUNDLE_LOCAL_FILE;
                } else {
                    return super.getJSBundleFile();
                }
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ReactNativeUpdate.init(this);
        getInstance = this;
    }
}