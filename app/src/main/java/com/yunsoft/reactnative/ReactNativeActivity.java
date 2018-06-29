package com.yunsoft.reactnative;

import com.facebook.react.ReactActivity;
import com.kye.reactnativeupdate.PreLoadReactActivity;

import static com.yunsoft.reactnative.MainActivity.MODEL;

/**
 * Author: yangyunfeng
 * Date: 公元2018-5-29 16:47
 * Description:this is ReactNativeActivity
 */

public class ReactNativeActivity extends PreLoadReactActivity {


    @Override
    protected String getMainComponentName() {
        return "test";
    }

}
