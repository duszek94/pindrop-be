package com.duszek.pindrop.dto.planning;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDaySummary {

	private int dayNumber;
	private LocalDate date;
}
