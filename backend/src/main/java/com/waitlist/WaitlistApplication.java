package com.waitlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = "com.waitlist")
public class WaitlistApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaitlistApplication.class, args);
    }
}
