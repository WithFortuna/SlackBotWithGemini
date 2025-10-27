package com.zuzihe.slackbot.bolt.application.docsbot.handler.event;

import aichatter.bolt.application.docsbot.service.DocsbotSlackEventService;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageFileShareEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DocsbotMessageFileShareHandlerRegistrar implements DocsbotEventHandlerRegistrar<MessageFileShareEvent> {
    private final DocsbotSlackEventService eventService;
    @Override
    public void register(App app) {
        app.event(MessageFileShareEvent.class, (payload, ctx) -> {
            eventService.handleMessageFileShare(getEvent(payload), getTeamId(payload));
            return ctx.ack();
        });
    }
}
