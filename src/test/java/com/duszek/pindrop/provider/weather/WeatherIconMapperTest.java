package com.duszek.pindrop.provider.weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherIconMapperTest {

	@Test
	void mapsOpenWeatherConditionsToAppIcons() {
		assertEquals("sun", WeatherIconMapper.toAppIcon("Clear"));
		assertEquals("cloud", WeatherIconMapper.toAppIcon("Clouds"));
		assertEquals("rain", WeatherIconMapper.toAppIcon("Rain"));
		assertEquals("rain", WeatherIconMapper.toAppIcon("Drizzle"));
		assertEquals("rain", WeatherIconMapper.toAppIcon("Thunderstorm"));
		assertEquals("cloud", WeatherIconMapper.toAppIcon("Mist"));
		assertEquals("cloud", WeatherIconMapper.toAppIcon(null));
	}
}
