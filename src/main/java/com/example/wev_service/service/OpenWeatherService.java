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
public class OpenWeatherService {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenWeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public WeatherData getWeatherByCity(String city) throws IOException {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                double temperature = jsonNode.path("main").path("temp").asDouble();
                double humidity = jsonNode.path("main").path("humidity").asDouble();

                return new WeatherData(temperature, humidity);
            } else {
                throw new CityNotFoundException("Город " + city + " не найден! Попробуйте еще раз.");
            }
        } catch (HttpClientErrorException e) {
            throw new CityNotFoundException("Город " + city + " не найден! Попробуйте еще раз.");
        }
    }
}

