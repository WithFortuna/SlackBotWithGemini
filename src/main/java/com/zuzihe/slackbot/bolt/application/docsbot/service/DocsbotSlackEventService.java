package com.zuzihe.slackbot.bolt.application.docsbot.service;

import aichatter.bolt.application.common.dto.DocsbotSlackSendMessageContext;
import aichatter.bolt.application.common.dto.SlackChatRoomCreateDTO;
import aichatter.bolt.application.common.dto.SlackChatRoomInfo;
import aichatter.bolt.application.common.dto.SlackChatRoomSearchDTO;
import aichatter.bolt.application.common.service.chatroom.SlackChatRoomService;
import aichatter.bolt.application.common.service.install.SlackWorkspaceInstallationService;
import aichatter.bolt.application.common.service.user.SlackWorkspaceUserService;
import aichatter.bolt.application.docsbot.DocsbotSlackEventType;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.domain.SlackChatRoom;
import aichatter.bolt.domain.SlackWorkspaceUser;
import aichatter.bolt.infra.SlackBoltClient;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.bolt.repository.SlackWorkspaceUserRepository;
import aichatter.bolt.util.SlackInfoTokenProvider;
import aichatter.bolt.util.SlackUtils;
import aichatter.dto.chat.ChatMessageDTO;
import aichatter.dto.chat.ChatRoomDTO;
import aichatter.dto.chat.FileUploadDTO;
import aichatter.entity.chat.util.FileTypeUtil;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import aichatter.service.chat.ChatService;
import aichatter.service.file.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.methods.request.views.ViewsPublishRequest;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.*;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.context.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;
import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;

@Service
@RequiredArgsConstructor
public class DocsbotSlackEventService {
    @Value("${slack.connect-slack-account-url}")
    private String connectSlackAccountURL;
    private final SlackWorkspaceUserRepository slackWorkspaceUserRepository;
    private final SlackBoltClient slackBoltClient;
    private final ChatService chatService;
    private final FileService fileService;
    private final SlackWorkspaceUserService slackWorkspaceUserService;
    private final SlackInfoTokenProvider slackInfoTokenProvider;
    private final SlackChatRoomService slackChatRoomService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlackWorkspaceInstallationService slackWorkspaceInstallationService;
    private final SlackAppRegistrationRepository appRegistrationRepository;
    private final SlackUtils slackUtils;

    private static final String FILTER_WARNING_MESSAGE = "금지어 필터링 진행 중";
    private static final String END_EVENT_PREFIX = "{\"success\":true,\"status\":\"end\"";
    private static final String TEMP_CHATROOM_TITLE = "제목 자동 생성이 되기 전 상태인 Slack 채팅방";
    private static final Integer MAXIMUM_NUMBER_OF_SUPPORTED_FILE = 1;


    @Async("slackAsyncExecutor")
    public void handleDirectMessage(MessageEvent ev, String teamId) {
        String text = ev.getText();
        String slackUserId = ev.getUser();
        String channel = ev.getChannel();
        String threadTs = getThreadTs(ev);
        DocsbotSlackSendMessageContext messageCtx = null;
        if (notDirectMessage(ev) || isEmptyMessage(text)) {
            return;
        }

        try {
            String botToken = getBotToken(getDocsbotAppRegistrationId(), teamId);
            messageCtx = DocsbotSlackSendMessageContext.forMessage(channel, threadTs, DocsbotSlackEventType.MESSAGE, botToken);
            logger.info("[Bolt] DM 수신 - Channel: {}, User: {}, Text: {}", messageCtx.getChannel(), slackUserId, text);

            // "답변 생성 중" 메시지 전송
            slackBoltClient.setThinkingStatus(messageCtx.getBotToken(), messageCtx.getChannel(), messageCtx.getThreadTs());

            // Slack 사용자 정보로 시스템 계정 조회
            Optional<SlackWorkspaceUser> workspaceUserOpt = getWorkspaceUser(teamId, slackUserId);
            if (workspaceUserOpt.isEmpty()) {
                sendWarningMessageToNotConnectedUser(teamId, messageCtx, slackUserId);
            } else {
                getChatRoomAndSendMessage(workspaceUserOpt.get(), messageCtx, text);
            }

        } catch (Exception e) {
            logger.error("[Bolt] DM 처리 중 오류", e);
            if (messageCtx != null) {
                slackBoltClient.sendMessageWithThread(messageCtx.getBotToken(), messageCtx.getChannel(), "처리 중 오류가 발생했습니다.", messageCtx.getThreadTs());
            }
        }
    }

    private static boolean notDirectMessage(MessageEvent ev) {
        return !("im".equals(ev.getChannelType()) && ev.getBotId() == null);
    }

    private static String getThreadTs(MessageEvent ev) {
        return ev.getThreadTs() != null ? ev.getThreadTs() : ev.getTs();
    }

    private String getBotToken(Long slackAppRegistrationId, String teamId) {
        Optional<String> botTokenOpt = slackWorkspaceInstallationService.getBotToken(teamId, slackAppRegistrationId);
        if (botTokenOpt.isEmpty()) {
            logger.error("[Bolt] 등록되지 않은 워크스페이스이기에 봇토큰을 찾을 수 없습니다 - teamId: {}", teamId);
            throw new CustomException(ErrorCode.SLACK_WORKSPACE_NOT_INSTALLED);
        }
        return botTokenOpt.get();
    }

    private Long getDocsbotAppRegistrationId() {
        return slackUtils.getDocsbotAppRegistrationId();
    }

    private void getChatRoomAndSendMessage(SlackWorkspaceUser slackWorkspaceUser,
                                           DocsbotSlackSendMessageContext messageCtx,
                                           String text) {
        String chatRoomId = findOrCreateChatRoom(
                slackWorkspaceUser.getSlackAppRegistrationId(),
                slackWorkspaceUser.getTeamId(),
                messageCtx,
                slackWorkspaceUser.getDomainId(),
                slackWorkspaceUser.getAccountId(),
                slackWorkspaceUser.getId());

        ChatMessageDTO chatMessage = ChatMessageDTO.builder()      // TODO: ChatMessage 항목 일부를 이렇게 하드코딩 해도 되는지 확인 필요
                .chatRoom_id(chatRoomId)
                .llmModel_id("aichatter")
                .messageData(text)
                .messageType("question")
                .parentChatMsg_id("0")
                .useResearch(false)
                .useWebSearch(false)
                .domain_id(slackWorkspaceUser.getDomainId())
                .build();

        getLLMResponseAndSendToSlack(messageCtx, chatMessage, slackWorkspaceUser.getDomainId(), slackWorkspaceUser.getId());
    }

    private void getLLMResponseAndSendToSlack(DocsbotSlackSendMessageContext initialMessageCtx,
                                              ChatMessageDTO chatMessage,
                                              Long domainId,
                                              Long accountId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AtomicBoolean filterNoticeSent = new AtomicBoolean(false);
        AtomicReference<DocsbotSlackSendMessageContext> contextRef = new AtomicReference<>(initialMessageCtx);

        chatService
                .validateAuthenticationOnlyAndSendStreamChatMessage(chatMessage, domainId, accountId)
                .contextWrite(Context.of("authentication", authentication))
                .doFinally(signal -> SecurityContextHolder.clearContext())
                .subscribe(
                        chunk -> processStreamChunk(chunk, contextRef, filterNoticeSent),
                        error -> handleStreamError(error, contextRef)
                );
    }

    private void processStreamChunk(
            Object chunk,
            AtomicReference<DocsbotSlackSendMessageContext> contextRef,
            AtomicBoolean filterNoticeSent
    ) {
        if (!(chunk instanceof String chunkString)) {
            return;
        }

        try {
            // 금지어 필터링 안내 청크 전송
            if (handleFilterWarningChunk(chunkString, contextRef, filterNoticeSent)) {
                return;
            }

            // 완료 이벤트 청크 전송
            if (handleEndChunk(chunkString, contextRef.get())) {
                return;
            }

            // 기타 이벤트는 무시 (start/map/일반 토큰 등)
        } catch (Exception e) {
            logger.error("[Bolt] 스트림 청크 처리 중 오류 - EventType: {}", contextRef.get().getSlackEventType(), e);
        }
    }

    private boolean handleFilterWarningChunk(String chunk,
                                             AtomicReference<DocsbotSlackSendMessageContext> contextRef,
                                             AtomicBoolean filterNoticeSent) {
        if (chunk.contains(FILTER_WARNING_MESSAGE) && filterNoticeSent.compareAndSet(false, true)) {
            String messageTs = sendSlackMessageAndGetTimeStamp(contextRef.get(), convertMarkdownToMrkdwn(chunk));

            updateContextRefWithMessageTs(contextRef, messageTs);

            return true;
        }
        return false;
    }

    private void updateContextRefWithMessageTs(AtomicReference<DocsbotSlackSendMessageContext> contextRef, String messageTs) {
        DocsbotSlackSendMessageContext currentCtx = contextRef.get();

        if(messageTs != null){
            DocsbotSlackSendMessageContext newMessageCtx = DocsbotSlackSendMessageContext.forMessageUpdate(currentCtx.getChannel(), currentCtx.getThreadTs(), messageTs, currentCtx.getSlackEventType(), currentCtx.getBotToken());
            contextRef.set(newMessageCtx);
        }
    }


    // 완료 이벤트 처리
    private boolean handleEndChunk(
            String chunk,
            DocsbotSlackSendMessageContext messageCtx
    ) throws Exception {
        if (!chunk.startsWith(END_EVENT_PREFIX)) {
            return false;
        }

        Map<?, ?> responseMap = objectMapper.readValue(chunk, Map.class);
        String completedMessage = extractCompletedMessage(responseMap);

        logger.info("[Bolt] 완료 메시지 수신 - EventType: {}, MessageLength: {}",
                messageCtx.getSlackEventType(), completedMessage.length());

        String formattedMessage = convertMarkdownToMrkdwn(completedMessage);

        if (messageCtx.getOriginalMessageTs() == null) {
            sendSlackMessageWithEventLogging(messageCtx, formattedMessage);
        } else {
            updateSlackMessage(messageCtx, formattedMessage);
        }

        return true;
    }

    // 완료 메시지 추출
    private String extractCompletedMessage(Map<?, ?> responseMap) {
        Object messageData = responseMap.get("messageData");
        if (messageData == null) {
            logger.warn("[Bolt] messageData가 null입니다. 빈 문자열 반환");
            return "";
        }
        return String.valueOf(messageData);
    }

    // 에러 처리
    private void handleStreamError(
            Throwable error,
            AtomicReference<DocsbotSlackSendMessageContext> contextRef
    ) {
        DocsbotSlackSendMessageContext messageCtx = contextRef.get();
        logger.error("[Bolt] ChatService 스트림 처리 실패 - EventType: {}", messageCtx.getSlackEventType(), error);
        String errorMessage = getErrorMessage(error);
        sendSlackMessageWithEventLogging(messageCtx, errorMessage);
    }

    private String sendSlackMessageAndGetTimeStamp(DocsbotSlackSendMessageContext messageCtx, String message) {
        try {
            return slackBoltClient.sendMessageWithThread(
                    messageCtx.getBotToken(),
                    messageCtx.getChannel(),
                    message,
                    messageCtx.getThreadTs()
            ).get();
        } catch (Exception e) {
            logger.error("[Bolt] Slack 메시지 전송 실패 - Channel: {}",
                    messageCtx.getChannel(), e);
            return null;
        }
    }

    // Slack 메시지 전송 (기본)
    private void sendSlackMessage(DocsbotSlackSendMessageContext messageCtx, String message) {
        try {
            slackBoltClient.sendMessageWithThread(
                    messageCtx.getBotToken(),
                    messageCtx.getChannel(),
                    message,
                    messageCtx.getThreadTs()
            );
        } catch (Exception e) {
            logger.error("[Bolt] Slack 메시지 전송 실패 - Channel: {}",
                    messageCtx.getChannel(), e);
        }
    }

    // Slack 메시지 전송 (이벤트 로깅 포함)
    private void sendSlackMessageWithEventLogging(DocsbotSlackSendMessageContext messageCtx, String message) {
        sendSlackMessage(messageCtx, message);
        logger.info("[Bolt] 메시지 전송 완료 - EventType: {}, Channel: {}",
                messageCtx.getSlackEventType(), messageCtx.getChannel());
    }

    // Slack 메시지 전송 (기본)
    private void updateSlackMessage(DocsbotSlackSendMessageContext messageCtx, String message) {
        try {
            slackBoltClient.updateMessageWithResponse(
                    messageCtx.getBotToken(),
                    messageCtx.getChannel(),
                    messageCtx.getOriginalMessageTs(),
                    message
            );
        } catch (Exception e) {
            logger.error("[Bolt] Slack 메시지 전송 실패 - Channel: {}",
                    messageCtx.getChannel(), e);
        }
    }

    private void sendWarningMessageToNotConnectedUser(String teamId,
                                                      DocsbotSlackSendMessageContext messageCtx,
                                                      String slackUserId) {
        slackBoltClient.sendMessageWithThread(messageCtx.getBotToken(), messageCtx.getChannel(), "AI채터 계정 연동이 필요합니다.", messageCtx.getThreadTs());
        logger.warn("[Bolt] DM - 연동되지 않은 사용자 - TeamId: {}, SlackUserId: {}", teamId, slackUserId);
    }

    private Optional<SlackWorkspaceUser> getWorkspaceUser(String teamId, String slackUserId) {
        String appId = appRegistrationRepository.findBySlackAppType(SlackAppType.AICHATTER_CHAT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getAppId();
        return slackWorkspaceUserRepository.findWorkspaceUserOnMultipleSlackApps(appId, teamId, slackUserId);
    }

    /**
     * Slack 스레드에 매핑된 SlackChatRoom을 조회하거나 새로 생성합니다.
     */
    private String findOrCreateChatRoom(Long slackAppRegistrationId,
                                        String teamId,
                                        DocsbotSlackSendMessageContext messageCtx,
                                        Long domainId,
                                        Long accountId,
                                        Long slackWorkspaceUserId) {

        String channel = messageCtx.getChannel();
        String threadTs = messageCtx.getThreadTs();

        SlackChatRoomSearchDTO searchDTO = new SlackChatRoomSearchDTO(slackAppRegistrationId, teamId, channel, threadTs, slackWorkspaceUserId);
        Optional<SlackChatRoom> slackChatRoomOpt = slackChatRoomService.findSlackChatRoom(searchDTO);

        if (slackChatRoomOpt.isPresent()) {
            String chatRoomId = slackChatRoomOpt.get().getChatRoomId();

            // slackchatroomrhk chatroom 제목 비교
            String aichatterChatRoomTitle = chatService.getChatRoom(domainId, chatRoomId).getChatRoomTitle();
            updateSlackChatRoomTitleIfNotMatched(aichatterChatRoomTitle, slackChatRoomOpt.get(), messageCtx.getSlackEventType());

            logger.info("[Bolt] 기존 SlackChatRoom 매핑 사용 - ChatRoomId: {}", chatRoomId);
            return chatRoomId;
        }

        logger.info("[Bolt] 새 ChatRoom 생성 - TeamId: {}, Channel: {}, ThreadTs: {}", teamId, channel, threadTs);
        String chatRoomId = chatService.createChatroom(
                ChatRoomDTO.builder()
                        .chatRoomTitle(TEMP_CHATROOM_TITLE)
                        .chatRoomType("normal")
                        .llmModel_id("aichatter")   // TODO: 사용자 권한에 따라 llm model 사용가능여부 처리 필요
                        .build(),
                domainId,
                accountId
        ).chatRoomId();

        slackChatRoomService.createSlackChatRoom(new SlackChatRoomCreateDTO(
                slackAppRegistrationId,
                teamId,
                channel,
                threadTs,
                slackWorkspaceUserId,
                chatRoomId,
                TEMP_CHATROOM_TITLE,
                domainId,
                accountId)
        );

        return chatRoomId;
    }

    private void updateSlackChatRoomTitleIfNotMatched(String aichatterChatRoomTitle, SlackChatRoom slackChatRoom, DocsbotSlackEventType slackEventType) {
        if (!aichatterChatRoomTitle.equals(slackChatRoom.getChatRoomTitle())) {
            slackChatRoom.updateChatRoomTitle(aichatterChatRoomTitle);
            slackWorkspaceInstallationService.getBotToken(slackChatRoom.getTeamId(), getDocsbotAppRegistrationId())
                    .ifPresent(token -> slackBoltClient.updateChatRoomTitle(token,
                            new SlackChatRoomInfo(slackChatRoom.getChannel(), slackChatRoom.getThreadTs()), aichatterChatRoomTitle));
        }
    }

    // mrkdwn은 슬랙 전용 문법
    private String convertMarkdownToMrkdwn(String text) {
        return text
                .replaceAll("## ", "*")
                .replaceAll("\\*\\*(.*?)\\*\\*", "*$1*")
                .replaceAll("(?m)^- ", "• ")
                .replaceAll("(?m)^\\d+\\. ", "• ");
    }

    @Async("slackAsyncExecutor")
    public void handleAppHomeOpened(AppHomeOpenedEvent event, String teamId) {
        String appId = appRegistrationRepository.findBySlackAppType(SlackAppType.AICHATTER_CHAT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getAppId();

        String slackUserId = event.getUser();
        logger.info("[Bolt] slack 사용자 홈화면 접근 teamId: {}, userId: {}", teamId, slackUserId);
        // Reuse existing WebClient blocks for now
        View view = Views.view(v ->
                v.type("home")
                        .blocks(isWorkspaceUserConnectedToAichatter(appId, teamId, slackUserId) ? getLinkedBlocks() : getUnlinkedBlocks(appId, teamId, slackUserId))
        );

        var req = ViewsPublishRequest.builder()
                .userId(slackUserId)
                .view(view)
                .build();
        String botToken = getBotToken(getDocsbotAppRegistrationId(), teamId);

        slackBoltClient.publishAppHome(botToken, req);
    }

    private boolean isWorkspaceUserConnectedToAichatter(String appId, String teamId, String slackUserId) {
        return slackWorkspaceUserService.isConnectedToAichatterAccount(appId, teamId, slackUserId);
    }

    private List<LayoutBlock> getLinkedBlocks() {

        return asBlocks(
                section(s -> s.text(markdownText("👋 *안녕하세요, aichatter입니다.*"))),
                divider(),
                section(s -> s.text(markdownText("*나의 문서봇*"))),
                divider(),
                section(s -> s
                        .text(markdownText("*아이채터 정보봇*\n최근 대화한 날짜 · *1일 전*"))
                        .accessory(button(b -> b.text(plainText("채팅")).value("open_docbot_apispec").actionId("open_docbot_apispec")))
                ),
                section(s -> s
                        .text(markdownText("*영업지원 문서봇*\n최근 대화한 날짜 · *2025-08-31 09:15*"))
                        .accessory(button(b -> b.text(plainText("채팅")).value("open_docbot_sales").actionId("open_docbot_sales")))
                )
        );
    }

    private List<LayoutBlock> getUnlinkedBlocks(String appId, String teamId, String slackUserId) {
        String token = slackInfoTokenProvider.generate(appId, teamId, slackUserId);
        String loginUrl = connectSlackAccountURL + "?slackInfoToken=" + token;

        return asBlocks(
                section(s -> s.text(markdownText("*aichatter를 슬랙에서 사용하려면 먼저 계정을 연동해주세요.*"))),
                divider(),
                actions(a -> a.elements(asElements(
                        button(b -> b.text(plainText("🔗 aichatter 로그인하기"))
                                .url(loginUrl)
                                .value("login_btn"))
                )))
        );
    }

    @Async("slackAsyncExecutor")
    public void handleAssistantThread(AssistantThreadStartedEvent event, String teamId) {
        String channelId = event.getAssistantThread().getChannelId();
        String threadTs = event.getAssistantThread().getThreadTs();
        String botToken = getBotToken(getDocsbotAppRegistrationId(), teamId);

        slackBoltClient.sendWelcomeMessage(botToken, channelId, threadTs);
    }

    @Async("slackAsyncExecutor")
    public void handleAppMention(AppMentionEvent event, String teamId) {
        String slackUserId = event.getUser();
        String threadTs = event.getThreadTs() != null ? event.getThreadTs() : event.getTs();
        String channel = event.getChannel();
        String text = event.getText();
        if (isEmptyMessage(text)) {
            return;
        }

        DocsbotSlackSendMessageContext messageCtx = null;
        try {
            String botToken = getBotToken(getDocsbotAppRegistrationId(), teamId);
            messageCtx = DocsbotSlackSendMessageContext.forMessage(channel, threadTs, DocsbotSlackEventType.APP_MENTION, botToken);
            logger.info("[Bolt] DM 수신 - Channel: {}, User: {}, Text: {}", messageCtx.getChannel(), slackUserId, text);

            // "답변 생성 중" 메시지 전송
            slackBoltClient.setThinkingStatus(messageCtx.getBotToken(), messageCtx.getChannel(), messageCtx.getThreadTs());

            Optional<SlackWorkspaceUser> workspaceUserOpt = getWorkspaceUser(teamId, slackUserId);
            if (workspaceUserOpt.isEmpty()) {
                sendWarningMessageToNotConnectedUser(teamId, messageCtx, slackUserId);
            } else {
                getChatRoomAndSendMessage(workspaceUserOpt.get(), messageCtx, text);
            }
        } catch (Exception e) {
            logger.error("[Bolt] App Mention 처리 중 오류", e);
            if (messageCtx != null) {
                slackBoltClient.sendMessageWithThread(messageCtx.getBotToken(), messageCtx.getChannel(), "처리 중 오류가 발생했습니다." + e.getMessage(), messageCtx.getThreadTs());
            }
        }
    }

    private static boolean isEmptyMessage(String textMessage) {
        return textMessage == null || textMessage.isBlank();
    }

    private String getErrorMessage(Throwable error) {
        String message = error.getMessage();
        if (message != null) {
            if (message.contains("금지어")) {
                return "사용할 수 없는 단어가 포함되어 있습니다.";
            } else if (message.contains("인증")) {
                return "인증 오류가 발생했습니다.";
            }
        }
        return "일시적인 오류가 발생했습니다.";
    }

    @Async("slackAsyncExecutor")
    public void handleMessageFileShare(MessageFileShareEvent ev, String teamId) {
        // 사용자가 업로드한 파일 다운로드
        // 파일 & 수신된 메시지 프롬프트 -> aichatter 도메인 서비스 로직을 따르도록 호출
        // sendMultiMediaChatMessage:같이 온 프롬프트 보내기 , uploadChatPromptAnswerFile: 사용자 업로드 파일

        String channel = ev.getChannel();
        String threadTs = ev.getThreadTs() != null ? ev.getThreadTs() : ev.getTs();
        String slackUserId = ev.getUser();
        String text = ev.getText() == null ? "" : ev.getText();

        try {
            // 기본 상태 메시지 표시
            // teamId 확보: 파일 내 user_team 값 사용
            List<com.slack.api.model.File> files = ev.getFiles();

            String botToken = getBotToken(getDocsbotAppRegistrationId(), teamId);
            DocsbotSlackSendMessageContext messageCtx = DocsbotSlackSendMessageContext.forMessage(channel, threadTs, DocsbotSlackEventType.MESSAGE, botToken);
            slackBoltClient.setThinkingStatus(botToken, messageCtx.getChannel(), messageCtx.getThreadTs());

            // 파일 유효성 체크
            if (files == null || files.isEmpty()) {
                slackBoltClient.sendMessageWithThread(botToken, messageCtx.getChannel(), "첨부된 파일이 전달되지 않았습니다.", messageCtx.getThreadTs());
                return;
            }

            Optional<SlackWorkspaceUser> workspaceUserOpt = getWorkspaceUser(teamId, slackUserId);
            if (workspaceUserOpt.isEmpty()) {
                sendWarningMessageToNotConnectedUser(teamId, messageCtx, slackUserId);
                return;
            }

            SlackWorkspaceUser slackWorkspaceUser = workspaceUserOpt.get();

            // 스레드 매핑된 ChatRoom 조회/생성
            String chatRoomId = findOrCreateChatRoom(
                    getDocsbotAppRegistrationId(),
                    slackWorkspaceUser.getTeamId(),
                    messageCtx,
                    slackWorkspaceUser.getDomainId(),
                    slackWorkspaceUser.getAccountId(),
                    slackWorkspaceUser.getId());

            // 파일 업로드 처리
            List<String> uploadedFileIds = new ArrayList<>();
            if (files.size() > MAXIMUM_NUMBER_OF_SUPPORTED_FILE) {
                slackBoltClient.sendMessageWithThread(botToken, messageCtx.getChannel(), "한번에 업로드 가능한 파일의 개수는 1개입니다.", messageCtx.getThreadTs());
                return;
            }

            for (com.slack.api.model.File f : files) {
                try {
                    String downloadUrl = f.getUrlPrivateDownload();
                    byte[] data = downloadSlackPrivateFile(botToken, downloadUrl);
                    if (data == null || data.length == 0) {
                        logger.warn("[Bolt] 파일 다운로드 실패 또는 빈 내용 - FileId: {}", f.getId());
                        continue;
                    }

                    String originalName = f.getName() != null ? f.getName() : ("file_" + f.getId());
                    String mimeType = f.getMimetype() != null ? f.getMimetype() : "application/octet-stream";
                    MultipartFile multipartFile = new MockMultipartFile(
                            "chatPromptFile",
                            originalName,
                            mimeType,
                            data
                    );
                    FileUploadDTO fileUploadDTO = createFileUploadDTO(chatRoomId, originalName);

                    var uploadResult = fileService.uploadChatPromptAnswerFile(
                            fileUploadDTO,
                            multipartFile,
                            slackWorkspaceUser.getDomainId(),
                            slackWorkspaceUser.getAccountId()
                    );
                    boolean aiStatus = Boolean.parseBoolean(String.valueOf(uploadResult.get("aiStatus")));
                    if (!aiStatus) {
                        slackBoltClient.sendMessageWithThread(botToken, messageCtx.getChannel(), "파일 업로드가 보안 정책에 의해 차단되었습니다.", messageCtx.getThreadTs());
                        return;
                    }

                    String fileId = String.valueOf(uploadResult.get("chatPromptAnswerFiles_id"));
                    uploadedFileIds.add(fileId);

                } catch (Exception ex) {
                    logger.error("[Bolt] 파일 업로드 처리 중 오류", ex);
                }
            }

            if (uploadedFileIds.isEmpty()) {
                slackBoltClient.sendMessageWithThread(botToken, messageCtx.getChannel(), "파일 업로드에 실패했습니다.", messageCtx.getThreadTs());
                return;
            }

            // 업로드가 성공한 경우, 멀티미디어 채팅 메시지 전송
            // TODO: 파일 업로드와 함꼐 전송되는 messageDTO의 포맷을 확인할 필요가 있음
            ChatMessageDTO chatMessage = ChatMessageDTO.builder()
                    .chatPromptAnswerFiles_id(uploadedFileIds)
                    .fileName(files.get(0).getName())
                    .fileSize(Long.valueOf(files.get(0).getSize()))
                    .chatRoom_id(chatRoomId)
                    .llmModel_id("aichatter")
                    .messageData(text)
                    .messageType("multiMedia")
                    .parentChatMsg_id("0")
                    .useResearch(false)
                    .useWebSearch(false)
                    .build();

            getMediaLLMResponseAndSendToSlack(messageCtx, chatMessage, slackWorkspaceUser.getDomainId(), slackWorkspaceUser.getId());
        } catch (Exception e) {
            logger.error("[Bolt] File Share 처리 중 오류", e);
            List<com.slack.api.model.File> files = ev.getFiles();
            if (files != null && !files.isEmpty()) {
                String teamId2 = files.get(0).getUserTeam();
                slackWorkspaceInstallationService.getBotToken(teamId2, getDocsbotAppRegistrationId())
                        .ifPresent(token -> slackBoltClient.sendMessageWithThread(token, channel, "처리 중 오류가 발생했습니다.", threadTs));
            }
        }
    }

    @NotNull
    private static FileUploadDTO createFileUploadDTO(String chatRoomId, String originalName) {
        FileUploadDTO fileUploadDTO = new FileUploadDTO();
        fileUploadDTO.setChatRoomId(chatRoomId);
        fileUploadDTO.setActionType("prompt");
        fileUploadDTO.setLlmModel_id("aichatter");

        String fileExt = FilenameUtils.getExtension(originalName);
        fileUploadDTO.setFileType(FileTypeUtil.getFileType(fileExt));
        return fileUploadDTO;
    }

    private byte[] downloadSlackPrivateFile(String botToken, String initialUrl) {
        try {
            // 일부 url_private_download는 302로 서명된 S3 URL로 리다이렉트됩니다.
            // 1) Slack에 Authorization 헤더로 요청
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

            if (initialUrl == null || initialUrl.isBlank()) {
                return null;
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(initialUrl))
                    .header("Authorization", "Bearer " + botToken)
                    .header("Accept", "*/*")
                    .GET()
                    .build();

            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            int status = resp.statusCode();
            if (status >= 200 && status < 300) {
                return resp.body();
            }

            // 2) 3xx면 Location 따라가되, 보통 외부 서명 URL이므로 Authorization 헤더 없이 재요청
            if (status >= 300 && status < 400) {
                var locationOpt = resp.headers().firstValue("location");
                if (locationOpt.isPresent()) {
                    String redirectUrl = locationOpt.get();
                    HttpClient redirectingClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
                    HttpRequest redirectedReq = HttpRequest.newBuilder()
                            .uri(URI.create(redirectUrl))
                            .header("Accept", "*/*")
                            .GET()
                            .build();
                    HttpResponse<byte[]> redirectedResp = redirectingClient.send(redirectedReq, HttpResponse.BodyHandlers.ofByteArray());
                    if (redirectedResp.statusCode() >= 200 && redirectedResp.statusCode() < 300) {
                        return redirectedResp.body();
                    } else {
                        logger.error("[Bolt] Slack 파일 리다이렉트 다운로드 실패 - status: {}, url: {}", redirectedResp.statusCode(), redirectUrl);
                    }
                }
            }

            logger.error("[Bolt] Slack 파일 다운로드 실패 - status: {}, url: {}", status, initialUrl);
            return null;
        } catch (Exception e) {
            logger.error("[Bolt] Slack 파일 다운로드 중 오류 - url: {}", initialUrl, e);
            return null;
        }
    }


    private void getMediaLLMResponseAndSendToSlack(DocsbotSlackSendMessageContext initialMessageCtx,
                                                   ChatMessageDTO chatMessage,
                                                   Long domainId,
                                                   Long accountId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AtomicBoolean filterNoticeSent = new AtomicBoolean(false);
        AtomicReference<DocsbotSlackSendMessageContext> contextRef = new AtomicReference<>(initialMessageCtx);

        chatService
                .sendMultiMediaChatMessage(chatMessage, domainId, accountId)
                .contextWrite(Context.of("authentication", authentication))
                .doFinally(signal -> SecurityContextHolder.clearContext())
                .subscribe(
                        chunk -> processStreamChunk(chunk, contextRef, filterNoticeSent),
                        error -> handleStreamError(error, contextRef)
                );
    }
}
