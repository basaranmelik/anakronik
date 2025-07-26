package com.badsector.anakronik.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RagRestTemplateConfig {

    @Bean
    @Qualifier("ragServiceClient")
    public RestTemplate ragServiceClient() {
        // RestTemplate'i timeout'lar veya diğer ayarlar ile yapılandırabilirsiniz
        return new RestTemplate();
    }

    // Eğer emotionClient da tanımlı değilse, onu da burada tanımlayabilirsiniz
    @Bean
    @Qualifier("ragClient")
    public RestTemplate ragClient() {
        return new RestTemplate();
    }
}