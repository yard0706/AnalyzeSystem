package ru.vrn.rt.analyzesystem.anlyze;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GeoIPLookup {
    private DatabaseReader dbReader;

    public GeoIPLookup(String geoLiteDbPath) throws IOException {
        // Загрузка базы данных GeoIP2
        dbReader = new DatabaseReader.Builder(new FileInputStream(geoLiteDbPath)).build();
    }

    public String lookupIP(String ipAddress) {
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            // Получение информации о стране
            Country country = response.getCountry();
            String countryName = country.getName();
//            String countryCode = country.getIsoCode();

//            // Получение информации о городе
//            City city = response.getCity();
//            String cityName = city.getName();
//
//            // Получение информации о местоположении
//            Location location = response.getLocation();
//            Double latitude = location.getLatitude();
//            Double longitude = location.getLongitude();
//            String timeZone = location.getTimeZone();
//
//            // Получение информации о почтовом индексе
//            Postal postal = response.getPostal();
//            String postalCode = postal.getCode();
//
//            // Получение информации о провайдере
//            String network = response.getTraits().getNetwork().toString();
//
//            System.out.println("=== Геолокация для IP: " + ipAddress + " ===");
//            System.out.println("Тип IP: " + (ipAddress.contains(":") ? "IPv6" : "IPv4"));
//            System.out.println("Страна: " + countryName + " (" + countryCode + ")");
//            System.out.println("Город: " + cityName);
//            System.out.println("Почтовый индекс: " + postalCode);
//            System.out.println("Координаты: " + latitude + ", " + longitude);
//            System.out.println("Часовой пояс: " + timeZone);
//            System.out.println("Сеть: " + network);
//            System.out.println();

            return countryName;
        } catch (AddressNotFoundException e) {
            System.out.println("IP адрес " + ipAddress + " не найден в базе данных");
        } catch (UnknownHostException e) {
            System.out.println("Неверный формат IP адреса: " + ipAddress);
        } catch (GeoIp2Exception | IOException e) {
            System.out.println("Ошибка при поиске IP " + ipAddress + ": " + e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    public void close() throws IOException {
        if (dbReader != null) {
            dbReader.close();
        }
    }
}