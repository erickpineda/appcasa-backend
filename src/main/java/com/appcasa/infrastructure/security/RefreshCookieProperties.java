package com.appcasa.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "appcasa.auth.refresh-cookie")
public class RefreshCookieProperties {

  private String name = "appcasa_refresh";
  private String path = "/api/v1/auth";
  private boolean secure;
  private String sameSite = "Lax";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public String getSameSite() {
    return sameSite;
  }

  public void setSameSite(String sameSite) {
    this.sameSite = sameSite;
  }
}
