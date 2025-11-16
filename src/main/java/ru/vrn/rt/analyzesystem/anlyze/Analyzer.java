package ru.vrn.rt.analyzesystem.anlyze;

import ru.vrn.rt.analyzesystem.persistence.service.TacService;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Analyzer {
    protected String filePath;
    protected String csvCharset;
    protected Character csvSeparator;
    protected TacService tacService;

    public abstract String analyze();

    protected String createXlsxFilenameExtention(String filePath, String suffics) {
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
}
