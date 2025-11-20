package ru.vrn.rt.analyzesystem.view;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.anlyze.InternetNoDecodeConnections;
import ru.vrn.rt.analyzesystem.anlyze.MobileConnections;
import ru.vrn.rt.analyzesystem.filereader.csv.CsvDataExtractor;
import ru.vrn.rt.analyzesystem.load.TacsLoader;
import ru.vrn.rt.analyzesystem.persistence.service.TacService;
import ru.vrn.rt.analyzesystem.view.constants.AnalyzeConstant;
import ru.vrn.rt.analyzesystem.view.model.ColumnModel;

import java.io.*;
import java.util.*;

@Component
@SessionScoped
public class DataViewBean {
    private List<ColumnModel> columns;
    private List<String[]> records;
    private Map<String, List<Integer>> filesPatternsMap;
    private Map<String, HashMap<Integer,String>> predictFilesPatternsMap;
    private String selectedFilesPattern;
    private List<String> filesPatterns = new ArrayList<>();
    private StreamedContent downloadFile;

    private String filePath;
    private String separatorChar;
    private String encoding = "CP1251";
    @Autowired
    private TacService tacService;

    @PostConstruct
    public void postConstructInit() {
        filesPatternsMap = new HashMap<>() {{
           put(AnalyzeConstant.MOBILE_CONNECTIONS, new LinkedList<Integer>() {{add(3);add(4);add(8);add(14);}});
           put(AnalyzeConstant.LOCATION, new LinkedList<Integer>() {{add(2);add(3);add(9);}});
           put(AnalyzeConstant.INTERNET_NODECODE, new LinkedList<Integer>() {{add(-1);}}); // -1 вывести все колонки как есть
        }};

        predictFilesPatternsMap = new HashMap<>() {{
            put(AnalyzeConstant.MOBILE_CONNECTIONS, new HashMap<Integer, String>() {{
                put(2,"Направление соединения");
                put(3,"Время начала соединения");
                put(4,"Длительность, сек");
                put(5,"Тип соединения");}});
            put(AnalyzeConstant.LOCATION, new HashMap<Integer, String>() {{
                put(2,"Время определения местоположения");
                put(3,"Номер абонента");
                put(4,"IMSI абонента");
                put(5,"IMEI абонента");}});
            put(AnalyzeConstant.INTERNET_NODECODE, new HashMap<Integer, String>() {{
                put(2,"Время начала соединения");
                put(3,"Время завершения соединения");
                put(4,"Ид.точки подключения");
                put(5,"IP-адрес/порт клиента");}});
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

    public String getFileTypeByFirstLine(Integer skipLinesAmount) {
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(separatorChar.charAt(0))
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), encoding))
                .withCSVParser(csvParser)
                .withSkipLines(skipLinesAmount != null ? skipLinesAmount : 0)
                .build()) {

            // Читаем первую строку
            String[] firstLine = reader.readNext();
            if (firstLine == null) {
                return null;
            }

            // Проверяем первую строку по каждому шаблону в predictFilesPatternsMap
            for (Map.Entry<String, HashMap<Integer, String>> entry : predictFilesPatternsMap.entrySet()) {
                String fileType = entry.getKey();
                HashMap<Integer, String> pattern = entry.getValue();

                boolean matches = true;

                // Проверяем каждую позицию в паттерне
                for (Map.Entry<Integer, String> columnEntry : pattern.entrySet()) {
                    int columnIndex = columnEntry.getKey();
                    String expectedValue = columnEntry.getValue();

                    // Если индекс выходит за границы массива или значения не совпадают
                    if (columnIndex >= firstLine.length ||
                            !expectedValue.equals(firstLine[columnIndex])) {
                        matches = false;
                        break;
                    }
                }

                // Если все проверки пройдены, возвращаем тип файла
                if (matches) {
                    return fileType;
                }
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Если ни один паттерн не подошел
        return null;
    }

    private String analyzeResultXlsxFileName = null;
    public void analyzeFile() {
        System.out.println("getAnalyzedFile start");
        if (filePath==null) {
            System.out.println("filePath is null");
            return;
        }

        analyzeResultXlsxFileName = analyzeWithConcretePattern(analyzeResultXlsxFileName);
        if (analyzeResultXlsxFileName == null) {
            sendMessage("Ошибка", "Не выбран шаблон для анализа файла!");
            return;
        }

        sendMessage("Анализ завершен", "Выгружаем файл..");

        System.out.println("filePath >>> " + analyzeResultXlsxFileName);
        System.out.println(StringUtils.substringAfterLast(analyzeResultXlsxFileName, File.separator));
    }

    private static void sendMessage(String title, String detail) {
        FacesMessage msg = new FacesMessage(title, detail);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update(":contentForm:msgs");
    }

    public StreamedContent getAnalyzedFile()
    {
        String analyzeResultXlsxFileNameFinalLocal = analyzeResultXlsxFileName;
        System.out.println("getAnalyzedFile start with "+analyzeResultXlsxFileNameFinalLocal);
        return DefaultStreamedContent.builder()
                .name(StringUtils.substringAfterLast(analyzeResultXlsxFileNameFinalLocal, File.separator))
                .contentType("application/vnd.ms-excel")
                .stream(() -> {
                    try {
                        return new FileInputStream(new File(analyzeResultXlsxFileNameFinalLocal));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();
    }

    private String analyzeWithConcretePattern(String analyzeResultXlsxFileName) {
        if (filePath.toLowerCase().endsWith("taci.txt")) {
            new TacsLoader(filePath, tacService).loadToDb();
            sendMessage("Обработан:", filePath);
        }
        if (selectedFilesPattern.equals(AnalyzeConstant.MOBILE_CONNECTIONS))
            analyzeResultXlsxFileName = ( new MobileConnections(filePath, encoding, separatorChar.charAt(0), tacService) ).analyze();
        if (selectedFilesPattern.equals(AnalyzeConstant.INTERNET_NODECODE))
            analyzeResultXlsxFileName = ( new InternetNoDecodeConnections(filePath, encoding, separatorChar.charAt(0)) ).analyze();

        if (analyzeResultXlsxFileName ==null) {
            return null;
        }
        return analyzeResultXlsxFileName;
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

    public void autoSelectFilePattern() {
        selectedFilesPattern = getFileTypeByFirstLine(0);
    }
}
