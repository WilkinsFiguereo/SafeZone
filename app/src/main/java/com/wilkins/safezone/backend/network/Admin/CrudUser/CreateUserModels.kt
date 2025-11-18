package com.wilkins.safezone.backend.network.Admin.CrudUser

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val address: String? = null,
    val roleId: Int = 1,
    val statusId: Int = 1
)

@Serializable
data class CreateUserResponse(
    val success: Boolean = false,
    val userId: String? = null,
    val message: String = "",
    val error: String? = null
)
