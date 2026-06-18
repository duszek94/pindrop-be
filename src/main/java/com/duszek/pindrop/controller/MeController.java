package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.dashboard.MeResponse;
import com.duszek.pindrop.security.SecurityUtils;
import com.duszek.pindrop.service.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

	private final MeService meService;

	@GetMapping
	public MeResponse getMe() {
		return meService.getMe(SecurityUtils.getCurrentUserId());
	}
}
