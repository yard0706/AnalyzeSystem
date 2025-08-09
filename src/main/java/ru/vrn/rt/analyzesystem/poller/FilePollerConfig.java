package ru.vrn.rt.analyzesystem.poller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import ru.vrn.rt.analyzesystem.parser.ParserFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;


@Configuration
public class FilePollerConfig {
    @Value("${input.folder}")
    private String inputFolder;

    @Value("${work.folder}")
    private String workFolder;

    private ParserFactory parserFactory = new ParserFactory();

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

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
