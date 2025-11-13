package ru.vrn.rt.analyzesystem.anlyze.excel.beans;

import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelColumn;

public class CountIpAggregateBean {
    @ExcelColumn(name = "IP", order = 1)
    private String ip;
    @ExcelColumn(name = "Место", order = 2)
    private String place;
    @ExcelColumn(name = "кол-во", order = 3)
    private int count;

    public CountIpAggregateBean(String ip, String place, int count) {
        this.ip = ip;
        this.place = place;
        this.count = count;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
