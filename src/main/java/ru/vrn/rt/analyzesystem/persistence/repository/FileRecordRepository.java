package ru.vrn.rt.analyzesystem.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vrn.rt.analyzesystem.persistence.entity.FileRecord;

import java.util.List;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    public List<FileRecord> findTop50ByOrderByLoadTimeDesc();
}
