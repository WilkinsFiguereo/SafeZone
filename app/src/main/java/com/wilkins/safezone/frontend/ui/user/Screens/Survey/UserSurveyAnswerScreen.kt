package com.wilkins.safezone.frontend.ui.user.Screens.Survey


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Moderator.Survery.SurveyQuestion
import com.wilkins.safezone.backend.network.Moderator.Survery.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSurveyAnswerScreen(
    navController: NavController,
    surveyId: String,
    userId: String,
    viewModel: SurveyViewModel = viewModel()
) {
    val selectedSurvey by viewModel.selectedSurvey.collectAsState()
    val questions by viewModel.surveyQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val answers = remember { mutableStateMapOf<String, String>() }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var hasAlreadyResponded by remember { mutableStateOf(false) }
    var checkingResponse by remember { mutableStateOf(true) }

    // Verificar si el usuario ya respondió esta encuesta
    LaunchedEffect(surveyId, userId) {
        checkingResponse = true
        viewModel.hasUserResponded(surveyId, userId) { hasResponded ->
            hasAlreadyResponded = hasResponded
            checkingResponse = false
        }
        viewModel.fetchSurveyById(surveyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Responder Encuesta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            validationError?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (checkingResponse || isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (hasAlreadyResponded) {
                // Mostrar mensaje de que ya respondió
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completado",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Ya respondiste esta encuesta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Gracias por tu participación. Solo puedes responder una vez por encuesta.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver")
                        }
                    }
                }
            } else {
                selectedSurvey?.let { survey ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = survey.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            survey.description?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Solo puedes responder una vez",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (questions.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            questions.forEach { question ->
                                QuestionAnswerCard(
                                    question = question,
                                    answer = answers[question.id] ?: "",
                                    onAnswerChange = { newAnswer ->
                                        answers[question.id] = newAnswer
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                validationError = null

                                val missingRequired = questions.filter { it.required }
                                    .find { answers[it.id].isNullOrBlank() }

                                if (missingRequired != null) {
                                    validationError = "Por favor responde todas las preguntas obligatorias"
                                    return@Button
                                }

                                viewModel.submitSurveyResponses(
                                    surveyId = surveyId,
                                    userId = userId,
                                    answers = answers.toMap()
                                ) {
                                    showSuccessDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    "Enviar Respuestas",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Encuesta Enviada!") },
            text = { Text("Gracias por responder la encuesta. Tus respuestas han sido registradas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun QuestionAnswerCard(
    question: SurveyQuestion,
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question.question_text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (question.required) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text("Obligatoria")
                    }
                }
            }

            when (question.question_type) {
                "text" -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = onAnswerChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escribe tu respuesta...") },
                        maxLines = 4
                    )
                }

                "multiple_choice" -> {
                    question.options?.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == option,
                                onClick = { onAnswerChange(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }

                "number" -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { if (it.all { char -> char.isDigit() }) onAnswerChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ingresa un número...") },
                        singleLine = true
                    )
                }

                "boolean" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilterChip(
                            selected = answer == "Sí",
                            onClick = { onAnswerChange("Sí") },
                            label = { Text("Sí") },
                            leadingIcon = {
                                if (answer == "Sí") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = answer == "No",
                            onClick = { onAnswerChange("No") },
                            label = { Text("No") },
                            leadingIcon = {
                                if (answer == "No") {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}