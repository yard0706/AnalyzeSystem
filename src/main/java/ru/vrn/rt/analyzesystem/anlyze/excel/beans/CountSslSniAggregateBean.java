package ru.vrn.rt.analyzesystem.anlyze.excel.beans;

import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelColumn;

public class CountSslSniAggregateBean {
    @ExcelColumn(name = "Значение", order = 1)
    private String sslsni;
    @ExcelColumn(name = "кол-во", order = 2)
    private int count;

    public CountSslSniAggregateBean(String sslsni, int count) {
        this.sslsni = sslsni;
        this.count = count;
    }

    public String getSslsni() {
        return sslsni;
    }

    public void setSslsni(String sslsni) {
        this.sslsni = sslsni;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
