package ru.vrn.rt.analyzesystem.view;

import jakarta.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.persistence.entity.FileRecord;
import ru.vrn.rt.analyzesystem.persistence.service.FileRecordService;

import java.util.List;

@Component
@ViewScoped
public class FilesMBean {
    @Autowired
    private FileRecordService fileService;

    private List<FileRecord> getFiles() {
        return fileService.getAllFiles();
    }
}
