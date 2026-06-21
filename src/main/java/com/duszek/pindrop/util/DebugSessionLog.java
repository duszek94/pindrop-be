package com.duszek.pindrop.util;

import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DebugSessionLog {

	private static final Path LOG_PATH = Path.of("c:/Development/pindrop-fe/.cursor/debug-59c406.log");
	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private DebugSessionLog() {
	}

	public static void log(String hypothesisId, String location, String message, Map<String, Object> data) {
		try {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("sessionId", "59c406");
			entry.put("hypothesisId", hypothesisId);
			entry.put("location", location);
			entry.put("message", message);
			entry.put("data", data);
			entry.put("timestamp", System.currentTimeMillis());
			String line = MAPPER.writeValueAsString(entry) + System.lineSeparator();
			Files.writeString(LOG_PATH, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Exception ignored) {
			// debug-only
		}
	}
}
