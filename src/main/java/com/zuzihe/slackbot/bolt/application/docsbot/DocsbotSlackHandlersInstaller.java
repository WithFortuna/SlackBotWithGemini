package com.zuzihe.slackbot.bolt.application.docsbot;

import aichatter.bolt.application.docsbot.handler.event.DocsbotEventHandlerRegistrar;
import aichatter.bolt.application.docsbot.handler.interactive.DocsbotInteractiveHandlerRegistrar;
import com.slack.api.bolt.App;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Component
@RequiredArgsConstructor
public class DocsbotSlackHandlersInstaller {
    private final List<DocsbotEventHandlerRegistrar<?>> eventRegistrars;
    private final List<DocsbotInteractiveHandlerRegistrar> interactiveRegistrars;

    public void installEventAndInteractiveHandlers(App app) {
        installEventHandlers(app);
        installInteractiveHandlers(app);
    }

    private void installEventHandlers(App app) {
        for (DocsbotEventHandlerRegistrar<?> r : eventRegistrars) {
            logger.info("Registering handler: " + r.getClass().getSimpleName());
            r.register(app);
        }
        System.out.println("Total event handlers registered: " + eventRegistrars.size());
    }

    private void installInteractiveHandlers(App app) {
        for (DocsbotInteractiveHandlerRegistrar r : interactiveRegistrars) {
            logger.info("Registering handler: " + r.getClass().getSimpleName());
            r.register(app);
        }
        System.out.println("Total interactive handlers registered: " + interactiveRegistrars.size());
    }
}
