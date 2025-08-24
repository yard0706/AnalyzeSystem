package ru.vrn.rt.analyzesystem.view.model;

public class ColumnModel {
    private String columnName;
    private Integer keyColumn;


    public ColumnModel(String columnName, Integer keyColumn) {
        this.columnName = columnName;
        this.keyColumn = keyColumn;
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
}
