package com.hackathon.tomolow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // Spring Data JPA에서, Auditing(감시)기능을 활성화하는 어노테이션
@EnableScheduling
public class TomolowBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(TomolowBeApplication.class, args);
  }
}
