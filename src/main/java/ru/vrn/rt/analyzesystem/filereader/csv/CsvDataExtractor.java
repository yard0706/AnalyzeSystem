package ru.vrn.rt.analyzesystem.filereader.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvDataExtractor {

    private String filePath;
    private Character csvSeparator;
    private String csvCharset;

    public CsvDataExtractor(String filePath, Character csvSeparator, String csvCharset) {
        this.filePath = filePath;
        this.csvSeparator = csvSeparator;
        this.csvCharset = csvCharset;
    }

    public Map<String, Integer> getColumnsMap() {
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(csvSeparator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
                .withCSVParser(csvParser)
                .withSkipLines(0) // пропускаем первые строки
                .build()) {
            // Читаем все строки
            List<String[]> allData = reader.readAll();

            if (!allData.isEmpty()) {
                // Первая строка обычно содержит заголовки (названия полей)
                String[] fields = allData.get(0);
                for (int i = 0; i < fields.length; i++)
                    resultMap.put(fields[i],i );
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public List<String[]> readLines(Integer skipLinesAmount) {
        List<String[]> resultList = new ArrayList<>();
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(csvSeparator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
                .withCSVParser(csvParser)
                .withSkipLines(skipLinesAmount) // пропускаем первые строки
                .build()) {

            List<String[]> allData = reader.readAll();

            if (!allData.isEmpty()) {
                for(String[] fields:allData) {
                    resultList.add(fields);
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return  resultList;
    }


}
