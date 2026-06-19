package com.duszek.pindrop.dto.planning;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDayResponse {

	private String dayLabel;
	private String icon;
	private int tempC;
}
