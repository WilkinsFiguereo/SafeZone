package com.wilkins.safezone.backend.network.Admin.CrudUser

import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class CreateUserService {

    private val supabase = SupabaseService.getInstance()
    private val httpClient = HttpClient(Android)

    // URL de tu proyecto Supabase
    private val supabaseUrl = "https://qkaknqaxezztqnugawrd.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFrYWtucWF4ZXp6dHFudWdhd3JkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk1MTkwMzAsImV4cCI6MjA3NTA5NTAzMH0.xOcCNhtp8UhgOL7PoKCmHKG4wTgwLb2d95LuXEEpMi8"

    /**
     * Crear usuario mediante Edge Function
     */
    suspend fun createVerifiedUser(request: CreateUserRequest): CreateUserResponse {
        return try {
            println("🔄 Iniciando creación de usuario: ${request.email}")

            // Obtener el token del usuario actual
            val currentSession = supabase.auth.currentSessionOrNull()
            val authToken = currentSession?.accessToken

            if (authToken == null) {
                println("❌ No hay sesión activa")
                return CreateUserResponse(
                    success = false,
                    message = "No estás autenticado. Por favor inicia sesión."
                )
            }

            println("🔑 Token obtenido, preparando petición...")

            // Preparar datos para la Edge Function
            val requestBody = buildJsonObject {
                put("name", request.name)
                put("email", request.email)
                put("password", request.password)
                put("phone", request.phone ?: "")
                put("address", request.address ?: "")
                put("roleId", request.roleId)
                put("statusId", request.statusId)
            }

            println("📤 Enviando petición a Edge Function...")
            println("📍 URL: $supabaseUrl/functions/v1/create-user")

            // Llamar a la Edge Function usando Ktor
            val response: HttpResponse = httpClient.post("$supabaseUrl/functions/v1/create-user") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $authToken")
                    append("apikey", supabaseKey)
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestBody.toString())
            }

            val responseText = response.bodyAsText()
            println("📥 Respuesta recibida (${response.status}): $responseText")

            // Parsear respuesta
            val json = Json { ignoreUnknownKeys = true }

            return when {
                response.status.value in 200..299 -> {
                    val result = json.decodeFromString<CreateUserResponse>(responseText)
                    if (result.success) {
                        println("✅ Usuario creado exitosamente: ${result.userId}")
                    } else {
                        println("⚠️ Respuesta exitosa pero con error: ${result.message}")
                    }
                    result
                }
                response.status.value == 401 -> {
                    CreateUserResponse(
                        success = false,
                        message = "No estás autenticado correctamente"
                    )
                }
                response.status.value == 403 -> {
                    CreateUserResponse(
                        success = false,
                        message = "No tienes permisos de administrador"
                    )
                }
                else -> {
                    val result = try {
                        json.decodeFromString<CreateUserResponse>(responseText)
                    } catch (e: Exception) {
                        CreateUserResponse(
                            success = false,
                            message = "Error del servidor: ${response.status.description}"
                        )
                    }
                    result
                }
            }

        } catch (e: Exception) {
            println("❌ Error llamando a Edge Function: ${e.message}")
            e.printStackTrace()

            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true ||
                        e.message?.contains("Failed to connect") == true ->
                    "Error de conexión. Verifica tu internet."

                e.message?.contains("timeout") == true ->
                    "La petición tardó demasiado. Intenta de nuevo."

                e.message?.contains("401") == true ->
                    "No estás autenticado. Por favor inicia sesión."

                e.message?.contains("403") == true ->
                    "No tienes permisos de administrador"

                e.message?.contains("duplicate key") == true ||
                        e.message?.contains("already exists") == true ||
                        e.message?.contains("ya está registrado") == true ->
                    "El email ${request.email} ya está registrado"

                else -> "Error: ${e.message ?: "Error desconocido"}"
            }

            CreateUserResponse(
                success = false,
                message = errorMessage
            )
        }
    }

    /**
     * Validar que el email no exista
     */
    suspend fun emailExists(email: String): Boolean {
        return try {
            println("🔍 Verificando email: $email")

            val result = supabase.from("profiles")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<Profile>()

            val exists = result.isNotEmpty()
            println("${if (exists) "⚠️" else "✅"} Email ${if (exists) "ya existe" else "disponible"}")

            exists

        } catch (e: Exception) {
            println("❌ Error verificando email: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Cerrar el cliente HTTP al destruir el servicio
     */
    fun close() {
        httpClient.close()
        println("🧹 HttpClient cerrado")
    }
}