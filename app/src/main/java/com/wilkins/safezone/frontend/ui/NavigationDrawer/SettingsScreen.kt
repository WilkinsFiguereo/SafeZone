//package com.wilkins.safezone.frontend.ui.NavigationDrawer
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.wilkins.safezone.ui.theme.PrimaryColor
//
//data class SettingsItem(
//    val title: String,
//    val icon: ImageVector,
//    val onClick: () -> Unit = {}
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsScreen(
//    navcontroller: NavController,
//    onBackClick: () -> Unit = {},
//    onNavigateToAccount: () -> Unit = {},
//    onNavigateToPrivacy: () -> Unit = {},
//    onNavigateToSecurity: () -> Unit = {},
//    onNavigateToShareProfile: () -> Unit = {},
//    onNavigateToDisplay: () -> Unit = {},
//    onNavigateToLanguage: () -> Unit = {},
//    onNavigateToAccessibility: () -> Unit = {},
//    onNavigateToNotifications: () -> Unit = {},
//    onNavigateToHelp: () -> Unit = {},
//    onNavigateToTerms: () -> Unit = {},
//    onNavigateToReportProblem: () -> Unit = {},
//    modifier: Modifier = Modifier
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Ajustes y privacidad",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Volver"
//                        )
//                    }
//
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White,
//                    titleContentColor = Color(0xFF1F1F1F)
//                )
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = modifier
//                .fillMaxSize()
//                .background(Color(0xFFF5F5F5))
//                .padding(paddingValues)
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Cuenta Section
//            SettingsSection(
//                title = "Cuenta",
//                items = listOf(
//                    SettingsItem("Cuenta", Icons.Outlined.AccountCircle, onNavigateToAccount),
//                    SettingsItem("Privacidad", Icons.Outlined.Lock, onNavigateToPrivacy),
//                    SettingsItem("Seguridad y permisos", Icons.Outlined.Shield, onNavigateToSecurity),
//                    SettingsItem("Compartir perfil", Icons.Outlined.Share, onNavigateToShareProfile)
//                )
//            )
//
//            // Configuraciones Section
//            SettingsSection(
//                title = "Configuraciones",
//                items = listOf(
//                    SettingsItem("Mostrar", Icons.Outlined.DarkMode, onNavigateToDisplay),
//                    SettingsItem("Idioma", Icons.Outlined.Language, onNavigateToLanguage),
//                    SettingsItem("Accesibilidad", Icons.Outlined.Accessibility, onNavigateToAccessibility),
//                    SettingsItem("Notificaciones", Icons.Outlined.Notifications, onNavigateToNotifications)
//                )
//            )
//
//            // Ayuda e información Section
//            SettingsSection(
//                title = "Ayuda e información",
//                items = listOf(
//                    SettingsItem("Ayuda", Icons.Outlined.Help, onNavigateToHelp),
//                    SettingsItem("Términos y condiciones", Icons.Outlined.Description, onNavigateToTerms),
//                    SettingsItem("Informa sobre un problema", Icons.Outlined.Headset, onNavigateToReportProblem)
//                )
//            )
//        }
//    }
//}
//
//@Composable
//fun SettingsSection(
//    title: String,
//    items: List<SettingsItem>,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        // Section Title
//        Text(
//            text = title,
//            fontSize = 13.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = Color(0xFF757575),
//            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
//        )
//
//        // Section Card
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = Color.White
//            ),
//            elevation = CardDefaults.cardElevation(
//                defaultElevation = 1.dp
//            )
//        ) {
//            Column(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                items.forEachIndexed { index, item ->
//                    SettingsItemRow(
//                        title = item.title,
//                        icon = item.icon,
//                        onClick = item.onClick
//                    )
//                    if (index < items.size - 1) {
//                        Divider(
//                            color = Color(0xFFF0F0F0),
//                            thickness = 1.dp,
//                            modifier = Modifier.padding(start = 56.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SettingsItemRow(
//    title: String,
//    icon: ImageVector,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(horizontal = 16.dp, vertical = 16.dp),
//        horizontalArrangement = Arrangement.spacedBy(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Icon
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = Color(0xFF1F1F1F),
//            modifier = Modifier.size(24.dp)
//        )
//
//        // Title
//        Text(
//            text = title,
//            fontSize = 15.sp,
//            color = Color(0xFF1F1F1F),
//            modifier = Modifier.weight(1f)
//        )
//
//        // Arrow
//        Icon(
//            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
//            contentDescription = null,
//            tint = Color(0xFF9E9E9E),
//            modifier = Modifier.size(24.dp)
//        )
//    }
//}
//
