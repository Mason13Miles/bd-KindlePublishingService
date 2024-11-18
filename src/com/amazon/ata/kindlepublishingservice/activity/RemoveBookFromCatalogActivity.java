package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.RemoveBookFromCatalogRequest;
import com.amazon.ata.kindlepublishingservice.models.response.RemoveBookFromCatalogResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import javax.validation.ValidationException;

public class RemoveBookFromCatalogActivity {

    private final CatalogDao catalogDao;

    @Inject
    public RemoveBookFromCatalogActivity(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    public RemoveBookFromCatalogResponse execute(RemoveBookFromCatalogRequest removeBookFromCatalogRequest) {
        String bookId = removeBookFromCatalogRequest.getBookId();

        if (bookId == null || bookId.isEmpty()) {
            throw new ValidationException("Book ID cannot be null or empty.");
        }

        // Fetch the latest version of the book
        CatalogItemVersion book;
        try {
            book = catalogDao.getBookFromCatalog(bookId);
        } catch (BookNotFoundException e) {
            // Rethrow with additional context
            throw new BookNotFoundException(String.format("Cannot remove book: %s", e.getMessage()));
        }

        // Mark the book as inactive
        book.setInactive(true);
        catalogDao.saveCatalogItemVersion(book);

        // Return an empty response
        return new RemoveBookFromCatalogResponse();
    }
}

