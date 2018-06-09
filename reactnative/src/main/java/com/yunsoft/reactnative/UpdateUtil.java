package com.yunsoft.reactnative;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Author: yangyunfeng
 * Date: 公元2018-6-8 15:36
 * Description:this is UpdateUtil
 */

public class UpdateUtil {

    /**
     * 获取app图片名称获取图片的资源id的方法
     *
     * @param reactPicStartCharts reactnatvie 本地到包文件的名称的开始标识
     * @return
     */
    public static ArrayList<DrawableModel> getResourceByReflect(String reactPicStartCharts) {
        Class drawable = R.drawable.class;
        ArrayList<DrawableModel> arrayList = new ArrayList();
        Field[] fields = drawable.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith(reactPicStartCharts)) {
                int r_id;
                try {
                    r_id = field.getInt(field.getName());
                    DrawableModel model = new DrawableModel(r_id, field.getName());
                    arrayList.add(model);
                } catch (Exception e) {
                    Log.e("ERROR", "PICTURE NOT　FOUND！");
                }
            }
        }
        return arrayList;
    }

    /**
     * 保存Bitmap 到目录
     * @param bitmap
     * @param dir
     * @param path
     * @param compressFormat
     */
    public static void saveBitMapToSdcard(Bitmap bitmap, String dir, String path, Bitmap.CompressFormat compressFormat) {
        File file = new File(path);
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (file != null && file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(compressFormat, 100, fileOutputStream);
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
