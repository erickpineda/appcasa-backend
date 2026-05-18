package com.appcasa.infrastructure.security;

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
class ActuatorSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void actuatorRoot_requiresJwtInLocalProfile() throws Exception {
    mockMvc.perform(get("/actuator"))
      .andExpect(status().isForbidden());
  }

  @Test
  void actuatorHealth_requiresJwtInLocalProfile() throws Exception {
    mockMvc.perform(get("/actuator/health"))
      .andExpect(status().isForbidden());
  }

  @Test
  void actuatorRoot_returnsLinks_whenJwtIsValid() throws Exception {
    String token = registrarYObtenerToken();

    mockMvc.perform(get("/actuator")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$._links").exists());
  }

  @Test
  void actuatorHealth_returnsOk_whenJwtIsValid() throws Exception {
    String token = registrarYObtenerToken();

    mockMvc.perform(get("/actuator/health")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").exists());
  }

  private String registrarYObtenerToken() throws Exception {
    String email = "actuator-" + UUID.randomUUID() + "@example.com";
    String payload = """
      {
        "nombre": "Actuator",
        "apellidos": "Tester",
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
