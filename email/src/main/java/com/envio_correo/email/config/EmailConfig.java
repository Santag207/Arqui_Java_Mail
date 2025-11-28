package com.envio_correo.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
@Configuration
// REMOVER ESTA LÍNEA: @PropertySource("classpath:email.properties")
public class EmailConfig {

    @Value("${spring.mail.username}")  // CAMBIAR AQUÍ
    private String username;

    @Value("${spring.mail.password}")  // CAMBIAR AQUÍ
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        log.info("🔧 Configurando JavaMailSender...");
        log.info("📧 Username: {}", username);
        log.info("🔑 Password: {} caracteres", password != null ? password.length() : "null");
        
        // Elimina esta línea que puede causar problemas de seguridad
        // log.info("🔑 Password (primeros 4 chars): {}", password != null ? password.substring(0, Math.min(4, password.length())) : "null");
        
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            mailSender.setJavaMailProperties(getMailProperties());
            
            // Comenta temporalmente el test de conexión
            // mailSender.testConnection();
            log.info("✅ JavaMailSender configurado correctamente");
            
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
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.debug", "true");
        return properties;
    }

    @Bean
    public ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }
}