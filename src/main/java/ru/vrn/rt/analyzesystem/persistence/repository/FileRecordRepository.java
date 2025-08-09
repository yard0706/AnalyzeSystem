package ru.vrn.rt.analyzesystem.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vrn.rt.analyzesystem.persistence.entity.FileRecord;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
}
