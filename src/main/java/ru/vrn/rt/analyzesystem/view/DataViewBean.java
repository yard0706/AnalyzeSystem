package ru.vrn.rt.analyzesystem.view;

import jakarta.faces.view.ViewScoped;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.filereader.csv.CsvDataExtractor;
import ru.vrn.rt.analyzesystem.view.model.ColumnModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@ViewScoped
public class DataViewBean {
    private List<ColumnModel> columns;
    private List<String[]> records;

    private String filePath;
    private String separatorChar;

    public void init() {

        CsvDataExtractor csvDataExtractor = new CsvDataExtractor(filePath, separatorChar.charAt(0));
        columns = new ArrayList<>();
        for(String cKey:csvDataExtractor.getColumnsMap().keySet()) {
            columns.add( new ColumnModel(cKey,csvDataExtractor.getColumnsMap().get(cKey)) );
        }
        records = csvDataExtractor.readLines(1);
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnModel> columns) {
        this.columns = columns;
    }

    public List<String[]> getRecords() {
        return records;
    }

    public void setRecords(List<String[]> records) {
        this.records = records;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(String separatorChar) {
        this.separatorChar = separatorChar;
    }
}
