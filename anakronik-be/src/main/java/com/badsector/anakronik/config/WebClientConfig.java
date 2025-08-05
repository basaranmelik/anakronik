package com.badsector.anakronik.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${RAG_SERVICE_BASE_URL}")
    private String ragServiceBaseUrl;

    @Bean
    @Qualifier("ragWebClient")
    public WebClient ragWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 50000)
                .responseTimeout(Duration.ofSeconds(30000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(ragServiceBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}