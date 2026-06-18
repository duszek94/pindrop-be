package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.common.CursorPage;
import com.duszek.pindrop.dto.dashboard.NotificationResponse;
import com.duszek.pindrop.dto.dashboard.NotificationSummaryResponse;
import com.duszek.pindrop.entity.Notification;
import com.duszek.pindrop.exception.ForbiddenException;
import com.duszek.pindrop.repository.NotificationRepository;
import com.duszek.pindrop.util.CursorPaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public NotificationSummaryResponse getSummary(Long userId) {
		long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);
		return new NotificationSummaryResponse(unreadCount);
	}

	@Transactional(readOnly = true)
	public CursorPage<NotificationResponse> listNotifications(Long userId, Long cursor, Integer limit) {
		int pageSize = CursorPaginationUtils.resolveLimit(limit);
		var fetched = notificationRepository.findByUserIdWithCursor(
				userId, cursor, PageRequest.of(0, pageSize + 1));
		return CursorPaginationUtils.toCursorPage(fetched, pageSize, this::toResponse, Notification::getId);
	}

	@Transactional
	public NotificationResponse markAsRead(Long userId, Long notificationId) {
		Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
				.orElseThrow(() -> new ForbiddenException("Notification not found or access denied"));
		notification.setRead(true);
		return toResponse(notificationRepository.save(notification));
	}

	@Transactional
	public void markAllAsRead(Long userId) {
		notificationRepository.markAllReadByUserId(userId);
	}

	private NotificationResponse toResponse(Notification notification) {
		return NotificationResponse.builder()
				.id(notification.getId())
				.type(notification.getType())
				.message(notification.getMessage())
				.read(notification.isRead())
				.createdAt(notification.getCreatedAt())
				.build();
	}
}
