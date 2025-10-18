package ru.vrn.rt.analyzesystem.anlyze.excel.beans;

import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelColumn;

public class CountMsisdnAggreagateBean {
    @ExcelColumn(name = "msisdn", order = 1)
    private String msisdn;
    @ExcelColumn(name = "кол-во", order = 2)
    private int count;

    public CountMsisdnAggreagateBean(String msisdn, int count) {
        this.msisdn = msisdn;
        this.count = count;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
