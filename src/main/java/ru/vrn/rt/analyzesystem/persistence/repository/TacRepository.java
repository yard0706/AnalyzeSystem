package ru.vrn.rt.analyzesystem.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vrn.rt.analyzesystem.persistence.entity.Tac;

import java.util.List;
import java.util.Optional;

public interface TacRepository extends JpaRepository<Tac,Long> {
    List<Tac> findDescriptionByTac(String tac);

    boolean existsByTac(String tac);

    default Tac saveIfUnique(Tac newTac) {
        if (existsByTac(newTac.getTac())) {
            throw new IllegalArgumentException("TAC with value " + newTac.getTac() + " already exists");
        }
        return save(newTac);
    }

    default Optional<Tac> saveIfUniqueOptional(Tac newTac) {
        if (existsByTac(newTac.getTac())) {
            return Optional.empty();
        }
        return Optional.of(save(newTac));
    }
}
