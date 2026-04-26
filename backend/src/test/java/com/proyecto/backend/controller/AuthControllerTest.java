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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
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

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9090));
        wireMockServer.start();
        WireMock.configureFor("localhost", 9090);

        stubFor(get(urlEqualTo("/validate-email/test@tienda.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": true, \"email\": \"test@tienda.com\"}")));

        stubFor(get(urlEqualTo("/validate-email/invalido@spam.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\": false, \"email\": \"invalido@spam.com\"}")));

        stubFor(WireMock.post(urlEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"fake-token-123\", \"token_type\": \"Bearer\"}")));

        userRepository.findByEmail("test@tienda.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("login@tienda.com").ifPresent(userRepository::delete);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
        userRepository.findByEmail("test@tienda.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("login@tienda.com").ifPresent(userRepository::delete);
    }

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

    @Test
    void register_conEmailDuplicado_debeRetornar409() throws Exception {
        User user = new User();
        user.setId(9991L);
        user.setNombreCompleto("Usuario Existente");
        user.setEmail("test@tienda.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

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

    @Test
    void login_conCredencialesCorrectas_debeRetornar200() throws Exception {
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

    @Test
    void wireMock_simulaServicioExterno_debeResponderCorrectamente() throws Exception {
        org.springframework.web.client.RestTemplate restTemplate =
                new org.springframework.web.client.RestTemplate();

        String response = restTemplate.getForObject(
                "http://localhost:9090/validate-email/test@tienda.com",
                String.class
        );

        assert response != null;
        assert response.contains("true");

        verify(getRequestedFor(urlEqualTo("/validate-email/test@tienda.com")));
    }

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

        verify(postRequestedFor(urlEqualTo("/oauth/token")));
    }
}