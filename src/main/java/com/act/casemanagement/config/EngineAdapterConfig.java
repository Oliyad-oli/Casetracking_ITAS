package com.act.casemanagement.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient beans for each external integration — mirrors bs-filing-core-server
 * EngineAdapterConfig. Named beans let adapters inject by qualifier.
 */
@Configuration
public class EngineAdapterConfig {

    @Value("${integrations.notification.base-url}")
    private String notificationBaseUrl;

    @Value("${integrations.notification.connect-timeout-ms:5000}")
    private int notificationConnectMs;

    @Value("${integrations.notification.read-timeout-ms:10000}")
    private int notificationReadMs;

    @Value("${integrations.dms.base-url}")
    private String dmsBaseUrl;

    @Value("${integrations.dms.connect-timeout-ms:5000}")
    private int dmsConnectMs;

    @Value("${integrations.dms.read-timeout-ms:15000}")
    private int dmsReadMs;

    @Value("${integrations.filing-service.base-url}")
    private String filingBaseUrl;

    @Value("${integrations.filing-service.connect-timeout-ms:5000}")
    private int filingConnectMs;

    @Value("${integrations.filing-service.read-timeout-ms:10000}")
    private int filingReadMs;

    @Bean("notificationWebClient")
    public WebClient notificationWebClient() {
        return buildClient(notificationBaseUrl, notificationConnectMs, notificationReadMs);
    }

    @Bean("dmsWebClient")
    public WebClient dmsWebClient() {
        return buildClient(dmsBaseUrl, dmsConnectMs, dmsReadMs);
    }

    @Bean("filingWebClient")
    public WebClient filingWebClient() {
        return buildClient(filingBaseUrl, filingConnectMs, filingReadMs);
    }

    private WebClient buildClient(String baseUrl, int connectMs, int readMs) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectMs)
                .responseTimeout(Duration.ofMillis(readMs))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readMs, TimeUnit.MILLISECONDS)));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
