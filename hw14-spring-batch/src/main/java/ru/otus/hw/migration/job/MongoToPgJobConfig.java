package ru.otus.hw.migration.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.otus.hw.domain.mongo.MongoAuthor;
import ru.otus.hw.domain.mongo.MongoBook;
import ru.otus.hw.domain.mongo.MongoComment;
import ru.otus.hw.domain.mongo.MongoGenre;
import ru.otus.hw.domain.pg.JpaAuthor;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.domain.pg.JpaComment;
import ru.otus.hw.domain.pg.JpaGenre;
import ru.otus.hw.migration.cache.IdMappingService;
import ru.otus.hw.migration.common.StepUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;

@Configuration
@RequiredArgsConstructor
public class MongoToPgJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager pgTxManager;

    private final IdMappingService idMappingService;

    private final ItemReader<MongoAuthor> authorsMongoReader;

    private final ItemReader<MongoGenre> genresMongoReader;

    private final ItemReader<MongoBook> booksMongoReader;

    private final ItemReader<MongoComment> commentsMongoReader;

    private final ItemProcessor<MongoAuthor, JpaAuthor> authorMongoToPgProcessor;

    private final ItemProcessor<MongoGenre, JpaGenre> genreMongoToPgProcessor;

    private final ItemProcessor<MongoBook, JpaBook> bookMongoToPgProcessor;

    private final ItemProcessor<MongoComment, JpaComment> commentMongoToPgProcessor;

    private final ItemWriter<JpaAuthor> authorPgUpsertWriter;

    private final ItemWriter<JpaGenre> genrePgUpsertWriter;

    private final ItemWriter<JpaBook> bookPgUpsertWriter;

    private final ItemWriter<JpaComment> commentPgUpsertWriter;

    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${migration.chunk-size:500}")
    private int chunkSize;

    @Value("${migration.split-threads:2}")
    private int splitThreads;

    @Bean
    public TaskExecutor migrationTaskExecutor2() {
        SimpleAsyncTaskExecutor ex = new SimpleAsyncTaskExecutor("batch-split-");
        ex.setConcurrencyLimit(splitThreads);
        return ex;
    }

    @Bean
    public Job mongoToPgJob() {
        Flow authorsFlow = new FlowBuilder<Flow>("mongo2pg-authorsFlow")
                .start(authorsMongoToPgStep())
                .build();
        Flow genresFlow = new FlowBuilder<Flow>("mongo2pg-genresFlow")
                .start(genresMongoToPgStep())
                .build();
        Flow splitFlow = new FlowBuilder<Flow>("mongo2pg-split-authors-genres")
                .split(migrationTaskExecutor2())
                .add(authorsFlow, genresFlow)
                .build();
        Flow fullFlow = new FlowBuilder<Flow>("mongo2pg-fullFlow")
                .start(truncatePgStep())
                .next(splitFlow)
                .next(booksMongoToPgStep())
                .next(commentsMongoToPgStep())
                .build();

        return new JobBuilder("mongoToPgJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fullFlow)
                .end()
                .build();
    }


    @Bean
    public Step truncatePgStep() {
        return new StepBuilder("truncatePgStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Object raw = chunkContext.getStepContext().getJobParameters().get("truncateBeforeLoad");
                    boolean truncate = raw != null && Boolean.parseBoolean(raw.toString());
                    if (!truncate) {
                        return RepeatStatus.FINISHED;
                    }

                    jdbcTemplate.update("DELETE FROM app.comments");
                    jdbcTemplate.update("DELETE FROM app.books_genres");
                    jdbcTemplate.update("DELETE FROM app.books");
                    jdbcTemplate.update("DELETE FROM app.genres");
                    jdbcTemplate.update("DELETE FROM app.authors");

                    return RepeatStatus.FINISHED;
                }, pgTxManager)
                .build();
    }

    @Bean
    public Step authorsMongoToPgStep() {
        return new StepBuilder("authorsMongoToPgStep", jobRepository)
                .<MongoAuthor, JpaAuthor>chunk(chunkSize, pgTxManager)
                .reader(authorsMongoReader)
                .processor(authorMongoToPgProcessor)
                .writer(authorPgUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("authorsMongoToPgStep"))
                .listener(jpaClearEachChunk())
                .stream(idMappingService)
                .build();
    }

    @Bean
    public Step genresMongoToPgStep() {
        return new StepBuilder("genresMongoToPgStep", jobRepository)
                .<MongoGenre, JpaGenre>chunk(chunkSize, pgTxManager)
                .reader(genresMongoReader)
                .processor(genreMongoToPgProcessor)
                .writer(genrePgUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("genresMongoToPgStep"))
                .listener(jpaClearEachChunk())
                .stream(idMappingService)
                .build();
    }

    @Bean
    public Step booksMongoToPgStep() {
        return new StepBuilder("booksMongoToPgStep", jobRepository)
                .<MongoBook, JpaBook>chunk(chunkSize, pgTxManager)
                .reader(booksMongoReader)
                .processor(bookMongoToPgProcessor)
                .writer(bookPgUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("booksMongoToPgStep"))
                .listener(jpaClearEachChunk())
                .stream(idMappingService)
                .build();
    }

    @Bean
    public Step commentsMongoToPgStep() {
        return new StepBuilder("commentsMongoToPgStep", jobRepository)
                .<MongoComment, JpaComment>chunk(chunkSize, pgTxManager)
                .reader(commentsMongoReader)
                .processor(commentMongoToPgProcessor)
                .writer(commentPgUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("commentsMongoToPgStep"))
                .listener(jpaClearEachChunk())
                .stream(idMappingService)
                .build();
    }

    private ChunkListener jpaClearEachChunk() {
        return StepUtils.jpaEntityManagerClearEveryNChunks(entityManager, 1);
    }
}
