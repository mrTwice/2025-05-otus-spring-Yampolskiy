package ru.otus.hw.dto;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> Mono<PageResponse<T>> from(Flux<T> pageFlux, Pageable pageable, Mono<Long> totalMono) {
        Mono<List<T>> contentMono = pageFlux.collectList();

        return Mono.zip(contentMono, totalMono.defaultIfEmpty(0L))
                .map(tuple -> {
                    List<T> content = tuple.getT1();
                    long total = tuple.getT2();
                    int size = pageable.getPageSize();
                    int pageNum = pageable.getPageNumber();
                    int totalPages = (size <= 0) ? 1 : (int) Math.ceil(total / (double) size);
                    boolean first = (pageNum == 0);
                    boolean last = (pageable.getOffset() + content.size()) >= total;

                    return new PageResponse<>(
                            content,
                            pageNum,
                            size,
                            total,
                            (total == 0) ? 0 : totalPages,
                            first,
                            (total == 0) ? true : last
                    );
                });
    }

    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isFirst(),
                p.isLast()
        );
    }
}

