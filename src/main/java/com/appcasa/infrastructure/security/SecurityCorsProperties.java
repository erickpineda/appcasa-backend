package com.appcasa.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "appcasa.security.cors")
public class SecurityCorsProperties {

  private List<String> allowedOrigins = new ArrayList<>();

  public List<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }
}
