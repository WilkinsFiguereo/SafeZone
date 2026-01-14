package com.wilkins.safezone.frontend.ui.Moderator.screens.Survey


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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wilkins.safezone.backend.network.Moderator.Survery.CreateQuestionRequest
import kotlinx.coroutines.launch
import java.util.UUID
import com.wilkins.safezone.backend.network.Moderator.Survery.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyCreateScreen(
    navController: NavController,
    viewModel: SurveyViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var questionsList by remember { mutableStateOf(listOf<QuestionData>()) }

    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.clearMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Encuesta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    questionsList = questionsList + QuestionData(id = UUID.randomUUID().toString())
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Pregunta")
            }
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(text = it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            successMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(text = it, modifier = Modifier.padding(16.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            validationError?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(text = it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la Encuesta *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            HorizontalDivider()

            Text("Preguntas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (questionsList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.QuestionMark, contentDescription = "Sin preguntas", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No hay preguntas agregadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Haz clic en el botón + para agregar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                questionsList.forEachIndexed { index, question ->
                    key(question.id) {
                        EditableQuestionItem(
                            question = question,
                            questionNumber = index + 1,
                            onQuestionChange = { updatedQuestion ->
                                questionsList = questionsList.toMutableList().apply {
                                    set(index, updatedQuestion)
                                }
                            },
                            onDelete = {
                                questionsList = questionsList.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    validationError = null
                    if (title.isBlank()) {
                        validationError = "El título es obligatorio"
                        return@Button
                    }
                    if (questionsList.isEmpty()) {
                        validationError = "Debe agregar al menos una pregunta"
                        return@Button
                    }

                    questionsList.forEachIndexed { i, q ->
                        if (q.text.isBlank()) {
                            validationError = "La pregunta ${i + 1} no puede estar vacía"
                            return@Button
                        }
                        if (q.type == "multiple_choice") {
                            val validOpts = q.options.filter { it.isNotBlank() }
                            if (validOpts.isEmpty()) {
                                validationError = "La pregunta ${i + 1} debe tener al menos una opción válida"
                                return@Button
                            }
                        }
                    }

                    val surveyQuestions = questionsList.mapIndexed { i, q ->
                        CreateQuestionRequest(
                            question_text = q.text,
                            question_type = q.type,
                            options = if (q.type == "multiple_choice") {
                                q.options.filter { it.isNotBlank() }
                            } else {
                                null
                            },
                            required = q.required,
                            order = i + 1
                        )
                    }

                    viewModel.createSurvey(title, description.ifBlank { null }, surveyQuestions) {
                        scope.launch {
                            title = ""
                            description = ""
                            questionsList = emptyList()
                            kotlinx.coroutines.delay(2000)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear Encuesta", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

data class QuestionData(
    val id: String = "",
    val text: String = "",
    val type: String = "text",
    val options: List<String> = listOf("", ""),
    val required: Boolean = false
)

@Composable
fun EditableQuestionItem(
    question: QuestionData,
    questionNumber: Int,
    onQuestionChange: (QuestionData) -> Unit,
    onDelete: () -> Unit
) {
    var localText by remember { mutableStateOf(question.text) }
    var localType by remember { mutableStateOf(question.type) }
    var localOptions by remember { mutableStateOf(question.options) }
    var localRequired by remember { mutableStateOf(question.required) }

    LaunchedEffect(localText, localType, localOptions, localRequired) {
        onQuestionChange(
            question.copy(
                text = localText,
                type = localType,
                options = localOptions,
                required = localRequired
            )
        )
    }

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
                Text("Pregunta $questionNumber", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = localText,
                onValueChange = { localText = it },
                label = { Text("Texto de la pregunta *") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Text("Tipo de pregunta:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = localType == "text",
                    onClick = { localType = "text" },
                    label = { Text("Texto") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = localType == "multiple_choice",
                    onClick = { localType = "multiple_choice" },
                    label = { Text("Opciones") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = localType == "number",
                    onClick = { localType = "number" },
                    label = { Text("Número") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = localType == "boolean",
                    onClick = { localType = "boolean" },
                    label = { Text("Sí/No") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (localType == "multiple_choice") {
                Text("Opciones:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                localOptions.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                localOptions = localOptions.toMutableList().apply {
                                    set(index, newValue)
                                }
                            },
                            label = { Text("Opción ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (localOptions.size > 2) {
                                    localOptions = localOptions.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            },
                            enabled = localOptions.size > 2
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Eliminar",
                                tint = if (localOptions.size > 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        localOptions = localOptions + ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar opción")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pregunta obligatoria", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = localRequired,
                    onCheckedChange = { localRequired = it }
                )
            }
        }
    }
}