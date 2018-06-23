package com.yunsoft.reactnative;

/**
 * Author: yangyunfeng
 * Date: 公元2018-6-6 10:47
 * Description:this is UpdateModel
 */

public class UpdateModel {
    private String now_version;//现在更新的版本
    private String old_version;//旧版本
    private boolean allUpdate;//是否全量更新
    private boolean backToOld;//是否回退
    private String downurl;//更新地址

    public UpdateModel(String now_version, String old_version, boolean allUpdate, boolean backToOld, String downurl) {
        this.now_version = now_version;
        this.old_version = old_version;
        this.allUpdate = allUpdate;
        this.backToOld = backToOld;
        this.downurl = downurl;
    }


    public String getDownurl() {
        return downurl;
    }

    public void setDownurl(String downurl) {
        this.downurl = downurl;
    }

    public String getNow_version() {
        return now_version;
    }

    public void setNow_version(String now_version) {
        this.now_version = now_version;
    }

    public String getOld_version() {
        return old_version;
    }

    public void setOld_version(String old_version) {
        this.old_version = old_version;
    }

    public boolean isAllUpdate() {
        return allUpdate;
    }

    public void setAllUpdate(boolean allUpdate) {
        this.allUpdate = allUpdate;
    }

    public boolean isBackToOld() {
        return backToOld;
    }

    public void setBackToOld(boolean backToOld) {
        this.backToOld = backToOld;
    }
}
