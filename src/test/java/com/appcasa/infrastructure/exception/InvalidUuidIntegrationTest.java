package com.appcasa.infrastructure.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class InvalidUuidIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void invalidUuid_returnsBadRequestProblemDetail() throws Exception {
    String token = registrarYObtenerToken();

    mockMvc.perform(get("/api/v1/tareas/hogar/placeholder-hogar-id/pendientes")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value("/errors/invalid-parameter"))
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.detail").value("Parametro idHogar invalido"));
  }

  private String registrarYObtenerToken() throws Exception {
    String email = "invalid-uuid-" + UUID.randomUUID() + "@example.com";
    String payload = """
      {
        "nombre": "Invalid",
        "apellidos": "Uuid",
        "email": "%s",
        "password": "Password123"
      }
      """.formatted(email);

    MvcResult result = mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isCreated())
      .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("token").asText();
  }
}
