package com.duszek.pindrop.provider.weather;

import java.time.LocalDate;
import java.util.List;

public record WeatherForecast(LocalDate date, String icon, int tempMinC, int tempMaxC) {
}
