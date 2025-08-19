package ru.vrn.rt.analyzesystem.view;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.persistence.entity.FileRecord;
import ru.vrn.rt.analyzesystem.persistence.service.FileRecordService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ViewScoped
public class FilesMBean {
    @Autowired
    private FileRecordService fileService;
    @Value("${work.folder}")
    private String workFolder;

    private List<FileRecordViewDTO> filesList = new ArrayList<>();

    private void updateFilesList() {
        ModelMapper modelMapper = new ModelMapper();
        List<FileRecordViewDTO> resultList = fileService.getAllFiles().stream()
                .map(file -> modelMapper.map(file, FileRecordViewDTO.class))
                .collect(Collectors.toList());

        resultList = resultList.stream()
                .map(f->{f.setGroupName(
                        StringUtils.substringBefore(f.getFilePath().replaceAll(workFolder,""),File.separator) //take first level folder from work folder
                ); return f;})
                .collect(Collectors.toList());

        filesList = resultList;
    }

    @PostConstruct
    public void init() {
        updateFilesList();
    }

    public List<FileRecordViewDTO> getFilesList() {
        return filesList;
    }

    public void setFilesList(List<FileRecordViewDTO> filesList) {
        this.filesList = filesList;
    }
}
