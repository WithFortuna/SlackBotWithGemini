package com.zuzihe.slackbot.bolt.application.common.service.manifest;

import aichatter.bolt.application.common.dto.SlackAppUriDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlackAppUriUtils {
    @Value("${slack.redirect-uri}")
    private String redirectUri;

    @Value("${slack.aichatter-chat.event-uri}")
    private String aichatterChatEventUri;

    @Value("${slack.aichatter-chat.interactive-uri}")
    private String aichatterChatInteractiveUri;

    @Value("${slack.docsbot.event-uri}")
    private String docsbotEventUri;

    @Value("${slack.docsbot.interactive-uri}")
    private String docsbotInteractiveUri;

    public SlackAppUriDTO getAichatterSlackAppUri() {
        return new SlackAppUriDTO(aichatterChatEventUri, aichatterChatInteractiveUri, redirectUri);
    }

    public SlackAppUriDTO getDocsbotSlackAppUri() {
        return new SlackAppUriDTO(docsbotEventUri, docsbotInteractiveUri, redirectUri);
    }
}
