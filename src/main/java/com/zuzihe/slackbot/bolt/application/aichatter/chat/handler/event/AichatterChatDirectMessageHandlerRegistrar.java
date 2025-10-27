package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import aichatter.bolt.application.aichatter.chat.service.AichatterChatSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AichatterChatDirectMessageHandlerRegistrar implements AichatterChatEventHandlerRegistrar<MessageEvent> {
    private final AichatterChatSlackEventService eventService;

    @Override
    public void register(App app) {
        app.event(MessageEvent.class, (payload, ctx) -> {
            eventService.handleDirectMessage(getEvent(payload), getTeamId(payload));

            return ctx.ack();
        });
    }
}
