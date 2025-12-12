package ru.vrn.rt.analyzesystem.load;

import ru.vrn.rt.analyzesystem.persistence.entity.Tac;
import ru.vrn.rt.analyzesystem.persistence.service.TacService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TacsLoader {
    private String filePath;
    private TacService tacService;

    public TacsLoader(String filePath, TacService tacService) {
        this.filePath = filePath;
        this.tacService = tacService;
    }

    public Map<String, Integer> loadToDb() {
        int savedCount = 0;
        int existingCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t", 2);

                if (parts.length >= 1) {
                    Tac tac = new Tac();

                    String firstColumn = parts[0].trim();
                    if (firstColumn.length() >= 8) {
                        tac.setTac(firstColumn.substring(0, 8));
                    } else {
                        tac.setTac(firstColumn);
                    }

                    if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                        tac.setDescription(parts[1].trim());
                    } else {
                        tac.setDescription(null);
                    }

                    Optional<Tac> savedTac = tacService.save(tac);

                    if (savedTac.isPresent()) {
                        savedCount++;
                    } else {
                        existingCount++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("saved", savedCount);
        result.put("existing", existingCount);
        return result;
    }
}
