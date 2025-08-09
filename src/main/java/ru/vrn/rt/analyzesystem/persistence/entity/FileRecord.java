package ru.vrn.rt.analyzesystem.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private LocalDateTime loadTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(LocalDateTime loadTime) {
        this.loadTime = loadTime;
    }
}
