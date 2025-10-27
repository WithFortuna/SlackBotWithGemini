package com.zuzihe.slackbot.bolt.application.docsbot.handler.event;

import aichatter.bolt.application.docsbot.service.DocsbotSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AppMentionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocsbotAppMentionHandlerRegistrar implements DocsbotEventHandlerRegistrar<AppMentionEvent> {
    private final DocsbotSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(AppMentionEvent.class, (payload, ctx) -> {
            eventService.handleAppMention(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}

