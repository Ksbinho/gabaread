package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.components.MetricCard
import com.example.ui.theme.BentoBackgroundLight
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
fun HomeScreen(
    viewModel: GabaritoViewModel,
    onClassClick: (SchoolClass) -> Unit,
    onExamClick: (Exam) -> Unit,
    onQuickScanClick: (Exam) -> Unit,
    onGenerateSheetClick: (Long?) -> Unit = {}
) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val submissions by viewModel.allSubmissions.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var showAddClassDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val globalAvg = if (submissions.isNotEmpty()) {
        submissions.map { it.finalScore }.average()
    } else null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EDUSCAN PRO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoPurplePrimary,
                                letterSpacing = 1.5.sp
                            )
                        }
                        Text(
                            text = if (classes.isNotEmpty()) "${classes.first().name} • Gabaritos" else "Leitor de Gabaritos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BentoPurpleContainer)
                            .clickable { showAddClassDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nova Turma",
                            tint = BentoPurpleDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddClassDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nova Turma") },
                text = { Text("Nova Turma", fontWeight = FontWeight.Bold) },
                containerColor = BentoPurplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("fab_add_class")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bento Grid Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 1. Bento Hero Card (Escanear Gabaritos)
                    val activeExam = exams.firstOrNull()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BentoBorderHero,
                                shape = RoundedCornerShape(28.dp)
                            )
                            .clickable {
                                if (activeExam != null) {
                                    onQuickScanClick(activeExam)
                                } else if (classes.isNotEmpty()) {
                                    onClassClick(classes.first())
                                } else {
                                    showAddClassDialog = true
                                }
                            }
                            .testTag("quick_scan_banner"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoPurpleLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(BentoPurpleDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Câmera",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BentoPurpleContainer)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "TEMPO REAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BentoPurpleDeep,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Column {
                                Text(
                                    text = "Escanear Gabaritos",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPurpleDeep
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (activeExam != null) "Pronto para ler: ${activeExam.title}" else "Aponte a câmera para as folhas de resposta",
                                    fontSize = 13.sp,
                                    color = BentoPurpleDeep.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // 2. Bento Asymmetric Middle Row (2 Tiles)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Tile: Bento Purple Container (Gabarito Mestre / Turmas)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .border(
                                    width = 1.dp,
                                    color = BentoBorderHero.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    if (classes.isNotEmpty()) {
                                        onClassClick(classes.first())
                                    } else {
                                        showAddClassDialog = true
                                    }
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoPurpleContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BentoPurpleDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Gabarito Mestre",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BentoPurpleDeep
                                    )
                                    Text(
                                        text = "${exams.size} provas cadastradas",
                                        fontSize = 11.sp,
                                        color = BentoTextSecondary
                                    )
                                }
                            }
                        }

                        // Right Tile: Bento Crisp White Tile (Média da Turma)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .border(
                                    width = 1.dp,
                                    color = BentoBorderMedium,
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BentoPurplePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Média da Turma",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoTextPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = if (globalAvg != null) String.format(Locale("pt", "BR"), "%.1f", globalAvg) else "--",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp,
                                            color = if (globalAvg != null && globalAvg >= 6.0) Color(0xFF10B981) else BentoTextPrimary
                                        )
                                        Text(
                                            text = "/10",
                                            fontSize = 12.sp,
                                            color = BentoTextSecondary,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Bento Action Cards: Gerar Folha de Respostas (PDF) & Exportar Resultados
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BentoBorderHero,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onGenerateSheetClick(null) }
                            .testTag("btn_create_custom_sheet_home"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoPurpleLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(BentoPurpleContainer)
                                        .border(1.dp, BentoBorderHero, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        tint = BentoPurpleDeep,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Criar Folha de Respostas",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPurpleDeep
                                    )
                                    Text(
                                        text = "Personalizar por turma • Salvar PDF / Imprimir",
                                        fontSize = 11.sp,
                                        color = BentoTextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = BentoPurpleDeep
                            )
                        }
                    }

                    // 4. Bento Full-Width Action Row (Exportar Resultados / Sheets)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BentoBorderLight,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (exams.isNotEmpty()) {
                                    val targetExam = exams.first()
                                    val targetClass = classes.find { it.id == targetExam.classId } ?: SchoolClass(name = "Turma")
                                    val targetSubs = submissions.filter { it.examId == targetExam.id }
                                    viewModel.exportSpreadsheet(context, targetExam, targetClass, targetSubs)
                                }
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(BentoSurfaceLight)
                                        .border(1.dp, BentoBorderLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableView,
                                        contentDescription = null,
                                        tint = BentoPurplePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Exportar Resultados",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = "Google Sheets • Excel (CSV)",
                                        fontSize = 11.sp,
                                        color = BentoTextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = BentoPurplePrimary
                            )
                        }
                    }
                }
            }

            // Bento Segmented Tab Selector
            item {
                Spacer(modifier = Modifier.height(4.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BentoSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BentoBorderLight, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoPurpleContainer)
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Turmas (${classes.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) BentoPurpleDeep else BentoTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Provas (${exams.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) BentoPurpleDeep else BentoTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Classes List in Bento cards
                if (classes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BentoBorderLight, RoundedCornerShape(24.dp))
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(BentoPurpleContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = BentoPurpleDeep,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Nenhuma turma cadastrada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toque em '+ Nova Turma' para organizar suas avaliações.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(classes, key = { it.id }) { schoolClass ->
                        val classExams = exams.filter { it.classId == schoolClass.id }
                        val classSubmissions = submissions.filter { it.classId == schoolClass.id }
                        val avgScore = if (classSubmissions.isNotEmpty()) {
                            classSubmissions.map { it.finalScore }.average()
                        } else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BentoBorderMedium.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                .clickable { onClassClick(schoolClass) }
                                .testTag("class_card_${schoolClass.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(BentoPurpleContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = BentoPurpleDeep,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = schoolClass.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextPrimary
                                        )
                                        Text(
                                            text = if (schoolClass.subject.isNotEmpty()) "${schoolClass.subject} • ${schoolClass.schoolYear}" else schoolClass.schoolYear,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BentoTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "${classExams.size} ${if (classExams.size == 1) "prova" else "provas"}",
                                                fontSize = 11.sp,
                                                color = BentoPurplePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(text = "•", fontSize = 11.sp, color = BentoBorderMedium)
                                            Text(
                                                text = "${classSubmissions.size} gabaritos",
                                                fontSize = 11.sp,
                                                color = BentoTextSecondary
                                            )
                                        }
                                    }
                                }

                                if (avgScore != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "MÉDIA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoTextSecondary
                                        )
                                        Text(
                                            text = String.format(Locale("pt", "BR"), "%.1f", avgScore),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = if (avgScore >= 6.0) Color(0xFF10B981) else Color(0xFFB3261E)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = BentoTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Exams List in Bento format
                if (exams.isEmpty()) {
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
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma prova cadastrada ainda.",
                                    color = BentoTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

                    items(exams, key = { it.id }) { exam ->
                        val targetClass = classes.find { it.id == exam.classId }
                        val examSubmissions = submissions.filter { it.examId == exam.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BentoBorderMedium.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                .clickable { onExamClick(exam) }
                                .testTag("exam_card_${exam.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(BentoPurpleLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = BentoPurpleDeep,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = exam.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextPrimary
                                        )
                                        Text(
                                            text = "${targetClass?.name ?: "Turma"} • ${exam.totalQuestions} questões",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BentoTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Criada em ${dateFormat.format(Date(exam.createdAt))}",
                                            fontSize = 11.sp,
                                            color = BentoTextTertiary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BentoPurpleContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${examSubmissions.size} lidos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = BentoPurpleDeep
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Add Class Dialog
    if (showAddClassDialog) {
        var className by remember { mutableStateOf("") }
        var classSubject by remember { mutableStateOf("") }
        var classYear by remember { mutableStateOf("2026") }

        AlertDialog(
            onDismissRequest = { showAddClassDialog = false },
            title = { Text("Cadastrar Nova Turma", fontWeight = FontWeight.Bold, color = BentoTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Nome da Turma (ex: 9º Ano A, 3º EM)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_class_name")
                    )
                    OutlinedTextField(
                        value = classSubject,
                        onValueChange = { classSubject = it },
                        label = { Text("Disciplina (ex: Matemática, História)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_class_subject")
                    )
                    OutlinedTextField(
                        value = classYear,
                        onValueChange = { classYear = it },
                        label = { Text("Ano Letivo") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_class_year")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (className.isNotBlank()) {
                            viewModel.createClass(className, classSubject, classYear)
                            showAddClassDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPurplePrimary),
                    modifier = Modifier.testTag("btn_confirm_add_class")
                ) {
                    Text("Criar Turma", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClassDialog = false }) {
                    Text("Cancelar", color = BentoTextSecondary)
                }
            }
        )
    }
}
