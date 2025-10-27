package com.zuzihe.slackbot.bolt.application.docsbot.handler.event;

import aichatter.bolt.application.docsbot.service.DocsbotSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AssistantThreadStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocsbotAssistantThreadStartedHandlerRegistrar implements DocsbotEventHandlerRegistrar<AssistantThreadStartedEvent> {
    private final DocsbotSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(AssistantThreadStartedEvent.class, (payload, ctx) -> {
            eventService.handleAssistantThread(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}

