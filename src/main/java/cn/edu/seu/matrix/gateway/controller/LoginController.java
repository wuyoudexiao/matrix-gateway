package cn.edu.seu.matrix.gateway.controller;

import cn.edu.seu.matrix.gateway.config.cas.CasClientConfig;
import cn.edu.seu.matrix.gateway.config.cas.GatewayCommonUtils;
import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api")
public class LoginController {

    protected TicketValidator ticketValidator;

    protected Protocol protocol;

    protected CasClientConfig casClientConfig;

    public LoginController(CasClientConfig casClientConfig) {
        this.casClientConfig=casClientConfig;
        this.protocol=Protocol.CAS2;
        this.ticketValidator = new Cas20ServiceTicketValidator(casClientConfig.casServiceUrl);
    }

    @GetMapping(value = "/call-back")
    public Mono<Void> callBack(
            @RequestParam @NotBlank String url,
            ServerWebExchange exchange
    ) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        String ticket= GatewayCommonUtils.safeGetParameter(request, this.protocol.getArtifactParameterName());
        if(StringUtils.isEmpty(ticket)){
            response.setStatusCode(HttpStatus.valueOf(302));
            return response.setComplete();
        }else {
            String urlToRedirectTo = casClientConfig.getServerName()+ url;
            try {
                Assertion assertion=ticketValidator.validate(ticket, GatewayCommonUtils.constructServiceUrl(request,true,true));
                exchange.getAttributes().put("_const_cas_assertion_", assertion);
                return exchange.getSession().flatMap(
                        webSession -> {
                            webSession.getAttributes().put("_const_cas_assertion_", assertion);
                            response.setStatusCode(HttpStatus.SEE_OTHER);
                            response.getHeaders().set("Location", urlToRedirectTo);
                            return response.setComplete();
                        });
            } catch (TicketValidationException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    @GetMapping(value = "/login")
    public Mono<Void> login(
            ServerHttpResponse response,
            @RequestParam @NotBlank String url
    ) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host("127.0.0.1:8080").path("/api/call-back").queryParam("url",url).build().encode();
        String urlToRedirectTo = casClientConfig.getCasServerLoginUrl()+"?service=" + uriComponents.toUriString();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location", urlToRedirectTo);
        return response.setComplete();
    }
}
