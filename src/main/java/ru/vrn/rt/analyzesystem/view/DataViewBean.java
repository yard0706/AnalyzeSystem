package ru.vrn.rt.analyzesystem.view;

import jakarta.faces.view.ViewScoped;
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

    public void init(String filePath, Character csvSeparator ) {
        CsvDataExtractor csvDataExtractor = new CsvDataExtractor(filePath, csvSeparator);
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
}
