package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.common.CursorPage;
import com.duszek.pindrop.dto.dashboard.NotificationResponse;
import com.duszek.pindrop.dto.dashboard.NotificationSummaryResponse;
import com.duszek.pindrop.security.SecurityUtils;
import com.duszek.pindrop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/summary")
	public NotificationSummaryResponse getSummary() {
		return notificationService.getSummary(SecurityUtils.getCurrentUserId());
	}

	@GetMapping
	public CursorPage<NotificationResponse> listNotifications(
			@RequestParam(required = false) Long cursor,
			@RequestParam(required = false) Integer limit) {
		return notificationService.listNotifications(SecurityUtils.getCurrentUserId(), cursor, limit);
	}

	@PatchMapping("/{id}/read")
	public NotificationResponse markAsRead(@PathVariable Long id) {
		return notificationService.markAsRead(SecurityUtils.getCurrentUserId(), id);
	}

	@PostMapping("/read-all")
	public void markAllAsRead() {
		notificationService.markAllAsRead(SecurityUtils.getCurrentUserId());
	}
}
