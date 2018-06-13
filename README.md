# React Native 热更新配置指南
## 更新流程图
![更新流程图](https://raw.githubusercontent.com/yangyunfeng666/image/master/react_update_1.png)
app 启动是否先判断是否是app第一次启动，如果是，那么要把本地ractnative涉及的图片（这里严格的把图片放在assets目录下开发，否则移动的时候找不到图片），移动到sdcard加载jsbundle资源的目录drawable-mdpi下。然后读取网络数据，如果有网络数据，那么就下载网络更新的bundle.zip文件，下载完成后解压，如果更新文件有图片，那么合并到sdcard drawable-mdpi目录下，如果是增量更新，需要合并以前的jsbundle文件和下载的资源文件，重新生成新版本的jsbundle文件。然后在重新在本地注册下reactnatice的初始入口加载路径jsbundle路径，当然这个注册也有提高加载速度的功能。本更新是以react native 0.48.0为版本

## 工程的引入依赖
### maven 依赖引入
在build.gradle添加maven依赖
```
allprojects {
    repositories {
        google()
        jcenter()
        maven{
            url "$rootDir/node_modules/react-native/android"
        }
    }
}
```
然后在app build.gradle里面添加dependencies
```
dependencies {
implementation 'com.kye.android:reactnativehotfix:1.0.0'
implementation 'com.facebook.react:react-native:+'
}
```
### 工程导入moudle引入
下载github上面的demo的reactnative moudle
```
dependencies {
implementation project(':reactnative')
}
```
## 配置代码
1.Application 实现ReactApplication
```
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
```
在AndroidManifest.xml里面配置MyApplication 和权限
```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />



<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:name=".MyApplication"
    android:theme="@style/AppTheme">
    .....
</>
```
在你合适的时机下移动图片到sdcard/包名/wan/drawable-mdpi目录下。
代码如下
```
List<DrawableModel> models = UpdateUtil.getResourceByReflect("assets");
new Thread(new Runnable() {
    @Override
    public void run() {
        for (DrawableModel model : models) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), model.getId());
            if (bitmap != null) {
                UpdateUtil.saveBitMapToSdcard(bitmap, FileConstant.DRAWABLE_PATH, FileConstant.DRAWABLE_PATH + File.separator + model.getName() + ".png", Bitmap.CompressFormat.PNG);
            }
        }
    }
}).start();
```
这里一个app只需要在安装后移动图片一次，不需要第二次移动，也杜绝第二次移动，你需要本地做一个标识，比如在SharedPreferences里面存放一个变量来区别是不是首次。
比如代码
```
//第一次就移动assets开头的图片到sdcard里面
SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
String update = updateShare.getString("firstUpdate", "0");
if (update.equals("0")) {
    //assets 是根据你本地文件在到包成react-native
    List<DrawableModel> models = UpdateUtil.getResourceByReflect("assets");
    new Thread(new Runnable() {
        @Override
        public void run() {
            for (DrawableModel model : models) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), model.getId());
                if (bitmap != null) {
                    UpdateUtil.saveBitMapToSdcard(bitmap, FileConstant.DRAWABLE_PATH, FileConstant.DRAWABLE_PATH + File.separator + model.getName() + ".png", Bitmap.CompressFormat.PNG);
                }
            }
        }
    }).start();
    updateShare.edit().putString("firstUpdate", "1").apply();
```
2.下载更新逻辑接入
当你发现有更新逻辑时候，需要提供如下几个参数。例子中是以UpdateModel 为例子
```
private String now_version;//现在更新的版本
private String old_version;//旧版本
private boolean allUpdate;//是否全量更新
private boolean backToOld;//是否回退
private String downurl;//更新地址
```
这里更新逻辑如下图
![](https://raw.githubusercontent.com/yangyunfeng666/image/master/reactnative_1.png)
这里我们代码如下
```
public void update(UpdateModel updateModel) {
    if (updateModel.isBackToOld()) {
        SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
        updateShare.edit().putString("reactive_version", updateModel.getNow_version()).apply();
        ((MyApplication) getApplication().getApplicationContext()).setVersion(updateModel.getNow_version()); //设置版本
        ReactNativePreLoader.preLoad(MainActivity.this, "test");//重新加载数据
    } else {
        Toast.makeText(this, "真正更新，请稍后...", Toast.LENGTH_SHORT).show();
        requstPermission(updateModel.getDownurl(), updateModel.getNow_version()); //下载更新好了，更新版本
    }
}


public void requstPermission(String RemoteUrl, String version) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
    } else {
        downLoadBundle(RemoteUrl, version);
    }
}



private void downLoadBundle(final String RemoteUrl, final String now_version) {
    new Thread(new Runnable() {
        @Override
        public void run() {
            MutilDownHelper downHelper = new MutilDownHelper();
            int result = downHelper.load(RemoteUrl, FileConstant.getInstance().JS_PATCH_LOCAL_PATH, Runtime.getRuntime().availableProcessors() + 1, now_version);
            if (result == 1) {
                handler.sendEmptyMessage(DOWNLOAD_FINISH);
            } else {
                Log.e("show", "下载失败");
            }
        }
    }).start();
}

```
这里的UpdateModel 是模拟的网络数据返回的值，这个根据你实际情况而定，而且这里需要判断权限，你可以在进入app，或者在统一授权时候做这样更加的让体验好一点。这里下载完成后，有一个hander来处理下载线程和主线程通讯问题，而且当解压完成以后，需要重新注册组件
```

private class MyHander extends Handler {
    SoftReference<ReactiveActivity> mActivity;

    public MyHander(com.yunsoft.mvpdemo.activity.ReactiveActivity reactiveActivity) {
        mActivity = new SoftReference<>(reactiveActivity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (mActivity.get() != null && !mActivity.get().isFinishing()) {
            switch (msg.what) {
                case HotUpdate.UNZIP_SUCCESS:
                //手动注册组件
                    Toast.makeText(mActivity.get(), "更新完成", Toast.LENGTH_SHORT).show();
                    String version = (String) msg.obj;
                    ((MyApplication) mActivity.get().getApplication()).setVersion(version); //设置版本
                    ReactNativePreLoader.preLoad(ReactiveActivity.this, "test");//重新加载数据
                    SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
                    updateShare.edit().putString("reactive_version", version).apply();
                    break;
                case DOWNLOAD_FINISH://解压处理
                    Log.e("show", "version:" + "解压");
                    HotUpdate.handleZIP(getApplicationContext(), mActivity.get().now_version, mActivity.get().old_version, mActivity.get().AllUpdate, mActivity.get().handler);
                    break;
            }
        }
    }
}
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
handler = new MyHander(this);
//这个可以在准备进入react的前一个页面步骤注册。
registLocalVersion();
}
/**
 * 注册本地版本组件
 */
public void registLocalVersion() {
    SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
    String version = updateShare.getString("reactive_version", "");
    Toast.makeText(this, "init version" + version, Toast.LENGTH_SHORT).show();
    ((MyApplication) getApplication().getApplicationContext()).setVersion(version); //设置版本
    ReactNativePreLoader.preLoad(ReactiveActivity.this, "test");//重新加载数据
}

```
这里注册本地版本里面的test参数是程序的入口组件名称。这里的版本是从SharedPreferences读取的，可以根据具体情况而定。
完整的Activity如下
```

public class MainActivity extends AppCompatActivity {

    private Button refresh_btn;
    private Button oldVersionUpdate_btn;
    private Button allUpdate_btn;
    private Button back_btn;
    private Button into_btn;
    public static final int DOWNLOAD_FINISH = 10202;
    private UpdateModel updateModel;//模拟网络更新得到的model对象

    //全量更新
    private String downUrl = "https://raw.githubusercontent.com/yangyunfeng666/image/master/bundle.zip";
    //增量更新
    private String addUpdate = "https://raw.githubusercontent.com/yangyunfeng666/image/master/new/bundle.zip";
    //增量更新1.0.3在1.0.1上的增量更新版本下载地址
    private String newUpdateUrl = "https://raw.githubusercontent.com/yangyunfeng666/image/master/add/bundle.zip";

    //更新通知hander
    private class MyHander extends Handler {
        SoftReference<MainActivity> mActivity;

        public MyHander(MainActivity reactiveActivity) {
            mActivity = new SoftReference<>(reactiveActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() != null && !mActivity.get().isFinishing()) {
                switch (msg.what) {
                    case HotUpdate.UNZIP_SUCCESS://解压合并完成 注册组件和本地保存版本
                        Toast.makeText(mActivity.get(), "更新完成", Toast.LENGTH_SHORT).show();
                        String version = (String) msg.obj;
                        //手动注册组件
                        ((MyApplication) mActivity.get().getApplication()).setVersion(version); //设置版本
                        ReactNativePreLoader.preLoad(MainActivity.this, "test");//重新加载数据
                        SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
                        updateShare.edit().putString("reactive_version", version).apply();
                        break;
                    case DOWNLOAD_FINISH://解压处理合并处理
                        Log.e("show", "version:" + "解压");
                        HotUpdate.handleZIP(getApplicationContext(), mActivity.get().updateModel.getNow_version(), mActivity.get().updateModel.getOld_version(), mActivity.get().updateModel.isAllUpdate(), mActivity.get().handler);
                        break;
                }
            }
        }
    }

    private MyHander handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refresh_btn = findViewById(R.id.refresh_btn);
        oldVersionUpdate_btn = findViewById(R.id.oldVersionUpdate_btn);
        allUpdate_btn = findViewById(R.id.allUpdate_btn);
        back_btn = findViewById(R.id.back_btn);
        into_btn = findViewById(R.id.into_btn);
        handler = new MyHander(this);

        allUpdate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全量更新
                updateModel = new UpdateModel("1.0.2", "", true, false,downUrl);
                update(updateModel);
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //回退
                updateModel = new UpdateModel("1.0.1", "", false, true,"");
                update(updateModel);
            }
        });

        oldVersionUpdate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //增量更新 以旧版本更新
                updateModel = new UpdateModel("1.0.3", "1.0.1", false, false,addUpdate);
                update(updateModel);
            }
        });

        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //增量更新
                updateModel = new UpdateModel("1.0.1", "1.0.0", false, false,newUpdateUrl);
                update(updateModel);
            }
        });

        //查看版本更新结果
        into_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReactNativeActivity.class);
                startActivity(intent);
            }
        });

        //设置调试模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1002);
            }
        }

        //写文件权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }
        //注册react 入口组件
        registLocalVersion();

    }

    /**
     * 注册本地版本
     */
    public void registLocalVersion() {
        SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
        String version = updateShare.getString("reactive_version", "");
        Toast.makeText(this, "init version" + version, Toast.LENGTH_SHORT).show();
        ((MyApplication) getApplication().getApplicationContext()).setVersion(version); //设置版本
        ReactNativePreLoader.preLoad(MainActivity.this, "test");//重新加载数据
    }

    public void update(UpdateModel updateModel) {
        if (updateModel.isBackToOld()) {
            SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
            updateShare.edit().putString("reactive_version", updateModel.getNow_version()).apply();
            ((MyApplication) getApplication().getApplicationContext()).setVersion(updateModel.getNow_version()); //设置版本
            ReactNativePreLoader.preLoad(MainActivity.this, "test");//重新加载数据
        } else {
            Toast.makeText(this, "真正更新，请稍后...", Toast.LENGTH_SHORT).show();
            requstPermission(updateModel.getDownurl(), updateModel.getNow_version()); //下载更新好了，更新版本
        }
    }


    public void requstPermission(String RemoteUrl, String version) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        } else {
            downLoadBundle(RemoteUrl, version);
        }
    }

    private void downLoadBundle(final String RemoteUrl, final String now_version) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MutilDownHelper downHelper = new MutilDownHelper();
                int result = downHelper.load(RemoteUrl, FileConstant.getInstance().JS_PATCH_LOCAL_PATH, Runtime.getRuntime().availableProcessors() + 1, now_version);
                if (result == 1) {
                    handler.sendEmptyMessage(DOWNLOAD_FINISH);
                } else {
                    Log.e("show", "下载失败");
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //第一次就移动assets开头的图片到sdcard里面
                    SharedPreferences updateShare = getSharedPreferences("update", Context.MODE_PRIVATE);
                    String update = updateShare.getString("firstUpdate", "0");
                    if (update.equals("0")) {
                        //assets 是根据你本地文件在到包成react-native
                        final List<DrawableModel> models = UpdateUtil.getResourceByReflect(R.drawable.class,"assets");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (DrawableModel model : models) {
                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), model.getId());
                                    if (bitmap != null) {
                                        UpdateUtil.saveBitMapToSdcard(bitmap, FileConstant.getInstance().DRAWABLE_PATH, FileConstant.getInstance().DRAWABLE_PATH + File.separator + model.getName() + ".png", Bitmap.CompressFormat.PNG);
                                    }
                                }
                            }
                        }).start();
                        updateShare.edit().putString("firstUpdate", "1").apply();
                    }
                }
                break;
            case 1002:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        // SYSTEM_ALERT_WINDOW permission not granted...
                        Toast.makeText(this, "需要授权", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
```
activity_main.xml如下
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">


    <Button
        android:id="@+id/refresh_btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="没有旧版本增量更新到1.0.1"
        />

    <Button
        android:id="@+id/oldVersionUpdate_btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="以1.0.1为旧版本增量更新到1.0.3"
        />

    <Button
        android:id="@+id/allUpdate_btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="全量更新1.0.2版本"
        />
    <Button
        android:id="@+id/back_btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="回退版本到1.0.1"></Button>

    <Button
        android:id="@+id/into_btn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="进入react"
        />

</LinearLayout>
```
### 加快reactnative加载解决
让你的ractnative 入口类继承PreLoadReactActivity
比如 替换以前继承的ReactActivity
```
public class ReactNativeActivity extends PreLoadReactActivity  {
    @javax.annotation.Nullable
    @Override
    protected String getMainComponentName() {
        return "test";
    }
}
```
这里的test也是注册的组件名称。
gitbub 地址：[项目地址](http://172.20.8.45/android_compoment/reactnativeupdate-android.git)
