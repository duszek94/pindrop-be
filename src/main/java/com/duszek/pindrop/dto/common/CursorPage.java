package com.duszek.pindrop.dto.common;

import java.util.List;

public record CursorPage<T>(
		List<T> items,
		Long nextCursor,
		boolean hasMore) {
}
