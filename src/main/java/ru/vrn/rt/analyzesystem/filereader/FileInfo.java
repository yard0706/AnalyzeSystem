package ru.vrn.rt.analyzesystem.filereader;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FileInfo {
    private final String filePath;
    private final long modifyTime; // время в миллисекундах
    private final LocalDateTime modifyDateTime; // время в удобном формате

    public FileInfo(String filePath, long modifyTime) {
        this.filePath = filePath;
        this.modifyTime = modifyTime;
        this.modifyDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(modifyTime), ZoneId.systemDefault());
    }

    // Геттеры
    public String getFilePath() { return filePath; }
    public long getModifyTime() { return modifyTime; }
    public LocalDateTime getModifyDateTime() { return modifyDateTime; }

    @Override
    public String toString() {
        return String.format("FileInfo{path='%s', modifyTime=%s}",
                filePath, modifyDateTime);
    }
}