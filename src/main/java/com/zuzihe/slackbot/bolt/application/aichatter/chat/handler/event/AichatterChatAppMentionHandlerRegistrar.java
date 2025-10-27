package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import aichatter.bolt.application.aichatter.chat.service.AichatterChatSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AppMentionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AichatterChatAppMentionHandlerRegistrar implements AichatterChatEventHandlerRegistrar<AppMentionEvent> {
    private final AichatterChatSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(AppMentionEvent.class, (payload, ctx) -> {
            eventService.handleAppMention(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}

