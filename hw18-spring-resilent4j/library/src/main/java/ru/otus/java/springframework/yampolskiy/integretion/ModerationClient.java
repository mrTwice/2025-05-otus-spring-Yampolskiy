package ru.otus.java.springframework.yampolskiy.integretion;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import ru.otus.java.springframework.yampolskiy.integretion.dto.ModerationRequest;
import ru.otus.java.springframework.yampolskiy.integretion.dto.ModerationResponse;
import ru.otus.java.springframework.yampolskiy.integretion.exception.ModerationClientException;
import ru.otus.java.springframework.yampolskiy.integretion.exception.ModerationServiceCallException;
import ru.otus.java.springframework.yampolskiy.integretion.exception.ModerationUnavailableException;

@Component
@RequiredArgsConstructor
public class ModerationClient {

    private static final String API_MODERATION_PATH = "/api/v1/moderation/check";

    private final RestClient moderationRestClient;

    @CircuitBreaker(name = "moderation", fallbackMethod = "fallback")
    @Retry(name = "moderation")
    @Bulkhead(name = "moderation")
    public ModerationResponse check(String text) {

        try {
            return moderateComment(text);

        } catch (ModerationClientException e) {
            throw e;

        } catch (ModerationServiceCallException e) {
            throw new ModerationUnavailableException("Moderation is unavailable", e);

        } catch (ResourceAccessException e) {
            throw new ModerationUnavailableException("Moderation is unavailable (timeout/network)", e);

        } catch (Exception e) {
            throw new ModerationUnavailableException("Moderation is unavailable", e);
        }
    }

    @Nullable
    private ModerationResponse moderateComment(String text) {
        return moderationRestClient.post()
                .uri(API_MODERATION_PATH)
                .body(new ModerationRequest(text))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ModerationClientException("Moderation 4xx: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ModerationServiceCallException("Moderation 5xx: " + res.getStatusCode());
                })
                .body(ModerationResponse.class);
    }

    public ModerationResponse fallback(String text, Throwable t) {
        throw new ModerationUnavailableException("Moderation is unavailable", t);
    }
}





