package com.example.parcial.parcial2.services;

import com.example.parcial.parcial2.domain.dtos.BookRequestDto;
import com.example.parcial.parcial2.domain.dtos.GenreCountDto;
import com.example.parcial.parcial2.domain.entities.Book;
import com.example.parcial.parcial2.domain.entities.Genre;
import com.example.parcial.parcial2.repositories.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(BookRequestDto dto) {
        return null;
    }

    public Book getBookById(UUID id) {
        return null;
    }

    public List<Book> getAllBooks(String author, String genre) {
        if (author != null && genre != null) {
            return bookRepository.findByAuthorAndGenre(author, Genre.valueOf(genre.toUpperCase()));
        } else if (author != null) {
            return bookRepository.findByAuthor(author);
        } else if (genre != null) {
            return bookRepository.findByGenre(Genre.valueOf(genre.toUpperCase()));
        } else {
            return bookRepository.findAll();
        }
    }

    public Book updateBook(UUID id, BookRequestDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        if (dto.getGenre() != null) {
            book.setGenre(Genre.valueOf(dto.getGenre().toUpperCase()));
        }
        book.setIsbn(dto.getIsbn());
        book.setAvailable(dto.isAvailable());
        book.setAvailableCount(dto.getAvailableCount());
        return bookRepository.save(book);
    }

    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setActive(false);
        bookRepository.save(book);
    }

    public List<GenreCountDto> getGenresAvailable() {
        return bookRepository.countByGenre();
    }
}
