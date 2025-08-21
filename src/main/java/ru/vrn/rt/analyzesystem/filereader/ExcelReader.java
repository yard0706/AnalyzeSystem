package ru.vrn.rt.analyzesystem.filereader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

public class ExcelReader implements FilesReader{
    @Override
    public String readFirstLines(String filePath, int lines) throws IOException {
        System.out.println("=== Первые " + lines + " строк из Excel файла: " + filePath + " ===");

        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0); // Первый лист
            int count = 0;

            for (Row row : sheet) {
                if (count >= lines) break;

                System.out.print((count + 1) + ": ");
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t");
                            break;
                        case BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t");
                            break;
                        default:
                            System.out.print("-\t");
                    }
                }
                System.out.println();
                count++;
            }
        }
        return "не реализовано для Excel. Данные в системауте..";
    }
}
