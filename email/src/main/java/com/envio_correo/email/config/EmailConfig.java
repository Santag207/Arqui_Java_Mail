package com.envio_correo.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
@Configuration
@PropertySource("classpath:email.properties")
public class EmailConfig {

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        log.info("🔧 Configurando JavaMailSender...");
        log.info("📧 Username: {}", username);
        log.info("🔑 Password: {} caracteres", password != null ? password.length() : "null");
        log.info("🔑 Password (primeros 4 chars): {}", password != null ? password.substring(0, Math.min(4, password.length())) : "null");
        
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setJavaMailProperties(getMailProperties());
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            
            // Test de conexión
            mailSender.testConnection();
            log.info("✅ CONEXIÓN EXITOSA con Gmail");
            
            return mailSender;
        } catch (Exception e) {
            log.error("❌ ERROR en configuración de email: {}", e.getMessage());
            throw new RuntimeException("Error configurando email: " + e.getMessage(), e);
        }
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.debug", "true"); // Para ver logs detallados
        return properties;
    }

    @Bean
    public ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }
}