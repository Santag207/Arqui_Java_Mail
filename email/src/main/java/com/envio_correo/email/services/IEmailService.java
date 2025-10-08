package com.envio_correo.email.services;

import com.envio_correo.email.services.models.EmailDTO;
import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendMail(EmailDTO email) throws MessagingException;
}