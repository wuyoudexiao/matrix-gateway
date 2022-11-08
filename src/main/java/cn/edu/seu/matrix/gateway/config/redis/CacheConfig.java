package cn.edu.seu.matrix.gateway.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;


@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Autowired
    private redisConfig redisConfig;
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 配置序列化（解决乱码的问题）,过期时间600秒
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisConfig.stringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(redisConfig.jackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                // 可以给每个cacheName不同的RedisCacheConfiguration  设置不同的过期时间
                //.withCacheConfiguration("Users",config.entryTtl(Duration.ofSeconds(100)))
                .transactionAware()
                .build();
        return cacheManager;
    }
}
