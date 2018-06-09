package com.yunsoft.reactnativeupdate;

import android.app.Application;

import com.facebook.soloader.SoLoader;

/**
 * Author: yangyunfeng
 * Date: 公元2018-6-9 11:05
 * Description:this is ReactNativeUpdate
 */

public class ReactNativeUpdate {
    public static void init(Application application){
        FileConstant.init(application);
        SoLoader.init(application, /* native exopackage */ false);
    }
}
