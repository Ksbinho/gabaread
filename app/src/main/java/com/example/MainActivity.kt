package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CameraScannerScreen
import com.example.ui.screens.ClassDetailScreen
import com.example.ui.screens.CreateExamScreen
import com.example.ui.screens.ExamDetailScreen
import com.example.ui.screens.GenerateAnswerSheetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SubmissionDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GabaritoViewModel
import com.example.viewmodel.GabaritoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GabaritoViewModel = viewModel(
                    factory = GabaritoViewModelFactory(applicationContext)
                )
                GabaritoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GabaritoApp(viewModel: GabaritoViewModel) {
    val navController = rememberNavController()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val submissions by viewModel.allSubmissions.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            // Home Screen (Turmas, Provas, Estatísticas)
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onClassClick = { schoolClass ->
                        navController.navigate("class_detail/${schoolClass.id}")
                    },
                    onExamClick = { exam ->
                        navController.navigate("exam_detail/${exam.id}")
                    },
                    onQuickScanClick = { exam ->
                        navController.navigate("camera_scanner/${exam.id}")
                    },
                    onGenerateSheetClick = { targetClassId ->
                        val route = if (targetClassId != null) "generate_sheet?classId=$targetClassId" else "generate_sheet"
                        navController.navigate(route)
                    }
                )
            }

            // Class Detail Screen (Provas da Turma)
            composable(
                route = "class_detail/{classId}",
                arguments = listOf(navArgument("classId") { type = NavType.LongType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getLong("classId") ?: return@composable
                val schoolClass = classes.find { it.id == classId }

                if (schoolClass != null) {
                    ClassDetailScreen(
                        schoolClass = schoolClass,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onExamClick = { exam ->
                            navController.navigate("exam_detail/${exam.id}")
                        },
                        onCreateExamClick = { targetClass ->
                            navController.navigate("create_exam/${targetClass.id}")
                        },
                        onGenerateSheetClick = { targetClass ->
                            navController.navigate("generate_sheet?classId=${targetClass.id}")
                        }
                    )
                }
            }

            // Create Exam & Answer Key Screen
            composable(
                route = "create_exam/{classId}",
                arguments = listOf(navArgument("classId") { type = NavType.LongType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getLong("classId") ?: return@composable
                val schoolClass = classes.find { it.id == classId }

                if (schoolClass != null) {
                    CreateExamScreen(
                        schoolClass = schoolClass,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onExamCreated = { newExamId ->
                            navController.navigate("exam_detail/$newExamId") {
                                popUpTo("class_detail/$classId")
                            }
                        }
                    )
                }
            }

            // Exam Detail Screen (Resultados dos Alunos, Exportar CSV, Estatísticas)
            composable(
                route = "exam_detail/{examId}",
                arguments = listOf(navArgument("examId") { type = NavType.LongType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getLong("examId") ?: return@composable
                val exam = exams.find { it.id == examId }

                if (exam != null) {
                    ExamDetailScreen(
                        exam = exam,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onOpenScannerClick = { targetExam ->
                            navController.navigate("camera_scanner/${targetExam.id}")
                        },
                        onSubmissionClick = { submission ->
                            navController.navigate("submission_detail/${submission.id}")
                        },
                        onGenerateSheetClick = { targetExam ->
                            navController.navigate("generate_sheet?classId=${targetExam.classId}&examId=${targetExam.id}")
                        }
                    )
                }
            }

            // Custom Answer Sheet Generator & PDF / Print Screen
            composable(
                route = "generate_sheet?classId={classId}&examId={examId}",
                arguments = listOf(
                    navArgument("classId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("examId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val classIdParam = backStackEntry.arguments?.getString("classId")?.toLongOrNull()
                val examIdParam = backStackEntry.arguments?.getString("examId")?.toLongOrNull()

                GenerateAnswerSheetScreen(
                    initialClassId = classIdParam,
                    initialExamId = examIdParam,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToExam = { examId ->
                        navController.navigate("exam_detail/$examId")
                    }
                )
            }

            // Camera Scanner Screen (Leitor Óptico com Câmera e IA)
            composable(
                route = "camera_scanner/{examId}",
                arguments = listOf(navArgument("examId") { type = NavType.LongType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getLong("examId") ?: return@composable
                val exam = exams.find { it.id == examId }

                if (exam != null) {
                    CameraScannerScreen(
                        exam = exam,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onFinishScan = {
                            navController.navigate("exam_detail/$examId") {
                                popUpTo("exam_detail/$examId") { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Student Submission Detail Screen (Revisão detalhada e ajustes manuais)
            composable(
                route = "submission_detail/{submissionId}",
                arguments = listOf(navArgument("submissionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val subId = backStackEntry.arguments?.getLong("submissionId") ?: return@composable
                val submission = submissions.find { it.id == subId }
                val exam = exams.find { it.id == submission?.examId }

                if (submission != null && exam != null) {
                    SubmissionDetailScreen(
                        submission = submission,
                        exam = exam,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
