package ru.vrn.rt.analyzesystem.filereader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TXTReader implements FilesReader {
    @Override
    public String readFirstLines(String filePath, int lines) throws IOException {
        System.out.println("=== Первые " + lines + " строк из TXT файла: " + filePath + " ===");
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
