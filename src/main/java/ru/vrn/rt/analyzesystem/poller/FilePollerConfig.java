package ru.vrn.rt.analyzesystem.poller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import ru.vrn.rt.analyzesystem.persistence.entity.FileRecord;
import ru.vrn.rt.analyzesystem.persistence.service.FileRecordService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Configuration
public class FilePollerConfig {

    @Autowired
    private FileRecordService fileService;

    @Value("${input.folder}")
    private String inputFolder;

    @Value("${work.folder}")
    private String workFolder;


    @Bean
    @InboundChannelAdapter(value = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
    public FileReadingMessageSource fileReadingMessageSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(inputFolder));
        String pattern = ".*\\.(txt|csv|xml)$";
        source.setFilter(new RegexPatternFileListFilter(pattern));
        return source;
    }

    @ServiceActivator(inputChannel = "fileInputChannel")
    public void ProcessFile(File file) {
        try {
            System.out.println("move:"+file.getAbsoluteFile());
            //move file to work directory
            Path source = Paths.get(file.getAbsolutePath());
            Path targetDir = Paths.get(workFolder);
            Path target = targetDir.resolve(source.getFileName());

            // Ensure target directory exists
            if (!Files.exists(targetDir))
                Files.createDirectory(targetDir);
            //rename file name if duplicate
            target = renameExistFile(target, targetDir, source);

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                FileRecord fileRecord = new FileRecord();
                fileRecord.setFileName(target.getFileName().toString());
                fileRecord.setFilePath(target.toAbsolutePath().toString());
                fileRecord.setLoadTime(LocalDateTime.now());
            fileService.createFileRecord(fileRecord);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path renameExistFile(Path target, Path targetDir, Path source) {
        //off course recursive
        if(Files.exists(target)) {
            target = targetDir.resolve(Paths.get(renameFileDuplicate(source.getFileName().toString())));
            return renameExistFile(target, targetDir, target);
        } else
            return target;
    }

    private String renameFileDuplicate(String name) {
        String regex = "(.*)\\((\\d+)\\)\\.(.*)";
        Matcher matcher= Pattern.compile(regex).matcher(name.trim());
        String newName = name;
        if (matcher.find()) {
            int newNumber = Integer.parseInt(matcher.group(2)) + 1;
            newName = name.trim().replaceAll(regex, "$1("+newNumber+").$3");
        }
        else
            newName = StringUtils.substringBeforeLast(name,".")+" (1)."+StringUtils.substringAfterLast(name,".");
        return newName;
    }

}

