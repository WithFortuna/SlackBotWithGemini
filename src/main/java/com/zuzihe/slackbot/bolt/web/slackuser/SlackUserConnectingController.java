package com.zuzihe.slackbot.bolt.web.slackuser;

import aichatter.bolt.application.common.service.user.SlackWorkspaceUserService;
import aichatter.bolt.web.dto.SlackWorkspaceUserRequestDTO;
import aichatter.global.common.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class SlackUserConnectingController {
    private final SlackWorkspaceUserService slackWorkspaceUserService;
    private final CommonService commonService;

    @PostMapping("/slack/login")
    public void connectSlackAccountToAichatter(@RequestBody SlackWorkspaceUserRequestDTO slackWorkspaceUserRequestDTO,
                                               HttpServletRequest request) {

        slackWorkspaceUserService.connectWorkspaceUserToAichatter(slackWorkspaceUserRequestDTO, commonService.getRequestAddress(request.getServerName()));
    }

    // TODO: 원래는 없어야할 엔드포인트. 프론트가 없으므로 임시로 생성
    @GetMapping("/slack/connect")
    public ResponseEntity<Void> renderConnectAccountPage(@RequestParam String slackInfoToken) {
        String redirectUrl = "/slack-login.html?slackInfoToken=" + slackInfoToken;
        return ResponseEntity.status(302)
                .location(URI.create(redirectUrl))
                .build();
    }

}
