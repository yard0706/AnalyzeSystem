package ru.vrn.rt.analyzesystem.load;

import ru.vrn.rt.analyzesystem.persistence.entity.Tac;
import ru.vrn.rt.analyzesystem.persistence.service.TacService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TacsLoader {
    private String filePath;
    private TacService tacService;

    public TacsLoader(String filePath, TacService tacService) {
        this.filePath = filePath;
        this.tacService = tacService;
    }

    public void loadToDb() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Разделяем строку по табуляции
                String[] parts = line.split("\t", 2);

                if (parts.length >= 1) {
                    Tac tac = new Tac();

                    // Извлекаем первые 8 символов из первого столбца
                    String firstColumn = parts[0].trim();
                    if (firstColumn.length() >= 8) {
                        tac.setTac(firstColumn.substring(0, 8));
                    } else {
                        // Если меньше 8 символов, используем всю строку
                        tac.setTac(firstColumn);
                    }

                    // Заполняем описание (второй столбец)
                    if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                        tac.setDescription(parts[1].trim());
                    } else {
                        tac.setDescription(null);
                    }

                    //to db -> !!!
                    tacService.save(tac);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
