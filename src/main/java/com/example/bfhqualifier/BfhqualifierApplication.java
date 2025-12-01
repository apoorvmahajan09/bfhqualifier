package com.example.bfhqualifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BfhqualifierApplication implements CommandLineRunner {

    private final AppRunner appRunner;

    public BfhqualifierApplication(AppRunner appRunner) {
        this.appRunner = appRunner;
    }

    public static void main(String[] args) {
        SpringApplication.run(BfhqualifierApplication.class, args);
    }

    @Override
    public void run(String... args) {
        appRunner.execute();
    }
}
