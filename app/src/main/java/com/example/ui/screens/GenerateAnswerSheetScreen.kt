package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.SchoolClass
import com.example.engine.AnswerSheetConfig
import com.example.ui.theme.BentoBorderHero
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
import com.example.ui.theme.BentoTextTertiary
import com.example.viewmodel.GabaritoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateAnswerSheetScreen(
    initialClassId: Long? = null,
    initialExamId: Long? = null,
    viewModel: GabaritoViewModel,
    onBackClick: () -> Unit,
    onNavigateToExam: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val initialClass = classes.find { it.id == initialClassId } ?: classes.firstOrNull()
    val initialExam = exams.find { it.id == initialExamId }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    val todayStr = remember { dateFormat.format(Date()) }

    var selectedClass by remember(classes, initialClassId) {
        mutableStateOf(initialClass)
    }
    var schoolName by remember { mutableStateOf("Escola Estadual / Colégio") }
    var customClassName by remember(selectedClass) {
        mutableStateOf(selectedClass?.name ?: "Turma 1")
    }
    var schoolYear by remember(selectedClass) {
        mutableStateOf(selectedClass?.schoolYear ?: "2026")
    }
    var subject by remember(selectedClass, initialExam) {
        mutableStateOf(initialExam?.subject ?: selectedClass?.subject ?: "Geral")
    }
    var examTitle by remember(initialExam) {
        mutableStateOf(initialExam?.title ?: "Avaliação Bimestral")
    }
    var teacherName by remember { mutableStateOf("Professor(a)") }
    var dateText by remember { mutableStateOf(todayStr) }

    var totalQuestions by remember(initialExam) {
        mutableIntStateOf(initialExam?.totalQuestions ?: 20)
    }
    var optionsCount by remember(initialExam) {
        mutableIntStateOf(initialExam?.optionsPerQuestion ?: 5)
    }
    var sheetsPerPage by remember(totalQuestions) {
        mutableIntStateOf(if (totalQuestions <= 15) 2 else 1)
    }

    var showFiducials by remember { mutableStateOf(true) }
    var showInstructions by remember { mutableStateOf(true) }
    var showStudentNumber by remember { mutableStateOf(true) }
    var showScoreBox by remember { mutableStateOf(true) }

    var classDropdownExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val currentConfig = remember(
        schoolName, customClassName, schoolYear, subject, examTitle, teacherName, dateText,
        totalQuestions, optionsCount, sheetsPerPage, showFiducials, showInstructions,
        showStudentNumber, showScoreBox, initialExam
    ) {
        AnswerSheetConfig(
            schoolName = schoolName.trim().ifEmpty { "Escola / Instituição" },
            className = customClassName.trim().ifEmpty { "Turma" },
            schoolYear = schoolYear.trim(),
            subject = subject.trim().ifEmpty { "Geral" },
            examTitle = examTitle.trim().ifEmpty { "Avaliação" },
            teacherName = teacherName.trim(),
            dateText = dateText.trim(),
            totalQuestions = totalQuestions.coerceIn(1, 80),
            optionsPerQuestion = optionsCount.coerceIn(3, 5),
            sheetsPerPage = sheetsPerPage,
            showFiducials = showFiducials,
            showInstructions = showInstructions,
            showStudentNumber = showStudentNumber,
            showScoreBox = showScoreBox,
            examId = initialExam?.id
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Folha de Respostas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Personalizar e salvar no celular para impressão",
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
                    IconButton(
                        onClick = {
                            viewModel.shareAnswerSheetPdf(context, currentConfig)
                        },
                        modifier = Modifier.testTag("btn_share_sheet_top")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar PDF", tint = BentoPurplePrimary)
                    }
                    IconButton(
                        onClick = {
                            viewModel.printAnswerSheet(context, currentConfig)
                        },
                        modifier = Modifier.testTag("btn_print_sheet_top")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Imprimir / Salvar", tint = BentoPurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Bento Hero Card: Quick Action Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoBorderHero, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoPurpleLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GABARITO PRONTO PARA IMPRESSÃO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPurpleDeep,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currentConfig.className} • ${currentConfig.totalQuestions} Questões (${currentConfig.optionsPerQuestion} opções)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Formato A4 • Salve em PDF ou imprima direto",
                                fontSize = 12.sp,
                                color = BentoTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BentoPurpleContainer)
                                .border(1.dp, BentoBorderHero, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = BentoPurpleDeep,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 2. Bento Card: Turma & Cabeçalho da Escola
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoBorderLight, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = BentoPurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dados da Turma e Instituição",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BentoTextPrimary
                            )
                        }

                        // Class Selector (Dropdown if classes exist)
                        if (classes.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = classDropdownExpanded,
                                onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedClass?.name ?: customClassName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Selecionar Turma Cadastrada") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoPurplePrimary,
                                        unfocusedBorderColor = BentoBorderMedium
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = classDropdownExpanded,
                                    onDismissRequest = { classDropdownExpanded = false }
                                ) {
                                    classes.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.name} (${c.schoolYear}) - ${c.subject}") },
                                            onClick = {
                                                selectedClass = c
                                                customClassName = c.name
                                                schoolYear = c.schoolYear
                                                if (c.subject.isNotEmpty()) subject = c.subject
                                                classDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Custom / Editable Class Name
                        OutlinedTextField(
                            value = customClassName,
                            onValueChange = { customClassName = it },
                            label = { Text("Nome da Turma na Folha") },
                            placeholder = { Text("Ex: 3º Ano A, 9º B, Turma 102") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_sheet_class_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPurplePrimary,
                                unfocusedBorderColor = BentoBorderMedium
                            )
                        )

                        // Institution / School Name
                        OutlinedTextField(
                            value = schoolName,
                            onValueChange = { schoolName = it },
                            label = { Text("Escola / Instituição de Ensino") },
                            placeholder = { Text("Ex: E.E. Professor João Silva") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_sheet_school_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPurplePrimary,
                                unfocusedBorderColor = BentoBorderMedium
                            )
                        )

                        // Disciplina & Avaliação
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("Disciplina") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoPurplePrimary,
                                    unfocusedBorderColor = BentoBorderMedium
                                )
                            )

                            OutlinedTextField(
                                value = examTitle,
                                onValueChange = { examTitle = it },
                                label = { Text("Título da Prova") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoPurplePrimary,
                                    unfocusedBorderColor = BentoBorderMedium
                                )
                            )
                        }

                        // Professor & Data
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = teacherName,
                                onValueChange = { teacherName = it },
                                label = { Text("Professor(a)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoPurplePrimary,
                                    unfocusedBorderColor = BentoBorderMedium
                                )
                            )

                            OutlinedTextField(
                                value = dateText,
                                onValueChange = { dateText = it },
                                label = { Text("Data da Prova") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoPurplePrimary,
                                    unfocusedBorderColor = BentoBorderMedium
                                )
                            )
                        }
                    }
                }
            }

            // 3. Bento Card: Quantidade de Questões & Alternativas
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoBorderLight, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                tint = BentoPurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Configuração das Questões e Alternativas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BentoTextPrimary
                            )
                        }

                        // Question Count Selector Header & Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Quantidade de Questões",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = BentoTextPrimary
                                )
                                Text(
                                    text = "$totalQuestions questões na folha",
                                    fontSize = 12.sp,
                                    color = BentoPurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Stepper (- / +)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoSurfaceVariant)
                                    .border(1.dp, BentoBorderLight, RoundedCornerShape(12.dp))
                                    .padding(2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (totalQuestions > 1) totalQuestions-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = totalQuestions.toString(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = BentoTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(
                                    onClick = { if (totalQuestions < 80) totalQuestions++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Quick Chips for common question counts
                        val quickCounts = listOf(5, 10, 15, 20, 25, 30, 40, 50)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickCounts) { count ->
                                val isSelected = totalQuestions == count
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        totalQuestions = count
                                        if (count <= 15 && sheetsPerPage == 1) {
                                            sheetsPerPage = 2 // Smart suggestion for economy
                                        }
                                    },
                                    label = { Text("$count Q") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoPurpleContainer,
                                        selectedLabelColor = BentoPurpleDeep
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = BentoBorderLight)

                        // Alternatives per Question
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Alternativas por Questão",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = BentoTextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val optionChoices = listOf(
                                    3 to "3 (A, B, C)",
                                    4 to "4 (A, B, C, D)",
                                    5 to "5 (A, B, C, D, E)"
                                )
                                optionChoices.forEach { (count, label) ->
                                    val isSelected = optionsCount == count
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) BentoPurplePrimary else BentoBorderLight,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { optionsCount = count },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) BentoPurpleContainer else BentoSurfaceVariant
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) BentoPurpleDeep else BentoTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = BentoBorderLight)

                        // Paper Economy: Sheets per A4 Page
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Economia de Papel (Layout A4)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = BentoTextPrimary
                                )
                                Text(
                                    text = when (sheetsPerPage) {
                                        2 -> "50% economia (2 por folha)"
                                        4 -> "75% economia (4 por folha)"
                                        else -> "Padrão (1 por folha)"
                                    },
                                    fontSize = 11.sp,
                                    color = BentoPurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val layoutChoices = listOf(
                                    1 to "1 por folha\n(Completo)",
                                    2 to "2 por folha\n(Econômico ✂)",
                                    4 to "4 por folha\n(Super Quiz)"
                                )
                                layoutChoices.forEach { (pages, title) ->
                                    val isSelected = sheetsPerPage == pages
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) BentoPurplePrimary else BentoBorderLight,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { sheetsPerPage = pages },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) BentoPurpleContainer else BentoSurfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) BentoPurpleDeep else BentoTextPrimary,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bento Card: Opções e Elementos da Folha
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoBorderLight, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Elementos da Folha Impressa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BentoTextPrimary
                        )

                        // Marcadores Ópticos de Canto
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Marcadores Ópticos nos Cantos [■]", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                Text("Facilita o alinhamento da câmera durante a leitura automática", fontSize = 11.sp, color = BentoTextSecondary)
                            }
                            Switch(
                                checked = showFiducials,
                                onCheckedChange = { showFiducials = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = BentoPurplePrimary, checkedTrackColor = BentoPurpleContainer)
                            )
                        }

                        HorizontalDivider(color = BentoBorderLight)

                        // Campo de Matrícula e Assinatura
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Campo de Matrícula / Nº e Assinatura", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                Text("Espaço dedicado para identificação formal do estudante", fontSize = 11.sp, color = BentoTextSecondary)
                            }
                            Switch(
                                checked = showStudentNumber,
                                onCheckedChange = { showStudentNumber = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = BentoPurplePrimary, checkedTrackColor = BentoPurpleContainer)
                            )
                        }

                        HorizontalDivider(color = BentoBorderLight)

                        // Espaço para Nota do Professor
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quadro de Nota do Professor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                Text("Quadrante reservado no cabeçalho para anotação da nota final", fontSize = 11.sp, color = BentoTextSecondary)
                            }
                            Switch(
                                checked = showScoreBox,
                                onCheckedChange = { showScoreBox = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = BentoPurplePrimary, checkedTrackColor = BentoPurpleContainer)
                            )
                        }
                    }
                }
            }

            // 5. Live Realistic Preview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, BentoBorderHero, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Title preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRÉ-VISUALIZAÇÃO DA FOLHA A4",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = if (sheetsPerPage == 2) "2 Folhas por página" else if (sheetsPerPage == 4) "4 Folhas por página" else "1 Folha completa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BentoPurpleDeep
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.Black, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Simulated Header
                        Text(
                            text = "FOLHA DE RESPOSTAS — GABARITO OFICIAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = currentConfig.schoolName.uppercase(Locale("pt", "BR")),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Turma: ${currentConfig.className}", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                            Text("Disciplina: ${currentConfig.subject}", fontSize = 11.sp, color = Color.Black)
                            Text("Data: ${currentConfig.dateText}", fontSize = 11.sp, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text("Aluno(a): ________________________   Nº: _____   Nota: [     ]", fontSize = 10.sp, color = Color.DarkGray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sample Preview Rows (First 5 questions)
                        val previewCount = minOf(totalQuestions, 5)
                        val optionLetters = listOf("A", "B", "C", "D", "E").take(optionsCount)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (q in 1..previewCount) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Q${q.toString().padStart(2, '0')}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for (opt in optionLetters) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .border(1.2.dp, Color.Black, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = opt, fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            if (totalQuestions > 5) {
                                Text(
                                    text = "... e mais ${totalQuestions - 5} questões organizadas em colunas perfeitamente alinhadas na página A4",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Bento Action Center: Save to Phone (PDF), Print, and Share
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Primary Save Button
                    Button(
                        onClick = {
                            val saved = viewModel.saveAnswerSheetToStorage(context, currentConfig)
                            if (saved) {
                                Toast.makeText(context, "Folha salva com sucesso na pasta Downloads/Gabaritos!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_pdf_storage"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPurplePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Salvar Folha no Celular (PDF)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Secondary Action Buttons Row (Print & Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.printAnswerSheet(context, currentConfig)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_print_sheet_action"),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BentoPurplePrimary))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = BentoPurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Imprimir", color = BentoPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.shareAnswerSheetPdf(context, currentConfig)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_share_sheet_action"),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BentoPurplePrimary))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = BentoPurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartilhar", color = BentoPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Visualizar PDF direto
                    TextButton(
                        onClick = {
                            viewModel.openAnswerSheetPdf(context, currentConfig)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abrir e Visualizar Arquivo PDF", color = BentoTextSecondary, fontSize = 13.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
