package com.zuzihe.slackbot.slack.bolt.handler.interactive;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.request.RequestType;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.zuzihe.slackbot.slack.bolt.SlackBoltService;
import com.zuzihe.slackbot.slack.bolt.handler.event.SlackBoltHandlerRegistrar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockActionHandlerRegistrar implements SlackBoltHandlerRegistrar {
    private final SlackBoltService slackBoltService;

    @Override
    public void register(App app) {
        // 공통 로깅 미들웨어
        app.use((req, resp, chain) -> {
            if (req.getRequestType() == RequestType.BlockAction) {
                BlockActionPayload payload = ((BlockActionRequest) req).getPayload();
                String actionId = payload.getActions().get(0).getActionId();
                String userId = payload.getUser().getId();
                log.info("[BlockAction] 버튼 클릭 감지 - ActionId: {}, UserId: {}", actionId, userId);
            }
            return chain.next(req);
        });

        // 특정 채팅 버튼 클릭 처리
        app.blockAction("open_docbot_apispec", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            log.info("[BlockAction] API 정보봇 채팅 버튼 클릭 - UserId: {}", userId);
            slackBoltService.openNewChatWithUser(userId, "roomA");
            return ctx.ack();
        });

        app.blockAction("open_docbot_sales", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            log.info("[BlockAction] 영업지원봇 채팅 버튼 클릭 - UserId: {}", userId);
            slackBoltService.openNewChatWithUser(userId, "roomB");
            return ctx.ack();
        });
    }
}