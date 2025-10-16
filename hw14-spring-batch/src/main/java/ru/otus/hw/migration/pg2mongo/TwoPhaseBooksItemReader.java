package ru.otus.hw.migration.pg2mongo;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.repo.pg.JpaBookRepository;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@RequiredArgsConstructor
public class TwoPhaseBooksItemReader implements ItemReader<JpaBook>, ItemStream {

    private static final String EC_PAGE_INDEX = "twoPhaseBooksReader.pageIndex";

    private final JpaBookRepository jpaBookRepository;

    private final int pageSize;

    private int pageIndex;

    private Deque<JpaBook> buffer;

    @Override
    public JpaBook read() {
        if (buffer == null || buffer.isEmpty()) {
            Page<Long> idPage = jpaBookRepository.findIdsPage(
                    PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "id"))
            );
            if (idPage.isEmpty()) {
                return null;
            }
            List<Long> ids = idPage.getContent();
            List<JpaBook> batch = jpaBookRepository.findByIdIn(ids);
            batch.sort((a, b) -> Long.compare(a.getId(), b.getId()));
            buffer = new ArrayDeque<>(batch);
            pageIndex++;
        }

        return buffer.pollFirst();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.pageIndex = executionContext.containsKey(EC_PAGE_INDEX)
                ? executionContext.getInt(EC_PAGE_INDEX)
                : 0;
        this.buffer = new ArrayDeque<>();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(EC_PAGE_INDEX, this.pageIndex);
    }

    @Override
    public void close() throws ItemStreamException {
        this.buffer = null;
    }
}
