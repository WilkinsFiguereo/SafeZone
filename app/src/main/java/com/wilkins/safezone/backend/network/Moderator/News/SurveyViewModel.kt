package com.wilkins.safezone.backend.network.Moderator.News


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService

@Serializable
data class Survey(
    val id: String,
    val title: String,
    val description: String?,
    val created_at: String
)

@Serializable
data class SurveyQuestion(
    val id: String,
    val survey_id: String,
    val question_text: String,
    val question_type: String,
    val options: List<String> = emptyList(),
    val required: Boolean,
    val order: Int,
    val created_at: String
)

@Serializable
data class CreateSurveyRequest(
    val title: String,
    val description: String?,
    val questions: List<CreateQuestionRequest>
)

@Serializable
data class CreateQuestionRequest(
    val question_text: String,
    val question_type: String,
    val options: List<String>? = null,
    val required: Boolean = false,
    val order: Int
)

@Serializable
data class SurveyResponse(
    val id: String,
    val survey_id: String,
    val user_id: String,
    val question_id: String,
    val answer_text: String,
    val created_at: String
)

class SurveyViewModel : ViewModel() {

    private val supabase = SupabaseService.getInstance()

    private val _surveys = MutableStateFlow<List<Survey>>(emptyList())
    val surveys: StateFlow<List<Survey>> = _surveys

    private val _surveyQuestions = MutableStateFlow<List<SurveyQuestion>>(emptyList())
    val surveyQuestions: StateFlow<List<SurveyQuestion>> = _surveyQuestions

    private val _selectedSurvey = MutableStateFlow<Survey?>(null)
    val selectedSurvey: StateFlow<Survey?> = _selectedSurvey

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _surveyResults = MutableStateFlow<Map<String, List<SurveyResponse>>>(emptyMap())
    val surveyResults: StateFlow<Map<String, List<SurveyResponse>>> = _surveyResults

    fun fetchSurveys() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val surveysList = supabase
                    .from("surveys")
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Survey>()

                _surveys.value = surveysList
            } catch (e: Exception) {
                _error.value = "Error al cargar encuestas: ${e.message}"
                Log.e("SurveyViewModel", "Error al cargar encuestas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSurveyById(surveyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val survey = supabase
                    .from("surveys")
                    .select {
                        filter { eq("id", surveyId) }
                    }
                    .decodeSingle<Survey>()

                _selectedSurvey.value = survey
                fetchQuestionsBySurvey(surveyId)
                fetchSurveyResults(surveyId)

            } catch (e: Exception) {
                _error.value = "Error al cargar encuesta: ${e.message}"
                Log.e("SurveyViewModel", "Error al cargar encuesta", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchQuestionsBySurvey(surveyId: String) {
        viewModelScope.launch {
            try {
                val questions = supabase
                    .from("survey_questions")
                    .select {
                        filter { eq("survey_id", surveyId) }
                        order("order", Order.ASCENDING)
                    }
                    .decodeList<SurveyQuestion>()

                _surveyQuestions.value = questions
                Log.d("SurveyViewModel", "Preguntas cargadas: ${questions.size}")
            } catch (e: Exception) {
                _error.value = "Error al cargar preguntas: ${e.message}"
                Log.e("SurveyViewModel", "Error al cargar preguntas", e)
            }
        }
    }

    fun createSurvey(
        title: String,
        description: String?,
        questions: List<CreateQuestionRequest>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                // Validar preguntas
                questions.forEachIndexed { index, question ->
                    if (question.question_text.isBlank()) {
                        _error.value = "La pregunta ${index + 1} no puede estar vacía"
                        _isLoading.value = false
                        return@launch
                    }

                    if (question.question_type == "multiple_choice") {
                        val validOptions = question.options?.filter { it.isNotBlank() } ?: emptyList()
                        if (validOptions.isEmpty()) {
                            _error.value = "La pregunta ${index + 1} de tipo 'Opciones' debe tener al menos una opción válida"
                            _isLoading.value = false
                            return@launch
                        }
                    }
                }

                // Crear la encuesta
                val surveyData = mapOf(
                    "title" to title,
                    "description" to description
                )

                val surveyResponse = supabase
                    .from("surveys")
                    .insert(surveyData)
                    .decodeSingle<Survey>()

                Log.d("SurveyViewModel", "Encuesta creada con ID: ${surveyResponse.id}")

                // Insertar preguntas usando una estrategia diferente
                questions.forEach { question ->
                    try {
                        // Crear el objeto según el tipo
                        when (question.question_type) {
                            "multiple_choice" -> {
                                val validOptions = question.options?.filter { it.isNotBlank() } ?: emptyList()

                                // NO decodificar la respuesta, solo insertar
                                supabase.from("survey_questions").insert(
                                    mapOf(
                                        "survey_id" to surveyResponse.id,
                                        "question_text" to question.question_text,
                                        "question_type" to question.question_type,
                                        "options" to validOptions,
                                        "required" to question.required,
                                        "order" to question.order
                                    )
                                ) {
                                    select()
                                }
                                Log.d("SurveyViewModel", "✓ Multiple choice insertada con ${validOptions.size} opciones")
                            }

                            "boolean" -> {
                                supabase.from("survey_questions").insert(
                                    mapOf(
                                        "survey_id" to surveyResponse.id,
                                        "question_text" to question.question_text,
                                        "question_type" to question.question_type,
                                        "options" to listOf("Sí", "No"),
                                        "required" to question.required,
                                        "order" to question.order
                                    )
                                ) {
                                    select()
                                }
                                Log.d("SurveyViewModel", "✓ Boolean insertada")
                            }

                            else -> {
                                // Para text y number, enviar array vacío explícitamente
                                supabase.from("survey_questions").insert(
                                    mapOf(
                                        "survey_id" to surveyResponse.id,
                                        "question_text" to question.question_text,
                                        "question_type" to question.question_type,
                                        "options" to emptyList<String>(),
                                        "required" to question.required,
                                        "order" to question.order
                                    )
                                ) {
                                    select()
                                }
                                Log.d("SurveyViewModel", "✓ ${question.question_type} insertada")
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("SurveyViewModel", "✗ Error insertando: ${question.question_text}")
                        Log.e("SurveyViewModel", "Tipo: ${question.question_type}")
                        Log.e("SurveyViewModel", "Error completo: ${e.message}")
                        Log.e("SurveyViewModel", "Stack trace:", e)
                        throw e
                    }
                }

                _successMessage.value = "Encuesta creada exitosamente"
                fetchSurveys()
                onSuccess()

            } catch (e: Exception) {
                _error.value = "Error al crear encuesta: ${e.message}"
                Log.e("SurveyViewModel", "Error al crear encuesta", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSurvey(surveyId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                supabase
                    .from("surveys")
                    .delete {
                        filter { eq("id", surveyId) }
                    }

                _successMessage.value = "Encuesta eliminada exitosamente"
                fetchSurveys()
                onSuccess()

            } catch (e: Exception) {
                _error.value = "Error al eliminar encuesta: ${e.message}"
                Log.e("SurveyViewModel", "Error al eliminar encuesta", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitSurveyResponses(
        surveyId: String,
        userId: String,
        answers: Map<String, String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                answers.forEach { (questionId, answerText) ->
                    val responseData = mapOf(
                        "survey_id" to surveyId,
                        "user_id" to userId,
                        "question_id" to questionId,
                        "answer_text" to answerText
                    )

                    supabase
                        .from("survey_responses")
                        .insert(responseData)
                }

                _successMessage.value = "Respuestas enviadas exitosamente"
                onSuccess()

            } catch (e: Exception) {
                _error.value = "Error al enviar respuestas: ${e.message}"
                Log.e("SurveyViewModel", "Error al enviar respuestas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hasUserResponded(
        surveyId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val responses = supabase
                    .from("survey_responses")
                    .select {
                        filter {
                            eq("survey_id", surveyId)
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<SurveyResponse>()

                onResult(responses.isNotEmpty())
            } catch (e: Exception) {
                Log.e("SurveyViewModel", "Error al verificar respuestas", e)
                onResult(false)
            }
        }
    }

    fun fetchSurveyResults(surveyId: String) {
        viewModelScope.launch {
            try {
                val responses = supabase
                    .from("survey_responses")
                    .select {
                        filter { eq("survey_id", surveyId) }
                    }
                    .decodeList<SurveyResponse>()

                _surveyResults.value = responses.groupBy { it.question_id }
                Log.d("SurveyViewModel", "Resultados cargados: ${responses.size} respuestas")
            } catch (e: Exception) {
                Log.e("SurveyViewModel", "Error al cargar resultados", e)
            }
        }
    }

    fun getQuestionStats(questionId: String, questionType: String): QuestionStats {
        val responses = _surveyResults.value[questionId] ?: emptyList()
        val totalResponses = responses.size

        return when (questionType) {
            "multiple_choice" -> {
                val answerCounts = responses.groupingBy { it.answer_text }.eachCount()
                QuestionStats.MultipleChoice(totalResponses, answerCounts)
            }
            "boolean" -> {
                val answerCounts = responses.groupingBy { it.answer_text }.eachCount()
                QuestionStats.Boolean(totalResponses, answerCounts)
            }
            "number" -> {
                val numbers = responses.mapNotNull { it.answer_text.toDoubleOrNull() }
                val average = if (numbers.isNotEmpty()) numbers.average() else 0.0
                val min = numbers.minOrNull() ?: 0.0
                val max = numbers.maxOrNull() ?: 0.0
                QuestionStats.Number(totalResponses, average, min, max)
            }
            else -> {
                QuestionStats.Text(totalResponses, responses.map { it.answer_text })
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}

sealed class QuestionStats {
    data class MultipleChoice(
        val totalResponses: Int,
        val answerCounts: Map<String, Int>
    ) : QuestionStats()

    data class Boolean(
        val totalResponses: Int,
        val answerCounts: Map<String, Int>
    ) : QuestionStats()

    data class Number(
        val totalResponses: Int,
        val average: Double,
        val min: Double,
        val max: Double
    ) : QuestionStats()

    data class Text(
        val totalResponses: Int,
        val answers: List<String>
    ) : QuestionStats()
}