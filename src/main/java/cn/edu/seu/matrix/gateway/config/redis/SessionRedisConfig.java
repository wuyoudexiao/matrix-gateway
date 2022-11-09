package cn.edu.seu.matrix.gateway.config.redis;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;


@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 1800,redisNamespace = "gateway")
public class SessionRedisConfig {
}

