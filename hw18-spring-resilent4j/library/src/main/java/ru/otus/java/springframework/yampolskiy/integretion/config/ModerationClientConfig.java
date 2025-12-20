package ru.otus.java.springframework.yampolskiy.integretion.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ModerationClientConfig {

    private static final String X_MODE = "X-Mode";

    @Bean
    public RestClient moderationRestClient(
            RestClient.Builder builder,
            @Value("${moderation.base-url}") String baseUrl,
            @Value("${moderation.timeouts.connect}") Duration connectTimeout,
            @Value("${moderation.timeouts.read}") Duration readTimeout
    ) {

        ConnectionConfig connectionConfig = getConnectionConfig(connectTimeout);
        PoolingHttpClientConnectionManager connectionManager = getPoolingHttpClientConnectionManager(connectionConfig);
        RequestConfig requestConfig = getRequestConfig(readTimeout);
        CloseableHttpClient httpClient = getCloseableHttpClient(connectionManager, requestConfig);
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return buildRestClient(builder, baseUrl, requestFactory);
    }

    @NotNull
    private RestClient buildRestClient(
            RestClient.Builder builder,
            String baseUrl,
            HttpComponentsClientHttpRequestFactory requestFactory
    ) {
        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                    if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
                        String mode = sra.getRequest().getHeader(X_MODE);
                        if (mode != null && !mode.isBlank()) {
                            request.getHeaders().add(X_MODE, mode);
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    private CloseableHttpClient getCloseableHttpClient(
            PoolingHttpClientConnectionManager connectionManager,
            RequestConfig requestConfig
    ) {
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private RequestConfig getRequestConfig(Duration readTimeout) {
        return RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout.toMillis()))
                .build();
    }

    private PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager(ConnectionConfig connectionConfig) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();
    }

    private ConnectionConfig getConnectionConfig(Duration connectTimeout) {
        return ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout.toMillis()))
                .build();
    }
}
