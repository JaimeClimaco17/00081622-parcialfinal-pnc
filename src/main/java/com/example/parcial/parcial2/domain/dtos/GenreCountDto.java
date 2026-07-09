package com.example.parcial.parcial2.domain.dtos;

import com.example.parcial.parcial2.domain.entities.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreCountDto {
    private String genre;
    private long count;

    public GenreCountDto(Genre genre, long count) {
        this.genre = genre != null ? genre.name() : "SIN_GENERO";
        this.count = count;
    }
}
