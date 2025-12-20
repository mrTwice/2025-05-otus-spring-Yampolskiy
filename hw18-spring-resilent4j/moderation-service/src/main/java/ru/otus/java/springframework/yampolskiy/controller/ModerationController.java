package ru.otus.java.springframework.yampolskiy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.java.springframework.yampolskiy.component.ModerationModeResolver;
import ru.otus.java.springframework.yampolskiy.dto.ModerationMode;
import ru.otus.java.springframework.yampolskiy.dto.ModerationRequest;
import ru.otus.java.springframework.yampolskiy.dto.ModerationResponse;
import ru.otus.java.springframework.yampolskiy.service.ModerationService;

@RestController
@RequestMapping("/api/v1/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping("/check")
    public ModerationResponse check(
            @RequestBody ModerationRequest req,
            @RequestHeader(value = "X-Mode", required = false) String xMode,
            @RequestParam(value = "mode", required = false) String mode
    ) {
        if (req == null || req.text() == null || req.text().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text must not be blank");
        }

        ModerationMode resolvedMode;
        try {
            resolvedMode = ModerationModeResolver.resolve(xMode, mode);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return moderationService.check(req, resolvedMode);
    }
}

