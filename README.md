# 📧 Arqui_Java_Mail

[![Estado del proyecto](https://img.shields.io/badge/status-Experimental-orange.svg)]()  
[![Java version](https://img.shields.io/badge/Java-8%2B-blue.svg)]()  

## 📖 Descripción

**Arqui_Java_Mail** es una aplicación Java con interfaz gráfica que permite enviar correos electrónicos a través de protocolo SMTP.  
Está especialmente diseñada como proyecto educativo / de aprendizaje para comprender y practicar el uso de la API JavaMail API y el envío de correos programático, con soporte para múltiples destinatarios, TLS/SSL, y configuración flexible mediante un archivo de propiedades.  

Este proyecto demuestra cómo combinar lógica de envío de correos, manejo de MIME, autenticación SMTP y una interfaz gráfica básica con Java Swing, ideal para quien empieza a trabajar con correo electrónico desde Java.

---

## 📁 Estructura del Proyecto


---

## 🛠 Tecnologías utilizadas

- **Java 8+** — Lenguaje de programación  
- **JavaMail API 1.6.2** — Manejo de correos electrónicos (SMTP, MIME)  
- **JAF (JavaBeans Activation Framework)** — Manejo de tipos MIME  
- **Java Swing** — Interfaz gráfica  

---

## ⚙️ Configuración Requerida

### Prerrequisitos

- Java JDK 8 o superior  
- Conexión a Internet  
- Credenciales válidas de un servicio de correo (por ejemplo Gmail, Outlook, etc.)

### Configuración SMTP

Editar el archivo `config.properties`, por ejemplo:

```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.user=tu.email@gmail.com
mail.password=tu-contraseña-app
