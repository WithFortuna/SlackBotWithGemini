package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import aichatter.bolt.application.aichatter.chat.service.AichatterChatSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AssistantThreadStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AichatterChatAssistantThreadStartedHandlerRegistrar implements AichatterChatEventHandlerRegistrar<AssistantThreadStartedEvent> {
    private final AichatterChatSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(AssistantThreadStartedEvent.class, (payload, ctx) -> {
            eventService.handleAssistantThread(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}

