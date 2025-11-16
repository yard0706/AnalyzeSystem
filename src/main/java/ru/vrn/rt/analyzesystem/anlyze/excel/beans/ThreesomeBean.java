package ru.vrn.rt.analyzesystem.anlyze.excel.beans;

import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelColumn;

public class ThreesomeBean {
    @ExcelColumn(name = "msisdn", order = 1)
    private String msisdn;
    @ExcelColumn(name = "imsi", order = 2)
    private String imsi;
    @ExcelColumn(name = "imei", order = 3)
    private String imei;
    @ExcelColumn(name = "device", order = 4)
    private String device;


    // Конструкторы
    public ThreesomeBean() {}

    public ThreesomeBean(String msisdn, String imsi, String imei) {
        this.msisdn = msisdn;
        this.imsi = imsi;
        this.imei = imei;
    }

    // Геттеры и сеттеры
    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "ThreesomeBean{" +
                "msisdn='" + msisdn + '\'' +
                ", imsi='" + imsi + '\'' +
                ", imei='" + imei + '\'' +
                ", device='" + device + '\'' +
                '}';
    }
}