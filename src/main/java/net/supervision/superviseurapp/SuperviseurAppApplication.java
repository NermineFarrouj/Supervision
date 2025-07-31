package net.supervision.superviseurapp;

import jakarta.annotation.PostConstruct;
import net.supervision.superviseurapp.entities.VMStatus;
import net.supervision.superviseurapp.repositories.VMStatusRepository;
import net.supervision.superviseurapp.service.VMStatusServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class SuperviseurAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuperviseurAppApplication.class, args);}


        @PostConstruct
        public void init() {
            System.out.println("Application démarrée");
        }
}





