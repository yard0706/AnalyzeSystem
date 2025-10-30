package ru.vrn.rt.analyzesystem.anlyze.excel.beans;

import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelColumn;

import java.util.Objects;

public class AddressCountBean {
    @ExcelColumn(name = "Адрес БС с 01:00 до 05:00", order = 1)
    private String value;
    @ExcelColumn(name = "кол-во", order = 2)
    private int count;

    public AddressCountBean(String value) {
        this.value = value;
        this.count = 0;
    }

    public void incrementCount() {
        this.count++;
    }

    public void incrementCount(int amount) {
        this.count += amount;
    }

    // Геттеры
    public String getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    // Сеттеры (если нужны)
    public void setValue(String value) {
        this.value = value;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ValueCountBean{value='" + value + "', count=" + count + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressCountBean that = (AddressCountBean) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}