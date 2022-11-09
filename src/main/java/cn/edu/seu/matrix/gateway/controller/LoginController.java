package cn.edu.seu.matrix.gateway.controller;

import cn.edu.seu.matrix.gateway.config.cas.CasClientConfig;
import cn.edu.seu.matrix.gateway.config.cas.GatewayCommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/cas")
@Validated
public class LoginController {

    @Autowired
    private CasClientConfig casClientConfig;


    @GetMapping(value = "call-back")
    public Mono<Void> callBack(
            @RequestParam @NotBlank String url,
            ServerWebExchange exchange
    ) throws IOException {
        url = new String(Base64.getUrlDecoder().decode(url.replace("*","=").getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8);
        return GatewayCommonUtils.redirect(exchange,casClientConfig.getServerName()+ URLDecoder.decode(url,StandardCharsets.UTF_8.toString()));
    }

    @GetMapping(value = "test")
    public Object test(
            ServerWebExchange exchange
    ) {
        return exchange.getSession().toString();
    }

    @GetMapping(value = "/login")
    public Mono<Void> login(
            ServerWebExchange exchange,
            @RequestParam @NotBlank String url
    ) throws IOException {
        String param =URLEncoder.encode(casClientConfig.getServerName()
                        + "/cas/call-back?url="
                        + Base64.getUrlEncoder().encodeToString(url.getBytes(StandardCharsets.UTF_8)).replace("=","*"),
                StandardCharsets.UTF_8.toString());
        String location = casClientConfig.getCasServerLoginUrl()+"?service=" + param;
        return GatewayCommonUtils.redirect(exchange,location);
    }
}
