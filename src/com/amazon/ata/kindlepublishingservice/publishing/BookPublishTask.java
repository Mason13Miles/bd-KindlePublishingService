package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.Book;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private final BookPublishRequestManager requestManager;
    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager requestManager,
                           PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao) {
        this.requestManager = requestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    @Override
    public void run() {
        BookPublishRequest request = requestManager.getBookPublishRequestToProcess();

        if (request == null) {
            return;
        }

        String publishingRecordId = request.getPublishingRecordId();
        try {
            publishingStatusDao.setPublishingStatus(
                    publishingRecordId,
                    PublishingRecordStatus.IN_PROGRESS,
                    request.getBookId()
            );

            Book publishedBook = catalogDao.publishBook(request);

            publishingStatusDao.setPublishingStatus(
                    publishingRecordId,
                    PublishingRecordStatus.SUCCESSFUL,
                    publishedBook.getBookId(),
                    "Book published successfully!"
            );

        } catch (BookNotFoundException e) {
            publishingStatusDao.setPublishingStatus(
                    publishingRecordId,
                    PublishingRecordStatus.FAILED,
                    request.getBookId(),
                    "Book not found: " + e.getMessage()
            );
        } catch (Exception e) {
            publishingStatusDao.setPublishingStatus(
                    publishingRecordId,
                    PublishingRecordStatus.FAILED,
                    request.getBookId(),
                    "Error publishing book: " + e.getMessage()
            );
        }
    }
}

