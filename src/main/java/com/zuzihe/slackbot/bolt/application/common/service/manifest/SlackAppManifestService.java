package com.zuzihe.slackbot.bolt.application.common.service.manifest;

import aichatter.bolt.application.common.dto.SlackAppUriDTO;
import aichatter.bolt.domain.SlackAppType;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Service
public class SlackAppManifestService {
    @Value("classpath:slack/manifest/aichatter_chat_manifest.json")
    private Resource aichatterTemplateJson;

    @Value("classpath:slack/manifest/aichatter_document_bot_manifest.json")
    private Resource docsbotTemplateJson;

    private final SlackAppUriUtils slackAppUriUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode createManifestByAppType(String baseUrl, SlackAppType slackAppType) throws IOException {
        if (notSupportHttps(baseUrl)) {
            throw new CustomException(ErrorCode.SLACK_CONFIG_URL_NEEDS_HTTPS);
        }

        String customizedManifest = getManifestAbout(slackAppType, baseUrl);

        // 파싱된 JSON 노드로 반환 (문자열 아님)
        return objectMapper.readTree(customizedManifest);
    }

    public String getManifestAbout(SlackAppType slackAppType, String baseUrl) throws IOException {
        switch (slackAppType) {
            case AICHATTER_CHAT -> {
                Resource manifestTemplateJson = aichatterTemplateJson;
                return getAichatterChatCustomizedManifest(baseUrl, manifestTemplateJson);
            }

            case DOCSBOT -> {
                Resource manifestTemplateJson = docsbotTemplateJson;
                return getDocsbotCustomizedManifest(baseUrl, manifestTemplateJson);
            }

            default -> {
                throw new CustomException(ErrorCode.SLACK_APP_TYPE_NOT_SUPPORTED);
            }
        }
    }

    @NotNull
    private String getAichatterChatCustomizedManifest(String baseUrl, Resource manifestTemplateJson) throws IOException {
        SlackAppUriDTO appUri = slackAppUriUtils.getAichatterSlackAppUri();
        String manifestTemplate = StreamUtils.copyToString(manifestTemplateJson.getInputStream(), StandardCharsets.UTF_8);

        String replacedManifestDomainName = replaceManifestDomainName(manifestTemplate, baseUrl);
        String replacedDefaultUriAndDomainName = replaceDefaultUri(replacedManifestDomainName, appUri);

        return replacedDefaultUriAndDomainName;
    }

    @NotNull
    private String getDocsbotCustomizedManifest(String baseUrl, Resource manifestTemplateJson) throws IOException {
        SlackAppUriDTO appUri = slackAppUriUtils.getDocsbotSlackAppUri();
        String manifestTemplate = StreamUtils.copyToString(manifestTemplateJson.getInputStream(), StandardCharsets.UTF_8);

        String replacedManifestDomainName = replaceManifestDomainName(manifestTemplate, baseUrl);
        String replacedDefaultUriAndDomainName = replaceDefaultUri(replacedManifestDomainName, appUri);

        return replacedDefaultUriAndDomainName;
    }

    @NotNull
    private String replaceManifestDomainName(String manifestTemplate, String baseUrl) {
        return manifestTemplate
                .replace("{PUT_YOUR_DOMAIN}", baseUrl);
    }

    private String replaceDefaultUri(String manifestTemplate,
                                     SlackAppUriDTO slackAppUriDTO) {
        return manifestTemplate.replace("{PUT_YOUR_EVENT_URI}", slackAppUriDTO.eventUri())
                .replace("{PUT_YOUR_INTERACTIVE_URI}", slackAppUriDTO.interactiveUri())
                .replace("{PUT_YOUR_OAUTH_REDIRECT_URI}", slackAppUriDTO.redirectUri());
    }


    private static boolean notSupportHttps(String baseUrl) {
        return baseUrl.indexOf("https") == -1;
    }
}
