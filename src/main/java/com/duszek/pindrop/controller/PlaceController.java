package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

	private final PlaceService placeService;

	@GetMapping("/search")
	public List<PlaceResponse> search(
			@RequestParam String q,
			@RequestParam(defaultValue = "8") int limit) {
		return placeService.search(q, limit);
	}
}
