package com.wilkins.safezone.frontend.ui.user.NavigationDrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilkins.safezone.ui.theme.PrimaryColor

data class UserProfile(
    val name: String = "",
    val pronouns: String = "",
    val status: String = "",
    val description: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val isEmailPublic: Boolean = false,
    val isPhonePublic: Boolean = false,
    val isLocationPublic: Boolean = false
)

@Composable
fun ProfileSection(
    userProfile: UserProfile = UserProfile(
        name = "Wilkins Radhames Figuereo Jimenez",
        pronouns = "El/ella/elle",
        status = "Disponible",
        description = "Usuario activo de SafeZone comprometido con la seguridad de la comunidad. Siempre dispuesto a reportar y colaborar.",
        email = "correo100%real@example.com",
        phone = "800-000-9999",
        location = "Ciudad Real"
    ),
    isFollowing: Boolean = false,
    onFollowClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onStatusChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var statusText by remember { mutableStateOf(userProfile.status) }
    var emailPublic by remember { mutableStateOf(userProfile.isEmailPublic) }
    var phonePublic by remember { mutableStateOf(userProfile.isPhonePublic) }
    var locationPublic by remember { mutableStateOf(userProfile.isLocationPublic) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Profile Card with gradient header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gradient Header Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryColor.copy(alpha = 0.8f),
                                    PrimaryColor.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar + Follow Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Enhanced Avatar with border
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                PrimaryColor.copy(alpha = 0.2f),
                                                PrimaryColor.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        // Follow Button
                        Button(
                            onClick = onFollowClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color(0xFFE8F5E9) else PrimaryColor,
                                contentColor = if (isFollowing) PrimaryColor else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isFollowing) 0.dp else 2.dp
                            )
                        ) {
                            Icon(
                                imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFollowing) "Siguiendo" else "Seguir",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Name and Pronouns
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = userProfile.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F),
                            lineHeight = 26.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = userProfile.pronouns,
                                fontSize = 12.sp,
                                color = PrimaryColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Status Input with Icon
                    OutlinedTextField(
                        value = statusText,
                        onValueChange = {
                            statusText = it
                            onStatusChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = PrimaryColor,
                            cursorColor = PrimaryColor
                        ),
                        placeholder = {
                            Text(
                                text = "Establecer estado",
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color(0xFF424242)
                        )
                    )

                    // Description with card background
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F9FA)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = userProfile.description,
                                fontSize = 13.sp,
                                color = Color(0xFF616161),
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Edit Profile Button
                    OutlinedButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryColor.copy(alpha = 0.5f),
                                    PrimaryColor
                                )
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryColor
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editar perfil",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp
                    )

                    // Contact Info Section with better styling
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Información de contacto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        EnhancedContactInfoRow(
                            icon = Icons.Outlined.Email,
                            text = userProfile.email,
                            checked = emailPublic,
                            onCheckedChange = { emailPublic = it }
                        )
                        EnhancedContactInfoRow(
                            icon = Icons.Outlined.Phone,
                            text = userProfile.phone,
                            checked = phonePublic,
                            onCheckedChange = { phonePublic = it }
                        )
                        EnhancedContactInfoRow(
                            icon = Icons.Outlined.LocationOn,
                            text = userProfile.location,
                            checked = locationPublic,
                            onCheckedChange = { locationPublic = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedContactInfoRow(
    icon: ImageVector,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (checked) PrimaryColor.copy(alpha = 0.08f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) PrimaryColor else Color(0xFF9E9E9E),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryColor,
                    uncheckedColor = Color(0xFFBDBDBD)
                ),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSectionPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        ProfileSection()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSectionFollowingPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        ProfileSection(isFollowing = true)
    }
}