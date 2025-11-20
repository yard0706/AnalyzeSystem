package ru.vrn.rt.analyzesystem.persistence.service;

import org.springframework.stereotype.Service;
import ru.vrn.rt.analyzesystem.persistence.entity.Tac;
import ru.vrn.rt.analyzesystem.persistence.repository.TacRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TacService {
    private final TacRepository tacRepository;

    public TacService(TacRepository tacRepository) {
        this.tacRepository = tacRepository;
    }

    public String getDescriptionByTac(String tac) {
        return Optional.ofNullable(tacRepository.findDescriptionByTac(tac))
                .stream()
                .flatMap(List::stream)
                .findFirst()
                .map(Tac::getDescription)
                .orElse("(нет данных)");
    }
}
