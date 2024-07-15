package com.example.wev_service.controller;

import com.example.wev_service.exception.CityNotFoundException;
import com.example.wev_service.model.WeatherData;
import com.example.wev_service.service.OpenWeatherService;
import com.example.wev_service.service.AccuWeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Controller
public class WeatherController {

    @Autowired
    private OpenWeatherService openWeatherService;

    @Autowired
    private AccuWeatherService accuWeatherService;

    @GetMapping("/weather")
    public String getWeather(@RequestParam(required = false, defaultValue = "Минск") String city, @RequestParam(required = false, defaultValue = "OpenWeather") String source, Model model) {
        try {
            WeatherData weatherData;
            if ("OpenWeather".equalsIgnoreCase(source)) {
                weatherData = openWeatherService.getWeatherByCity(city);
            } else if ("AccuWeather".equalsIgnoreCase(source)) {
                weatherData = accuWeatherService.getWeatherByCity(city);
            } else {
                throw new IllegalArgumentException("Invalid weather source");
            }

            model.addAttribute("city", city);
            model.addAttribute("source", source);
            model.addAttribute("temperature", weatherData.getTemperature());
            model.addAttribute("humidity", weatherData.getHumidity());
        } catch (IOException | IllegalArgumentException e) {
            model.addAttribute("error", "Ошибка получения данных о погоде: " + e.getMessage());
        }

        return "weather";
    }

    @ExceptionHandler(CityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCityNotFoundException(CityNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "weather";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "weather";
    }
}


