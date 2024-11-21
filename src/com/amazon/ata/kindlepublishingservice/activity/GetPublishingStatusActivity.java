package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the GetPublishingStatus operation which retrieves the publishing status history
 * of a book submission from the PublishingStatus table.
 */
public class GetPublishingStatusActivity {

    private final PublishingStatusDao publishingStatusDao;

    /**
     * Constructs a new GetPublishingStatusActivity.
     *
     * @param publishingStatusDao The DAO used to access the PublishingStatus table.
     */
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    /**
     * Executes the GetPublishingStatus operation.
     *
     * @param request The request containing the publishingRecordId to retrieve.
     * @return The response containing the publishing status history.
     */
    public GetPublishingStatusResponse execute(GetPublishingStatusRequest request) {
        String publishingRecordId = request.getPublishingRecordId();
        try {
            List<PublishingStatusItem> statusItems = publishingStatusDao.getPublishingStatuses(publishingRecordId);
            if (statusItems == null || statusItems.isEmpty()) {

                throw new PublishingStatusNotFoundException(
                        String.format("No publishing status found for ID: %s", publishingRecordId)
                );
            }

            List<PublishingStatusRecord> statusRecords = statusItems.stream()
                    .map(this::toPublishingStatusRecord)
                    .collect(Collectors.toList());

            return GetPublishingStatusResponse.builder()
                    .withPublishingStatusHistory(statusRecords)
                    .build();

        } catch(PublishingStatusNotFoundException e) {
            System.out.println("No publishing Id found: " + e);

            return null;
        }

    }

    /**
     * Converts a PublishingStatusItem to a PublishingStatusRecord.
     *
     * @param item The PublishingStatusItem to convert.
     * @return The converted PublishingStatusRecord.
     */
    private PublishingStatusRecord toPublishingStatusRecord(PublishingStatusItem item) {
        return PublishingStatusRecord.builder()
                .withStatus(item.getStatus().toString())
                .withStatusMessage(item.getStatusMessage())
                .withBookId(item.getBookId())
                .build();
    }
}


