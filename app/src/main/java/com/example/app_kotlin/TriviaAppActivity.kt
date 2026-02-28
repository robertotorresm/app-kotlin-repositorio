package com.example.app_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_kotlin.trivia.Feedback
import com.example.app_kotlin.trivia.QuizUiState
import com.example.app_kotlin.trivia.QuizViewModel
import com.example.app_kotlin.ui.theme.AppkotlinTheme

class TriviaAppActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppkotlinTheme {
                val viewModel: QuizViewModel = viewModel()
                val state = viewModel.uiState.collectAsStateWithLifecycle().value

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Trivia App", color = Color.White) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF3F51B5)
                            )
                        )
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        if (state.isFinished) {
                            FinishedScreen(
                                score = state.score,
                                total = state.questions.size * 100,
                                livesLeft = state.lives,
                                onRestart = viewModel::restartQuiz
                            )
                        } else {
                            QuestionScreen(
                                state = state,
                                onSelectedOption = viewModel::onSelectedOption,
                                onConfirm = viewModel::onConfirmAnswer,
                                onNext = viewModel::onNextQuestion
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionScreen(
    state: QuizUiState,
    onSelectedOption: (Int) -> Unit,
    onConfirm: () -> Unit,
    onNext: () -> Unit,
) {
    val q = state.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Barra de progreso y vidas
        val progressFactor = (state.currentIndex + 1).toFloat() / state.questions.size
        val progressPercentage = (progressFactor * 100).toInt()

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Porcentaje de avance: $progressPercentage%",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1E88E5)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vidas: ",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        val heartsDisplay = "❤️".repeat(state.lives) + "🖤".repeat(3 - state.lives)
                        Text(text = heartsDisplay)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressFactor },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF1E88E5),
                    trackColor = Color.LightGray,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Índice
        Text(
            text = "Pregunta ${state.currentIndex + 1} de ${state.questions.size}",
            style = MaterialTheme.typography.titleSmall,
            color = Color.DarkGray
        )
        // Título de la pregunta
        Text(
            text = q.title,
            style = MaterialTheme.typography.headlineSmall
        )

        // Opciones
        q.options.forEachIndexed { index, option ->
            val isSelected = state.selectedIndex == index

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectedOption(index) },
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = if (isSelected) 14.dp else 1.dp
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectedOption(index) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        //  1. Feedback inmediato
        if (state.feedback != null) {
            val (emoji, msg, color) = when (state.feedback) {
                Feedback.CORRECT -> Triple("✅", "¡Correcto!", Color(0xFF79C77A))
                Feedback.INCORRECT -> Triple("❌", "Incorrecto", Color(0xFFD94F4F))
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
            ) {
                Text(
                    text = "$emoji $msg",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        //  Botón Confirmar o Siguiente
        if (state.feedback == null) {

            Button(
                onClick = onConfirm,
                enabled = state.selectedIndex != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar")
            }
        } else {
            // 2. Cambiar texto en la última pregunta o si perdió vidas
            val isLast = state.currentIndex == state.questions.size - 1
            val buttonLabel = if (isLast || state.lives <= 0) "Ver resultados" else "Siguiente"

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
fun FinishedScreen(
    score: Int,
    total: Int,
    livesLeft: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val emoji = if (livesLeft <= 0) "☹️" else "😎"
        val message = if (livesLeft <= 0) "¡Te quedaste sin vidas!" else "¡Completaste el Quiz!"

        Text(
            text = emoji,
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu puntaje: $score de $total",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(onClick = onRestart) {
            Text("🔄 Reintentar Quiz")
        }
    }
}