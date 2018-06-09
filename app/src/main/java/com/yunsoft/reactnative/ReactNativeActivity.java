package com.yunsoft.reactnative;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

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
