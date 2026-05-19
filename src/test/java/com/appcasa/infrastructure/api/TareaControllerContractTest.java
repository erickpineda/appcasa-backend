package com.appcasa.infrastructure.api;

import com.appcasa.domain.hogar.Hogar;
import com.appcasa.domain.hogar.HogarRepository;
import com.appcasa.domain.miembro.MiembroHogar;
import com.appcasa.domain.miembro.MiembroHogarRepository;
import com.appcasa.domain.tarea.Tarea;
import com.appcasa.domain.tarea.TareaRepository;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TareaControllerContractTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HogarRepository hogarRepository;

  @Autowired
  private TareaRepository tareaRepository;

  @Autowired
  private MiembroHogarRepository miembroHogarRepository;

  private String token;
  private Hogar hogar;
  private Hogar otroHogar;
  private Tarea tarea;
  private MiembroHogar miembroUno;
  private MiembroHogar miembroDos;
  private MiembroHogar miembroOtroHogar;

  @BeforeEach
  void setUp() throws Exception {
    token = registrarYObtenerToken();
    hogar = hogarRepository.save(Hogar.builder()
      .nombre("Casa Contrato")
      .descripcion("Hogar de prueba")
      .codigo("CASA" + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
      .idEstado(1)
      .build());
    otroHogar = hogarRepository.save(Hogar.builder()
      .nombre("Casa Externa")
      .descripcion("Otro hogar de prueba")
      .codigo("OTRA" + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
      .idEstado(1)
      .build());
    miembroUno = miembroHogarRepository.save(MiembroHogar.builder()
      .idHogar(hogar.getId())
      .idTipoMiembro(1)
      .nombre("Ana")
      .idEstado(1)
      .build());
    miembroDos = miembroHogarRepository.save(MiembroHogar.builder()
      .idHogar(hogar.getId())
      .idTipoMiembro(1)
      .nombre("Luis")
      .idEstado(1)
      .build());
    miembroOtroHogar = miembroHogarRepository.save(MiembroHogar.builder()
      .idHogar(otroHogar.getId())
      .idTipoMiembro(1)
      .nombre("Intruso")
      .idEstado(1)
      .build());
    tarea = tareaRepository.save(Tarea.builder()
      .idHogar(hogar.getId())
      .titulo("Comprar pienso")
      .descripcion("Para la semana")
      .idPrioridad(3)
      .categoria("Compras")
      .fechaLimite(LocalDate.now())
      .esPeriodica(false)
      .esPersonal(false)
      .idEstado(1)
      .build());
  }

  @Test
  void listarPendientes_doesNotExposeTechnicalIds() throws Exception {
    mockMvc.perform(get("/api/v1/tareas/hogar/{hogarCodigo}/pendientes", hogar.getCodigo())
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(tarea.getId().toString()))
      .andExpect(jsonPath("$[0].hogarCodigo").value(hogar.getCodigo()))
      .andExpect(jsonPath("$[0].prioridad.codigo").value("ALTA"))
      .andExpect(jsonPath("$[0].estado.codigo").value("ACTIVA"))
      .andExpect(jsonPath("$[0].idPrioridad").doesNotExist())
      .andExpect(jsonPath("$[0].idEstado").doesNotExist())
      .andExpect(jsonPath("$[0].idHogar").doesNotExist());
  }

  @Test
  void obtener_usesPublicResponseShape() throws Exception {
    mockMvc.perform(get("/api/v1/tareas/{id}", tarea.getId())
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(tarea.getId().toString()))
      .andExpect(jsonPath("$.prioridad.codigo").value("ALTA"))
      .andExpect(jsonPath("$.idPrioridad").doesNotExist())
      .andExpect(jsonPath("$.idEstado").doesNotExist());
  }

  @Test
  void crear_usesCodigosInsteadOfTechnicalIds() throws Exception {
    mockMvc.perform(post("/api/v1/tareas")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "hogarCodigo":"%s",
            "titulo":"Sacar la basura",
            "prioridadCodigo":"MEDIA",
            "esPersonal":false
          }
          """.formatted(hogar.getCodigo())))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.hogarCodigo").value(hogar.getCodigo()))
      .andExpect(jsonPath("$.prioridad.codigo").value("MEDIA"))
      .andExpect(jsonPath("$.idPrioridad").doesNotExist())
      .andExpect(jsonPath("$.idHogar").doesNotExist());
  }

  @Test
  void crear_includesRealAssignmentsAndForcesEsPersonalFalse() throws Exception {
    mockMvc.perform(post("/api/v1/tareas")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "hogarCodigo":"%s",
            "titulo":"Pasear al perro",
            "esPersonal":true,
            "miembroIds":["%s","%s"]
          }
          """.formatted(hogar.getCodigo(), miembroUno.getId(), miembroDos.getId())))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.esPersonal").value(false))
      .andExpect(jsonPath("$.asignaciones.length()").value(2))
      .andExpect(jsonPath("$.asignaciones[0].miembroId").value(miembroUno.getId().toString()))
      .andExpect(jsonPath("$.asignaciones[0].nombreMiembro").value("Ana"))
      .andExpect(jsonPath("$.asignaciones[1].miembroId").value(miembroDos.getId().toString()))
      .andExpect(jsonPath("$.asignaciones[1].nombreMiembro").value("Luis"));
  }

  @Test
  void actualizar_replacesAssignmentsWithNewSelection() throws Exception {
    mockMvc.perform(put("/api/v1/tareas/{id}", tarea.getId())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "hogarCodigo":"%s",
            "titulo":"Comprar pienso inicial",
            "esPersonal":false,
            "miembroIds":["%s","%s"]
          }
          """.formatted(hogar.getCodigo(), miembroUno.getId(), miembroDos.getId())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.asignaciones.length()").value(2));

    mockMvc.perform(put("/api/v1/tareas/{id}", tarea.getId())
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "hogarCodigo":"%s",
            "titulo":"Comprar pienso actualizado",
            "esPersonal":false,
            "miembroIds":["%s"]
          }
          """.formatted(hogar.getCodigo(), miembroDos.getId())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.titulo").value("Comprar pienso actualizado"))
      .andExpect(jsonPath("$.asignaciones.length()").value(1))
      .andExpect(jsonPath("$.asignaciones[0].miembroId").value(miembroDos.getId().toString()))
      .andExpect(jsonPath("$.asignaciones[0].nombreMiembro").value("Luis"));
  }

  @Test
  void crear_rejectsMembersFromAnotherHogar() throws Exception {
    mockMvc.perform(post("/api/v1/tareas")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "hogarCodigo":"%s",
            "titulo":"Intento invalido",
            "miembroIds":["%s"]
          }
          """.formatted(hogar.getCodigo(), miembroOtroHogar.getId())))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.type").value("/errors/conflict"))
      .andExpect(jsonPath("$.detail").value(containsString("no pertenecen al hogar")));
  }

  @Test
  void crear_rejectsLegacyTechnicalFields() throws Exception {
    mockMvc.perform(post("/api/v1/tareas")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "hogarCodigo":"%s",
            "titulo":"Campo legacy",
            "idHogar":"%s",
            "idPrioridad":3
          }
          """.formatted(hogar.getCodigo(), hogar.getId())))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value("/errors/validation"))
      .andExpect(jsonPath("$.detail").value(containsString("idHogar: No usar idHogar en el contrato publico")))
      .andExpect(jsonPath("$.detail").value(containsString("idPrioridad: No usar idPrioridad en el contrato publico")));
  }

  private String registrarYObtenerToken() throws Exception {
    String email = "tarea-contract-" + UUID.randomUUID() + "@example.com";
    MvcResult result = mockMvc.perform(post("/api/v1/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "nombre": "Contrato",
            "apellidos": "Api",
            "email": "%s",
            "password": "Password123"
          }
          """.formatted(email)))
      .andExpect(status().isCreated())
      .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("token").asText();
  }
}
