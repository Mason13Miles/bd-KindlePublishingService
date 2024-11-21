package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {

    private final Queue<BookPublishRequest> requestQueue;

    @Inject
    public BookPublishRequestManager() {
        this.requestQueue = new LinkedList<>();
    }

    public void addBookPublishRequest(BookPublishRequest request) {
        try {
            requestQueue.add(request);
        } catch(Exception e) {
            System.out.println("Queue is full");
        }
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        if (requestQueue.isEmpty()) {
            return null;
        }

        BookPublishRequest request = requestQueue.poll();

        System.out.println(request);
        System.out.println(requestQueue);

        return request;
    }

    public boolean hasRequests() {
        return !requestQueue.isEmpty();
    }
}

