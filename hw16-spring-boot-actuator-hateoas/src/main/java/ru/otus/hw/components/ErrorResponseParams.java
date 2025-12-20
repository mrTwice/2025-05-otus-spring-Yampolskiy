package ru.otus.hw.components;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ErrorResponseParams(
        HttpStatus status,
        String viewNameOrNull,
        String title,
        String detail,
        Map<String, ?> props
) {
    public static Builder builder(HttpStatus status) {
        return new Builder(status);
    }

    public static final class Builder {

        private final HttpStatus status;

        private String viewNameOrNull;

        private String title;

        private String detail;

        private final Map<String, Object> props = new LinkedHashMap<>();

        public Builder(HttpStatus status) {
            this.status = Objects.requireNonNull(status, "status must not be null");
        }

        public Builder view(String view) {
            this.viewNameOrNull = view;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder props(Map<String, ?> props) {
            this.props.clear();
            if (props != null && !props.isEmpty()) {
                props.forEach(this::addProp);
            }
            return this;
        }

        public Builder addProp(String key, Object value) {
            if (key != null && value != null) {
                this.props.put(key, value);
            }
            return this;
        }

        public Builder addProps(Map<String, ?> more) {
            if (more != null && !more.isEmpty()) {
                more.forEach(this::addProp);
            }
            return this;
        }

        public ErrorResponseParams build() {
            Map<String, Object> safeProps = this.props.isEmpty()
                    ? Map.of()
                    : Map.copyOf(this.props);
            return new ErrorResponseParams(
                    status,
                    viewNameOrNull,
                    title,
                    detail,
                    safeProps
            );
        }
    }

    public static Builder badRequest() {
        return builder(HttpStatus.BAD_REQUEST).view("error/400");
    }

    public static Builder notFound() {
        return builder(HttpStatus.NOT_FOUND).view("error/404");
    }

    public static Builder internalServerError() {
        return builder(HttpStatus.INTERNAL_SERVER_ERROR).view("error/500");
    }
}
