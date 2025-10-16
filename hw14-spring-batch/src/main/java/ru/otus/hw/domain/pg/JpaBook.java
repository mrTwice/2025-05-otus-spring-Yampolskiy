package ru.otus.hw.domain.pg;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collections;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "books", uniqueConstraints = {
        @UniqueConstraint(name = "uq_books_author_title", columnNames = {"author_id", "title"})
})
public class JpaBook {

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
    private JpaAuthor jpaAuthor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @BatchSize(size = 16)
    private Set<JpaGenre> jpaGenres = new LinkedHashSet<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public JpaBook(String title, JpaAuthor jpaAuthor, Set<JpaGenre> jpaGenres) {
        this.title = title;
        this.jpaAuthor = jpaAuthor;
        this.jpaGenres = (jpaGenres == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(jpaGenres);
    }

    public void replaceGenres(Set<JpaGenre> newJpaGenres) {
        Set<JpaGenre> safe = (newJpaGenres == null) ? Collections.emptySet() : new LinkedHashSet<>(newJpaGenres);
        this.jpaGenres.clear();
        this.jpaGenres.addAll(safe);
    }

    public Set<JpaGenre> getJpaGenres() {
        return Collections.unmodifiableSet(jpaGenres);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        JpaBook other = (JpaBook) o;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : getClass().hashCode();
    }
}
