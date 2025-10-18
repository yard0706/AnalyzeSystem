package ru.vrn.rt.analyzesystem.anlyze.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ExcelExporter {

    /**
     * Экспортирует коллекцию объектов в новый файл Excel
     * @param collection отсортированная коллекция объектов
     * @param filePath путь для сохранения файла
     * @param sheetName имя листа
     * @param <T> тип объектов в коллекции
     */
    public static <T> void exportToExcel(Collection<T> collection, String filePath, String sheetName) {
        exportToExcel(collection, filePath, sheetName, true);
    }

    /**
     * Экспортирует коллекцию объектов в Excel файл
     * @param collection отсортированная коллекция объектов
     * @param filePath путь для сохранения файла
     * @param sheetName имя листа
     * @param overwriteSheet если true - перезаписывает лист, если false - добавляет данные в существующий
     * @param <T> тип объектов в коллекции
     */
    public static <T> void exportToExcel(Collection<T> collection, String filePath, String sheetName, boolean overwriteSheet) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException("Коллекция не может быть пустой");
        }

        File file = new File(filePath);
        Workbook workbook;

        try {
            if (file.exists()) {
                // Если файл существует, открываем его
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(inputStream);
                }
            } else {
                // Если файла нет, создаем новый
                workbook = new XSSFWorkbook();
            }

            // Работа с листом
            Sheet sheet;
            if (workbook.getSheet(sheetName) != null) {
                if (overwriteSheet) {
                    // Удаляем существующий лист и создаем новый с тем же именем
                    int sheetIndex = workbook.getSheetIndex(sheetName);
                    workbook.removeSheetAt(sheetIndex);
                    sheet = workbook.createSheet(sheetName);
                } else {
                    // Используем существующий лист для добавления данных
                    sheet = workbook.getSheet(sheetName);
                }
            } else {
                // Создаем новый лист
                sheet = workbook.createSheet(sheetName);
            }

            // Получаем первый объект для определения полей
            T firstObject = collection.iterator().next();
            List<FieldInfo> fields = getAnnotatedFields(firstObject.getClass());

            // Определяем начальную строку для данных
            int startRow = 0;
            if (!overwriteSheet && sheet.getPhysicalNumberOfRows() > 0) {
                // Если добавляем данные и в листе уже есть данные, начинаем с последней строки + 2 (пропуск и заголовок)
                startRow = sheet.getLastRowNum() + 2;
            }

            // Создаем заголовки (только если overwriteSheet = true или лист пустой)
            if (overwriteSheet || startRow == 0) {
                createHeaderRow(sheet, fields, startRow);
                startRow++; // Увеличиваем для данных
            }

            // Заполняем данными
            fillDataRows(sheet, collection, fields, startRow);

            // Автоматическое изменение размера колонок
            autoSizeColumns(sheet, fields.size());

            // Сохраняем файл
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

            workbook.close();

            System.out.println("Данные успешно экспортированы в файл: " + filePath + " на лист: " + sheetName);

        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException("Ошибка при создании/обновлении Excel файла", e);
        }
    }

    /**
     * Получает список всех листов в файле
     * @param filePath путь к файлу
     * @return список имен листов
     */
    public static List<String> getSheetNames(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            return sheetNames;

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла Excel", e);
        }
    }

    /**
     * Удаляет лист из файла
     * @param filePath путь к файлу
     * @param sheetName имя листа для удаления
     */
    public static void removeSheet(String filePath, String sheetName) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            int sheetIndex = workbook.getSheetIndex(sheetName);
            if (sheetIndex != -1) {
                workbook.removeSheetAt(sheetIndex);

                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                    workbook.write(outputStream);
                }
                System.out.println("Лист '" + sheetName + "' удален из файла: " + filePath);
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при удалении листа из файла Excel", e);
        }
    }

    /**
     * Получает поля с аннотацией ExcelColumn в порядке объявления в классе
     */
    private static List<FieldInfo> getAnnotatedFields(Class<?> clazz) {
        List<FieldInfo> fields = new ArrayList<>();

        // Получаем все поля класса (в порядке объявления)
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                fields.add(new FieldInfo(field, annotation));
            }
        }

        // Сортируем по порядку, указанному в аннотации
        fields.sort(Comparator.comparingInt(f -> f.annotation.order()));

        return fields;
    }

    /**
     * Создает строку с заголовками
     */
    private static void createHeaderRow(Sheet sheet, List<FieldInfo> fields, int startRow) {
        Row headerRow = sheet.createRow(startRow);
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        for (int i = 0; i < fields.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields.get(i).annotation.name());
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Заполняет строки данными
     */
    private static <T> void fillDataRows(Sheet sheet, Collection<T> collection,
                                         List<FieldInfo> fields, int startRow) throws IllegalAccessException {
        int rowNum = startRow;
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());

        for (T object : collection) {
            Row row = sheet.createRow(rowNum++);

            for (int i = 0; i < fields.size(); i++) {
                FieldInfo fieldInfo = fields.get(i);
                Field field = fieldInfo.field;
                field.setAccessible(true);
                Object value = field.get(object);

                Cell cell = row.createCell(i);
                cell.setCellStyle(dataStyle);
                setCellValue(cell, value);
            }
        }
    }

    /**
     * Устанавливает значение ячейки в зависимости от типа данных
     */
    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Создает стиль для заголовков
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Создает стиль для данных
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    /**
     * Автоматически изменяет размер колонок
     */
    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Вспомогательный класс для хранения информации о поле и аннотации
     */
    private static class FieldInfo {
        Field field;
        ExcelColumn annotation;

        FieldInfo(Field field, ExcelColumn annotation) {
            this.field = field;
            this.annotation = annotation;
        }
    }
}