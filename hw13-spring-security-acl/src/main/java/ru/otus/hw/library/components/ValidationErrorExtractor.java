package ru.otus.hw.library.components;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ValidationErrorExtractor {
    public List<Map<String, String>> toList(BindingResult br) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fe : br.getFieldErrors()) {
            errors.add(Map.of(
                    "field", fe.getField(),
                    "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"
            ));
        }
        return errors;
    }
}