package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.config.AppProperties;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
public class OllamaTripPlanProvider implements TripPlanAiProvider {

	private final WebClient webClient;
	private final AppProperties appProperties;
	private final JsonMapper jsonMapper;
	private final TripPlanTemplateEngine templateEngine;

	public OllamaTripPlanProvider(
			AppProperties appProperties,
			JsonMapper jsonMapper,
			TripPlanTemplateEngine templateEngine) {
		this.appProperties = appProperties;
		this.jsonMapper = jsonMapper;
		this.templateEngine = templateEngine;
		this.webClient = WebClient.builder()
				.baseUrl(appProperties.getAi().getBaseUrl())
				.build();
	}

	@Override
	public TripPlanGenerationResult generateProposals(TripPlanGenerationRequest request) {
		try {
			String prompt = TripPlanPromptBuilder.buildGenerationPrompt(request);
			String response = chat(prompt);
			return TripPlanJsonParser.parse(jsonMapper, response, request, templateEngine);
		} catch (Exception ex) {
			log.warn("Ollama generation failed, using stub fallback: {}", ex.getMessage());
			return templateEngine.generateProposals(request);
		}
	}

	@Override
	public TripPlanGenerationResult.GeneratedActivity regenerateActivity(
			TripPlanGenerationRequest tripContext,
			TripPlanGenerationResult.GeneratedActivity currentActivity) {
		return templateEngine.regenerateActivity(tripContext, currentActivity);
	}

	private String chat(String prompt) {
		Map<String, Object> body = Map.of(
				"model", appProperties.getAi().getModel(),
				"messages", List.of(Map.of("role", "user", "content", prompt)),
				"stream", false);

		Map<?, ?> response = webClient.post()
				.uri("/api/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.retrieve()
				.bodyToMono(Map.class)
				.block();

		if (response == null) {
			throw new IllegalStateException("Empty Ollama response");
		}
		Object message = response.get("message");
		if (message instanceof Map<?, ?> messageMap) {
			return String.valueOf(messageMap.get("content"));
		}
		throw new IllegalStateException("Unexpected Ollama response format");
	}
}
