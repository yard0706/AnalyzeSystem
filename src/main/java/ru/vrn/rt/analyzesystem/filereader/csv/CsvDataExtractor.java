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
import java.time.LocalDateTime;
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

//    public Map<String, Integer> getColumnsMap() {
//        Map<String, Integer> resultMap = new LinkedHashMap<>();
//        CSVParser csvParser = new CSVParserBuilder()
//                .withSeparator(csvSeparator)
//                .build();
//
//        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
//                .withCSVParser(csvParser)
//                .build()) {
//
//            String[] firstLine = reader.readNext();
//            if (firstLine == null) throw new IllegalArgumentException("CSV файл пуст или не содержит данных");
//
//            for (int i = 0; i < firstLine.length; i++)
//                resultMap.put(firstLine[i],i );
//
//        } catch (IOException | CsvException e) {
//            throw new IllegalStateException("Ошибка при чтении CSV файла: " + filePath, e);
//        }
//        return resultMap;
//    }
public Map<String, Integer> getColumnsMap(List<Integer> selectedIndices) {
    Map<String, Integer> resultMap = new LinkedHashMap<>();
    CSVParser csvParser = new CSVParserBuilder()
            .withSeparator(csvSeparator)
            .build();

    try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
            .withCSVParser(csvParser)
            .build()) {

        String[] firstLine = reader.readNext();
        if (firstLine == null) throw new IllegalArgumentException("CSV файл пуст или не содержит данных");

        if (selectedIndices == null || selectedIndices.isEmpty() || selectedIndices.get(0).equals(-1)) {
            for (int i = 0; i < firstLine.length; i++) {
                resultMap.put(firstLine[i], i);
            }
        } else {
            for (Integer index : selectedIndices) {
                if (index != null && index >= 0 && index < firstLine.length) {
                    resultMap.put(firstLine[index], index);
                }
            }
        }

    } catch (IOException | CsvException e) {
        throw new IllegalStateException("Ошибка при чтении CSV файла: " + filePath, e);
    }
    return resultMap;
}

    public List<String[]> readLines(Integer skipLinesAmount) {
        List<String[]> resultList = new ArrayList<>();
        final int MAX_LINES = 100;

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(csvSeparator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
                .withCSVParser(csvParser)
                .withSkipLines(skipLinesAmount != null ? skipLinesAmount : 0)
                .build()) {

            String[] previousLine = null;
            String[] currentLine;
            int lineCount = 0;

            while ((currentLine = reader.readNext()) != null && lineCount < MAX_LINES) {
                if (previousLine == null || !Arrays.equals(previousLine, currentLine)) {
                    resultList.add(currentLine);
                    lineCount++;
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return resultList;
    }

//    public List<String[]> readLines(Integer skipLinesAmount, List<Integer> indices) {
//        List<String[]> resultList = new ArrayList<>();
//        final int MAX_LINES = 100;
//
//        CSVParser csvParser = new CSVParserBuilder()
//                .withSeparator(csvSeparator)
//                .build();
//
//        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
//                .withCSVParser(csvParser)
//                .withSkipLines(skipLinesAmount != null ? skipLinesAmount : 0)
//                .build()) {
//
//            String[] previousLine = null;
//            String[] currentLine;
//            int lineCount = 0;
//
//            while ((currentLine = reader.readNext()) != null && lineCount < MAX_LINES) {
//                if (previousLine == null || !Arrays.equals(previousLine, currentLine)) {
//                    System.out.println(">>>"+String.join(",",currentLine));
//                    String[] filteredLine = filterByIndices(currentLine, indices);
//                    System.out.println("<<<"+String.join(",",filteredLine));
//                    resultList.add(filteredLine);
//                    lineCount++;
//                }
//                previousLine = currentLine;
//            }
//
//        } catch (IOException | CsvException e) {
//            e.printStackTrace();
//        }
//
//        return resultList;
//    }
//
//    private String[] filterByIndices(String[] originalArray, List<Integer> indices) {
//        if (indices == null || indices.isEmpty()) {
//            return originalArray;
//        }
//
//        return indices.stream()
//                .filter(index -> index != null && index >= 0 && index < originalArray.length)
//                .map(index -> originalArray[index])
//                .toArray(String[]::new);
//    }


}
