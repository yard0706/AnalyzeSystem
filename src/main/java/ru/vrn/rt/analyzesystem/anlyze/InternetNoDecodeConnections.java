package ru.vrn.rt.analyzesystem.anlyze;

//Единый проход по файлу - все данные собираются за один проход в методе performSinglePassAnalysis()
//
//        Разделение ответственности - логика разбита на небольшие методы:
//
//        processLine() - обработка одной строки
//
//        processTrafficData() - обработка данных о трафике
//
//        buildAnalysisResult() - сбор итоговых результатов
//
//
//        Сохранена расширяемость - можно легко добавить новые методы анализа:
//
//        Добавить новую структуру данных в AnalysisResult
//
//        Добавить обработку в processLine()
//
//        Добавить преобразование в buildAnalysisResult()
//
//        Константы для столбцов - вынесены в константы для удобства изменения
//
//        Вспомогательные методы - processDomain(), processIp() для унификации обработки данных
//
//        Для добавления нового анализа:
//
//        Добавить поле в класс AnalysisResult
//
//        Добавить соответствующую Map в performSinglePassAnalysis()
//
//        Добавить обработку в processLine()
//
//        Добавить преобразование в buildAnalysisResult()
//
//        Добавить экспорт в метод analyze()

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

    // Константы для индексов столбцов
    private static final int SSL_SNI_COLUMN = 16;
    private static final int GET_DATA_VALUE_COLUMN = 13;
    private static final int SEND_DATA_VALUE_COLUMN = 14;
    private static final int SERVER_IP_COLUMN = 7;
    private static final int CLIENT_IP_COLUMN = 5;

    @Override
    public String analyze() {
        String xlsxFilePath = createXlsxFilenameExtention(filePath, " analytics");

        // Выполняем все вычисления за один проход
        AnalysisResult result = performSinglePassAnalysis(1);

        // Экспортируем результаты
        result.uniqueListLevel2.sort(Comparator.comparingInt(CountSslSniAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.uniqueListLevel2, xlsxFilePath, "домены 2");

        result.uniqueListLevel3.sort(Comparator.comparingInt(CountSslSniAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.uniqueListLevel3, xlsxFilePath, "домены 3");

        result.uniqueList.sort(Comparator.comparingInt(CountSslSniAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.uniqueList, xlsxFilePath, "домены");

        result.durationSumList.sort(Comparator.comparingInt(SumAndAvrSslSniAggregateBean::getSum).reversed());
        ExcelExporter.exportToExcel(result.durationSumList, xlsxFilePath, "трафик");

        result.uniqueIpList.sort(Comparator.comparingInt(CountIpAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.uniqueIpList, xlsxFilePath, "IP сервера");

        result.uniqueClientIpList.sort(Comparator.comparingInt(CountIpAggregateBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.uniqueClientIpList, xlsxFilePath, "IP клиента");

        return xlsxFilePath;
    }

    public InternetNoDecodeConnections(String filePath, String csvCharset, Character csvSeparator, String geoLiteDbPath) {
        this.filePath = filePath;
        this.csvCharset = csvCharset;
        this.csvSeparator = csvSeparator;
        this.geoLiteDbPath = geoLiteDbPath;
    }

    /**
     * Основной метод, выполняющий все вычисления за один проход по файлу
     */
    private AnalysisResult performSinglePassAnalysis(Integer skipLinesAmount) {
        // Структуры для сбора данных
        Map<String, Integer> domainCounts = new HashMap<>();
        Map<String, Integer> domainLevel2Counts = new HashMap<>();
        Map<String, Integer> domainLevel3Counts = new HashMap<>();
        Map<String, SumAndAvrSslSniAggregateBean> trafficAggregates = new HashMap<>();
        Map<String, Integer> serverIpCounts = new HashMap<>();
        Map<String, Integer> clientIpCounts = new HashMap<>();

        GeoIPLookup geoLookup = initializeGeoIPLookup();

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
                    processLine(currentLine, domainCounts, domainLevel2Counts, domainLevel3Counts,
                            trafficAggregates, serverIpCounts, clientIpCounts);
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Преобразуем собранные данные в итоговые результаты
        return buildAnalysisResult(domainCounts, domainLevel2Counts, domainLevel3Counts,
                trafficAggregates, serverIpCounts, clientIpCounts, geoLookup);
    }

    /**
     * Обрабатывает одну строку CSV, обновляя все структуры данных
     */
    private void processLine(String[] line,
                             Map<String, Integer> domainCounts,
                             Map<String, Integer> domainLevel2Counts,
                             Map<String, Integer> domainLevel3Counts,
                             Map<String, SumAndAvrSslSniAggregateBean> trafficAggregates,
                             Map<String, Integer> serverIpCounts,
                             Map<String, Integer> clientIpCounts) {

        // Обработка доменов
        if (line.length > SSL_SNI_COLUMN) {
            String domain = line[SSL_SNI_COLUMN];
            String processedDomain = processDomain(domain);

            // Полные домены
            domainCounts.merge(processedDomain, 1, Integer::sum);

            // Домены уровня 2
            String domainLevel2 = getDomainByLevel(domain, 2);
            domainLevel2Counts.merge(domainLevel2, 1, Integer::sum);

            // Домены уровня 3
            String domainLevel3 = getDomainByLevel(domain, 3);
            domainLevel3Counts.merge(domainLevel3, 1, Integer::sum);

            // Обработка трафика
            processTrafficData(line, processedDomain, trafficAggregates);
        }

        // Обработка IP сервера
        if (line.length > SERVER_IP_COLUMN) {
            String serverIp = processIp(line[SERVER_IP_COLUMN]);
            serverIpCounts.merge(serverIp, 1, Integer::sum);
        }

        // Обработка IP клиента
        if (line.length > CLIENT_IP_COLUMN) {
            String clientIp = processIp(line[CLIENT_IP_COLUMN]);
            clientIpCounts.merge(clientIp, 1, Integer::sum);
        }
    }

    /**
     * Обрабатывает данные о трафике для домена
     */
    private void processTrafficData(String[] line, String domain,
                                    Map<String, SumAndAvrSslSniAggregateBean> trafficAggregates) {
        if (line.length > GET_DATA_VALUE_COLUMN && line.length > SEND_DATA_VALUE_COLUMN) {
            String firstValueStr = line[GET_DATA_VALUE_COLUMN];
            String secondValueStr = line[SEND_DATA_VALUE_COLUMN];

            int sum = 0;
            boolean hasValidValues = false;

            if (firstValueStr != null && !firstValueStr.trim().isEmpty()) {
                try {
                    sum += Integer.parseInt(firstValueStr.trim());
                    hasValidValues = true;
                } catch (NumberFormatException e) {
                    System.err.println("Некорректное числовое значение в столбце 13: " + firstValueStr);
                }
            }

            if (secondValueStr != null && !secondValueStr.trim().isEmpty()) {
                try {
                    sum += Integer.parseInt(secondValueStr.trim());
                    hasValidValues = true;
                } catch (NumberFormatException e) {
                    System.err.println("Некорректное числовое значение в столбце 14: " + secondValueStr);
                }
            }

            if (hasValidValues) {
                SumAndAvrSslSniAggregateBean bean = trafficAggregates.getOrDefault(domain,
                        new SumAndAvrSslSniAggregateBean(domain));
                bean.addValue(sum);
                trafficAggregates.put(domain, bean);
            }
        }
    }

    /**
     * Собирает итоговые результаты из накопленных данных
     */
    private AnalysisResult buildAnalysisResult(Map<String, Integer> domainCounts,
                                               Map<String, Integer> domainLevel2Counts,
                                               Map<String, Integer> domainLevel3Counts,
                                               Map<String, SumAndAvrSslSniAggregateBean> trafficAggregates,
                                               Map<String, Integer> serverIpCounts,
                                               Map<String, Integer> clientIpCounts,
                                               GeoIPLookup geoLookup) {

        AnalysisResult result = new AnalysisResult();

        result.uniqueList = domainCounts.entrySet().stream()
                .map(e -> new CountSslSniAggregateBean(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        result.uniqueListLevel2 = domainLevel2Counts.entrySet().stream()
                .map(e -> new CountSslSniAggregateBean(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        result.uniqueListLevel3 = domainLevel3Counts.entrySet().stream()
                .map(e -> new CountSslSniAggregateBean(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        result.durationSumList = new ArrayList<>(trafficAggregates.values());

        result.uniqueIpList = serverIpCounts.entrySet().stream()
                .map(e -> new CountIpAggregateBean(e.getKey(), geoLookup.lookupIP(e.getKey()), e.getValue()))
                .collect(Collectors.toList());

        result.uniqueClientIpList = clientIpCounts.entrySet().stream()
                .map(e -> new CountIpAggregateBean(e.getKey(), geoLookup.lookupIP(e.getKey()), e.getValue()))
                .collect(Collectors.toList());

        return result;
    }

    private String processDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return "(пустое значение)";
        }
        return domain.trim();
    }

    private String processIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "(пустое значение)";
        }

        String processedIp = ip.trim();
        // Берем значение до первого символа "/"
        int slashIndex = processedIp.indexOf('/');
        if (slashIndex != -1) {
            processedIp = processedIp.substring(0, slashIndex);
        }
        return processedIp;
    }

    private GeoIPLookup initializeGeoIPLookup() {
        try {
            return new GeoIPLookup(geoLiteDbPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDomainByLevel(String domain, int levels) {
        if (domain == null || domain.trim().isEmpty()) {
            return "(пустое значение)";
        }

        String cleanDomain = domain.trim();
        String[] parts = cleanDomain.split("\\.");
        int levelsToTake = Math.min(levels, parts.length);

        StringBuilder result = new StringBuilder();
        for (int i = parts.length - levelsToTake; i < parts.length; i++) {
            if (result.length() > 0) {
                result.append(".");
            }
            result.append(parts[i]);
        }

        return result.toString();
    }

    /**
     * Внутренний класс для хранения результатов анализа
     */
    private static class AnalysisResult {
        List<CountSslSniAggregateBean> uniqueList;
        List<CountSslSniAggregateBean> uniqueListLevel2;
        List<CountSslSniAggregateBean> uniqueListLevel3;
        List<SumAndAvrSslSniAggregateBean> durationSumList;
        List<CountIpAggregateBean> uniqueIpList;
        List<CountIpAggregateBean> uniqueClientIpList;
    }
}