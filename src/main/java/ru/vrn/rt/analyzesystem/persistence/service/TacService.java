package ru.vrn.rt.analyzesystem.persistence.service;

import org.springframework.stereotype.Service;
import ru.vrn.rt.analyzesystem.persistence.entity.Tac;
import ru.vrn.rt.analyzesystem.persistence.repository.TacRepository;

import java.util.List;

@Service
public class TacService {
    private final TacRepository tacRepository;

    public TacService(TacRepository tacRepository) {
        this.tacRepository = tacRepository;
    }

    public String getDescriptionByTac(String tac) {
        List<Tac> resultList = tacRepository.findDescriptionByTac(tac);
        if(resultList == null || resultList.size()==0) return "(нет данных)";
        return resultList.get(0).getDescription();
    }
}
