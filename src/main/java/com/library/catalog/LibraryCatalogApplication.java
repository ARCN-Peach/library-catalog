package com.library.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryCatalogApplication.class, args);
    }
}
