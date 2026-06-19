package com.duszek.pindrop.dto.planning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateDestinationRequest {

	@NotBlank
	private String destination;

	@NotNull
	private Double lat;

	@NotNull
	private Double lng;

	@NotNull
	private LocalDate startDate;

	@NotNull
	private LocalDate endDate;
}
