package com.example.wev_service.service;


import com.example.wev_service.model.WeatherData;
import com.example.wev_service.exception.CityNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class AccuWeatherService {

    @Value("${accuweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AccuWeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public WeatherData getWeatherByCity(String city) throws IOException {
        // Сначала нужно получить ключ местоположения (location key) для города
        String locationUrl = "https://dataservice.accuweather.com/locations/v1/cities/search?apikey=" + apiKey + "&q=" + city;
        try {
            ResponseEntity<String> locationResponse = restTemplate.exchange(locationUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);
            JsonNode locationJson = objectMapper.readTree(locationResponse.getBody());

            if (locationResponse.getStatusCode().is2xxSuccessful() && locationJson.isArray() && !locationJson.isEmpty()) {
                String locationKey = locationJson.get(0).path("Key").asText();

                // Теперь получить погоду по ключу местоположения
                String weatherUrl = "https://dataservice.accuweather.com/currentconditions/v1/" + locationKey + "?apikey=" + apiKey + "&details=true";
                ResponseEntity<String> weatherResponse = restTemplate.exchange(weatherUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);
                JsonNode weatherJson = objectMapper.readTree(weatherResponse.getBody());

                if (weatherResponse.getStatusCode().is2xxSuccessful() && weatherJson.isArray() && !weatherJson.isEmpty()) {
                    JsonNode currentWeather = weatherJson.get(0);
                    double temperature = currentWeather.path("Temperature").path("Metric").path("Value").asDouble();
                    double humidity = currentWeather.path("RelativeHumidity").asDouble();
                    return new WeatherData(temperature, humidity);
                } else {
                    throw new CityNotFoundException("Погода для города " + city + " не найдена! Попробуйте еще раз.");
                }
            } else {
                throw new CityNotFoundException("Город " + city + " не найден! Попробуйте еще раз.");
            }
        } catch (HttpClientErrorException e) {
            throw new CityNotFoundException("Город " + city + " не найден! Попробуйте еще раз.");
        }
    }
}

