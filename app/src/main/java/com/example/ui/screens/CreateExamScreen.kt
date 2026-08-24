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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.SchoolClass
import com.example.ui.components.AnswerKeyGrid
import com.example.ui.theme.BentoBackgroundLight
import com.example.ui.theme.BentoBorderMedium
import com.example.ui.theme.BentoPurpleContainer
import com.example.ui.theme.BentoPurpleDeep
import com.example.ui.theme.BentoPurplePrimary
import com.example.ui.theme.BentoSurfaceLight
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.WrongRed
import com.example.ui.theme.WrongRedContainer
import com.example.viewmodel.GabaritoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    schoolClass: SchoolClass,
    viewModel: GabaritoViewModel,
    onBackClick: () -> Unit,
    onExamCreated: (Long) -> Unit
) {
    var title by remember { mutableStateOf("Avaliação Bimestral") }
    var subject by remember { mutableStateOf(schoolClass.subject.ifEmpty { "Geral" }) }
    var totalQuestions by remember { mutableIntStateOf(10) }
    var optionsCount by remember { mutableIntStateOf(5) }
    var maxScore by remember { mutableDoubleStateOf(10.0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedKeys = remember {
        mutableStateListOf<String>().apply {
            repeat(30) { add("A") }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cadastrar Prova & Gabarito", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = BentoTextPrimary)
                        Text("Turma: ${schoolClass.name}", style = MaterialTheme.typography.bodySmall, color = BentoTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
            // General Info Bento Card
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Informações da Prova",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da Prova / Avaliação") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_title")
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Disciplina") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_subject")
                    )

                    Text(
                        text = "Quantidade de Questões:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 20).forEach { qCount ->
                            FilterChip(
                                selected = totalQuestions == qCount,
                                onClick = { totalQuestions = qCount },
                                label = { Text("$qCount Questões") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("chip_questions_$qCount")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Alternativas:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = optionsCount == 5,
                                    onClick = { optionsCount = 5 },
                                    label = { Text("A - E (5)") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                FilterChip(
                                    selected = optionsCount == 4,
                                    onClick = { optionsCount = 4 },
                                    label = { Text("A - D (4)") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Nota Máxima:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = maxScore == 10.0,
                                    onClick = { maxScore = 10.0 },
                                    label = { Text("10,0") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                FilterChip(
                                    selected = maxScore == 100.0,
                                    onClick = { maxScore = 100.0 },
                                    label = { Text("100") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Answer Key Bento Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorderMedium.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "2. Gabarito Oficial (Respostas)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "Toque nas alternativas corretas para cada questão",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val opts = listOf("A", "B", "C", "D", "E").take(optionsCount)
                                for (i in 0 until totalQuestions) {
                                    selectedKeys[i] = opts.random()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_random_key")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoPurplePrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preencher Exemplo", fontSize = 11.sp, color = BentoPurplePrimary)
                        }

                        OutlinedButton(
                            onClick = {
                                for (i in 0 until totalQuestions) {
                                    selectedKeys[i] = "A"
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resetar (Tudo A)", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Visual Answer Key Grid
            AnswerKeyGrid(
                totalQuestions = totalQuestions,
                optionsPerQuestion = optionsCount,
                selectedAnswers = selectedKeys.take(totalQuestions),
                onAnswerSelected = { index, option ->
                    selectedKeys[index] = option
                }
            )

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WrongRedContainer)
                        .padding(14.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = WrongRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Por favor, informe o título da prova."
                        return@Button
                    }
                    val finalKey = selectedKeys.take(totalQuestions).joinToString(",")
                    viewModel.createExam(
                        classId = schoolClass.id,
                        title = title,
                        subject = subject,
                        totalQuestions = totalQuestions,
                        optionsPerQuestion = optionsCount,
                        maxScore = maxScore,
                        answerKey = finalKey,
                        onComplete = { newExamId ->
                            onExamCreated(newExamId)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_exam"),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPurplePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Prova & Gabarito Oficial", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
