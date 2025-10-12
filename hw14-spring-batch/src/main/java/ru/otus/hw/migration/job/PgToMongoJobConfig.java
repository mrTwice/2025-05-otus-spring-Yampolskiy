package ru.otus.hw.migration.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;

import ru.otus.hw.domain.pg.JpaAuthor;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.domain.pg.JpaComment;
import ru.otus.hw.domain.pg.JpaGenre;
import ru.otus.hw.domain.mongo.MongoAuthor;
import ru.otus.hw.domain.mongo.MongoBook;
import ru.otus.hw.domain.mongo.MongoComment;
import ru.otus.hw.domain.mongo.MongoGenre;
import ru.otus.hw.migration.cache.IdMappingService;
import ru.otus.hw.migration.common.StepUtils;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;

import org.springframework.batch.item.ItemProcessor;

import org.springframework.batch.item.ItemWriter;

@Configuration
@RequiredArgsConstructor
public class PgToMongoJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager pgTxManager;

    private final IdMappingService idMappingService;

    private final RepositoryItemReader<JpaAuthor> authorsPgReader;

    private final RepositoryItemReader<JpaGenre> genresPgReader;

    private final ItemReader<JpaBook> booksPgReader;

    private final JpaPagingItemReader<JpaComment> commentsPgReader;

    private final ItemProcessor<JpaAuthor, MongoAuthor> authorPgToMongoProcessor;

    private final ItemProcessor<JpaGenre, MongoGenre> genrePgToMongoProcessor;

    private final ItemProcessor<JpaBook, MongoBook> bookPgToMongoProcessor;

    private final ItemProcessor<JpaComment, MongoComment> commentPgToMongoProcessor;

    private final ItemWriter<MongoAuthor> authorMongoUpsertWriter;

    private final ItemWriter<MongoGenre> genreMongoUpsertWriter;

    private final ItemWriter<MongoBook> bookMongoUpsertWriter;

    private final ItemWriter<MongoComment> commentMongoUpsertWriter;

    private final MongoTemplate mongoTemplate;

    @Value("${migration.chunk-size:500}")
    private int chunkSize;

    @Value("${migration.split-threads:2}")
    private int splitThreads;

    @Bean
    public TaskExecutor migrationTaskExecutor() {
        SimpleAsyncTaskExecutor ex = new SimpleAsyncTaskExecutor("batch-split-");
        ex.setConcurrencyLimit(splitThreads);
        return ex;
    }

    @Bean
    public Job pgToMongoJob() {
        Flow authorsFlow = new FlowBuilder<Flow>("pg2mongo-authorsFlow")
                .start(authorsPgToMongoStep())
                .build();
        Flow genresFlow = new FlowBuilder<Flow>("pg2mongo-genresFlow")
                .start(genresPgToMongoStep())
                .build();
        Flow splitFlow = new FlowBuilder<Flow>("pg2mongo-split-authors-genres")
                .split(migrationTaskExecutor())
                .add(authorsFlow, genresFlow)
                .build();
        Flow fullFlow = new FlowBuilder<Flow>("pg2mongo-fullFlow")
                .start(truncateMongoStep())
                .next(splitFlow)
                .next(booksPgToMongoStep())
                .next(commentsPgToMongoStep())
                .build();

        return new JobBuilder("pgToMongoJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fullFlow)
                .end()
                .build();
    }

    @Bean
    public Step truncateMongoStep() {
        return new StepBuilder("truncateMongoStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Object raw = chunkContext.getStepContext().getJobParameters().get("truncateBeforeLoad");
                    boolean truncate = raw != null && Boolean.parseBoolean(raw.toString());
                    if (!truncate) {
                        return RepeatStatus.FINISHED;
                    }
                    mongoTemplate.getCollection("comments").deleteMany(new org.bson.Document());
                    mongoTemplate.getCollection("books").deleteMany(new org.bson.Document());
                    mongoTemplate.getCollection("genres").deleteMany(new org.bson.Document());
                    mongoTemplate.getCollection("authors").deleteMany(new org.bson.Document());
                    return RepeatStatus.FINISHED;
                }, pgTxManager)
                .build();
    }

    @Bean
    public Step authorsPgToMongoStep() {
        return new StepBuilder("authorsPgToMongoStep", jobRepository)
                .<JpaAuthor, MongoAuthor>chunk(chunkSize, pgTxManager)
                .reader(authorsPgReader)
                .processor(authorPgToMongoProcessor)
                .writer(authorMongoUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("authorsPgToMongoStep"))
                .stream(idMappingService)
                .build();
    }

    @Bean
    public Step genresPgToMongoStep() {
        return new StepBuilder("genresPgToMongoStep", jobRepository)
                .<JpaGenre, MongoGenre>chunk(chunkSize, pgTxManager)
                .reader(genresPgReader)
                .processor(genrePgToMongoProcessor)
                .writer(genreMongoUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("genresPgToMongoStep"))
                .stream(idMappingService)
                .build();
    }

    @Bean
    public Step booksPgToMongoStep() {
        return new StepBuilder("booksPgToMongoStep", jobRepository)
                .<JpaBook, MongoBook>chunk(chunkSize, pgTxManager)
                .reader(booksPgReader)
                .processor(bookPgToMongoProcessor)
                .writer(bookMongoUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("booksPgToMongoStep"))
                .stream(idMappingService)
                .build();
    }

    @Bean
    public Step commentsPgToMongoStep() {
        return new StepBuilder("commentsPgToMongoStep", jobRepository)
                .<JpaComment, MongoComment>chunk(chunkSize, pgTxManager)
                .reader(commentsPgReader)
                .processor(commentPgToMongoProcessor)
                .writer(commentMongoUpsertWriter)
                .listener(StepUtils.loggingReadListener())
                .listener(StepUtils.loggingWriteListener())
                .listener(StepUtils.loggingSkipListener())
                .listener(StepUtils.timingStepListener("commentsPgToMongoStep"))
                .stream(idMappingService)
                .build();
    }
}