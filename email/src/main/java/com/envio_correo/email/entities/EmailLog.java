package com.envio_correo.email.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String destinatario;
    
    @Column(nullable = false)
    private String asunto;
    
    @Column(columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(nullable = false)
    private String estado;
    
    @Column(columnDefinition = "TEXT")
    private String error;
    
    // Constructores
    public EmailLog() {}
    
    public EmailLog(String destinatario, String asunto, String mensaje, String estado) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaEnvio = LocalDateTime.now();
        this.estado = estado;
    }
    
    public EmailLog(String destinatario, String asunto, String mensaje, String estado, String error) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaEnvio = LocalDateTime.now();
        this.estado = estado;
        this.error = error;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}