package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.service.PlaceService;
import com.duszek.pindrop.service.PopularDestinationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

	private final PlaceService placeService;
	private final PopularDestinationsService popularDestinationsService;

	@GetMapping("/popular")
	public List<PlaceResponse> popular(
			@RequestParam(defaultValue = "6") int limit,
			@RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
		return popularDestinationsService.getPopular(limit, acceptLanguage);
	}

	@GetMapping("/search")
	public List<PlaceResponse> search(
			@RequestParam String q,
			@RequestParam(defaultValue = "8") int limit,
			@RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
		return placeService.search(q, limit, acceptLanguage);
	}

	@GetMapping("/reverse")
	public PlaceResponse reverse(
			@RequestParam double lat,
			@RequestParam double lng,
			@RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
		return placeService.reverse(lat, lng, acceptLanguage);
	}
}
