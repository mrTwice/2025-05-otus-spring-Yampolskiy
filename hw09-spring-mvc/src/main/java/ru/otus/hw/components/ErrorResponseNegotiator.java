package ru.otus.hw.components;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Component
public class ErrorResponseNegotiator {

    private final ProblemDetailFactory pdf;

    public ErrorResponseNegotiator(ProblemDetailFactory pdf) {
        this.pdf = pdf;
    }

    public boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xrw = req.getHeader("X-Requested-With");
        return (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xrw);
    }

    public Object respond(HttpServletRequest req,
                          HttpStatus status,
                          String viewNameOrNull,
                          String title,
                          String detail,
                          Map<String, ?> props) {
        if (wantsJson(req)) {
            return ResponseEntity.status(status)
                    .body(pdf.create(status, title, detail, props));
        }
        String view = (viewNameOrNull != null) ? viewNameOrNull : ("error/" + status.value());
        ModelAndView mav = new ModelAndView(view);
        mav.setStatus(status);
        mav.addObject("message", detail);
        return mav;
    }
}