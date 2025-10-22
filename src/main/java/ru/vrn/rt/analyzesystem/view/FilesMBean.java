package ru.vrn.rt.analyzesystem.view;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vrn.rt.analyzesystem.filereader.FolderInfo;
import ru.vrn.rt.analyzesystem.filereader.FolderReader;
import ru.vrn.rt.analyzesystem.filereader.preview.FileReaderFactory;
import ru.vrn.rt.analyzesystem.filereader.preview.FilesReader;
import ru.vrn.rt.analyzesystem.persistence.service.FileRecordService;
import ru.vrn.rt.analyzesystem.view.dto.FileRecordViewDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ViewScoped
public class FilesMBean {
    @Autowired
    private FileRecordService fileService;
    @Autowired
    private DataViewBean dataViewBean;
    @Value("${work.folder}")
    private String workFolder;
    @Value("${default.csv.separator}")
    private String separatorChar;
    @Value("${default.csv.encoding}")
    private String encoding;


    private List<FileRecordViewDTO> filesList = new ArrayList<>();
    private FileRecordViewDTO selectedFile;
    private String selectedFilePreview = "test text";

    private List<FolderInfo> allFiles;
    private FolderInfo foundFile;

    public List<FolderInfo> getAllFiles() {
        return allFiles;
    }
    public void setAllFiles(List<FolderInfo> allFiles) {
        this.allFiles = allFiles;
    }

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
        System.out.println("-=> init start");
        updateFilesList();

        String folderPath = "/home/user/temp";
        String[] extensions = {"csv", "xls", "xlsx"};
        allFiles = FolderReader.getAllFilePathsWithTime(folderPath, extensions);
        System.out.println(">>>> allFiles size "+allFiles.size());
        System.out.println("-=> finish start");
    }

    public List<FolderInfo> completeFolders(String query) {
        return allFiles.stream().filter(f-> f.getFilePath().toLowerCase().contains(query.toLowerCase())).toList();
    }

    public void onFolderSearchSelect(SelectEvent<FolderInfo> event) {
        FacesMessage msg = new FacesMessage("File Selected", String.valueOf(event.getObject().getFilePath()));
        FacesContext.getCurrentInstance().addMessage(null, msg);

        FileRecordViewDTO fileRecordViewDTO = new FileRecordViewDTO();
        fileRecordViewDTO.setFilePath(event.getObject().getFilePath());
        selectedFile = fileRecordViewDTO;
        openFile();
    }

    public void onRowSelect(SelectEvent<FileRecordViewDTO> event) {
        FacesMessage msg = new FacesMessage("File Selected", String.valueOf(event.getObject().getFileName()));
        FacesContext.getCurrentInstance().addMessage(null, msg);

        openFile();
    }

    private void openFile() {
        File fileObj = new File(selectedFile.getFilePath());
        if (fileObj.exists()) {
            FilesReader reader = FileReaderFactory.getReader(selectedFile.getFilePath());
            try {
                selectedFilePreview = reader.readFirstLines(selectedFile.getFilePath(), 10);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Файл " + selectedFile.getFilePath() + " не найден");
        }

        dataViewBean.setFilePath(selectedFile.getFilePath());
        dataViewBean.setSeparatorChar(separatorChar);
        dataViewBean.init();
        PrimeFaces.current().ajax().update(":contentForm:fileDataTable");
    }

    public void onChangeParams() {
        System.out.println("onChangeSeparator method start");
        if(selectedFile == null) return;
        System.out.println("onChangeSeparator selected file not null");
        dataViewBean.setFilePath(selectedFile.getFilePath());
        dataViewBean.setSeparatorChar(separatorChar);
        dataViewBean.setEncoding(encoding);
        dataViewBean.init();
        PrimeFaces.current().ajax().update(":contentForm:fileDataTable");
    }


    public List<FileRecordViewDTO> getFilesList() {
        return filesList;
    }

    public void setFilesList(List<FileRecordViewDTO> filesList) {
        this.filesList = filesList;
    }

    public FileRecordViewDTO getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(FileRecordViewDTO selectedFile) {
        this.selectedFile = selectedFile;
    }

    public String getSelectedFilePreview() {
        return selectedFilePreview;
    }

    public void setSelectedFilePreview(String selectedFilePreview) {
        this.selectedFilePreview = selectedFilePreview;
    }

    public String getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(String separatorChar) {
        this.separatorChar = separatorChar;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public FolderInfo getFoundFile() {
        return foundFile;
    }

    public void setFoundFile(FolderInfo foundFile) {
        this.foundFile = foundFile;
    }
}
