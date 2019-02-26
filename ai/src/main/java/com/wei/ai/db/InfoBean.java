package com.wei.ai.db;

/**
 * Created by Administrator on 2019-02-25.
 */

public class InfoBean {

    private String name, sex, card, birthday, date, address, head, department;

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    @Override
    public String toString() {
        return "{" +
                "\"name\":\"" + name + '\"' +
                ", \"sex\":\"" + sex + '\"' +
                ", \"card\":\"" + card + '\"' +
                ", \"birthday\":\"" + birthday + '\"' +
                ", \"date\":\"" + date + '\"' +
                ", \"address\":\"" + address + '\"' +
                ", \"head\":\"\"" +
                ", \"department\":\"" + department + '\"' +
                "}";
    }
}
