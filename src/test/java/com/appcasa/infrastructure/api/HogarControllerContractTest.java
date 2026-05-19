package com.appcasa.infrastructure.api;

import com.appcasa.domain.hogar.Hogar;
import com.appcasa.domain.hogar.HogarRepository;
import com.appcasa.domain.hogar.HogarUsuario;
import com.appcasa.domain.hogar.HogarUsuarioRepository;
import com.appcasa.domain.miembro.MiembroHogar;
import com.appcasa.domain.miembro.MiembroHogarRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class HogarControllerContractTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HogarRepository hogarRepository;

  @Autowired
  private HogarUsuarioRepository hogarUsuarioRepository;

  @Autowired
  private MiembroHogarRepository miembroHogarRepository;

  private String token;
  private UUID idUsuario;
  private Hogar hogarPrincipal;
  private Hogar hogarSecundario;

  @BeforeEach
  void setUp() throws Exception {
    RegistroAuth registro = registrarYObtenerAuth();
    token = registro.token();
    idUsuario = registro.idUsuario();

    hogarPrincipal = hogarRepository.save(Hogar.builder()
      .nombre("Casa Norte")
      .descripcion("Principal")
      .codigo("CASA" + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
      .idEstado(1)
      .build());

    hogarSecundario = hogarRepository.save(Hogar.builder()
      .nombre("Casa Sur")
      .descripcion("Secundaria")
      .codigo("OTRA" + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
      .idEstado(1)
      .build());

    hogarUsuarioRepository.save(HogarUsuario.builder()
      .idHogar(hogarPrincipal.getId())
      .idUsuario(idUsuario)
      .idRol(1)
      .esPrincipal(true)
      .idEstado(1)
      .build());
  }

  @Test
  void listar_returnsOnlyAuthenticatedUserHouseholds() throws Exception {
    mockMvc.perform(get("/api/v1/hogares")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].id").value(hogarPrincipal.getId().toString()))
      .andExpect(jsonPath("$[0].nombre").value("Casa Norte"))
      .andExpect(jsonPath("$[0].codigo").value(hogarPrincipal.getCodigo()));
  }

  @Test
  void crear_createsHouseholdAndMemberships() throws Exception {
    mockMvc.perform(post("/api/v1/hogares")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "nombre":"Casa Nueva",
            "descripcion":"Creada desde test"
          }
          """))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.nombre").value("Casa Nueva"))
      .andExpect(jsonPath("$.codigo").value(containsString("CASA")));

    mockMvc.perform(get("/api/v1/hogares")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void unirse_joinsByCodeAndIsIdempotent() throws Exception {
    mockMvc.perform(post("/api/v1/hogares/unirse")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "codigo":"%s"
          }
          """.formatted(hogarSecundario.getCodigo())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(hogarSecundario.getId().toString()))
      .andExpect(jsonPath("$.codigo").value(hogarSecundario.getCodigo()));

    mockMvc.perform(post("/api/v1/hogares/unirse")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "codigo":"%s"
          }
          """.formatted(hogarSecundario.getCodigo())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(hogarSecundario.getId().toString()));

    mockMvc.perform(get("/api/v1/hogares")
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void unirse_returnsNotFoundForInvalidCode() throws Exception {
    mockMvc.perform(post("/api/v1/hogares/unirse")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "codigo":"NOEXISTE"
          }
          """))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value("/errors/not-found"));
  }

  @Test
  void listarMiembrosPorHogar_returnsActiveMembersForAuthenticatedUser() throws Exception {
    miembroHogarRepository.save(MiembroHogar.builder()
      .idHogar(hogarPrincipal.getId())
      .idTipoMiembro(1)
      .nombre("Luis")
      .idEstado(1)
      .build());

    miembroHogarRepository.save(MiembroHogar.builder()
      .idHogar(hogarPrincipal.getId())
      .idTipoMiembro(2)
      .nombre("Bobby")
      .idEstado(1)
      .build());

    mockMvc.perform(get("/api/v1/miembros/hogar/{idHogar}", hogarPrincipal.getId())
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(3))
      .andExpect(jsonPath("$[0].nombre").value("Ana Casa"))
      .andExpect(jsonPath("$[1].nombre").value("Bobby"))
      .andExpect(jsonPath("$[2].nombre").value("Luis"));
  }

  @Test
  void listarMiembrosPorHogar_returnsNotFoundWhenHouseholdIsNotOwnedByUser() throws Exception {
    mockMvc.perform(get("/api/v1/miembros/hogar/{idHogar}", hogarSecundario.getId())
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value("/errors/not-found"));
  }

  private RegistroAuth registrarYObtenerAuth() throws Exception {
    String email = "hogar-contract-" + UUID.randomUUID() + "@example.com";
    MvcResult result = mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "nombre": "Ana",
            "apellidos": "Casa",
            "email": "%s",
            "password": "Password123"
          }
          """.formatted(email)))
      .andExpect(status().isCreated())
      .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return new RegistroAuth(
      body.get("token").asText(),
      UUID.fromString(body.get("usuario").get("id").asText())
    );
  }

  private record RegistroAuth(String token, UUID idUsuario) {}
}
