package ru.vrn.rt.analyzesystem.anlyze;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.CountMsisdnAggreagateBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.SumAndAvrAggregateBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelExporter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MobileConnections {
    private String filePath;
    private String csvCharset;
    private Character csvSeparator;

    public MobileConnections(String filePath, String csvCharset, Character csvSeparator) {
        this.filePath = filePath;
        this.csvCharset = csvCharset;
        this.csvSeparator = csvSeparator;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String analyze() {
        String xlsxFilePath = createXlsxFilenameExtention(filePath, " analytics");
            //count unique contacts and them amount
            List<CountMsisdnAggreagateBean> uniqueList = countUniqueMsisdn(1);
            uniqueList.sort(Comparator.comparingInt(CountMsisdnAggreagateBean::getCount).reversed());
            ExcelExporter.exportToExcel(uniqueList, xlsxFilePath, "контакты");
            //count unique contacts and summ them duration
            List<SumAndAvrAggregateBean> durationSumList = sumValuesFromColumnDuration(1);
            durationSumList.sort(Comparator.comparingInt(SumAndAvrAggregateBean::getSum).reversed());
            ExcelExporter.exportToExcel(durationSumList, xlsxFilePath, "длительность");

        return xlsxFilePath;
    }

    private String createXlsxFilenameExtention(String filePath, String suffics) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        // Заменяем расширение на .xlsx
        int lastDotIndex = fileName.lastIndexOf(".");
        String newFileName = (lastDotIndex != -1)
                ? fileName.substring(0, lastDotIndex) + suffics +".xlsx"
                : fileName + ".xlsx";

        return path.resolveSibling(newFileName).toString();
    }


    public List<CountMsisdnAggreagateBean> countUniqueMsisdn(Integer skipLinesAmount) {
        Map<String, Integer> valueCounts = new HashMap<>();
        final int CONTACT_PHONE_COLUMN = 14; // Столбец 15 (индексация с 0)

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(csvSeparator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
                .withCSVParser(csvParser)
                .withSkipLines(skipLinesAmount != null ? skipLinesAmount : 0)
                .build()) {

            String[] previousLine = null;
            String[] currentLine;

            while ((currentLine = reader.readNext()) != null) {
                if (previousLine == null || !Arrays.equals(previousLine, currentLine)) {
                    // Проверяем, что столбец 14 существует в текущей строке
                    if (currentLine.length > CONTACT_PHONE_COLUMN) {
                        String value = currentLine[CONTACT_PHONE_COLUMN];
                        // Если значение пустое или null, считаем как "пустое значение"
                        if (value == null || value.trim().isEmpty()) {
                            value = "(пустое значение)";
                        }
                        valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
                    }
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return valueCounts.entrySet().stream().map(e ->new CountMsisdnAggreagateBean(e.getKey(), e.getValue())).collect(Collectors.toList());
    }


    public List<SumAndAvrAggregateBean> sumValuesFromColumnDuration(Integer skipLinesAmount) {
        Map<String, SumAndAvrAggregateBean> aggregateMap = new HashMap<>();
        final int TARGET_COLUMN = 14; // Столбец 15 (индексация с 0)
        final int VALUE_COLUMN = 4;   // Столбец 4 (индексация с 0)

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(csvSeparator)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(filePath), csvCharset))
                .withCSVParser(csvParser)
                .withSkipLines(skipLinesAmount != null ? skipLinesAmount : 0)
                .build()) {

            String[] previousLine = null;
            String[] currentLine;

            while ((currentLine = reader.readNext()) != null) {
                if (previousLine == null || !Arrays.equals(previousLine, currentLine)) {
                    // Проверяем, что оба столбца существуют в текущей строке
                    if (currentLine.length > TARGET_COLUMN && currentLine.length > VALUE_COLUMN) {
                        String key = currentLine[TARGET_COLUMN];
                        String valueStr = currentLine[VALUE_COLUMN];

                        // Если ключ пустой или null, считаем как "пустое значение"
                        if (key == null || key.trim().isEmpty()) {
                            key = "(пустое значение)";
                        }

                        // Парсим значение из столбца 4 и обновляем AggregateBean
                        if (valueStr != null && !valueStr.trim().isEmpty()) {
                            try {
                                int value = Integer.parseInt(valueStr.trim());

                                // Получаем или создаем AggregateBean для данного ключа
                                SumAndAvrAggregateBean bean = aggregateMap.getOrDefault(key, new SumAndAvrAggregateBean(key));
                                bean.addValue(value);
                                aggregateMap.put(key, bean);

                            } catch (NumberFormatException e) {
                                // Если значение не число, игнорируем или можно добавить обработку ошибок
                                System.err.println("Некорректное числовое значение в столбце 4: " + valueStr);
                            }
                        }
                    }
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return aggregateMap.values().stream().collect(Collectors.toList());
    }
}
