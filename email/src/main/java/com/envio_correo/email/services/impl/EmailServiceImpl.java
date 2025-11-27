package com.envio_correo.email.services.impl;

import com.envio_correo.email.entities.EmailLog;
import com.envio_correo.email.repositories.EmailLogRepository;
import com.envio_correo.email.services.IEmailService;
import com.envio_correo.email.services.models.EmailDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Service
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    public EmailServiceImpl(JavaMailSender javaMailSender, 
                           TemplateEngine templateEngine,
                           EmailLogRepository emailLogRepository) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.emailLogRepository = emailLogRepository;
    }

    @Override
    public void sendMail(EmailDTO email) throws MessagingException {
        // For testing: override all recipients to the test address
        final String TEST_EMAIL = "castrosantiago476@gmail.com";
        log.info("🔄 INICIANDO ENVÍO DE CORREO (original destinatario: {})", email.getDestinatario());
        email.setDestinatario(TEST_EMAIL);
        log.info("🔁 Destinatario forzado a: {}", TEST_EMAIL);
        
        // 1. Validar datos de entrada
        if (email.getDestinatario() == null || email.getDestinatario().trim().isEmpty()) {
            log.error("❌ DESTINATARIO VACÍO - No se puede enviar correo");
            throw new MessagingException("El destinatario no puede estar vacío");
        }
        
        // 2. Guardar log INICIAL (transacción separada)
        Long logId = guardarLogInicial(email);
        if (logId == null) {
            log.error("❌ ERROR CRÍTICO - No se pudo guardar log inicial");
            throw new MessagingException("Error al guardar log inicial");
        }
        
        try {
            // 3. Enviar correo (sin transacción para evitar rollback)
            log.info("📤 ENVIANDO CORREO REAL a: {}", email.getDestinatario());
            enviarCorreoReal(email);
            
            // 4. Actualizar a ÉXITO (transacción separada)
            actualizarLogExitoso(logId);
            
            log.info("✅ CORREO ENVIADO EXITOSAMENTE - ID: {} - Para: {}", 
                     logId, email.getDestinatario());
            
        } catch (Exception e) {
            // 5. Actualizar a ERROR (transacción separada)
            actualizarLogError(logId, e.getMessage());
            
            log.error("❌ ERROR AL ENVIAR CORREO - ID: {} - Para: {} - Error: {}", 
                      logId, email.getDestinatario(), e.getMessage());
            
            throw new MessagingException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda el log inicial - TRANSACCIÓN SEPARADA
     */
    @Transactional
    public Long guardarLogInicial(EmailDTO email) {
        try {
            EmailLog emailLog = new EmailLog();
            emailLog.setDestinatario(email.getDestinatario().trim());
            emailLog.setAsunto(email.getAsunto() != null ? email.getAsunto() : "Sin asunto");
            emailLog.setMensaje(email.getMensaje() != null ? email.getMensaje() : "");
            emailLog.setFechaEnvio(LocalDateTime.now());
            emailLog.setEstado("EN_PROCESO");
            
            EmailLog savedLog = emailLogRepository.save(emailLog);
            log.info("💾 LOG INICIAL GUARDADO - ID: {} - Estado: EN_PROCESO", savedLog.getId());
            return savedLog.getId();
            
        } catch (Exception e) {
            log.error("❌ ERROR CRÍTICO: No se pudo guardar log inicial - {}", e.getMessage());
            return null;
        }
    }

    /**
     * Actualiza log a estado exitoso - TRANSACCIÓN SEPARADA
     */
    @Transactional
    public void actualizarLogExitoso(Long logId) {
        try {
            emailLogRepository.findById(logId).ifPresent(emailLog -> {
                emailLog.setEstado("ENVIADO");
                emailLogRepository.save(emailLog);
                log.info("✏️ LOG ACTUALIZADO - ID: {} - Estado: ENVIADO", logId);
            });
        } catch (Exception e) {
            log.error("❌ ERROR actualizando log a ÉXITO - ID: {}: {}", logId, e.getMessage());
        }
    }

    /**
     * Actualiza log a estado error - TRANSACCIÓN SEPARADA  
     */
    @Transactional
    public void actualizarLogError(Long logId, String error) {
        try {
            emailLogRepository.findById(logId).ifPresent(emailLog -> {
                emailLog.setEstado("ERROR");
                emailLog.setError(error != null ? error : "Error desconocido");
                emailLogRepository.save(emailLog);
                log.info("✏️ LOG ACTUALIZADO - ID: {} - Estado: ERROR", logId);
            });
        } catch (Exception e) {
            log.error("❌ ERROR actualizando log a ERROR - ID: {}: {}", logId, e.getMessage());
        }
    }

    /**
     * ENVÍO REAL DE CORREO - VERSIÓN PRODUCCIÓN
     */
    private void enviarCorreoReal(EmailDTO email) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Configurar destinatario y asunto
        helper.setTo(email.getDestinatario().trim());
        helper.setSubject(email.getAsunto() != null ? email.getAsunto() : "Sin asunto");

        // Procesar plantilla Thymeleaf
        Context context = new Context();
        context.setVariable("message", email.getMensaje() != null ? email.getMensaje() : "");
        context.setVariable("fecha", LocalDateTime.now().toString());
        String contentHTML = templateEngine.process("email.html", context);

        helper.setText(contentHTML, true);
        
        log.info("🚀 Ejecutando envío mediante SMTP...");
        
        // Enviar correo
        javaMailSender.send(message);
        
        log.info("📨 Correo enviado exitosamente a: {}", email.getDestinatario());
    }
}