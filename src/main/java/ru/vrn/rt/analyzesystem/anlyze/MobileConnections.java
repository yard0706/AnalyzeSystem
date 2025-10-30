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
            //count base stations addresses at night
            List<AddressCountBean> nightBSList = getUniqueValuesFromAddressDuringNightHours(1);
            nightBSList.sort(Comparator.comparingInt(AddressCountBean::getCount).reversed());
            ExcelExporter.exportToExcel(nightBSList, xlsxFilePath, "ночные БС");
            //count base stations addresses at night
            List<ThreesomeBean> threesList = getUniqueThreesome(1);
            ExcelExporter.exportToExcel(threesList, xlsxFilePath, "тройка");


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

    public List<AddressCountBean> getUniqueValuesFromAddressDuringNightHours(Integer skipLinesAmount) {
        Map<String, AddressCountBean> valueCountMap = new HashMap<>();
        final int TIME_COLUMN = 3; // Столбец 4 (индексация с 0) - время
        final int TARGET_COLUMN = 22; // Столбец 23 (индексация с 0)

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
                    if (currentLine.length > TIME_COLUMN && currentLine.length > TARGET_COLUMN) {
                        String timeValue = currentLine[TIME_COLUMN];
                        String targetValue = currentLine[TARGET_COLUMN];

                        // Проверяем время (с 01:00 до 05:00)
                        if (isTimeBetween1AMAnd5AM(timeValue)) {
                            // Обрабатываем значение из столбца 23
                            if (targetValue != null && !targetValue.trim().isEmpty()) {
                                String value = targetValue.trim();

                                // Обновляем счетчик для данного значения
                                AddressCountBean bean = valueCountMap.get(value);
                                if (bean == null) {
                                    bean = new AddressCountBean(value);
                                    valueCountMap.put(value, bean);
                                }
                                bean.incrementCount();
                            }
                        }
                    }
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(valueCountMap.values());
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

    public List<ThreesomeBean> getUniqueThreesome(Integer skipLinesAmount) {
        Map<String, ThreesomeBean> uniqueMap = new HashMap<>();
        final int MSISDN = 8;  // Столбец 9 (индексация с 0)
        final int IMSI = 9; // Столбец 10 (индексация с 0)
        final int IMEI = 10; // Столбец 11 (индексация с 0)

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
                    // Проверяем, что все три столбца существуют в текущей строке
                    if (currentLine.length > IMEI) {
                        String value9 = getValueOrEmpty(currentLine[MSISDN]);
                        String value10 = getValueOrEmpty(currentLine[IMSI]);
                        String value11 = getValueOrEmpty(currentLine[IMEI]);

                        // Создаем ключ для уникальности по всем трем значениям
                        String key = value9 + "|" + value10 + "|" + value11;

                        // Если такого сочетания значений еще нет, добавляем в карту
                        if (!uniqueMap.containsKey(key)) {
                            ThreesomeBean bean = new ThreesomeBean();
                            bean.setMsisdn(value9);
                            bean.setImsi(value10);
                            bean.setImei(value11);
                            uniqueMap.put(key, bean);
                        }
                    }
                }
                previousLine = currentLine;
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(uniqueMap.values());
    }

    // Вспомогательный метод для обработки пустых значений
    private String getValueOrEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? "(пустое значение)" : value.trim();
    }
}
