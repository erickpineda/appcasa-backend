package com.appcasa;

import com.appcasa.infrastructure.security.JwtProperties;
import com.appcasa.infrastructure.security.RefreshCookieProperties;
import com.appcasa.infrastructure.security.SecurityCorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableScheduling
@EnableConfigurationProperties({
  JwtProperties.class,
  RefreshCookieProperties.class,
  SecurityCorsProperties.class
})
public class AppCasaApplication {

  public static void main(String[] args) {
    SpringApplication.run(AppCasaApplication.class, args);
  }
}
