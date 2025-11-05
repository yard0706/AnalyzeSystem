package ru.vrn.rt.analyzesystem.filereader.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.filereader.FileInfo;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class FilesMonitorService {
    @Value("${folders.path}")
    private String folderPath;

    FileMonitor monitor;
    ExecutorService statusService;

    @PostConstruct
    public void init() {
        monitor = new FileMonitor(folderPath);
        try {
            // Запуск мониторинга
            monitor.start();

//            // Демонстрация работы - выводим список файлов каждые 10 секунд
//            statusService = Executors.newSingleThreadExecutor();
//            statusService.submit(() -> {
//                while (true) {
//                    try {
//                        Thread.sleep(10000);
//                        System.out.println("Current CSV files (" + monitor.getFileCount() + "):");
//                        monitor.getCsvFiles().forEach(System.out::println);
//                        System.out.println("---");
//                    } catch (InterruptedException e) {
//                        break;
//                    }
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
            monitor.stop();
        }
    }

    @PreDestroy
    public void destroy() {
        statusService.shutdownNow();
        monitor.stop();
    }

    public List<FileInfo> getAllFiles() {
        monitor.printStatus();
        return monitor.getCsvFiles().stream()
                .sorted(Comparator.comparing(FileInfo::getModifyDateTime).reversed())
                .collect(Collectors.toList());
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }



}