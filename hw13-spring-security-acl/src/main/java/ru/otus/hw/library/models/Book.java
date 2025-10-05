package ru.otus.hw.library.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import jakarta.persistence.GenerationType;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "books", uniqueConstraints = {
@UniqueConstraint(name = "uq_books_author_title", columnNames = {"author_id", "title"})
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    @ToString.Include
    @NotBlank
    @Size(max = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull
    private Author author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @BatchSize(size = 16)
    private Set<Genre> genres = new LinkedHashSet<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Book(String title, Author author, Set<Genre> genres) {
        this.title = title;
        this.author = author;
        this.genres = (genres == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(genres);
    }

    public void replaceGenres(Set<Genre> newGenres) {
        Set<Genre> safe = (newGenres == null) ? Collections.emptySet() : new LinkedHashSet<>(newGenres);
        this.genres.clear();
        this.genres.addAll(safe);
    }

    public Set<Genre> getGenres() {
        return Collections.unmodifiableSet(genres);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Book other = (Book) o;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : getClass().hashCode();
    }
}

