package ru.vrn.rt.analyzesystem.filereader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

public class FolderReader {
   /**
     * Получает все файлы с указанными расширениями из папки (включая подпапки)
     * При возникновении ошибок для отдельных файлов - продолжает работу и информирует
     */
    public static List<FileInfo> getAllFilePathsWithTime(String folderPath, String[] extensions) {
        List<FileInfo> result = new ArrayList<>();

//        System.out.println("===> "+folderPath);
//
//        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
//            result = paths
//                    .filter(path -> {
//                        try {
//                            return Files.isReadable(path);
//                        } catch (SecurityException e) {
//                            System.err.println("Доступ запрещен: " + path);
//                            return false;
//                        }
//                    })
//                    .filter(Files::isRegularFile)
//                    .filter(path -> {
//                        try {
//                            return hasExtension(path, extensions);
//                        } catch (Exception e) {
//                            System.err.println("Ошибка при проверке расширения файла: " + path + " - " + e.getMessage());
//                            return false;
//                        }
//                    })
//                    .map(path -> {
//                        try {
//                            return pathToFileInfo(path);
//                        } catch (Exception e) {
//                            System.err.println("Ошибка при обработке файла: " + path + " - " + e.getMessage());
//                            return null; // возвращаем null для последующей фильтрации
//                        }
//                    })
//                    .filter(fileInfo -> fileInfo != null) // фильтруем null значения
//                    .collect(Collectors.toList());
//
//        } catch (IOException e) {
//            System.err.println("Ошибка при доступе к папке: " + folderPath + " - " + e.getMessage());
//            // Возвращаем пустой список или частично заполненный
//        } catch (Exception e) {
//            System.err.println("Неожиданная ошибка: " + e.getMessage());
//        }

        return result;
    }

    private static FileInfo pathToFileInfo(Path path) throws IOException {
        String filePath = path.toString();
        FileTime lastModifiedTime = Files.getLastModifiedTime(path);
        long modifyTime = lastModifiedTime.toMillis();
        return new FileInfo(filePath, modifyTime);
    }

    private static boolean hasExtension(Path path, String[] extensions) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
            for (String ext : extensions) {
                if (fileExtension.equals(ext.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}