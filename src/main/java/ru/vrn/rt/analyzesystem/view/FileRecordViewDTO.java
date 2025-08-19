package ru.vrn.rt.analyzesystem.view;

import ru.vrn.rt.analyzesystem.persistence.entity.FileRecord;

public class FileRecordViewDTO extends FileRecord {
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "FileRecordViewDTO{" + super.toString() +
                " groupName='" + groupName + '\'' +
                '}';
    }
}
