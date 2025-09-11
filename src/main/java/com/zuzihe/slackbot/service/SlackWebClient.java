package com.zuzihe.slackbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SlackWebClient {

    @Value("${slack.botToken}")
    private String botToken;

    private final WebClient slackClient = WebClient.create("https://slack.com/api");

    // 일반 메시지 전송
    public void sendMessage(String channel, String text) {
        Map<String, Object> payload = Map.of(
                "channel", channel,
                "text", text
        );
        slackClient.post()
                .uri("/chat.postMessage")
                .header("Authorization", "Bearer " + botToken)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> log.info("Slack 메시지 전송 완료: {}", resp),
                        error -> log.error("Slack 메시지 전송 실패: {}", error.getMessage(), error)
                );
    }

    // 스레드 메시지 전송
    public void sendMessageWithThread(String channelId, String message, String threadTs) {
        Map<String, Object> block = Map.of(
                "type", "section",
                "text", Map.of(
                        "type", "mrkdwn",
                        "text", message   // 여기서 Slack이 지원하는 서식만 적용됨
                )
        );

        Map<String, Object> requestBody = Map.of(
                "channel", channelId,
                "thread_ts", threadTs,
                "blocks", List.of(block)
        );

        slackClient.post()
                .uri("/chat.postMessage")
                .header("Authorization", "Bearer " + botToken)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                        response -> log.info("스레드 메시지 전송 완료: {}", response),
                        error -> log.error("스레드 메시지 전송 실패", error)
                );
    }


    // 홈 탭 업데이트
    public void publishAppHome(String userId) {
        boolean isLinked = isAichatterLinked(userId); // 로그인 여부 판단

        Map<String, Object> view = Map.of(
                "type", "home",
                "blocks", isLinked ? getlinkedBlocks() : getUnlinkedBlocks(userId)
        );

        Map<String, Object> payload = Map.of(
                "user_id", userId,
                "view", view
        );

        slackClient.post()
                .uri("/views.publish")
                .header("Authorization", "Bearer " + botToken)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> log.info("홈 탭 전송 성공: {}", resp),
                        err -> log.error("홈 탭 전송 실패: {}", err.getMessage(), err)
                );
    }
    // 로그인된 사용자용 홈 탭
    private List<Map<String, Object>> getlinkedBlocks() {
        return List.of(
                section("👋 *안녕하세요, aichatter입니다.*"),
                divider(),
                section("*나의 문서봇*"),
                divider(),
                sectionWithButton("*apispec-bot · aichatter*\n최근 대화한 날짜 · *1일 전*",
                        button("열기", "open_docbot_apispec")),
                sectionWithButton("*영업지원 문서봇*\n최근 대화한 날짜 · *2025-07-31 09:15*",
                        button("열기", "open_docbot_sales"))
        );
    }

    // 로그인 안 된 사용자용 홈 탭
    private List<Map<String, Object>> getUnlinkedBlocks(String userId) {
        String loginUrl = "http://mcloudoc.aichatter.net:6500/sign-in?slack_user_id=" + userId;
        return List.of(
                section("* aichatter를 슬랙에서 사용하려면 먼저 계정을 연동해주세요.*"),
                section("""
                        aichatter를 연동하면 다음 기능을 사용할 수 있어요.

                        • `/aichatter` 명령어로 바로 질문
                        • 문서봇 선택 후 대화형 질의
                        • 질문 기록 자동 저장
                        • 사내 데이터 기반 답변
                        """),
                divider(),
                Map.of("type", "actions", "elements", List.of(
                        urlButton("🔗 aichatter 로그인하기", loginUrl)
                ))
        );
    }
    public void sendWelcomeMessageWithButtons(String channelId, String threadTs) {
        List<Map<String, Object>> blocks = List.of(
                section("안녕하세요! \n저는 aichatter입니다."),
                section("\n일반 채팅을 원하시면 바로 채팅을 보내주세요. \n 아래는 예시 프롬프트입니다."),

                Map.of("type", "actions", "elements", List.of(
                        button("aichatter에 대해 알려주세요!!!", "latest_trends"),
                        button("문서봇을 만드는 방법이 무엇인강요?", "b2b_social_media")
                )),

                section("문서봇 이용하고싶ㅇ므면 아래에서 문서봇을 선택하세요."),

                Map.of("type", "actions", "elements", List.of(
                        button("지혜의 문서봇", "customer_feedback"),
                        button("아이채터 정보봇ㅎ", "product_brainstorm")
                ))
        );

        Map<String, Object> requestBody = Map.of(
                "channel", channelId,
                "thread_ts", threadTs,
                "text", "환영 메시지",
                "blocks", blocks
        );

        slackClient.post()
                .uri("/chat.postMessage")
                .header("Authorization", "Bearer " + botToken)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                        response -> log.info("환영 메시지 전송 완료: {}", response),
                        error -> log.error("환영 메시지 전송 실패", error)
                );
    }

    private boolean isAichatterLinked(String slackUserId) {
        // TODO: DB 조회 실제 로직으로 대체
        return false;
    }

    ///**************************헬퍼메서드
    public static Map<String, Object> section(String markdownText) {
        return Map.of(
                "type", "section",
                "text", Map.of("type", "mrkdwn", "text", markdownText)
        );
    }

    // 구분선(divider)
    public static Map<String, Object> divider() {
        return Map.of("type", "divider");
    }

    // 버튼
    public static Map<String, Object> button(String text, String actionId) {
        return Map.of(
                "type", "button",
                "text", Map.of("type", "plain_text", "text", text),
                "action_id", actionId
        );
    }

    public static Map<String, Object> sectionWithButton(String markdownText, Map<String, Object> button) {
        return Map.of(
                "type", "section",
                "text", Map.of("type", "mrkdwn", "text", markdownText),
                "accessory", button
        );
    }

    public static Map<String, Object> urlButton(String text, String url) {
        return Map.of(
                "type", "button",
                "text", Map.of("type", "plain_text", "text", text),
                "url", url,
                "style", "primary"
        );
    }

}
