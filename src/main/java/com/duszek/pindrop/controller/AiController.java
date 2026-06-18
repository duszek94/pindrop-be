package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.dashboard.SuggestedDestinationResponse;
import com.duszek.pindrop.security.SecurityUtils;
import com.duszek.pindrop.service.AiSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

	private final AiSuggestionService aiSuggestionService;

	@GetMapping("/suggested-destinations")
	public List<SuggestedDestinationResponse> getSuggestedDestinations() {
		return aiSuggestionService.getSuggestedDestinations(SecurityUtils.getCurrentUser());
	}
}
