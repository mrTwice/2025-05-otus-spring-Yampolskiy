package ru.otus.hw.migration.common;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.lang.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public final class StepUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepUtils.class);

    private StepUtils() {
    }

    public static ChunkListener jpaEntityManagerClearListener(EntityManager em) {
        return new ChunkListener() {
            @Override
            public void afterChunk(ChunkContext context) {
                try {
                    em.clear();
                } catch (Exception ignored) {
                    LOGGER.warn("Исключение проигнорировано %s", ignored);
                }
            }
        };
    }


    public static ChunkListener jpaEntityManagerClearEveryNChunks(EntityManager em, long n) {
        AtomicLong counter = new AtomicLong(0);
        return new ChunkListener() {
            @Override
            public void afterChunk(ChunkContext context) {
                long c = counter.incrementAndGet();
                if (c % Math.max(1, n) == 0) {
                    try {
                        em.clear();
                    } catch (Exception ignored) {
                        LOGGER.warn("Исключение проигнорировано %s", ignored);
                    }
                }
            }
        };
    }

    public static <T> ItemReadListener<T> loggingReadListener() {
        return new ItemReadListener<>() {
            @Override
            public void beforeRead() {

            }

            @Override
            public void afterRead(T item) {

            }

            @Override
            public void onReadError(Exception ex) {
                org.slf4j.LoggerFactory.getLogger("batch.read").error("Read error", ex);
            }
        };
    }

    public static <T> ItemWriteListener<T> loggingWriteListener() {
        return new ItemWriteListener<>() {
            @Override
            public void beforeWrite(org.springframework.batch.item.Chunk<? extends T> items) {

            }

            @Override
            public void afterWrite(org.springframework.batch.item.Chunk<? extends T> items) {

            }

            @Override
            public void onWriteError(Exception exception, org.springframework.batch.item.Chunk<? extends T> items) {
                org.slf4j.LoggerFactory.getLogger("batch.write")
                        .error("Write error for chunk size {}: {}", items.size(), exception.toString(), exception);
            }
        };
    }

    public static <T, S> SkipListener<T, S> loggingSkipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInRead(Throwable t) {
                LOGGER.warn("Skip in read: {}", t.toString(), t);
            }

            @Override
            public void onSkipInWrite(@Nullable S item, Throwable t) {
               LOGGER.warn("Skip in write. Item={}, cause={}", item, t.toString(), t);
            }

            @Override
            public void onSkipInProcess(@Nullable T item, Throwable t) {
                LOGGER.warn("Skip in process. Item={}, cause={}", item, t.toString(), t);
            }
        };
    }

    public static StepExecutionListener timingStepListener(String stepName) {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                stepExecution.getExecutionContext().putLong(stepName + ".startedAt", System.currentTimeMillis());
            }

            @Override
            public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                long start = stepExecution
                        .getExecutionContext()
                        .getLong(stepName + ".startedAt", System.currentTimeMillis());
                long ms = System.currentTimeMillis() - start;
                org.slf4j.LoggerFactory.getLogger("batch.timing")
                        .info("Step '{}' finished in {} ms. Read={}, Write={}, Filtered={}",
                                stepName, ms,
                                stepExecution.getReadCount(),
                                stepExecution.getWriteCount(),
                                stepExecution.getFilterCount());
                return stepExecution.getExitStatus();
            }
        };
    }
}
