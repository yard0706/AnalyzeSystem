package ru.vrn.rt.analyzesystem.filereader.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CsvDataExtractor {

    private String filePath;
    private Character csvSeparator;

    public CsvDataExtractor(String filePath, Character csvSeparator) {
        this.filePath = filePath;
        this.csvSeparator = csvSeparator;
    }

    public Map<String, Integer> getColumnsMap() {
        Map<String, Integer> resultMap = new LinkedHashMap<>();
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(csvSeparator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
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

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
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

    public static void main(String[] args) {
        String csvFile = "/home/user/temp/work/2025-08-19/data-20181114-structure-20181114.csv";
//        csvFile = "/home/user/temp/work/2025-08-19/lots-2025-08-19.csv";

        CsvDataExtractor csvDataExtractor = new CsvDataExtractor(csvFile,',');
        for(String c:csvDataExtractor.getColumnsMap().keySet()) {
            System.out.println(c+" | "+csvDataExtractor.getColumnsMap().get(c));
        }

        for(String[] sarr:csvDataExtractor.readLines(1)) {
            System.out.println(Arrays.toString(sarr));
        }


    }

}
