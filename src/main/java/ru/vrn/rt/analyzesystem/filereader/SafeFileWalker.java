package ru.vrn.rt.analyzesystem.filereader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SafeFileWalker {

    /**
     * Рекурсивно получает список всех доступных файлов
     */
    public static List<Path> getAccessibleFiles(Path startDir) {
        List<Path> accessibleFiles = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(startDir)) {
            accessibleFiles = stream
                    .filter(SafeFileWalker::isPathAccessible)
                    .collect(Collectors.toList());

        } catch (AccessDeniedException e) {
            System.err.println("⛔ Доступ к корневой директории запрещен: " + startDir);
        } catch (IOException e) {
            System.err.println("❌ Ошибка при обходе: " + e.getMessage());
        }

        return accessibleFiles;
    }

    /**
     * Проверяет доступность пути
     */
    private static boolean isPathAccessible(Path path) {
        try {
            // Пытаемся прочитать базовые атрибуты
            Files.readAttributes(path, BasicFileAttributes.class);
            return true;
        } catch (AccessDeniedException e) {
            System.err.println("⛔ Доступ запрещен: " + path);
            return false;
        } catch (IOException e) {
            System.err.println("⚠️ Ошибка доступа к " + path + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Обходит файловую систему с детальной обработкой ошибок
     */
    public static void safeWalk(Path startDir) {
        try {
            Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    processAccessibleFile(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    System.out.println("📁 Директория: " + dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    handleAccessFailure(file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (AccessDeniedException e) {
            System.err.println("❌ Доступ к начальной директории запрещен: " + startDir);
        } catch (IOException e) {
            System.err.println("❌ Ошибка при запуске обхода: " + e.getMessage());
        }
    }

    private static void processAccessibleFile(Path file) {
        try {
            System.out.println("✅ " + file + " (размер: " + Files.size(file) + " байт)");
        } catch (IOException e) {
            System.err.println("⚠️ Не удалось получить размер файла " + file + ": " + e.getMessage());
        }
    }

    private static void handleAccessFailure(Path path, IOException exc) {
        if (exc instanceof AccessDeniedException) {
            System.err.println("⛔ AccessDeniedException: " + path);
        } else {
            System.err.println("⚠️ IOException: " + path + " - " + exc.getMessage());
        }
    }
    // Пример использования
    public static void main(String[] args) {
        Path startDir = Paths.get("/home/user/temp");

        // Способ 1: Получить список доступных файлов
        List<Path> files = SafeFileWalker.getAccessibleFiles(startDir);
        System.out.println("Найдено доступных файлов: " + files.size());

        // Способ 2: Обход с выводом
        SafeFileWalker.safeWalk(startDir);
    }
}

