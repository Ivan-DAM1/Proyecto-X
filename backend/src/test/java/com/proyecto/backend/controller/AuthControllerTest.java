package com.proyecto.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.backend.model.User;
import com.proyecto.backend.repository.UserRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Servidor WireMock que simula un servicio externo
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        // Levanta WireMock en el puerto 9090
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9090));
        wireMockServer.start();
        WireMock.configureFor("localhost", 9090);

        // Simula que un servicio externo de validación de emails responde OK
        stubFor(get(urlEqualTo("/validate-email/test@tienda.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": true, \"email\": \"test@tienda.com\"}")));

        // Simula que el servicio externo rechaza un email inválido
        stubFor(get(urlEqualTo("/validate-email/invalido@spam.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": false, \"email\": \"invalido@spam.com\"}")));

        // Simula respuesta de un servicio externo de autenticación OAuth
        stubFor(post(urlEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"fake-token-123\", \"token_type\": \"Bearer\"}")));

        // Limpia usuarios de test anteriores
        userRepository.findByEmail("test@tienda.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("login@tienda.com").ifPresent(userRepository::delete);
    }

    @AfterEach
    void tearDown() {
        // Para el servidor WireMock después de cada test
        wireMockServer.stop();
        // Limpia usuarios creados en los tests
        userRepository.findByEmail("test@tienda.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("login@tienda.com").ifPresent(userRepository::delete);
    }

    // POST /api/auth/register → registro correcto debe devolver 201
    @Test
    void register_conDatosValidos_debeRetornar201() throws Exception {
        Map<String, String> request = Map.of(
                "nombreCompleto", "Usuario Test",
                "email", "test@tienda.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@tienda.com"))
                .andExpect(jsonPath("$.nombreCompleto").value("Usuario Test"));
    }

    // POST /api/auth/register → email duplicado debe devolver 409
    @Test
    void register_conEmailDuplicado_debeRetornar409() throws Exception {
        // Primero creamos el usuario
        User user = new User();
        user.setId(9991L);
        user.setNombreCompleto("Usuario Existente");
        user.setEmail("test@tienda.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

        // Intentamos registrar el mismo email
        Map<String, String> request = Map.of(
                "nombreCompleto", "Otro Usuario",
                "email", "test@tienda.com",
                "password", "otrapassword"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El email ya está registrado"));
    }

    // POST /api/auth/register → datos vacíos debe devolver 400
    @Test
    void register_conDatosVacios_debeRetornar400() throws Exception {
        Map<String, String> request = Map.of(
                "nombreCompleto", "",
                "email", "",
                "password", ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"));
    }

    // POST /api/auth/login → credenciales correctas debe devolver 200
    @Test
    void login_conCredencialesCorrectas_debeRetornar200() throws Exception {
        // Creamos el usuario primero
        User user = new User();
        user.setId(9992L);
        user.setNombreCompleto("Usuario Login");
        user.setEmail("login@tienda.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

        Map<String, String> request = Map.of(
                "email", "login@tienda.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("login@tienda.com"));
    }

    // POST /api/auth/login → contraseña incorrecta debe devolver 401
    @Test
    void login_conPasswordIncorrecta_debeRetornar401() throws Exception {
        User user = new User();
        user.setId(9993L);
        user.setNombreCompleto("Usuario Login");
        user.setEmail("login@tienda.com");
        user.setPasswordHash(passwordEncoder.encode("passwordcorrecta"));
        userRepository.save(user);

        Map<String, String> request = Map.of(
                "email", "login@tienda.com",
                "password", "passwordincorrecta"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    // Verifica que WireMock responde correctamente simulando servicio externo
    @Test
    void wireMock_simulaServicioExterno_debeResponderCorrectamente() throws Exception {
        // Verificamos que WireMock está funcionando como servidor externo falso
        // Esto simula lo que haría tu app al llamar a un servicio externo
        org.springframework.web.client.RestTemplate restTemplate = 
            new org.springframework.web.client.RestTemplate();
        
        String response = restTemplate.getForObject(
            "http://localhost:9090/validate-email/test@tienda.com", 
            String.class
        );

        assert response != null;
        assert response.contains("true");

        // Verificamos que WireMock registró la llamada
        verify(getRequestedFor(urlEqualTo("/validate-email/test@tienda.com")));
    }

    // Verifica que WireMock simula correctamente el endpoint OAuth
    @Test
    void wireMock_simulaOAuth_debeRetornarTokenFalso() throws Exception {
        org.springframework.web.client.RestTemplate restTemplate = 
            new org.springframework.web.client.RestTemplate();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<String> entity = 
            new org.springframework.http.HttpEntity<>("{}", headers);

        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:9090/oauth/token",
            entity,
            String.class
        );

        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().contains("fake-token-123");

        // Verificamos que WireMock registró la llamada al endpoint OAuth
        verify(postRequestedFor(urlEqualTo("/oauth/token")));
    }
}