#!/bin/bash

# Script para probar los endpoints de autenticación
# Ejecutar con: bash test_auth.sh

BASE_URL="http://localhost:8081"

echo "========================================"
echo "PRUEBAS DE AUTENTICACIÓN - AUTH SERVICE"
echo "========================================"
echo ""

# 1. Health Check
echo "1. Verificando servicio (Health Check)..."
curl -X GET "$BASE_URL/auth/health" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# 2. Login con usuario válido
echo "2. Login con usuario CLIENTE..."
curl -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@mail.com",
    "password": "user123"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 3. Login con admin
echo "3. Login con usuario ADMIN..."
curl -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@mail.com",
    "password": "admin123"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 4. Login con credenciales incorrectas
echo "4. Login con credenciales incorrectas..."
curl -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@mail.com",
    "password": "wrongpassword"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 5. Registrar nuevo usuario
echo "5. Registrar nuevo usuario..."
curl -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@mail.com",
    "password": "newpass123",
    "rol": "CLIENTE"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 6. Obtener token y usarlo para validar
echo "6. Obtener token para validación..."
TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@mail.com",
    "password": "user123"
  }')

# Extraer token usando jq si está disponible, o grep
if command -v jq &> /dev/null; then
  TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.token')
else
  TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
fi

echo "Token obtenido: ${TOKEN:0:50}..."
echo ""

# 7. Validar token
echo "7. Validar token..."
curl -X GET "$BASE_URL/auth/validate" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "========================================"
echo "PRUEBAS COMPLETADAS"
echo "========================================"
