package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import aichatter.bolt.application.aichatter.chat.service.AichatterChatSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.AppHomeOpenedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AichatterChatAppHomeOpenedHandlerRegistrar implements AichatterChatEventHandlerRegistrar<AppHomeOpenedEvent> {
    private final AichatterChatSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
            eventService.handleAppHomeOpened(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}

