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

    //应用部署地址
    public String serviceUrl;

    //CAS 服务路径
    public String casContextPath="/cas";

    //应用部署路径
    public String clientContextPath;

    //CAS登录地址
    public String loginUrl="/login";

    //登出地址
    public String logoutUrl="/logout";


    //白名单的正则表达式的值
    public String whiteUrl="^.*(/logout/?)$";

    //白名单鉴权模式，现在只支持正则模式
    public String ignoreUrlPatternType="REGEX";

    //cookie保存登录信息方式默认本地存储
    public String cookieHolderPattern="com.liu.MapCookieHolder";

    //cookie 的key
    public String authKey="my_authKey";

    //服务端缓存多长时间单位毫秒
    public String millisBetweenCleanUps="3600000";

}
