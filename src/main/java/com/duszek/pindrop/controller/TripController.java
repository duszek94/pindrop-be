package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.common.CursorPage;
import com.duszek.pindrop.dto.dashboard.CreateDraftTripResponse;
import com.duszek.pindrop.dto.dashboard.GeoMarkerResponse;
import com.duszek.pindrop.dto.dashboard.TripResponse;
import com.duszek.pindrop.security.SecurityUtils;
import com.duszek.pindrop.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

	private final TripService tripService;

	@PostMapping("/draft")
	@ResponseStatus(HttpStatus.CREATED)
	public CreateDraftTripResponse createDraftTrip() {
		return tripService.createDraftTrip(SecurityUtils.getCurrentUserId());
	}

	@GetMapping
	public CursorPage<TripResponse> listTrips(
			@RequestParam(defaultValue = "mine") String scope,
			@RequestParam(required = false) Long cursor,
			@RequestParam(required = false) Integer limit) {
		return tripService.listTrips(SecurityUtils.getCurrentUserId(), scope, cursor, limit);
	}

	@GetMapping("/geo")
	public List<GeoMarkerResponse> getGeoMarkers() {
		return tripService.getGeoMarkers(SecurityUtils.getCurrentUserId());
	}

	@GetMapping("/{id}")
	public TripResponse getTrip(@PathVariable Long id) {
		return tripService.getTrip(SecurityUtils.getCurrentUserId(), id);
	}
}
