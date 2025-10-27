package com.zuzihe.slackbot.bolt.application.aichatter.chat;

import aichatter.bolt.application.aichatter.chat.handler.event.AichatterChatEventHandlerRegistrar;
import aichatter.bolt.application.aichatter.chat.handler.interactive.AichatterChatInteractiveHandlerRegistrar;
import com.slack.api.bolt.App;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Component
@RequiredArgsConstructor
public class AichatterChatSlackHandlersInstaller {
    private final List<AichatterChatEventHandlerRegistrar<?>> eventRegistrars;
    private final List<AichatterChatInteractiveHandlerRegistrar> interactiveRegistrars;

    public void installEventAndInteractiveHandlers(App app) {
        installEventHandlers(app);
        installInteractiveHandlers(app);
    }

    private void installEventHandlers(App app) {
        for (AichatterChatEventHandlerRegistrar<?> r : eventRegistrars) {
            logger.info("Registering handler: " + r.getClass().getSimpleName());
            r.register(app);
        }
        System.out.println("Total event handlers registered: " + eventRegistrars.size());
    }

    private void installInteractiveHandlers(App app) {
        for (AichatterChatInteractiveHandlerRegistrar r : interactiveRegistrars) {
            logger.info("Registering handler: " + r.getClass().getSimpleName());
            r.register(app);
        }
        System.out.println("Total interactive handlers registered: " + interactiveRegistrars.size());
    }
}
