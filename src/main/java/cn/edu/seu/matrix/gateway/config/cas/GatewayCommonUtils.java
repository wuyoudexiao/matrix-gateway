package cn.edu.seu.matrix.gateway.config.cas;

import org.jasig.cas.client.util.URIBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class GatewayCommonUtils {

    //从request中 获取参数
    public static String safeGetParameter(ServerHttpRequest request, String parameter) {
        if(request.getQueryParams().get(parameter)!=null){
            return request.getQueryParams().getFirst(parameter);
        }
        return null;
    }





    //将访问的url进行编码后返回
    public static String constructServiceUrl(ServerHttpRequest request,  boolean encode,boolean ticketValidate) {

            //如果是验证ticket拼接url的话
            if(ticketValidate){
                String url=request.getURI().toString();
                if(url.contains("?")){
                    int index=url.indexOf("?");
                    String urlWithoutParams=url.substring(0,index+1);
                    StringBuilder stringBuilder=new StringBuilder(urlWithoutParams);
                    MultiValueMap<String,String> params=request.getQueryParams();
                    Set<String> paramsSet=params.keySet();
                    Iterator<String> paramsIterator=paramsSet.iterator();
                    while (paramsIterator.hasNext()){
                        String key=paramsIterator.next();
                        if(!"ticket".equals(key)){
                            stringBuilder.append(key).append("=").append(params.getFirst(key)).append("&");
                        }

                    }
                    return stringBuilder.substring(0,stringBuilder.length()-1);
                }else {
                    return url;
                }
            }
            //如果不是验证ticketurl
        return encode ? URLEncoder.encode(request.getURI().toString()) : request.getURI().toString();
    }

    //获取客户端部署地址，并加入host做判断逻辑处理
    protected static String findMatchingServerName(ServerHttpRequest request, String serverName) {
        String[] serverNames = serverName.split(" ");
        if (serverNames.length != 0 && serverNames.length != 1) {
            String host = request.getHeaders().getFirst("Host");
            String xHost = request.getHeaders().getFirst("X-Forwarded-Host");
            String comparisonHost;
            if (xHost != null && host == "localhost") {
                comparisonHost = xHost;
            } else {
                comparisonHost = host;
            }

            if (comparisonHost == null) {
                return serverName;
            } else {
                String[] var6 = serverNames;
                int var7 = serverNames.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    String server = var6[var8];
                    String lowerCaseServer = server.toLowerCase();
                    if (lowerCaseServer.contains(comparisonHost)) {
                        return server;
                    }
                }

                return serverNames[0];
            }
        } else {
            return serverName;
        }
    }


    //检查url时候包含端口
    private static boolean serverNameContainsPort(boolean containsScheme, String serverName) {
        if (!containsScheme && serverName.contains(":")) {
            return true;
        } else {
            int schemeIndex = serverName.indexOf(":");
            int portIndex = serverName.lastIndexOf(":");
            return schemeIndex != portIndex;
        }
    }


    public static String constructRedirectUrl(String casServerLoginUrl, String serviceParameterName, String serviceUrl) {
        return casServerLoginUrl + (casServerLoginUrl.contains("?") ? "&" : "?") + serviceParameterName + "=" + serviceUrl ;
    }





    private static boolean requestIsOnStandardPort(ServerHttpRequest request) {
        int serverPort = request.getURI().getPort();
        return serverPort == 80 || serverPort == 443;
    }


    //Url编码
    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException(var2);
        }
    }

    //重定向方法
    public static Mono<Void> redirect(ServerWebExchange exchange, String urlToRedirectTo){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location", urlToRedirectTo);
        return exchange.getResponse().setComplete();
    }

}
