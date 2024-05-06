package org.reploop.beancount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImporterApp {
    public static void main(String... args) {
        SpringApplication.run(ImporterApp.class, args);
    }
}
