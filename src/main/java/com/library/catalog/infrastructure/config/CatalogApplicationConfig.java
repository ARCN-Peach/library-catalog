package com.library.catalog.infrastructure.config;

import com.library.catalog.domain.repository.BookRepository;
import com.library.catalog.domain.service.CatalogSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogApplicationConfig {

    @Bean
    public CatalogSearchService catalogSearchService(BookRepository bookRepository) {
        return new CatalogSearchService(bookRepository);
    }
}
