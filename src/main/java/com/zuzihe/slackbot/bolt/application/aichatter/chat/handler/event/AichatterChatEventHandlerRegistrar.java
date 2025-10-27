package com.zuzihe.slackbot.bolt.application.aichatter.chat.handler.event;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.model.event.Event;

public interface AichatterChatEventHandlerRegistrar<E extends Event> {
    void register(App app);

    default String getTeamId(EventsApiPayload<?> payload) {
        return payload.getTeamId();
    }

    default E getEvent(EventsApiPayload<E> payload) {
        return payload.getEvent();
    }
}

