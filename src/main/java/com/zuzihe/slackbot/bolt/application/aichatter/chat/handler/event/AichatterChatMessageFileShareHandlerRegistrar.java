package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import aichatter.bolt.application.aichatter.chat.service.AichatterChatSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageFileShareEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AichatterChatMessageFileShareHandlerRegistrar implements AichatterChatEventHandlerRegistrar<MessageFileShareEvent> {
    private final AichatterChatSlackEventService eventService;
    @Override
    public void register(App app) {
        app.event(MessageFileShareEvent.class, (payload, ctx) -> {
            eventService.handleMessageFileShare(getEvent(payload), getTeamId(payload));
            return ctx.ack();
        });
    }
}
