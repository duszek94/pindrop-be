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
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiTripPlanProvider implements TripPlanAiProvider {

	private final WebClient webClient;
	private final AppProperties appProperties;
	private final JsonMapper jsonMapper;
	private final TripPlanTemplateEngine templateEngine;

	public OpenAiTripPlanProvider(
			AppProperties appProperties,
			JsonMapper jsonMapper,
			TripPlanTemplateEngine templateEngine) {
		this.appProperties = appProperties;
		this.jsonMapper = jsonMapper;
		this.templateEngine = templateEngine;
		this.webClient = WebClient.builder()
				.baseUrl("https://api.openai.com")
				.defaultHeader("Authorization", "Bearer " + appProperties.getAi().getApiKey())
				.build();
	}

	@Override
	public TripPlanGenerationResult generateProposals(TripPlanGenerationRequest request) {
		try {
			String prompt = TripPlanPromptBuilder.buildGenerationPrompt(request);
			String response = chat(prompt);
			return TripPlanJsonParser.parse(jsonMapper, response, request, templateEngine);
		} catch (Exception ex) {
			log.warn("OpenAI generation failed, using stub fallback: {}", ex.getMessage());
			return templateEngine.generateProposals(request);
		}
	}

	@Override
	public TripPlanGenerationResult.GeneratedActivity regenerateActivity(
			TripPlanGenerationRequest tripContext,
			TripPlanGenerationResult.GeneratedActivity currentActivity) {
		return templateEngine.regenerateActivity(tripContext, currentActivity);
	}

	@SuppressWarnings("unchecked")
	private String chat(String prompt) {
		Map<String, Object> body = Map.of(
				"model", appProperties.getAi().getModel(),
				"messages", List.of(Map.of("role", "user", "content", prompt)),
				"response_format", Map.of("type", "json_object"));

		Map<String, Object> response = webClient.post()
				.uri("/v1/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.retrieve()
				.bodyToMono(Map.class)
				.block();

		if (response == null) {
			throw new IllegalStateException("Empty OpenAI response");
		}
		List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
		if (choices == null || choices.isEmpty()) {
			throw new IllegalStateException("No OpenAI choices");
		}
		Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
		return String.valueOf(message.get("content"));
	}
}
