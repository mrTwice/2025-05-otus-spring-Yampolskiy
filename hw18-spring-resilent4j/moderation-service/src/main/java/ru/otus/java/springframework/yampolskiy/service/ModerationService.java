package ru.otus.java.springframework.yampolskiy.service;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.otus.java.springframework.yampolskiy.config.ModerationProperties;
import ru.otus.java.springframework.yampolskiy.dto.ModerationMode;
import ru.otus.java.springframework.yampolskiy.dto.ModerationRequest;
import ru.otus.java.springframework.yampolskiy.dto.ModerationResponse;
import ru.otus.java.springframework.yampolskiy.exception.ModerationInternalException;
import ru.otus.java.springframework.yampolskiy.exception.ModerationUnavailableException;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ModerationService {

    private final ModerationProperties props;

    public ModerationService(ModerationProperties props) {
        this.props = props;
    }

    public ModerationResponse check(ModerationRequest req, ModerationMode mode) {
        if (mode == null) {
            return probabilisticBehavior(req);
        }
        return strictModeBehavior(req, mode);
    }

    private ModerationResponse strictModeBehavior(ModerationRequest req, ModerationMode mode) {
        switch (mode) {
            case OK -> {
                return allowed();
            }
            case FORBIDDEN -> {
                return getForbbidenModerationResponse(req);
            }
            case ERROR_500 -> throw new ModerationInternalException("Forced internal error (X-Mode=ERROR_500)");
            case ERROR_503 -> throw new ModerationUnavailableException("Forced unavailable (X-Mode=ERROR_503)");
            case SLOW -> {
                return getSlowModerationResponse(req);
            }
            default -> throw new ModerationInternalException("Unknown mode: " + mode);
        }
    }

    private @NonNull ModerationResponse getSlowModerationResponse(ModerationRequest req) {
        sleep(props.getFailure().getSlowDelayMs());

        String forbidden = findForbidden(req.text());
        if (forbidden != null) {
            return forbidden(forbidden);
        }
        return allowed();
    }

    private @NonNull ModerationResponse getForbbidenModerationResponse(ModerationRequest req) {
        String found = findForbidden(req.text());
        if (found == null) {
            found = "spam";
        }
        return forbidden(found);
    }

    private ModerationResponse probabilisticBehavior(ModerationRequest req) {

        double rnd = ThreadLocalRandom.current().nextDouble();

        double errorP = props.getFailure().getErrorProbability();
        double slowP = props.getFailure().getSlowProbability();

        if (rnd < errorP) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                throw new ModerationUnavailableException("Random unavailable (probabilistic)");
            }
            throw new ModerationInternalException("Random internal error (probabilistic)");
        }

        if (rnd < errorP + slowP) {
            sleep(props.getFailure().getSlowDelayMs());
        }

        String forbidden = findForbidden(req.text());
        if (forbidden != null) {
            return forbidden(forbidden);
        }

        return allowed();
    }

    private String findForbidden(String text) {
        if (text == null) {
            return null;
        }
        String lower = text.toLowerCase();
        for (String w : props.getRules().getForbidden()) {
            if (w != null && !w.isBlank() && lower.contains(w.toLowerCase())) {
                return w;
            }
        }
        return null;
    }

    private ModerationResponse allowed() {
        return new ModerationResponse(true, null);
    }

    private ModerationResponse forbidden(String word) {
        return new ModerationResponse(false, "FORBIDDEN_WORD: " + word);
    }

    private void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModerationInternalException("Interrupted during artificial delay");
        }
    }
}

