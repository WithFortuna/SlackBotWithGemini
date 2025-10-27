package com.zuzihe.slackbot.bolt.application.docsbot.handler.event;

import aichatter.bolt.application.docsbot.service.DocsbotSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AppHomeOpenedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocsbotAppHomeOpenedHandlerRegistrar implements DocsbotEventHandlerRegistrar<AppHomeOpenedEvent> {
    private final DocsbotSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
            eventService.handleAppHomeOpened(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}

