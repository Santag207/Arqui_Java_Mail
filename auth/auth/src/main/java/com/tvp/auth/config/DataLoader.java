package com.tvp.auth.config;

import com.tvp.auth.entity.Usuario;
import com.tvp.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(UsuarioRepository repo) {
        return args -> {
            // Cargar usuarios de prueba
            if (repo.findByEmail("admin@mail.com").isEmpty()) {
                repo.save(Usuario.builder()
                        .email("admin@mail.com")
                        .password("admin123")
                        .rol(Usuario.Rol.ADMIN)
                        .build());
            }

            if (repo.findByEmail("user@mail.com").isEmpty()) {
                repo.save(Usuario.builder()
                        .email("user@mail.com")
                        .password("user123")
                        .rol(Usuario.Rol.CLIENTE)
                        .build());
            }

            if (repo.findByEmail("agent@mail.com").isEmpty()) {
                repo.save(Usuario.builder()
                        .email("agent@mail.com")
                        .password("agent123")
                        .rol(Usuario.Rol.AGENTE)
                        .build());
            }

            System.out.println("✓ Usuarios de prueba cargados");
        };
    }
}