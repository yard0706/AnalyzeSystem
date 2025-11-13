package ru.vrn.rt.analyzesystem.anlyze;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelExporter;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.CountIpAggregateBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.CountSslSniAggregateBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.SumAndAvrSslSniAggregateBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class InternetNoDecodeConnections extends Analyzer {
    @Override
    public String analyze() {
        String xlsxFilePath = createXlsxFilenameExtention(filePath, " analytics");
        //count unique domains and them amount with level 2
        List<CountSslSniAggregateBean> uniqueListLevel2 = countUniqueSslSniLevelTrim(1, 2);
        uniqueListLevel2.sort(Comparator.comparingInt(CountSslSniAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(uniqueListLevel2, xlsxFilePath, "домены 2");
        //count unique domains and them amount with level 3
        List<CountSslSniAggregateBean> uniqueListLevel3 = countUniqueSslSniLevelTrim(1, 3);
        uniqueListLevel3.sort(Comparator.comparingInt(CountSslSniAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(uniqueListLevel3, xlsxFilePath, "домены 3");
        //count unique domains and them amount
        List<CountSslSniAggregateBean> uniqueList = countUniqueSslSni(1);
        uniqueList.sort(Comparator.comparingInt(CountSslSniAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(uniqueList, xlsxFilePath, "домены");
        //count unique contacts and summ them duration
        List<SumAndAvrSslSniAggregateBean> durationSumList = sumValuesFromColumnsTransferredData(1);
        durationSumList.sort(Comparator.comparingInt(SumAndAvrSslSniAggregateBean::getSum).reversed());
        ExcelExporter.exportToExcel(durationSumList, xlsxFilePath, "трафик");
        //count unique servers ip
        List<CountIpAggregateBean> uniqueIpList = countUniqueIpServer(1);
        uniqueIpList.sort(Comparator.comparingInt(CountIpAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(uniqueIpList, xlsxFilePath, "IP сервера");
        //count unique client ip
        List<CountIpAggregateBean> uniqueClientIpList = countUniqueIpClient(1);
        uniqueClientIpList.sort(Comparator.comparingInt(CountIpAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(uniqueClientIpList, xlsxFilePath, "IP клиента");
        return xlsxFilePath;
    }

    public InternetNoDecodeConnections(String filePath, String csvCharset, Character csvSeparator) {
        this.filePath = filePath;
        this.csvCharset = csvCharset;
        this.csvSeparator = csvSeparator;
    }

    public List<CountSslSniAggregateBean> countUniqueSslSni(Integer skipLinesAmount) {
        Map<String, Integer> valueCounts = new HashMap<>();
        final int SSL_SNI_COLUMN = 16; // Столбец 17 (индексация с 0)

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
                    // Проверяем, что столбец существует в текущей строке
                    if (currentLine.length > SSL_SNI_COLUMN) {
                        String value = currentLine[SSL_SNI_COLUMN];
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

        return valueCounts.entrySet().stream().map(e ->new CountSslSniAggregateBean(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public List<CountSslSniAggregateBean> countUniqueSslSniLevelTrim(Integer skipLinesAmount, int domainLevel) {
        Map<String, Integer> valueCounts = new HashMap<>();
        final int SSL_SNI_COLUMN = 16; // Столбец 17 (индексация с 0)

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
                    // Проверяем, что столбец существует в текущей строке
                    if (currentLine.length > SSL_SNI_COLUMN) {
                        String value = getDomainByLevel(currentLine[SSL_SNI_COLUMN], domainLevel);
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

        return valueCounts.entrySet().stream().map(e ->new CountSslSniAggregateBean(e.getKey(), e.getValue())).collect(Collectors.toList());
    }



    public List<SumAndAvrSslSniAggregateBean> sumValuesFromColumnsTransferredData(Integer skipLinesAmount) {
        Map<String, SumAndAvrSslSniAggregateBean> aggregateMap = new HashMap<>();
        final int SSL_SNI_COLUMN = 16;
        final int GET_DATA_VALUE_COLUMN = 13;
        final int SEND_DATA_VALUE_COLUMN = 14;

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
                    // Проверяем, что все необходимые столбцы существуют в текущей строке
                    if (currentLine.length > SSL_SNI_COLUMN && currentLine.length > GET_DATA_VALUE_COLUMN && currentLine.length > SEND_DATA_VALUE_COLUMN) {
                        String key = currentLine[SSL_SNI_COLUMN];
                        String firstValueStr = currentLine[GET_DATA_VALUE_COLUMN];
                        String secondValueStr = currentLine[SEND_DATA_VALUE_COLUMN];

                        // Если ключ пустой или null, считаем как "пустое значение"
                        if (key == null || key.trim().isEmpty()) {
                            key = "(пустое значение)";
                        }

                        // Парсим значения из столбцов 13 и 14 и вычисляем сумму
                        int sum = 0;
                        boolean hasValidValues = false;

                        // Обрабатываем первое значение
                        if (firstValueStr != null && !firstValueStr.trim().isEmpty()) {
                            try {
                                sum += Integer.parseInt(firstValueStr.trim());
                                hasValidValues = true;
                            } catch (NumberFormatException e) {
                                System.err.println("Некорректное числовое значение в столбце 13: " + firstValueStr);
                            }
                        }

                        // Обрабатываем второе значение
                        if (secondValueStr != null && !secondValueStr.trim().isEmpty()) {
                            try {
                                sum += Integer.parseInt(secondValueStr.trim());
                                hasValidValues = true;
                            } catch (NumberFormatException e) {
                                System.err.println("Некорректное числовое значение в столбце 14: " + secondValueStr);
                            }
                        }

                        // Если есть хотя бы одно валидное значение, обновляем AggregateBean
                        if (hasValidValues) {
                            SumAndAvrSslSniAggregateBean bean = aggregateMap.getOrDefault(key, new SumAndAvrSslSniAggregateBean(key));
                            bean.addValue(sum);
                            aggregateMap.put(key, bean);
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

    public List<CountIpAggregateBean> countUniqueIpServer(Integer skipLinesAmount) {
        GeoIPLookup geoLookup;
        try {
            String databasePath = new ClassPathResource("GeoLite2-City.mmdb").getFile().getAbsolutePath();
            geoLookup = new GeoIPLookup(databasePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Integer> valueCounts = new HashMap<>();
        final int SSL_SNI_COLUMN = 7;

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
                    // Проверяем, что столбец существует в текущей строке
                    if (currentLine.length > SSL_SNI_COLUMN) {
                        String value = currentLine[SSL_SNI_COLUMN];
                        // Если значение пустое или null, считаем как "пустое значение"
                        if (value == null || value.trim().isEmpty()) {
                            value = "(пустое значение)";
                        } else {
                            // Берем значение до первого символа "/"
                            int slashIndex = value.indexOf('/');
                            if (slashIndex != -1) {
                                value = value.substring(0, slashIndex);
                            }
                        }
                        valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
                    }
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return valueCounts.entrySet().stream()
                .map(e -> new CountIpAggregateBean(e.getKey(), geoLookup.lookupIP(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<CountIpAggregateBean> countUniqueIpClient(Integer skipLinesAmount) {
        GeoIPLookup geoLookup;
        try {
            String databasePath = new ClassPathResource("GeoLite2-City.mmdb").getFile().getAbsolutePath();
            geoLookup = new GeoIPLookup(databasePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Integer> valueCounts = new HashMap<>();
        final int SSL_SNI_COLUMN = 5;

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
                    // Проверяем, что столбец существует в текущей строке
                    if (currentLine.length > SSL_SNI_COLUMN) {
                        String value = currentLine[SSL_SNI_COLUMN];
                        // Если значение пустое или null, считаем как "пустое значение"
                        if (value == null || value.trim().isEmpty()) {
                            value = "(пустое значение)";
                        } else {
                            // Берем значение до первого символа "/"
                            int slashIndex = value.indexOf('/');
                            if (slashIndex != -1) {
                                value = value.substring(0, slashIndex);
                            }
                        }
                        valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
                    }
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return valueCounts.entrySet().stream()
                .map(e -> new CountIpAggregateBean(e.getKey(), geoLookup.lookupIP(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    private String getDomainByLevel(String domain, int levels) {
        if (domain == null || domain.trim().isEmpty()) {
            return "";
        }

        String cleanDomain = domain.trim();

        // Разбиваем домен на части
        String[] parts = cleanDomain.split("\\.");

        // Определяем, сколько уровней нам нужно взять
        int levelsToTake = Math.min(levels, parts.length);

        // Собираем домен из последних levelsToTake частей
        StringBuilder result = new StringBuilder();
        for (int i = parts.length - levelsToTake; i < parts.length; i++) {
            if (result.length() > 0) {
                result.append(".");
            }
            result.append(parts[i]);
        }

        return result.toString();
    }
}
