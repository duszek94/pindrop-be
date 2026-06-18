package com.duszek.pindrop.util;

import com.duszek.pindrop.dto.common.CursorPage;

import java.util.List;
import java.util.function.Function;

public final class CursorPaginationUtils {

	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 50;

	private CursorPaginationUtils() {
	}

	public static int resolveLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	public static <T, R> CursorPage<R> toCursorPage(
			List<T> fetched,
			int limit,
			Function<T, R> mapper,
			Function<T, Long> idExtractor) {
		boolean hasMore = fetched.size() > limit;
		List<T> pageItems = hasMore ? fetched.subList(0, limit) : fetched;
		List<R> mapped = pageItems.stream().map(mapper).toList();
		Long nextCursor = hasMore ? idExtractor.apply(pageItems.get(pageItems.size() - 1)) : null;
		return new CursorPage<>(mapped, nextCursor, hasMore);
	}
}
