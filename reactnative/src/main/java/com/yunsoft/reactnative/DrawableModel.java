package com.yunsoft.reactnative;

/**
 * Author: yangyunfeng
 * Date: 公元2018-6-5 17:38
 * Description:this is DrawableModel
 */

public class DrawableModel {
    private Integer id;
    private String name;

    public DrawableModel(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
