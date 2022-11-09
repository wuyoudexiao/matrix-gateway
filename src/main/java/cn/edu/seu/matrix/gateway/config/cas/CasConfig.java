package cn.edu.seu.matrix.gateway.config.cas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CasClientConfig.class)
@AutoConfigureAfter(CasClientConfig.class )
public class CasConfig {

    @Autowired
    private CasClientConfig casClientConfig;

    @Bean
    public AuthenticationGatewayFilter authenticationGatewayFilter(){
        return new AuthenticationGatewayFilter(casClientConfig);
    }

    @Bean
    public MyCas20ProxyTicketValidationFilter myCas20ProxyTicketValidationFilter(){
        return new MyCas20ProxyTicketValidationFilter(casClientConfig);
    }

}
