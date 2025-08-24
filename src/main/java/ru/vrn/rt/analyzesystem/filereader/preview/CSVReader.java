package ru.vrn.rt.analyzesystem.filereader.preview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class CSVReader implements FilesReader {
    @Override
    public String readFirstLines(String filePath, int lines) throws IOException {
        System.out.println("=== Первые " + lines + " строк из CSV файла: " + filePath + " ===");
        StringBuilder sbResult = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null && count < lines) {
                sbResult.append((count + 1) + ": " + line);
                sbResult.append("\n");
                count++;
            }
        }
        return sbResult.toString();
    }
}
