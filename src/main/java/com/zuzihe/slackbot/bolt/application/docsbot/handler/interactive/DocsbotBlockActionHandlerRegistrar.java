package com.zuzihe.slackbot.bolt.application.docsbot.handler.interactive;

import aichatter.bolt.application.docsbot.service.DocsbotSlackInteractiveService;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.request.RequestType;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Component
@RequiredArgsConstructor
public class DocsbotBlockActionHandlerRegistrar implements DocsbotInteractiveHandlerRegistrar {
    private final DocsbotSlackInteractiveService interactiveService;

    @Override
    public void register(App app) {
        // 공통 로깅 미들웨어
        app.use((req, resp, chain) -> {
            if (req.getRequestType() == RequestType.BlockAction) {
                BlockActionPayload payload = ((BlockActionRequest) req).getPayload();
                String actionId = payload.getActions().get(0).getActionId();
                String userId = payload.getUser().getId();
                logger.info("[BlockAction] 버튼 클릭 감지 - ActionId: {}, UserId: {}", actionId, userId);
            }
            return chain.next(req);
        });

        // 특정 채팅 버튼 클릭 처리
        app.blockAction("open_docbot_apispec", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            String teamId = req.getPayload().getTeam() != null ? req.getPayload().getTeam().getId() : ctx.getTeamId();
            logger.info("[BlockAction] API 정보봇 채팅 버튼 클릭 - UserId: {}", userId);
            interactiveService.openNewChatWithUser(teamId, userId, "roomA");
            return ctx.ack();
        });

        app.blockAction("open_docbot_sales", (req, ctx) -> {
            String userId = req.getPayload().getUser().getId();
            String teamId = req.getPayload().getTeam() != null ? req.getPayload().getTeam().getId() : ctx.getTeamId();
            logger.info("[BlockAction] 영업지원봇 채팅 버튼 클릭 - UserId: {}", userId);
            interactiveService.openNewChatWithUser(teamId, userId, "roomB");
            return ctx.ack();
        });
    }
}
