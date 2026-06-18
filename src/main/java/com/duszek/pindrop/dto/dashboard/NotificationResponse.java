package com.duszek.pindrop.dto.dashboard;

import com.duszek.pindrop.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

	private Long id;
	private NotificationType type;
	private String message;
	private boolean read;
	private LocalDateTime createdAt;
}
