package ru.otus.hw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(
        classes = MigratorApplication.class,
        properties = {
                "spring.shell.interactive.enabled=false",
                "spring.shell.command.script.enabled=false",
                "spring.main.web-application-type=none",
                "spring.main.banner-mode=off"
        }
)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MigrationE2ETest {

    @Container
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("library")
                    .withUsername("user")
                    .withPassword("password")
                    .withInitScript("test-init.sql");
    ;

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> PG.getJdbcUrl() + "?currentSchema=app");
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);

        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.flyway.create-schemas", () -> "true");
        r.add("spring.flyway.default-schema", () -> "app");

        r.add("spring.batch.jdbc.initialize-schema", () -> "always");
        r.add("spring.batch.jdbc.table-prefix", () -> "public.BATCH_");
        r.add("spring.batch.job.enabled", () -> "false");

        r.add("spring.data.mongodb.uri", () -> MONGO.getConnectionString() + "/library");

        r.add("spring.shell.interactive.enabled", () -> "false");
        r.add("spring.shell.command.script.enabled", () -> "false");
        r.add("spring.main.web-application-type", () -> "none");
    }

    @Autowired
    JobLauncherTestUtils utils;

    @Autowired
    MongoTemplate mongo;

    @Autowired
    DataSource dataSource;

    @Autowired
    Job pgToMongoJob;

    @Autowired
    Job mongoToPgJob;

    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        mongo.getDb().drop();
        jdbc.execute("set search_path=app");
    }

    @Test
    void fullRoundTrip_usesFlywaySeed_pgToMongo_then_dropPg_then_mongoToPg() throws Exception {

        Map<String, Object> countsSeed = countsPg();
        assertThat(countsSeed.get("authors")).isIn(4L, 4);
        assertThat(countsSeed.get("genres")).isIn(5L, 5);
        assertThat(countsSeed.get("books")).isIn(4L, 4);
        assertThat(countsSeed.get("comments")).isIn(5L, 5);

        utils.setJob(pgToMongoJob);
        JobExecution exec1 = utils.launchJob(new JobParametersBuilder()
                .addString("truncateBeforeLoad", "true")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters());
        assertThat(exec1.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        long a1 = mongo.getCollection("authors").countDocuments();
        long g1 = mongo.getCollection("genres").countDocuments();
        long b1 = mongo.getCollection("books").countDocuments();
        long c1 = mongo.getCollection("comments").countDocuments();
        assertThat(a1).isEqualTo(4);
        assertThat(g1).isEqualTo(5);
        assertThat(b1).isEqualTo(4);
        assertThat(c1).isEqualTo(5);

        truncatePgDomainTables();
        Map<String, Object> countsAfterTruncate = countsPg();
        assertThat(countsAfterTruncate.get("authors")).isIn(0L, 0);
        assertThat(countsAfterTruncate.get("genres")).isIn(0L, 0);
        assertThat(countsAfterTruncate.get("books")).isIn(0L, 0);
        assertThat(countsAfterTruncate.get("comments")).isIn(0L, 0);

        utils.setJob(mongoToPgJob);
        JobExecution exec2 = utils.launchJob(new JobParametersBuilder()
                .addString("truncateBeforeLoad", "false")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters());
        assertThat(exec2.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        Map<String, Object> countsRestored = countsPg();
        assertThat(countsRestored.get("authors")).isIn(4L, 4);
        assertThat(countsRestored.get("genres")).isIn(5L, 5);
        assertThat(countsRestored.get("books")).isIn(4L, 4);
        assertThat(countsRestored.get("comments")).isIn(5L, 5);

        List<String> authorsSorted = jdbc.queryForList(
                "select full_name from app.authors order by full_name", String.class);
        assertThat(authorsSorted).containsExactly(
                "Brian Goetz", "Joshua Bloch", "Martin Fowler", "Robert C. Martin"
        );

        Long refactoringBookId = jdbc.queryForObject("""
                select b.id from app.books b 
                  join app.authors a on a.id=b.author_id 
                where a.full_name='Martin Fowler' and b.title='Refactoring'
                """, Long.class);

        var when = java.time.OffsetDateTime.parse("2024-12-30T10:00:00Z");

        Integer cnt = jdbc.queryForObject("""
                 select count(*) 
                from app.comments 
                where book_id = ? and created_at = ? and text = ?
                """, Integer.class, refactoringBookId, when, "Classic on refactoring"
        );
        assertThat(cnt).isEqualTo(1);
    }


    private Map<String, Object> countsPg() {
        return jdbc.queryForMap("""
                select 
                  (select count(*) from app.authors)  as authors,
                  (select count(*) from app.genres)   as genres,
                  (select count(*) from app.books)    as books,
                  (select count(*) from app.comments) as comments
                """);
    }

    private void truncatePgDomainTables() {
        jdbc.execute("""
                    TRUNCATE TABLE 
                        app.comments,
                        app.books_genres,
                        app.books,
                        app.authors,
                        app.genres
                    RESTART IDENTITY CASCADE
                """);
    }
}
