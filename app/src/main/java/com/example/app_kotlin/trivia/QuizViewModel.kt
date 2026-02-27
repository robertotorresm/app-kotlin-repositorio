package com.example.app_kotlin.trivia

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        QuizUiState(questions = seedQuestions())
    )

    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun onSelectedOption(index: Int) {
        val current = _uiState.value
        if (current.isFinished) return
        // No permitir cambiar opción si ya se confirmó (hay feedback)
        if (current.feedback != null) return
        _uiState.value = current.copy(selectedIndex = index)
    }

    fun onConfirmAnswer() {
        val current = _uiState.value
        val selected = current.selectedIndex ?: return
        val currentQuestion = current.currentQuestion ?: return

        val isCorrect = selected == currentQuestion.correctIndex
        val newScore = if (isCorrect) current.score + 100 else current.score
        val newLives = if (isCorrect) current.lives else current.lives - 1

        // Mostrar feedback — la navegación ocurre en onNextQuestion()
        _uiState.value = current.copy(
            score = newScore,
            lives = newLives,
            feedback = if (isCorrect) Feedback.CORRECT else Feedback.INCORRECT
        )
    }

    fun onNextQuestion() {
        val current = _uiState.value

        // Fin por vidas agotadas
        if (current.lives <= 0) {
            _uiState.value = current.copy(isFinished = true, feedback = null)
            return
        }

        val nextIndex = current.currentIndex + 1
        val finished = nextIndex >= current.questions.size

        _uiState.value = current.copy(
            currentIndex = nextIndex,
            selectedIndex = null,
            feedback = null,
            isFinished = finished
        )
    }

    private fun seedQuestions(): List<Question> {
        return listOf(
            Question(
                id = 1,
                title = "¿Que palabra clave se usa para declarar una variable inmutable en Kotlin?",
                options = listOf("var", "val", "let", "const"),
                correctIndex = 1
            ),
            Question(
                id = 2,
                title = "En Jetpack Compose, ¿que anotacion marca una funcion como UI?",
                options = listOf("@UI", "@Widget", "@Composable", "@Compose"),
                correctIndex = 2
            ),
            Question(
                id = 3,
                title = "¿Que componente se usa para listas eficientes y scrolleables?",
                options = listOf("Column", "RecyclerView", "Stack", "LazyColumn"),
                correctIndex = 3
            ),
            Question(
                id = 4,
                title = "La instrucción que permite restaurar estado tras recreación de Activity es",
                options = listOf("intentData", "savedInstanceState", "activityState", "bundleConfig"),
                correctIndex = 1
            )
        )
    }
}