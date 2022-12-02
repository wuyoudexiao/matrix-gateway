package cn.edu.seu.matrix.gateway.config.cas;

import org.jasig.cas.client.authentication.ContainsPatternUrlPatternMatcherStrategy;
import org.jasig.cas.client.authentication.ExactUrlPatternMatcherStrategy;
import org.jasig.cas.client.authentication.RegexUrlPatternMatcherStrategy;
import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

    //白名单鉴权器
    private UrlPatternMatcherStrategy ignoreUrlPatternMatcherStrategyClass;

    //白名单鉴权容器
    private static final Map<String, Class<? extends UrlPatternMatcherStrategy>> PATTERN_MATCHER_TYPES = new HashMap();

    //初始化白名单鉴权容器
    static {
        PATTERN_MATCHER_TYPES.put("CONTAINS", ContainsPatternUrlPatternMatcherStrategy.class);
        PATTERN_MATCHER_TYPES.put("REGEX", RegexUrlPatternMatcherStrategy.class);
        PATTERN_MATCHER_TYPES.put("EXACT", ExactUrlPatternMatcherStrategy.class);
    }


    public AuthenticationGatewayFilter(CasClientConfig casClientConfig) {
        //从容器中获取白名单验证器类型,默认正则方式
        Class<? extends UrlPatternMatcherStrategy> ignoreUrlPatternClass = PATTERN_MATCHER_TYPES.get(casClientConfig.getIgnoreUrlPatternType());
        //用反射新建白名单验证器类
        this.ignoreUrlPatternMatcherStrategyClass = ReflectUtils.newInstance(ignoreUrlPatternClass.getName(), new Object[0]);
        //如果鉴权器不为空
        if (this.ignoreUrlPatternMatcherStrategyClass != null) {
            this.ignoreUrlPatternMatcherStrategyClass.setPattern(casClientConfig.whiteUrl);
        }
    }

    private boolean isRequestUrlExcluded(ServerHttpRequest request) {
        if (this.ignoreUrlPatternMatcherStrategyClass == null) {
            return false;
        } else {
            StringBuilder urlBuffer = new StringBuilder(request.getURI().toString());
            if (request.getURI().getQuery() != null) {
                urlBuffer.append("?").append(request.getURI().getQuery());
            }

            String requestUri = urlBuffer.toString();
            return this.ignoreUrlPatternMatcherStrategyClass.matches(requestUri);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response=exchange.getResponse();

        //如果是白名单跳过拦截器
        if (this.isRequestUrlExcluded(request)) {
            return chain.filter(exchange);
        }
        //从已经登录的容器中获取登录信息
        return exchange.getSession().flatMap(
                webSession -> {
                    Assertion assertion = (Assertion) webSession.getAttributes().get("_const_cas_assertion_");
                    if (assertion != null) {
                        //把新的 exchange放回到过滤链
                        try {
                            Map<String, Object> attributes = assertion.getAttributes();
                            return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().header("UserId", "123123").header("UserName", URLEncoder.encode("吴星灿", "UTF-8")).build()).build());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            response.setStatusCode(HttpStatus.valueOf(302));
                            return response.setComplete();
                        }
                    } else {
                        //如果没有验证过ticket，说明还未登录过，报302
                        response.setStatusCode(HttpStatus.valueOf(302));
                        return response.setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return -99;
    }
}
