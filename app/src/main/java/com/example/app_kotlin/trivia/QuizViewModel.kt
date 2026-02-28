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

        _uiState.value = current.copy(
            score = newScore,
            lives = newLives,
            feedback = if (isCorrect) Feedback.CORRECT else Feedback.INCORRECT
        )
    }

    fun onNextQuestion() {
        val current = _uiState.value

        // Término por vidas agotadas
        if (current.lives <= 0 || current.currentIndex >= current.questions.size - 1) {
            _uiState.value = current.copy(isFinished = true)
            return
        }

        val nextIndex = current.currentIndex + 1
        val finished = nextIndex >= current.questions.size

        _uiState.value = current.copy(
            currentIndex = current.currentIndex + 1,
            selectedIndex = null,
            feedback = null
        )
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState(questions = seedQuestions())
    }
    private fun seedQuestions(): List<Question> {
        return listOf(
            Question(
                id = 1,
                title = "¿Qué palabra clave se usa para declarar una variable inmutable en Kotlin?",
                options = listOf("var", "val", "let", "const"),
                correctIndex = 1
            ),
            Question(
                id = 2,
                title = "En Jetpack Compose, ¿qué anotación marca una función como UI?",
                options = listOf("@UI", "@Widget", "@Composable", "@Compose"),
                correctIndex = 2
            ),
            Question(
                id = 3,
                title = "¿Qué componente se usa para listas eficientes y scrolleables en Compose?",
                options = listOf("Column", "RecyclerView", "Stack", "LazyColumn"),
                correctIndex = 3
            ),
            Question(
                id = 4,
                title = "¿Qué función se usa para recordar un valor a través de recomposiciones?",
                options = listOf("rememberState()", "remember { }", "stateOf()", "mutableOf()"),
                correctIndex = 1
            ),
            Question(
                id = 5,
                title = "¿Qué operador en Kotlin lanza NullPointerException si el valor es null?",
                options = listOf("?.", "?:", "!!", "::"),
                correctIndex = 2
            ),
            Question(
                id = 6,
                title = "En Java, ¿cuál es la palabra clave para heredar de una clase?",
                options = listOf("implements", "inherits", "extends", "import"),
                correctIndex = 2
            ),
            Question(
                id = 7,
                title = "¿Qué pilar de la POO permite que una clase oculte sus detalles internos?",
                options = listOf("Herencia", "Polimorfismo", "Abstracción", "Encapsulamiento"),
                correctIndex = 3
            ),
            Question(
                id = 8,
                title = "¿Qué método es el punto de entrada principal en una aplicación Java estándar?",
                options = listOf("main()", "start()", "init()", "run()"),
                correctIndex = 0
            ),
            Question(
                id = 9,
                title = "En Java, ¿cómo se llama el constructor de una clase?",
                options = listOf("constructor()", "new()", "Igual que la clase", "init()"),
                correctIndex = 2
            ),
            Question(
                id = 10,
                title = "¿Qué comando SQL se usa para insertar nuevos registros en una tabla?",
                options = listOf("ADD RECORD", "INSERT INTO", "UPDATE", "CREATE"),
                correctIndex = 1
            ),
            Question(
                id = 11,
                title = "¿Cuál es la cláusula SQL utilizada para filtrar los resultados de una consulta?",
                options = listOf("WHERE", "FILTER", "GROUP BY", "ORDER BY"),
                correctIndex = 0
            ),
            Question(
                id = 12,
                title = "¿Qué tipo de JOIN devuelve solo las filas que tienen coincidencia en ambas tablas?",
                options = listOf("LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "INNER JOIN"),
                correctIndex = 3
            ),
            Question(
                id = 13,
                title = "Para eliminar todos los datos de una tabla sin borrar la estructura, usamos:",
                options = listOf("REMOVE", "DELETE FROM", "DROP", "CLEAR"),
                correctIndex = 1
            ),
            Question(
                id = 14,
                title = "¿En qué archivo se declaran los permisos y componentes de la App?",
                options = listOf("build.gradle", "settings.gradle", "AndroidManifest.xml", "MainActivity.java"),
                correctIndex = 2
            ),
            Question(
                id = 15,
                title = "¿Qué componente de Android sobrevive a los cambios de configuración (como rotar pantalla)?",
                options = listOf("Activity", "Fragment", "ViewModel", "Intent"),
                correctIndex = 2
            )
        )
    }
}