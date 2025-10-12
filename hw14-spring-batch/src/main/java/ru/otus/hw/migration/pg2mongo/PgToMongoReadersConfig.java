package ru.otus.hw.migration.pg2mongo;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;


import jakarta.persistence.EntityManagerFactory;
import ru.otus.hw.domain.pg.JpaAuthor;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.domain.pg.JpaComment;
import ru.otus.hw.domain.pg.JpaGenre;
import ru.otus.hw.repo.pg.JpaAuthorRepository;
import ru.otus.hw.repo.pg.JpaBookRepository;
import ru.otus.hw.repo.pg.JpaGenreRepository;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PgToMongoReadersConfig {

    private static final int PAGE_SIZE = 500;

    private final JpaAuthorRepository jpaAuthorRepository;

    private final JpaGenreRepository jpaGenreRepository;

    private final JpaBookRepository jpaBookRepository;

    private final EntityManagerFactory emf;

    @Bean
    public RepositoryItemReader<JpaAuthor> authorsPgReader() {
        RepositoryItemReader<JpaAuthor> reader = new RepositoryItemReader<>();
        reader.setRepository(jpaAuthorRepository);
        reader.setMethodName("findAll");
        reader.setPageSize(PAGE_SIZE);
        reader.setSort(Map.of("id", Sort.Direction.ASC));
        return reader;
    }


    @Bean
    public RepositoryItemReader<JpaGenre> genresPgReader() {
        RepositoryItemReader<JpaGenre> reader = new RepositoryItemReader<>();
        reader.setRepository(jpaGenreRepository);
        reader.setMethodName("findAll");
        reader.setPageSize(PAGE_SIZE);
        reader.setSort(Map.of("id", Sort.Direction.ASC));
        return reader;
    }

    @Bean
    public ItemReader<JpaBook> booksPgReader() {
        return new TwoPhaseBooksItemReader(jpaBookRepository, PAGE_SIZE);
    }

    @Bean
    public JpaPagingItemReader<JpaComment> commentsPgReader() {
        JpaPagingItemReader<JpaComment> reader = new JpaPagingItemReader<>();
        reader.setName("commentsPgReader");
        reader.setEntityManagerFactory(emf);
        reader.setPageSize(PAGE_SIZE);
        reader.setQueryString(
                "select c " +
                        "from JpaComment c " +
                        " join fetch c.jpaBook b " +
                        " join fetch b.jpaAuthor a " +
                        "order by c.id asc"
        );
        return reader;
    }
}
