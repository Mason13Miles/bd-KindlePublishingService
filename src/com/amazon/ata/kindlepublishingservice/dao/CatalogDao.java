package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
                .withHashKeyValues(book)
                .withScanIndexForward(false)
                .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public Book publishBook(BookPublishRequest request) {

        String bookId = StringUtils.isBlank(request.getBookId()) ? UUID.randomUUID().toString() : request.getBookId();
        int version = 1;

        Book book = Book.builder()
                .withBookId(bookId)
                .withTitle(request.getTitle())
                .withAuthor(request.getAuthor())
                .withText(request.getText())
                .withGenre(request.getGenre().toString())
                .withVersion(version)
                .build();


        if (StringUtils.isBlank(request.getBookId())) {
            saveBookToCatalog(book);
        } else {
            validateBookExists(request.getBookId());
            updateBookInCatalog(book);
        }

        return book;
    }


    private void saveBookToCatalog(Book book) {
        dynamoDbMapper.save(book);
    }

    private void updateBookInCatalog(Book book) {
        dynamoDbMapper.save(book);
    }

    public void removeBookFromCatalog(String bookId) {
        CatalogItemVersion book = getBookFromCatalog(bookId);
        book.setInactive(true);

        dynamoDbMapper.save(book);
        System.out.println(getBookFromCatalog(bookId));

    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null) {
            throw new BookNotFoundException(String.format("No catalog item found for bookId: %s", bookId));
        }
    }
}

