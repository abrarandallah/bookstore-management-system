package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// A shelf category a book can be tagged with (e.g. "Business",
// "Psychology"). Many-to-many with Book - see Book.genres, which owns the
// relationship via the book_genres join table. Genre itself doesn't hold a
// reference back to its books; counts and lookups go through queries
// instead (see GenreRepository), so this stays a plain, small entity.
@Getter
@Entity
@Table(name = "genres", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Genre name is required.")
    @Column(nullable = false, unique = true)
    private String name;

    public Genre() {
        super();
    }

    public Genre(String name) {
        super();
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}