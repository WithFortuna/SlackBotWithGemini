package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import aichatter.bolt.application.aichatter.chat.AichatterChatSlackEventType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.bolt.App;
import com.slack.api.bolt.middleware.Middleware;
import org.springframework.stereotype.Component;

import java.util.Map;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Component
public class AichatterChatUnknownEventLoggingRegistrar implements AichatterChatEventHandlerRegistrar {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void register(App app) {
        Middleware loggerMw = (req, resp, chain) -> {
            try {
                String body = req.getRequestBodyAsString();
                if (body != null && body.contains("\"type\":")) {
                    Map<String, Object> root = objectMapper.readValue(body, new TypeReference<>() {});
                    String envelopeType = (String) root.get("type");
                    if ("event_callback".equals(envelopeType)) {
                        Map<String, Object> event = (Map<String, Object>) root.getOrDefault("event", Map.of());
                        String eventType = (String) event.get("type");
                        if (eventType != null && !AichatterChatSlackEventType.isRegisteredEventType(eventType)) {
                            String eventId = (String) root.get("event_id");
                            Object teamId = root.get("team_id");
                            logger.warn("[Slack] Unhandled event type: {}, team: {}, event_id: {}", eventType, teamId, eventId);
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("UnknownEventLogging parsing skipped: {}", e.getMessage());
            }
            return chain.next(req);
        }; 

        app.use(loggerMw);
    }
}

