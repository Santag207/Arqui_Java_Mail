package com.tvp.auth.controller;

import com.tvp.auth.dto.LoginRequest;
import com.tvp.auth.dto.LoginResponse;
import com.tvp.auth.dto.RegistroRequest;
import com.tvp.auth.entity.Usuario;
import com.tvp.auth.repository.UsuarioRepository;
import com.tvp.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080", "*"}, 
             allowedHeaders = "*", 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email y password son requeridos"));
            }
            
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest dto) {
        try {
            // Validaciones
            if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email es requerido"));
            }
            
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Password es requerido"));
            }
            
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Email ya está registrado"));
            }

            // Crear nuevo usuario
            Usuario usuario = Usuario.builder()
                    .email(dto.getEmail())
                    .password(dto.getPassword()) // TODO: Encriptar con BCrypt
                    .rol(dto.getRol() != null ? dto.getRol() : Usuario.Rol.CLIENTE)
                    .build();

            usuarioRepository.save(usuario);

            // Devolver token
            LoginResponse response = authService.login(LoginRequest.builder()
                    .email(usuario.getEmail())
                    .password(dto.getPassword())
                    .build());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error al registrar: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token inválido o no proporcionado"));
            }
            
            String jwt = token.substring(7);
            boolean isValid = authService.validateToken(jwt);
            
            if (isValid) {
                String email = authService.getEmailFromToken(jwt);
                Long usuarioId = authService.getUsuarioIdFromToken(jwt);
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("email", email);
                response.put("usuarioId", usuarioId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token expirado o inválido"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Error validando token: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(createSuccessResponse("Auth service está funcionando"));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
}