package ru.vrn.rt.analyzesystem.view;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.view.ViewScoped;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.filereader.csv.CsvDataExtractor;
import ru.vrn.rt.analyzesystem.view.model.ColumnModel;

import java.util.*;

import jakarta.faces.model.SelectItem;

@Component
@ViewScoped
public class DataViewBean {
    private List<ColumnModel> columns;
    private List<String[]> records;
    private Map<String, List<Integer>> filesPatternsMap;
    private String selectedFilesPattern;
    private List<String> filesPatterns = new ArrayList<>();

    private String filePath;
    private String separatorChar;
    private String encoding = "CP1251";

    @PostConstruct
    public void postConstructInit() {
        filesPatternsMap = new HashMap<>() {{
           put("Мобильная связь", new LinkedList<Integer>() {{add(3);add(4);add(8);add(14);}});
           put("Местоположение", new LinkedList<Integer>() {{add(2);add(3);add(9);}});
        }};

        filesPatterns.addAll(filesPatternsMap.keySet());
    }

    public void init() {
        CsvDataExtractor csvDataExtractor = new CsvDataExtractor(filePath, separatorChar.charAt(0), encoding);
        columns = new ArrayList<>();
        for(String cKey:csvDataExtractor.getColumnsMap(filesPatternsMap.get(selectedFilesPattern)).keySet()) {
            columns.add( new ColumnModel(cKey,csvDataExtractor.getColumnsMap(filesPatternsMap.get(selectedFilesPattern)).get(cKey), "width: 200px") );
        }
        records = csvDataExtractor.readLines(1); // 1 - means skip headers
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

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Map<String, List<Integer>> getFilesPatternsMap() {
        return filesPatternsMap;
    }

    public void setFilesPatternsMap(Map<String, List<Integer>> filesPatternsMap) {
        this.filesPatternsMap = filesPatternsMap;
    }

    public String getSelectedFilesPattern() {
        return selectedFilesPattern;
    }

    public void setSelectedFilesPattern(String selectedFilesPattern) {
        this.selectedFilesPattern = selectedFilesPattern;
    }

    public List<String> getFilesPatterns() {
        return filesPatterns;
    }

    public void setFilesPatterns(List<String> filesPatterns) {
        this.filesPatterns = filesPatterns;
    }
}
