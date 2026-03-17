package com.proyecto.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity // Indica que es una entidad en el mapeado
@Table(name = "usuarios") // establece la referencia a la tabla correspondiente
@Data // Crea getters, setters, toString, equals y hashcode por defecto para la clase (implicitos)
@NoArgsConstructor // Constructor sin parámetros por defecto
@AllArgsConstructor // Constructor con todos los parámetros automático
public class User {
    
    @Id // Es un id, hay una etiqueta para ello
    private Long id; 
    
    // Nombre de la columna dentro de la base de datos
    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(length = 200)
    private String direccion;
    
    @Column(name = "password_hash", length = 200)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
