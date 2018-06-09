package com.yunsoft.reactnativeupdate;

import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by jingchen on 2017/12/4.
 */


public class ReactBridgeModule extends ReactContextBaseJavaModule {
    public ReactBridgeModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }
    // ReactContextBaseJavaModule要求派生类实现getName方法。这个函数用于返回一个字符串
    // 这个字符串用于在JavaScript端标记这个原生模块
    @Override
    public String getName() {
        return "ReactBridgeModule";
    }
    // 获取应用包名
    // 要导出一个方法给JavaScript使用，Java方法需要使用注解@ReactMethod


    @ReactMethod
    public void getPackageName() {
        String name = getReactApplicationContext().getPackageName();
        Toast.makeText(getReactApplicationContext(),name,Toast.LENGTH_LONG).show();
    }


    @ReactMethod
    public void setWindowStatusBarColor() {
        String name = getReactApplicationContext().getPackageName();
        Toast.makeText(getReactApplicationContext(),name,Toast.LENGTH_LONG).show();
    }

    @ReactMethod
    public void setWindowStatusBarTextColor(int i) {
        String name = getReactApplicationContext().getPackageName();
        Toast.makeText(getReactApplicationContext(),name,Toast.LENGTH_LONG).show();
    }



}
