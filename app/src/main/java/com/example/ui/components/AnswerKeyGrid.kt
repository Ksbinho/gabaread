package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoBorderMedium
import com.example.ui.theme.BentoPurpleContainer
import com.example.ui.theme.BentoPurpleDeep
import com.example.ui.theme.BentoPurpleLight
import com.example.ui.theme.BentoPurplePrimary
import com.example.ui.theme.BentoSurfaceLight
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenContainer
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedContainer

@Composable
fun AnswerKeyGrid(
    totalQuestions: Int,
    optionsPerQuestion: Int,
    selectedAnswers: List<String>,
    onAnswerSelected: (questionIndex: Int, option: String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    correctAnswers: List<String>? = null
) {
    val optionLetters = listOf("A", "B", "C", "D", "E").take(optionsPerQuestion)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorderMedium.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gabarito das Questões",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = "$totalQuestions questões (${optionLetters.joinToString("-")})",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoPurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (qIndex in 0 until totalQuestions) {
                    val questionNum = qIndex + 1
                    val currentSelected = selectedAnswers.getOrNull(qIndex) ?: ""
                    val expected = correctAnswers?.getOrNull(qIndex)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (qIndex % 2 == 0) BentoSurfaceVariant.copy(alpha = 0.6f)
                                else Color.Transparent
                            )
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Question Number Pill
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q${questionNum.toString().padStart(2, '0')}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BentoPurpleDeep
                            )
                        }

                        // Bubbles A, B, C, D, (E)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (opt in optionLetters) {
                                val isSelected = currentSelected == opt
                                val isExpected = expected != null && expected == opt

                                val bubbleBg = when {
                                    expected != null && isSelected && isExpected -> CorrectGreen
                                    expected != null && isSelected && !isExpected -> WrongRed
                                    expected != null && !isSelected && isExpected -> CorrectGreenContainer
                                    isSelected -> BentoPurplePrimary
                                    else -> BentoSurfaceLight
                                }

                                val bubbleBorderColor = when {
                                    expected != null && isExpected -> CorrectGreen
                                    expected != null && isSelected && !isExpected -> WrongRed
                                    isSelected -> BentoPurplePrimary
                                    else -> BentoBorderMedium
                                }

                                val textColor = when {
                                    isSelected -> Color.White
                                    expected != null && isExpected -> Color(0xFF047857)
                                    else -> BentoTextPrimary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(bubbleBg)
                                        .border(
                                            width = if (isSelected || isExpected) 2.dp else 1.dp,
                                            color = bubbleBorderColor,
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = !readOnly) {
                                            onAnswerSelected(qIndex, opt)
                                        }
                                        .testTag("bubble_${questionNum}_$opt"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontWeight = if (isSelected || isExpected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
