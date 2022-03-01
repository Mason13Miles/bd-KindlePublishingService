package com.amazon.ata.kindlepublishingservice;

import com.amazon.ata.kindlepublishingservice.dagger.ATAKindlePublishingServiceManager;
import com.amazon.ata.kindlepublishingservice.dagger.ApplicationComponent;
import com.amazon.ata.kindlepublishingservice.dagger.DaggerApplicationComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    private static final ApplicationComponent component = DaggerApplicationComponent.create();
    public static void main(String[] args) {
        ATAKindlePublishingServiceManager publishingMananger = component.provideATAKindlePublishingServiceManager();
        try {
            publishingMananger.start();
        } catch (Exception e) {
            System.out.println(e);
        }
        SpringApplication.run(App.class, args);
    }
}