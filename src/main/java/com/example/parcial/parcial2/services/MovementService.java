package com.example.parcial.parcial2.services;

import com.example.parcial.parcial2.domain.dtos.MovementRequestDto;
import com.example.parcial.parcial2.domain.entities.Book;
import com.example.parcial.parcial2.domain.entities.Lector;
import com.example.parcial.parcial2.domain.entities.Movement;
import com.example.parcial.parcial2.domain.entities.MovementType;
import com.example.parcial.parcial2.repositories.BookRepository;
import com.example.parcial.parcial2.repositories.LectorRepository;
import com.example.parcial.parcial2.repositories.MovementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MovementService {

    private final MovementRepository movementRepository;
    private final LectorRepository lectorRepository;
    private final BookRepository bookRepository;

    public MovementService(MovementRepository movementRepository,
                           LectorRepository lectorRepository,
                           BookRepository bookRepository) {
        this.movementRepository = movementRepository;
        this.lectorRepository = lectorRepository;
        this.bookRepository = bookRepository;
    }

    public Movement borrowBook(MovementRequestDto dto) {
        Book book = bookRepository.findByIsbn(dto.getIsbn())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        Lector lector = lectorRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Lector not found"));

        if (book.getAvailableCount() <= 0) {
            throw new RuntimeException("No copies available");
        }

        book.setAvailableCount(book.getAvailableCount() - 1);
        book.setAvailable(book.getAvailableCount() > 0);
        bookRepository.save(book);

        Movement movement = new Movement();
        movement.setBook(book);
        movement.setLector(lector);
        movement.setTimestamp(Instant.now());
        movement.setType(MovementType.BORROWING);
        return movementRepository.save(movement);
    }

    public Movement returnBook(MovementRequestDto dto) {
        Book book = bookRepository.findByIsbn(dto.getIsbn())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        Lector lector = lectorRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Lector not found"));

        Movement lastMovement = movementRepository.findTopByLectorAndBookOrderByTimestampDesc(lector, book)
                .orElseThrow(() -> new RuntimeException("Lector has never borrowed this book"));

        if (lastMovement.getType() != MovementType.BORROWING) {
            throw new RuntimeException("Lector does not currently have this book borrowed");
        }

        book.setAvailableCount(book.getAvailableCount() + 1);
        book.setAvailable(true);
        bookRepository.save(book);

        Movement movement = new Movement();
        movement.setBook(book);
        movement.setLector(lector);
        movement.setTimestamp(Instant.now());
        movement.setType(MovementType.RETURN);
        return movementRepository.save(movement);
    }
}
