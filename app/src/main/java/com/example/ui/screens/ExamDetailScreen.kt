package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Exam
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass
import com.example.ui.components.AnswerSheetPreviewDialog
import com.example.ui.components.MetricCard
import com.example.ui.components.QuestionAccuracyChart
import com.example.ui.components.StudentResultCard
import com.example.ui.theme.BentoBackgroundLight
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
import com.example.ui.theme.WrongRed
import com.example.viewmodel.GabaritoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    exam: Exam,
    viewModel: GabaritoViewModel,
    onBackClick: () -> Unit,
    onOpenScannerClick: (Exam) -> Unit,
    onSubmissionClick: (ExamSubmission) -> Unit,
    onGenerateSheetClick: (Exam) -> Unit = {}
) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val allSubmissions by viewModel.allSubmissions.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val schoolClass = classes.find { it.id == exam.classId } ?: SchoolClass(name = "Turma")
    val submissions = allSubmissions.filter { it.examId == exam.id }
    val stats = remember(submissions, exam) {
        viewModel.calculateStats(exam, submissions)
    }

    var showPrintDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("all") }
    var submissionToDelete by remember { mutableStateOf<ExamSubmission?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val filteredSubmissions = submissions.filter { sub ->
        val matchesSearch = sub.studentName.contains(searchQuery, ignoreCase = true) ||
                sub.studentNumber.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (statusFilter) {
            "approved" -> sub.percentage >= 60.0
            "recovery" -> sub.percentage < 60.0
            else -> true
        }
        matchesSearch && matchesStatus
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = exam.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${schoolClass.name} • ${exam.totalQuestions} questões • Gabarito: ${exam.answerKey}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.exportSpreadsheet(context, exam, schoolClass, submissions)
                        },
                        modifier = Modifier.testTag("btn_export_csv_top")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar Planilha", tint = BentoPurplePrimary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Prova", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onOpenScannerClick(exam) },
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                text = { Text("Escanear Gabarito", fontWeight = FontWeight.Bold) },
                containerColor = BentoPurplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("fab_open_camera")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bento Grid Stats (2x2 Modular Tiles)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "MÉDIA DA TURMA",
                            value = if (submissions.isNotEmpty()) String.format(Locale("pt", "BR"), "%.1f", stats.averageScore) else "--",
                            subtitle = if (submissions.isNotEmpty() && stats.averageScore >= 6.0) "Bom desempenho" else "Abaixo da média",
                            icon = Icons.Default.Grade,
                            accentColor = if (stats.averageScore >= 6.0) CorrectGreen else WrongRed,
                            containerColor = BentoSurfaceLight,
                            iconBackgroundColor = if (stats.averageScore >= 6.0) CorrectGreen.copy(alpha = 0.15f) else WrongRed.copy(alpha = 0.15f),
                            iconTint = if (stats.averageScore >= 6.0) CorrectGreen else WrongRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "TAXA DE APROVAÇÃO",
                            value = if (submissions.isNotEmpty()) "${String.format(Locale("pt", "BR"), "%.0f", stats.passRatePercentage)}%" else "--",
                            subtitle = "${submissions.count { it.percentage >= 60.0 }} de ${submissions.size} alunos",
                            icon = Icons.Default.TrendingUp,
                            accentColor = BentoPurplePrimary,
                            containerColor = BentoPurpleContainer,
                            iconBackgroundColor = BentoPurpleDeep,
                            iconTint = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "MAIOR NOTA",
                            value = if (submissions.isNotEmpty()) String.format(Locale("pt", "BR"), "%.1f", stats.highestScore) else "--",
                            subtitle = "Máx: ${exam.maxScore}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = CorrectGreen,
                            containerColor = BentoSurfaceLight,
                            iconBackgroundColor = CorrectGreen.copy(alpha = 0.15f),
                            iconTint = CorrectGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "CORRIGIDOS",
                            value = submissions.size.toString(),
                            subtitle = "Gabaritos lidos",
                            icon = Icons.Default.Description,
                            accentColor = BentoPurpleDeep,
                            containerColor = BentoSurfaceVariant,
                            iconBackgroundColor = BentoPurpleLight,
                            iconTint = BentoPurpleDeep,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bento Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, BentoBorderLight, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.exportSpreadsheet(context, exam, schoolClass, submissions)
                            }
                            .testTag("btn_export_spreadsheet"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BentoPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableView,
                                    contentDescription = null,
                                    tint = BentoPurpleDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Exportar CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                                Text("Excel / Sheets", fontSize = 10.sp, color = BentoTextSecondary)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, BentoBorderLight, RoundedCornerShape(20.dp))
                            .clickable { showPrintDialog = true }
                            .testTag("btn_print_template"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BentoSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = BentoPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Folha Impressa", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                                Text("Visualizar / PDF", fontSize = 10.sp, color = BentoTextSecondary)
                            }
                        }
                    }
                }
            }

            // Question Accuracy / Error analysis
            if (submissions.isNotEmpty()) {
                item {
                    QuestionAccuracyChart(exam = exam, stats = stats)
                }
            }

            // Search and Filters Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Alunos e Gabaritos (${submissions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por nome ou número...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoPurplePrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_student"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = statusFilter == "all",
                            onClick = { statusFilter = "all" },
                            label = { Text("Todos (${submissions.size})") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilterChip(
                            selected = statusFilter == "approved",
                            onClick = { statusFilter = "approved" },
                            label = { Text("Aprovados (${submissions.count { it.percentage >= 60.0 }})") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilterChip(
                            selected = statusFilter == "recovery",
                            onClick = { statusFilter = "recovery" },
                            label = { Text("Recuperação (${submissions.count { it.percentage < 60.0 }})") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Submissions List
            if (filteredSubmissions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoBorderLight, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(BentoPurpleContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = BentoPurpleDeep,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Nenhum aluno encontrado" else "Nenhum gabarito lido ainda",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toque em 'Escanear Gabarito' para corrigir automaticamente com a câmera!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredSubmissions, key = { it.id }) { submission ->
                    StudentResultCard(
                        submission = submission,
                        onCardClick = { onSubmissionClick(submission) },
                        onDeleteClick = { submissionToDelete = submission }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Answer Sheet Template Dialog
    if (showPrintDialog) {
        AnswerSheetPreviewDialog(
            schoolClass = schoolClass,
            exam = exam,
            onDismiss = { showPrintDialog = false },
            onCustomizeClick = {
                showPrintDialog = false
                onGenerateSheetClick(exam)
            }
        )
    }

    // Delete Exam Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Prova") },
            text = { Text("Deseja realmente excluir esta prova e todas as leituras de gabaritos salvas?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExam(exam)
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

    // Delete Single Submission Dialog
    submissionToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { submissionToDelete = null },
            title = { Text("Remover Gabarito") },
            text = { Text("Deseja excluir a nota e correção do aluno '${sub.studentName}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubmission(sub)
                        submissionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { submissionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
