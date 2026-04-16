package it.unicam.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HackHub {
    public static void main(String[] args) {
        SpringApplication.run(HackHub.class, args);
    }
}