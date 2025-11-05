package ru.vrn.rt.analyzesystem.view.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.vrn.rt.analyzesystem.filereader.FileInfo;
import ru.vrn.rt.analyzesystem.view.FilesMBean;

@Named
@ApplicationScoped
@FacesConverter(value = "foldersConverter", managed = true)
public class FoldersConverter implements Converter<FileInfo> {

    @Inject
    private FilesMBean filesMBean;


    @Override
    public FileInfo getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        return filesMBean.getAllFiles().stream().filter(f->f.getFilePath().equals(s)).toList().getFirst();
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, FileInfo fileInfo) {
        return fileInfo.getFilePath();
    }
}
