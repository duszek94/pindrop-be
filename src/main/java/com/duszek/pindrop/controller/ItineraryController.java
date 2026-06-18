package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.common.CursorPage;
import com.duszek.pindrop.dto.dashboard.ItineraryResponse;
import com.duszek.pindrop.security.SecurityUtils;
import com.duszek.pindrop.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

	private final ItineraryService itineraryService;

	@GetMapping("/favorites")
	public List<ItineraryResponse> getFavorites() {
		return itineraryService.getFavorites(SecurityUtils.getCurrentUserId());
	}

	@GetMapping("/explore")
	public CursorPage<ItineraryResponse> explore(
			@RequestParam(required = false) Long cursor,
			@RequestParam(required = false) Integer limit) {
		return itineraryService.explore(SecurityUtils.getCurrentUserId(), cursor, limit);
	}

	@PostMapping("/{id}/like")
	public ItineraryResponse likeItinerary(@PathVariable Long id) {
		return itineraryService.likeItinerary(SecurityUtils.getCurrentUserId(), id);
	}

	@DeleteMapping("/{id}/like")
	public ItineraryResponse unlikeItinerary(@PathVariable Long id) {
		return itineraryService.unlikeItinerary(SecurityUtils.getCurrentUserId(), id);
	}
}
