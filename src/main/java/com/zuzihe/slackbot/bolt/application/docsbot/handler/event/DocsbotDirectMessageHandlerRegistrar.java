package com.zuzihe.slackbot.bolt.application.docsbot.handler.event;

import aichatter.bolt.application.docsbot.service.DocsbotSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocsbotDirectMessageHandlerRegistrar implements DocsbotEventHandlerRegistrar<MessageEvent> {
    private final DocsbotSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(MessageEvent.class, (payload, ctx) -> {
            eventService.handleDirectMessage(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}
