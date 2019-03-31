package com.wei.wlib.biz.update;

import java.io.Serializable;

/**
 * APP升级实体类
 */
public class WLibUpdateConfig implements Serializable {

    public String versionCode;
    public String versionName;
    public String downurl;
    public String title;
    public String text;
    public String autoup;
    public String sign;

}
