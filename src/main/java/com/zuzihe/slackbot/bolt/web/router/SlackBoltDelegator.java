package com.zuzihe.slackbot.bolt.web.router;

import aichatter.bolt.domain.SlackAppRegistration;
import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServletAdapter;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

public class SlackBoltDelegator {

    /// 하단은 SlackAppServlet의 코드를 복붙한 것 -> app.run()에게 실제 타입별 핸들링은 위임한다.
    public static void delegateSlackRequest(HttpServletRequest request, HttpServletResponse response, App app, SlackAppRegistration appRegistration) throws IOException {
        SlackAppServletAdapter adapter = new SlackAppServletAdapter(app.config());
        Request slackReq = adapter.buildSlackRequest(request);
        if (slackReq != null) {
            try {
                Response slackResp = app.run(slackReq);
                logger.info("[SlackEventRouter] app.run 실행됨 \n clientid: {}", appRegistration.getClientId());
                logger.info("[SlackEventRouter] outgoing Slack response\n" +
                                "status: {}\n" +
                                "contentType: {}\n" +
                                "body: {}",
                        slackResp.getStatusCode(),
                        slackResp.getContentType(),
                        slackResp.getBody());

                adapter.writeResponse(response, slackResp);
            } catch (Exception e) {
                logger.error("Failed to handle a request - {}", e.getMessage(), e);
                response.setStatus(500);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Something is wrong\"}");
            }
        } else {
            adapter.writeResponse(response, Response.builder().statusCode(400).body("Invalid Request").build());
        }
    }
}
