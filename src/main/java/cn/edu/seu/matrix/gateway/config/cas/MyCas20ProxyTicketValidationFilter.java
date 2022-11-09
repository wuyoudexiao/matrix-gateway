package cn.edu.seu.matrix.gateway.config.cas;

import org.jasig.cas.client.Protocol;
import org.jasig.cas.client.validation.*;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class MyCas20ProxyTicketValidationFilter implements GlobalFilter, Ordered {
    protected TicketValidator ticketValidator;

    protected Protocol protocol;

    protected CasClientConfig casClientConfig;

    public MyCas20ProxyTicketValidationFilter(CasClientConfig casClientConfig) {
        this.casClientConfig=casClientConfig;
        this.protocol=Protocol.CAS2;
        this.ticketValidator = new Cas20ServiceTicketValidator(casClientConfig.casServiceUrl);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request=exchange.getRequest();
        //从参数中获取ticket参数
        String ticket=GatewayCommonUtils.safeGetParameter(request, this.protocol.getArtifactParameterName());
        //如果ticket参数为空则跳过ticket验证器，进入到认证拦截器，由认证拦截器去跳转到登录页面进行登录
        if(StringUtils.isEmpty(ticket)){
            return chain.filter(exchange);
        }else {
            try {
                Assertion assertion=ticketValidator.validate(ticket, constructServiceUrl(request));
                return exchange.getSession().flatMap(
                        webSession -> {
                            webSession.getAttributes().put("_const_cas_assertion_", assertion);
                            return GatewayCommonUtils.redirect(exchange, constructServiceUrl(request));
                        });
            } catch (TicketValidationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //将访问的地址编码进行URLEncode后返回
    protected final String constructServiceUrl(ServerHttpRequest request) {
        return GatewayCommonUtils.constructServiceUrl(request,true,true);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
