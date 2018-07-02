package com.kye.reactnativeupdate;

import android.app.Application;

import com.facebook.soloader.SoLoader;

/**
 * Author: yangyunfeng
 * Date: 公元2018-6-9 11:05
 * Description:this is ReactNativeUpdate
 */

public class ReactNativeUpdate {
    /**
     *
     * @param application application
     * @param drwableType 所有图片存放的drwable目录 必须是所有文件存放统一的目录，不然热更新有可能找不到图片找不到
     */
    public static void init(Application application,@FileConstant.DrwableType String drwableType){
        FileConstant.init(application,drwableType);
        SoLoader.init(application, /* native exopackage */ false);
    }

    public static void init(Application application){
        FileConstant.init(application);
        SoLoader.init(application, /* native exopackage */ false);
    }
}
