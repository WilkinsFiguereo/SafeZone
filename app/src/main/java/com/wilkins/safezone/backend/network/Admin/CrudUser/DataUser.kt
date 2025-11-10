package com.wilkins.safezone.backend.network.Admin.CrudUser

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: String,
    val idCompleto: String,
    val nombre: String,
    val rol: String,
    val roleId: Int,
    val email: String? = null,
    val telefono: String? = null, // ✅ Agregar este campo
    val photoProfile: String? = null,
    val createdAt: String,
    val estado: String = "Activo" // ✅ Agregar este campo
)