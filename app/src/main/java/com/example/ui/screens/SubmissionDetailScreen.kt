package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.ExamSubmission
import com.example.ui.components.AnswerKeyGrid
import com.example.ui.theme.BentoBackgroundLight
import com.example.ui.theme.BentoBorderMedium
import com.example.ui.theme.BentoPurpleContainer
import com.example.ui.theme.BentoPurpleDeep
import com.example.ui.theme.BentoPurplePrimary
import com.example.ui.theme.BentoSurfaceLight
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenContainer
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedContainer
import com.example.viewmodel.GabaritoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailScreen(
    submission: ExamSubmission,
    exam: Exam,
    viewModel: GabaritoViewModel,
    onBackClick: () -> Unit
) {
    var studentName by remember { mutableStateOf(submission.studentName) }
    var studentNumber by remember { mutableStateOf(submission.studentNumber) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val currentAnswers = remember {
        mutableStateListOf<String>().apply {
            addAll(submission.getAnswersList())
            while (size < exam.totalQuestions) {
                add("-")
            }
        }
    }

    val officialKeys = exam.getAnswerKeyList()

    val correctCount = currentAnswers.take(exam.totalQuestions).filterIndexed { index, ans ->
        ans == officialKeys.getOrNull(index)
    }.size

    val percentage = if (exam.totalQuestions > 0) (correctCount.toDouble() / exam.totalQuestions) * 100.0 else 0.0
    val rawScore = if (exam.totalQuestions > 0) (correctCount.toDouble() / exam.totalQuestions) * exam.maxScore else 0.0
    val finalScore = Math.round(rawScore * 10.0) / 10.0

    val isApproved = percentage >= 60.0
    val statusColor = if (isApproved) CorrectGreen else WrongRed
    val statusBg = if (isApproved) CorrectGreenContainer else WrongRedContainer

    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gabarito do Aluno",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = exam.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score Bento Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$correctCount de ${exam.totalQuestions} Acertos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            text = "Nota ${String.format(Locale("pt", "BR"), "%.1f", finalScore)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(statusColor)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isApproved) "Aprovado" else "Recuperação",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lido em ${dateFormat.format(Date(submission.scannedAt))}",
                            fontSize = 10.sp,
                            color = BentoTextSecondary
                        )
                    }
                }
            }

            // Student Information Bento Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorderMedium.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Dados do Estudante",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Nome do Aluno") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BentoPurplePrimary) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_student_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        label = { Text("Nº de Chamada / Matrícula") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_student_number"),
                        singleLine = true
                    )
                }
            }

            // Visual Question Correction Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Respostas do Aluno vs Gabarito Oficial",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = "Você pode tocar nas alternativas para alterar a marcação do aluno caso necessário.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnswerKeyGrid(
                    totalQuestions = exam.totalQuestions,
                    optionsPerQuestion = exam.optionsPerQuestion,
                    selectedAnswers = currentAnswers.take(exam.totalQuestions),
                    onAnswerSelected = { index, option ->
                        currentAnswers[index] = option
                    },
                    correctAnswers = officialKeys
                )
            }

            // Save Changes Button
            Button(
                onClick = {
                    viewModel.updateSubmissionAnswers(
                        submission = submission,
                        exam = exam,
                        newAnswers = currentAnswers.take(exam.totalQuestions),
                        studentName = studentName,
                        studentNumber = studentNumber
                    )
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_submission_changes"),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPurplePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Alterações", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Correção") },
            text = { Text("Deseja realmente remover o gabarito do aluno '${submission.studentName}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubmission(submission)
                        showDeleteConfirm = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
