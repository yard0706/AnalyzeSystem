package ru.vrn.rt.analyzesystem.view.model;

import javax.swing.text.Style;

public class ColumnModel {
    private String columnName;
    private Integer keyColumn;
    private String style = "";


    public ColumnModel(String columnName, Integer keyColumn) {
        this.columnName = columnName;
        this.keyColumn = keyColumn;
    }

    public ColumnModel(String columnName, Integer keyColumn, String style) {
        this.columnName = columnName;
        this.keyColumn = keyColumn;
        this.style = style;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(Integer keyColumn) {
        this.keyColumn = keyColumn;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
