package com.wei.ai.db;

import java.util.TimeZone;

public class CheckDataBean {

    private long create_time;
    private String name, sex, card_number;
    private int status;
    private long data_zero;

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.data_zero=create_time/(1000*3600*24)*(1000*3600*24)- TimeZone.getDefault().getRawOffset();
        this.create_time = create_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getData_zero() {
        return data_zero;
    }

    public void setData_zero(long data_zero) {
        this.data_zero = data_zero;
    }
}
