package com.library.catalog.infrastructure.messaging.consumer;

import com.library.catalog.application.usecase.UpdateBookStockUseCase;
import com.library.catalog.domain.exception.BookNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RentalEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RentalEventConsumer.class);

    private final UpdateBookStockUseCase updateBookStockUseCase;

    public RentalEventConsumer(UpdateBookStockUseCase updateBookStockUseCase) {
        this.updateBookStockUseCase = updateBookStockUseCase;
    }

    @RabbitListener(queues = "catalog.rental.book-lent")
    public void onBookLent(BookLentMessage message) {
        log.info("BookLentEvent received for book {} [correlationId={}]",
                message.bookId(), message.correlationId());
        try {
            updateBookStockUseCase.decrementStock(message.bookId(), message.correlationId());
        } catch (BookNotFoundException e) {
            log.error("Book not found when processing BookLentEvent [bookId={}, correlationId={}]",
                    message.bookId(), message.correlationId());
        }
    }

    @RabbitListener(queues = "catalog.rental.book-returned")
    public void onBookReturned(BookReturnedMessage message) {
        log.info("BookReturnedEvent received for book {} [correlationId={}]",
                message.bookId(), message.correlationId());
        try {
            updateBookStockUseCase.incrementStock(message.bookId(), message.correlationId());
        } catch (BookNotFoundException e) {
            log.error("Book not found when processing BookReturnedEvent [bookId={}, correlationId={}]",
                    message.bookId(), message.correlationId());
        }
    }
}
