package ru.vrn.rt.analyzesystem.anlyze;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.AddressCountBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.ThreesomeBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.CountMsisdnAggreagateBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.beans.SumAndAvrAggregateBean;
import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelExporter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class MobileConnections extends Analyzer {

    // Константы для индексов столбцов
    private static final int CONTACT_PHONE_COLUMN = 14;
    private static final int DURATION_VALUE_COLUMN = 4;
    private static final int TIME_COLUMN = 3;
    private static final int ADDRESS_COLUMN = 22;
    private static final int MSISDN_COLUMN = 8;
    private static final int IMSI_COLUMN = 9;
    private static final int IMEI_COLUMN = 10;

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

        // Выполняем все вычисления за один проход
        MobileAnalysisResult result = performSinglePassAnalysis(1);

        // Экспортируем результаты
        result.uniqueList.sort(Comparator.comparingInt(CountMsisdnAggreagateBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.uniqueList, xlsxFilePath, "контакты");

        result.durationSumList.sort(Comparator.comparingInt(SumAndAvrAggregateBean::getSum).reversed());
        ExcelExporter.exportToExcel(result.durationSumList, xlsxFilePath, "длительность");

        result.nightBSList.sort(Comparator.comparingInt(AddressCountBean::getCount).reversed());
        ExcelExporter.exportToExcel(result.nightBSList, xlsxFilePath, "ночные БС");

        ExcelExporter.exportToExcel(result.threesList, xlsxFilePath, "тройка");

        return xlsxFilePath;
    }

    /**
     * Основной метод, выполняющий все вычисления за один проход по файлу
     */
    private MobileAnalysisResult performSinglePassAnalysis(Integer skipLinesAmount) {
        // Структуры для сбора данных
        Map<String, Integer> msisdnCounts = new HashMap<>();
        Map<String, SumAndAvrAggregateBean> durationAggregates = new HashMap<>();
        Map<String, AddressCountBean> nightAddressBeans = new HashMap<>();
        Map<String, ThreesomeBean> uniqueThreesomes = new HashMap<>();

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
                    processLine(currentLine, msisdnCounts, durationAggregates, nightAddressBeans, uniqueThreesomes);
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Преобразуем собранные данные в итоговые результаты
        return buildAnalysisResult(msisdnCounts, durationAggregates, nightAddressBeans, uniqueThreesomes);
    }

    /**
     * Обрабатывает одну строку CSV, обновляя все структуры данных
     */
    private void processLine(String[] line,
                             Map<String, Integer> msisdnCounts,
                             Map<String, SumAndAvrAggregateBean> durationAggregates,
                             Map<String, AddressCountBean> nightAddressBeans,
                             Map<String, ThreesomeBean> uniqueThreesomes) {

        // Обработка уникальных контактов (столбец 15)
        if (line.length > CONTACT_PHONE_COLUMN) {
            String msisdn = processValue(line[CONTACT_PHONE_COLUMN]);
            msisdnCounts.merge(msisdn, 1, Integer::sum);
        }

        // Обработка длительности звонков (столбцы 15 и 4)
        if (line.length > CONTACT_PHONE_COLUMN && line.length > DURATION_VALUE_COLUMN) {
            String msisdn = processValue(line[CONTACT_PHONE_COLUMN]);
            String durationStr = line[DURATION_VALUE_COLUMN];

            processDurationData(msisdn, durationStr, durationAggregates);
        }

        // Обработка ночных базовых станций (столбцы 4 и 23)
        if (line.length > TIME_COLUMN && line.length > ADDRESS_COLUMN) {
            String timeValue = line[TIME_COLUMN];
            String addressValue = line[ADDRESS_COLUMN];

            processNightAddressData(timeValue, addressValue, nightAddressBeans);
        }

        // Обработка уникальных троек (столбцы 9, 10, 11)
        if (line.length > IMEI_COLUMN) {
            processThreesomeData(line, uniqueThreesomes);
        }
    }

    /**
     * Обрабатывает данные о длительности звонков
     */
    private void processDurationData(String msisdn, String durationStr,
                                     Map<String, SumAndAvrAggregateBean> durationAggregates) {
        if (durationStr != null && !durationStr.trim().isEmpty()) {
            try {
                int duration = Integer.parseInt(durationStr.trim());

                SumAndAvrAggregateBean bean = durationAggregates.getOrDefault(msisdn,
                        new SumAndAvrAggregateBean(msisdn));
                bean.addValue(duration);
                durationAggregates.put(msisdn, bean);

            } catch (NumberFormatException e) {
                System.err.println("Некорректное числовое значение в столбце 4: " + durationStr);
            }
        }
    }

    /**
     * Обрабатывает данные о ночных базовых станциях
     */
    private void processNightAddressData(String timeValue, String addressValue,
                                         Map<String, AddressCountBean> nightAddressBeans) {
        if (isTimeBetween1AMAnd5AM(timeValue)) {
            if (addressValue != null && !addressValue.trim().isEmpty()) {
                String address = addressValue.trim();

                // Используем подход из оригинального кода
                AddressCountBean bean = nightAddressBeans.get(address);
                if (bean == null) {
                    bean = new AddressCountBean(address);
                    nightAddressBeans.put(address, bean);
                }
                bean.incrementCount();
            }
        }
    }

    /**
     * Обрабатывает данные об уникальных тройках значений
     */
    private void processThreesomeData(String[] line, Map<String, ThreesomeBean> uniqueThreesomes) {
        String msisdn = getValueOrEmpty(line[MSISDN_COLUMN]);
        String imsi = getValueOrEmpty(line[IMSI_COLUMN]);
        String imei = getValueOrEmpty(line[IMEI_COLUMN]);

        // Создаем ключ для уникальности по всем трем значениям
        String key = msisdn + "|" + imsi + "|" + imei;

        // Если такого сочетания значений еще нет, добавляем в карту
        if (!uniqueThreesomes.containsKey(key)) {
            ThreesomeBean bean = new ThreesomeBean();
            bean.setMsisdn(msisdn);
            bean.setImsi(imsi);
            bean.setImei(imei);
            uniqueThreesomes.put(key, bean);
        }
    }

    /**
     * Собирает итоговые результаты из накопленных данных
     */
    private MobileAnalysisResult buildAnalysisResult(Map<String, Integer> msisdnCounts,
                                                     Map<String, SumAndAvrAggregateBean> durationAggregates,
                                                     Map<String, AddressCountBean> nightAddressBeans,
                                                     Map<String, ThreesomeBean> uniqueThreesomes) {

        MobileAnalysisResult result = new MobileAnalysisResult();

        result.uniqueList = msisdnCounts.entrySet().stream()
                .map(e -> new CountMsisdnAggreagateBean(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        result.durationSumList = new ArrayList<>(durationAggregates.values());

        result.nightBSList = new ArrayList<>(nightAddressBeans.values());

        result.threesList = new ArrayList<>(uniqueThreesomes.values());

        return result;
    }

    private String processValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "(пустое значение)";
        }
        return value.trim();
    }

    // Вспомогательный метод для проверки времени в формате "01.01.2023 15:20:18"
    private boolean isTimeBetween1AMAnd5AM(String timeValue) {
        if (timeValue == null || timeValue.trim().isEmpty()) {
            return false;
        }

        try {
            // Разделяем дату и время
            String[] datetimeParts = timeValue.split(" ");
            if (datetimeParts.length >= 2) {
                String timePart = datetimeParts[1]; // Берем часть с временем "15:20:18"
                String[] timeParts = timePart.split(":");
                if (timeParts.length >= 2) {
                    int hour = Integer.parseInt(timeParts[0].trim());
                    // Проверяем период с 1:00 до 5:00
                    return hour >= 1 && hour < 5;
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Некорректный формат времени: " + timeValue);
        }

        return false;
    }

    // Вспомогательный метод для обработки пустых значений
    private String getValueOrEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? "(пустое значение)" : value.trim();
    }

    /**
     * Внутренний класс для хранения результатов анализа мобильных соединений
     */
    private static class MobileAnalysisResult {
        List<CountMsisdnAggreagateBean> uniqueList;
        List<SumAndAvrAggregateBean> durationSumList;
        List<AddressCountBean> nightBSList;
        List<ThreesomeBean> threesList;
    }
}