package com.zuzihe.slackbot.bolt.infra;

import aichatter.bolt.application.common.dto.SlackChatRoomInfo;
import com.slack.api.Slack;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.assistant.threads.AssistantThreadsSetStatusRequest;
import com.slack.api.methods.request.assistant.threads.AssistantThreadsSetTitleRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.chat.ChatUpdateRequest;
import com.slack.api.methods.request.conversations.ConversationsOpenRequest;
import com.slack.api.methods.request.views.ViewsPublishRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;
import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackBoltClient {
    private AsyncMethodsClient clientAsync(String botToken) {
        return Slack.getInstance().methodsAsync(botToken);
    }

    public CompletableFuture<String> sendMessageWithThread(String botToken, String channel, String text, String threadTs) {
        var req = ChatPostMessageRequest.builder()
                .channel(channel)
                .threadTs(threadTs)
                .text(text)
                .build();

        return clientAsync(botToken).chatPostMessage(req)
                .thenApply(resp -> {
                    if (!resp.isOk()) {
                        logger.error("Bolt chat.postMessage failed: {}", resp.getError());
                        return null;
                    }
                    return resp.getTs();
                })
                .exceptionally(e -> {
                    logger.error("Bolt chat.postMessage error", e);
                    return null;
                });
    }

    public void publishAppHome(String botToken, ViewsPublishRequest req) {
        clientAsync(botToken).viewsPublish(req)
                .thenAccept(resp -> {
                    if (!resp.isOk()) {
                        logger.error("Bolt views.publish failed: {}", resp.getError());
                    }
                })
                .exceptionally(e -> {
                    logger.error("Bolt views.publish error", e);
                    return null;
                });
    }

    // 슬랙앱과 채팅방이 처음 생성됐을 때 메시지 전송
    public void sendWelcomeMessageWithButtons(String botToken, String channelId, String threadTs) {
        List<LayoutBlock> blocks = asBlocks(
                section(s -> s.text(markdownText("안녕하세요 \n저는 aichatter with bolt 입니다"))),
                section(s -> s.text(markdownText("\n아래는 예시 프롬프트입니다."))),
                actions(a -> a.elements(asElements(
                        button(b -> b.text(plainText("button 예시 텍스트: aichatter란?"))
                                .value("latest_trends")
                                .actionId("latest_trends")
                        ),
                        button(b -> b.text(plainText("문서봇을 만드는 방법이란"))
                                .value("b2b_social_media")
                                .actionId("b2b_social_media")
                        )
                ))),
                section(s -> s.text(markdownText("아래는 문서봇 목록입니다"))),
                actions(a -> a.elements(asElements(
                        button(b -> b.text(plainText(p -> p.text("문서봇1")))
                                .value("customer_feedback")
                                .actionId("customer_feedback")
                        ),
                        button(b -> b.text(plainText(p -> p.text("문서봇2")))
                                .value("product_brainstorm")
                                .actionId("product_brainstorm")
                        )
                ))),
                input(i -> i
                        .blockId("block-id")
                        .label(plainText("문서봇 목록"))
                        .element(
                                staticSelect(s -> s
                                        .options(asOptions(
                                                option(o -> o.text(plainText("문서봇1"))),
                                                option(o -> o.text(plainText("문서봇2")))
                                        ))
                                        .placeholder(plainText("원하는 문서봇을 선택해주세요")))
                        ))
        );

        try {
            ChatPostMessageResponse resp = clientAsync(botToken).chatPostMessage(r -> r
                    .channel(channelId)
                    .threadTs(threadTs)
                    .text("환영 메시지")
                    .blocks(blocks)
            ).get();
            if (!resp.isOk()) {
                logger.error("환영 메시지 전송 실패: {}", resp.getError());
            }
        } catch (Exception e) {
            logger.error("환영 메시지 전송 중 오류", e);
        }
    }

    // 슬랙앱과 채팅방이 처음 생성됐을 때 메시지 전송
    public void sendWelcomeMessage(String botToken, String channelId, String threadTs) {
        List<LayoutBlock> blocks = asBlocks(
                section(s -> s.text(markdownText("안녕하세요 *아이채터* 입니다. \n더 빠르고 효율적으로 업무를 도와드립니다.")))
        );

        try {
            ChatPostMessageResponse resp = clientAsync(botToken).chatPostMessage(r -> r
                    .channel(channelId)
                    .threadTs(threadTs)
                    .text("환영 메시지")
                    .blocks(blocks)
            ).get();
            if (!resp.isOk()) {
                logger.error("환영 메시지 전송 실패: {}", resp.getError());
            }
        } catch (Exception e) {
            logger.error("환영 메시지 전송 중 오류", e);
        }
    }

    // channel에 메시지 전송
    public void sendMessage(String botToken, String channel, String text) {
        var req = ChatPostMessageRequest.builder()
                .channel(channel)
                .text(text)
                .build();
        clientAsync(botToken).chatPostMessage(req)
                .thenAccept(resp -> {
                    if (!resp.isOk()) {
                        logger.error("Bolt chat.postMessage failed: {}", resp.getError());
                    }
                })
                .exceptionally(e -> {
                    logger.error("Bolt chat.postMessage error", e);
                    return null;
                });
    }

    // 버튼 클릭 후 대화 생성
    public void openConversationAndSendWelcomeMessageWithChatRoomId(String botToken, String userId, String chatRoomId) {
        var openRequest = ConversationsOpenRequest.builder()
                .users(List.of(userId))
                .build();

        clientAsync(botToken).conversationsOpen(openRequest)
                .thenAccept(response -> {
                    if (response.isOk()) {
                        String channelId = response.getChannel().getId();
                        logger.info("[Bolt] DM 채널 열기 성공 - ChannelId: {}", channelId);

                        // 2. 환영 메시지 전송
                        sendNewChatWelcomeMessage(botToken, channelId, chatRoomId);
                    } else {
                        logger.error("[Bolt] DM 채널 열기 실패: {}", response.getError());
                    }
                })
                .exceptionally(e -> {
                    logger.error("[Bolt] DM 채널 열기 중 오류", e);
                    return null;
                });
    }

    private void sendNewChatWelcomeMessage(String botToken, String channelId, String chatRoomId) {
        List<LayoutBlock> blocks = asBlocks(
                section(s -> s.text(markdownText("🎉 *새로운 채팅을 시작합니다!*"))),
                divider(),
                section(s->s.text(markdownText("현재 진행중인 채팅방: "+ chatRoomId))),
                section(s -> s.text(markdownText("안녕하세요! 저는 aichatter 봇입니다.\n무엇을 도와드릴까요?"))),
                section(s -> s.text(markdownText("*💡 예시 질문들:*\n• 오늘 날씨 어때?\n• 프로그래밍 질문하기\n• 창작 도움 요청하기")))
        );

        var req = ChatPostMessageRequest.builder()
                .channel(channelId)
                .text("새로운 채팅을 시작합니다!")
                .blocks(blocks)
                .build();

        clientAsync(botToken).chatPostMessage(req)
                .thenAccept(resp -> {
                    if (!resp.isOk()) {
                        logger.error("[Bolt] 새 채팅 환영 메시지 전송 실패: {}", resp.getError());
                    } else {
                        logger.info("[Bolt] 새 채팅 환영 메시지 전송 성공 - ChannelId: {}", channelId);
                    }
                })
                .exceptionally(e -> {
                    logger.error("[Bolt] 새 채팅 환영 메시지 전송 중 오류", e);
                    return null;
                });
    }

    public void setThinkingStatus(String botToken, String channel, String threadTs) {
        String thinkingText = "가(이) 답변을 생성하고 있습니다 \"\uD83D\uDCAD\"";

        AssistantThreadsSetStatusRequest req = AssistantThreadsSetStatusRequest.builder()
                .channelId(channel)
                .threadTs(threadTs)
                .status(thinkingText)
                .build();

        clientAsync(botToken).assistantThreadsSetStatus(req)
                .thenAccept(resp -> {
                    if (resp.isOk()) {
                        logger.info("[Bolt] aichatter 스켈레톤 UI 전송 성공 - Channel: {}", channel);
                    } else {
                        logger.error("[Bolt] aichatter 스켈레톤 UI 전송 성공: {}", resp.getError());
                    }
                })
                .exceptionally(e -> {
                    logger.error("[Bolt] 답변 생성 중 메시지 전송 실패: {}", e);
                    return null;
                });

    }

    // 기존 메시지를 실제 답변으로 업데이트
    public void updateMessageWithResponse(String botToken, String channel, String messageTs, String newMessage) {
        var req = ChatUpdateRequest.builder()
                .channel(channel)
                .ts(messageTs)
                .text(newMessage)
                .build();

        clientAsync(botToken).chatUpdate(req)
                .thenAccept(response -> {
                    if (response.isOk()) {
                        logger.info("[Bolt] 메시지 업데이트 성공 - Channel: {}, Ts: {}", channel, messageTs);
                    } else {
                        logger.error("[Bolt] 메시지 업데이트 실패: {}", response.getError());
                    }
                })
                .exceptionally(e -> {
                    logger.error("[Bolt] 메시지 업데이트 중 오류", e);
                    return null;
                });
    }

    public void updateChatRoomTitle(String botToken, SlackChatRoomInfo chatRoomInfo, String title) {
        var req = AssistantThreadsSetTitleRequest.builder()
                .channelId(chatRoomInfo.channel())
                .threadTs(chatRoomInfo.threadTs())
                .title(title)
                .build();

        clientAsync(botToken).assistantThreadsSetTitle(req)
                .thenAccept(response -> {
                    if (response.isOk()) {
                        logger.info("[Bolt] 채팅방 제목 업데이트 성공 - Channel: {}, threadTs: {}, title: {}", chatRoomInfo.channel(), chatRoomInfo.threadTs(), title);
                    } else {
                        logger.error("[Bolt] 채팅방 제목 업데이트 실패: {}", response.getError());
                    }
                })
                .exceptionally(e -> {
                    logger.error("[Bolt] 채팅방 제목 업데이트 중 오류", e);
                    return null;
                });
    }
}
