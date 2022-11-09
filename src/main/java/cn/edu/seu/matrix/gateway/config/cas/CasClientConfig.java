package cn.edu.seu.matrix.gateway.config.cas;

import lombok.Data;
import lombok.Value;
import org.jasig.cas.client.Protocol;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cas")
public class CasClientConfig {

    //CAS服务端地址
    public String casServiceUrl;

    //白名单的正则表达式的值
    public String whiteUrl="^.*(/logout/?)$";

    //白名单鉴权模式，现在只支持正则模式
    public String ignoreUrlPatternType="REGEX";

    public String serverName;

    public String casServerLoginUrl;

}
