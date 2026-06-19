package com.duszek.pindrop.provider.weather;

import java.time.LocalDate;
import java.util.List;

public interface WeatherProvider {

	List<WeatherForecast> getForecast(double lat, double lng, LocalDate startDate, LocalDate endDate);
}
