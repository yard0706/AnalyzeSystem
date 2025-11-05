package ru.vrn.rt.analyzesystem.filereader.service;

import ru.vrn.rt.analyzesystem.filereader.FileInfo;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileMonitor {
    private final Path directory;
    private final Map<String, FileInfo> csvFiles;
    private final Map<WatchKey, Path> watchKeys;
    private WatchService watchService;
    private ExecutorService executorService;
    private volatile boolean running;

    public FileMonitor(String directoryPath) {
        this.directory = Paths.get(directoryPath);
        this.csvFiles = new ConcurrentHashMap<>();
        this.watchKeys = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(3);
        this.running = false;
    }

    public void start() throws IOException {
        if (running) {
            return;
        }

        running = true;

        // Инициализация WatchService
        watchService = FileSystems.getDefault().newWatchService();

        // Рекурсивная регистрация всех подпапок
        registerAllDirectories(directory);

        // Загрузка существующих CSV файлов
        loadExistingCsvFiles();

        // Запуск мониторинга в отдельном потоке
        executorService.submit(this::monitorDirectory);

        System.out.println("FileMonitor started for directory: " + directory + " (recursive)");
    }

    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (watchService != null) {
                watchService.close();
            }
            watchKeys.clear();
        } catch (IOException | InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("FileMonitor stopped");
    }

    private void registerAllDirectories(Path startDir) throws IOException {
        // Регистрируем все поддиректории рекурсивно
        Files.walk(startDir)
                .filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        registerDirectory(dir);
                    } catch (IOException e) {
                        System.err.println("Failed to register directory: " + dir + ", " + e.getMessage());
                    }
                });
    }

    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        watchKeys.put(key, dir);
        System.out.println("Monitoring directory: " + dir);
    }

    private void loadExistingCsvFiles() {
        try {
            List<Path> csvPaths = Files.walk(directory)
                    .filter(this::isCsvFile)
                    .collect(Collectors.toList());

            csvPaths.forEach(path -> {
                try {
                    FileInfo fileInfo = createFileInfo(path);
                    csvFiles.put(path.toAbsolutePath().toString(), fileInfo);
                } catch (IOException e) {
                    System.err.println("Error loading file: " + path + ", " + e.getMessage());
                }
            });

            System.out.println("Loaded " + csvFiles.size() + " existing CSV files from all subdirectories");
        } catch (IOException e) {
            System.err.println("Error loading existing files: " + e.getMessage());
        }
    }

    private void monitorDirectory() {
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    continue;
                }

                Path dir = watchKeys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    Path child = dir.resolve(fileName);

                    handleEvent(kind, child);
                }

                boolean valid = key.reset();
                if (!valid) {
                    watchKeys.remove(key);
                    System.err.println("WatchKey is no longer valid for: " + dir);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }
    }

    private void handleEvent(WatchEvent.Kind<?> kind, Path path) {
        if (Files.isDirectory(path)) {
            handleDirectoryEvent(kind, path);
        } else if (isCsvFile(path)) {
            handleFileEvent(kind, path);
        }
    }

    private void handleDirectoryEvent(WatchEvent.Kind<?> kind, Path dir) {
        try {
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                // Новая директория - регистрируем её и все её поддиректории
                Thread.sleep(100); // Небольшая задержка
                registerAllDirectories(dir);
                System.out.println("New directory registered: " + dir);

                // Также загружаем все CSV файлы из новой директории
                loadCsvFilesFromDirectory(dir);
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                // Удаляем все файлы из этой директории из нашего списка
                removeFilesFromDirectory(dir);
                System.out.println("Directory removed from monitoring: " + dir);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error handling directory event: " + dir + ", " + e.getMessage());
        }
    }

    private void handleFileEvent(WatchEvent.Kind<?> kind, Path file) {
        String fileKey = file.toAbsolutePath().toString();

        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            try {
                Thread.sleep(100); // Небольшая задержка для гарантии, что файл полностью создан
                FileInfo fileInfo = createFileInfo(file);
                csvFiles.put(fileKey, fileInfo);
                System.out.println("File created: " + fileInfo);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error handling file creation: " + file + ", " + e.getMessage());
            }
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            FileInfo removed = csvFiles.remove(fileKey);
            if (removed != null) {
                System.out.println("File deleted: " + removed);
            }
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            try {
                FileInfo fileInfo = createFileInfo(file);
                csvFiles.put(fileKey, fileInfo);
                System.out.println("File modified: " + fileInfo);
            } catch (IOException e) {
                System.err.println("Error handling file modification: " + file + ", " + e.getMessage());
            }
        }
    }

    private void loadCsvFilesFromDirectory(Path dir) {
        try {
            Files.list(dir)
                    .filter(this::isCsvFile)
                    .forEach(path -> {
                        try {
                            FileInfo fileInfo = createFileInfo(path);
                            csvFiles.put(path.toAbsolutePath().toString(), fileInfo);
                            System.out.println("Loaded file from new directory: " + fileInfo);
                        } catch (IOException e) {
                            System.err.println("Error loading file from new directory: " + path + ", " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error loading files from directory: " + dir + ", " + e.getMessage());
        }
    }

    private void removeFilesFromDirectory(Path dir) {
        String dirPath = dir.toAbsolutePath().toString();
        List<String> filesToRemove = csvFiles.keySet().stream()
                .filter(filePath -> filePath.startsWith(dirPath))
                .collect(Collectors.toList());

        filesToRemove.forEach(filePath -> {
            FileInfo removed = csvFiles.remove(filePath);
            if (removed != null) {
                System.out.println("File removed due to directory deletion: " + removed);
            }
        });
    }

    private boolean isCsvFile(Path path) {
        return !Files.isDirectory(path) && path.toString().toLowerCase().endsWith(".csv");
    }

    private FileInfo createFileInfo(Path path) throws IOException {
        long lastModified = Files.getLastModifiedTime(path).toMillis();
        return new FileInfo(path.toAbsolutePath().toString(), lastModified);
    }

    // Методы для получения информации о файлах
    public List<FileInfo> getCsvFiles() {
        return new ArrayList<>(csvFiles.values());
    }

    public List<FileInfo> getCsvFilesFromDirectory(String directoryPath) {
        Path searchDir = Paths.get(directoryPath).toAbsolutePath();
        return csvFiles.values().stream()
                .filter(fileInfo -> fileInfo.getFilePath().startsWith(searchDir.toString()))
                .collect(Collectors.toList());
    }

    public Optional<FileInfo> getFileInfo(String filePath) {
        Path absolutePath = Paths.get(filePath).toAbsolutePath();
        return Optional.ofNullable(csvFiles.get(absolutePath.toString()));
    }

    public int getFileCount() {
        return csvFiles.size();
    }

    public int getMonitoredDirectoriesCount() {
        return watchKeys.size();
    }

    public void printStatus() {
        System.out.println("=== FileMonitor Status ===");
        System.out.println("Monitored directories: " + getMonitoredDirectoriesCount());
        System.out.println("Total CSV files: " + getFileCount());
        System.out.println("Active threads: " + Thread.activeCount());
        System.out.println("==========================");
    }
}