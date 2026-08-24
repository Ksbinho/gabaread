package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.ExamStatistics
import com.example.ui.theme.BentoBorderMedium
import com.example.ui.theme.BentoPurplePrimary
import com.example.ui.theme.BentoSurfaceLight
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WrongRed
import java.util.Locale

@Composable
fun QuestionAccuracyChart(
    exam: Exam,
    stats: ExamStatistics,
    modifier: Modifier = Modifier
) {
    if (stats.totalSubmissions == 0) return

    val answerKeyList = exam.getAnswerKeyList()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorderMedium.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Raio-X de Acertos por Questão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = "${stats.totalSubmissions} alunos",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoPurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (q in 1..exam.totalQuestions) {
                    val accuracy = stats.questionAccuracy[q] ?: 0.0
                    val correctKey = answerKeyList.getOrNull(q - 1) ?: "-"
                    val mostCommonMistake = stats.questionMostWrongAnswers[q]

                    val barColor = when {
                        accuracy >= 70.0 -> CorrectGreen
                        accuracy >= 50.0 -> WarningAmber
                        else -> WrongRed
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Q${q.toString().padStart(2, '0')} (Gabarito: $correctKey)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoTextPrimary
                                )
                                if (accuracy < 50.0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Questão com alto índice de erro",
                                        tint = WrongRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Maior erro: $mostCommonMistake",
                                        fontSize = 11.sp,
                                        color = WrongRed,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${String.format(Locale("pt", "BR"), "%.0f", accuracy)}% acertos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Visual progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BentoSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (accuracy.toFloat() / 100f).coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
