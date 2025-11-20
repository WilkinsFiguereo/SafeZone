<<<<<<<< HEAD:app/src/main/java/com/wilkins/safezone/backend/network/AppUser.kt
package com.wilkins.safezone.backend.network
import kotlinx.serialization.SerialName
========
package com.wilkins.alertaya.frontend.ui.network
>>>>>>>> ca56ce16a5f9ae9a4b46b712c16bb89abf6ea575:app/src/main/java/com/wilkins/alertaya/frontend/ui/network/AppUser.kt
import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: String,
    val name: String?,
    @SerialName("role_id")
    val role_id: Int?,
    val status_id: Int?,
    val email: String? = null,
    val emailConfirmedAt: String? = null,
    val confirmedAt: String? = null
)

