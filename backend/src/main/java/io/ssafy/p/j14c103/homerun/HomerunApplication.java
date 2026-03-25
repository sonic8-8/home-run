package io.ssafy.p.j14c103.homerun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomerunApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomerunApplication.class, args);
    }

}
