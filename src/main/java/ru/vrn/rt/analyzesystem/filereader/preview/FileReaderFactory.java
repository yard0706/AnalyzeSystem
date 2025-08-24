package ru.vrn.rt.analyzesystem.filereader.preview;

public class FileReaderFactory {
    public static FilesReader getReader(String filePath) {
        if (filePath.toLowerCase().endsWith(".csv")) {
            return new CSVReader();
        } else if (filePath.toLowerCase().endsWith(".txt")) {
            return new TXTReader();
        } else if (filePath.toLowerCase().endsWith(".xlsx") ||
                filePath.toLowerCase().endsWith(".xls")) {
            return new ExcelReader();
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }
}
