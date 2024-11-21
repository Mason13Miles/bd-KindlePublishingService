package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;

import javax.inject.Inject;

/**
 * Implementation of the SubmitBookForPublishingActivity for ATACurriculumKindlePublishingService's
 * SubmitBookForPublishing API.
 *
 * This API allows the client to submit a new book to be published in the catalog or update an existing book.
 */
public class SubmitBookForPublishingActivity {

    private final PublishingStatusDao publishingStatusDao;
    private final BookPublishRequestManager requestManager;
    private final CatalogDao catalogDao;

    /**
     * Instantiates a new SubmitBookForPublishingActivity object.
     *
     * @param publishingStatusDao PublishingStatusDao to access the publishing status table.
     * @param requestManager      BookPublishRequestManager to manage publishing requests.
     * @param catalogDao          CatalogDao to interact with the catalog table.
     */
    @Inject
    public SubmitBookForPublishingActivity(PublishingStatusDao publishingStatusDao,
                                           BookPublishRequestManager requestManager,
                                           CatalogDao catalogDao) {
        this.publishingStatusDao = publishingStatusDao;
        this.requestManager = requestManager;
        this.catalogDao = catalogDao;
    }

    /**
     * Submits the book in the request for publishing.
     *
     * @param request Request object containing the book data to be published. If the request is updating an existing
     *                book, then the corresponding book id should be provided. Otherwise, the request will be treated
     *                as a new book.
     * @return SubmitBookForPublishingResponse Response object that includes the publishing status id, which can be used
     * to check the publishing state of the book.
     */
    public SubmitBookForPublishingResponse execute(SubmitBookForPublishingRequest request) {

        final BookPublishRequest bookPublishRequest = BookPublishRequestConverter.toBookPublishRequest(request);

        if (bookPublishRequest.getBookId() != null) {
            try {
                catalogDao.validateBookExists(bookPublishRequest.getBookId());
            } catch(Exception e) {
                System.out.println("Book not found by id");
            }
        }

        PublishingStatusItem item = publishingStatusDao.setPublishingStatus(
                bookPublishRequest.getPublishingRecordId(),
                PublishingRecordStatus.QUEUED,
                bookPublishRequest.getBookId()
        );

        requestManager.addBookPublishRequest(bookPublishRequest);

        return SubmitBookForPublishingResponse.builder()
                .withPublishingRecordId(item.getPublishingRecordId())
                .build();
    }
}

