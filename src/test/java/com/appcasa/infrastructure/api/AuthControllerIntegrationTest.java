package com.appcasa.infrastructure.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void registro_returnsTokenUsuarioAndRefreshCookie() throws Exception {
    String email = "registro-" + UUID.randomUUID() + "@example.com";

    mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registroPayload(email)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.token").isString())
      .andExpect(jsonPath("$.usuario.email").value(email))
      .andExpect(cookie().exists("appcasa_refresh"))
      .andExpect(cookie().httpOnly("appcasa_refresh", true));
  }

  @Test
  void registro_duplicateEmail_returnsConflict() throws Exception {
    String email = "duplicado-" + UUID.randomUUID() + "@example.com";

    mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registroPayload(email)))
      .andExpect(status().isCreated());

    mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registroPayload(email)))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.type").value("/errors/conflict"))
      .andExpect(jsonPath("$.detail").value("El email ya esta registrado"));
  }

  @Test
  void login_returnsTokenUsuarioAndRefreshCookie() throws Exception {
    String email = "login-" + UUID.randomUUID() + "@example.com";
    registrarUsuario(email);

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginPayload(email)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isString())
      .andExpect(jsonPath("$.usuario.email").value(email))
      .andExpect(cookie().exists("appcasa_refresh"))
      .andExpect(cookie().httpOnly("appcasa_refresh", true));
  }

  @Test
  void refresh_rotatesCookieAndReturnsNewToken() throws Exception {
    String email = "refresh-" + UUID.randomUUID() + "@example.com";
    MvcResult registro = registrarUsuario(email);
    Cookie refreshCookie = registro.getResponse().getCookie("appcasa_refresh");

    MvcResult refresh = mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isString())
      .andExpect(jsonPath("$.usuario.email").value(email))
      .andExpect(cookie().exists("appcasa_refresh"))
      .andReturn();

    Cookie rotatedCookie = refresh.getResponse().getCookie("appcasa_refresh");
    assertThat(rotatedCookie).isNotNull();
    assertThat(rotatedCookie.getValue()).isNotEqualTo(refreshCookie.getValue());
  }

  @Test
  void logout_revokesSessionAndRefreshFailsAfterwards() throws Exception {
    String email = "logout-" + UUID.randomUUID() + "@example.com";
    MvcResult registro = registrarUsuario(email);
    Cookie refreshCookie = registro.getResponse().getCookie("appcasa_refresh");

    mockMvc.perform(post("/api/v1/auth/logout").cookie(refreshCookie))
      .andExpect(status().isNoContent())
      .andExpect(cookie().maxAge("appcasa_refresh", 0));

    mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.type").value("/errors/unauthorized"))
      .andExpect(jsonPath("$.detail").value("Sesion no valida"));
  }

  private MvcResult registrarUsuario(String email) throws Exception {
    return mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registroPayload(email)))
      .andExpect(status().isCreated())
      .andReturn();
  }

  private String registroPayload(String email) {
    return """
      {
        "nombre": "Ana",
        "apellidos": "Casa",
        "email": "%s",
        "password": "Password123"
      }
      """.formatted(email);
  }

  private String loginPayload(String email) {
    return """
      {
        "email": "%s",
        "password": "Password123"
      }
      """.formatted(email);
  }

  @SuppressWarnings("unused")
  private String extraerToken(MvcResult result) throws Exception {
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("token").asText();
  }
}
