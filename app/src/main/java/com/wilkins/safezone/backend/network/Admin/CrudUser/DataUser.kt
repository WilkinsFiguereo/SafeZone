package com.wilkins.safezone.backend.network.Admin.CrudUser


data class Usuario(
    val id: String,
    val idCompleto: String,
    val nombre: String,
    val rol: String,
    val roleId: Int,
    val email: String?,
    val photoProfile: String?,
    val createdAt: String
)