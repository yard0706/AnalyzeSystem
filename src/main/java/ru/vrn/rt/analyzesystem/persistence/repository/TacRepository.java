package ru.vrn.rt.analyzesystem.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vrn.rt.analyzesystem.persistence.entity.Tac;

import java.util.List;
import java.util.Optional;

public interface TacRepository extends JpaRepository<Tac,Long> {
    List<Tac> findDescriptionByTac(String tac);
}
