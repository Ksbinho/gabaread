package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Exam
import com.example.data.model.ExamStatistics
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass
import com.example.data.repository.GabaritoRepository
import com.example.engine.OmrScannerEngine
import com.example.engine.ScanEvaluationResult
import com.example.engine.SpreadsheetExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Processing : ScanUiState
    data class Success(val result: ScanEvaluationResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class GabaritoViewModel(
    private val repository: GabaritoRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val classes: StateFlow<List<SchoolClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubmissions: StateFlow<List<ExamSubmission>> = repository.allSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    private val _selectedExamId = MutableStateFlow<Long?>(null)
    val selectedExamId: StateFlow<Long?> = _selectedExamId.asStateFlow()

    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun selectClass(classId: Long?) {
        _selectedClassId.value = classId
    }

    fun selectExam(examId: Long?) {
        _selectedExamId.value = examId
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun resetScanState() {
        _scanUiState.value = ScanUiState.Idle
    }

    fun createClass(name: String, subject: String, schoolYear: String, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createClass(name.trim(), subject.trim(), schoolYear.trim())
            _userMessage.value = "Turma criada com sucesso!"
            onComplete(id)
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.deleteClass(schoolClass)
            _userMessage.value = "Turma excluída."
        }
    }

    fun createExam(
        classId: Long,
        title: String,
        subject: String,
        totalQuestions: Int,
        optionsPerQuestion: Int,
        maxScore: Double,
        answerKey: String,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.createExam(
                classId = classId,
                title = title.trim(),
                subject = subject.trim(),
                totalQuestions = totalQuestions,
                optionsPerQuestion = optionsPerQuestion,
                maxScore = maxScore,
                answerKey = answerKey
            )
            _userMessage.value = "Prova e gabarito oficial cadastrados!"
            onComplete(id)
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            repository.deleteExam(exam)
            _userMessage.value = "Prova excluída."
        }
    }

    fun deleteSubmission(submission: ExamSubmission) {
        viewModelScope.launch {
            repository.deleteSubmission(submission)
            _userMessage.value = "Gabarito removido."
        }
    }

    /**
     * Updates an existing student submission when teacher manually changes a question mark.
     */
    fun updateSubmissionAnswers(
        submission: ExamSubmission,
        exam: Exam,
        newAnswers: List<String>,
        studentName: String,
        studentNumber: String
    ) {
        viewModelScope.launch {
            val officialKeys = exam.getAnswerKeyList()
            var correctCount = 0
            for (i in 0 until exam.totalQuestions) {
                val ans = newAnswers.getOrNull(i) ?: "-"
                val expected = officialKeys.getOrNull(i) ?: ""
                if (ans == expected) correctCount++
            }

            val percentage = (correctCount.toDouble() / exam.totalQuestions) * 100.0
            val rawScore = (correctCount.toDouble() / exam.totalQuestions) * exam.maxScore
            val finalScore = (rawScore * 10.0).roundToInt() / 10.0

            val updated = submission.copy(
                studentName = studentName.trim().ifEmpty { submission.studentName },
                studentNumber = studentNumber.trim(),
                scannedAnswers = newAnswers.joinToString(","),
                correctCount = correctCount,
                finalScore = finalScore,
                percentage = (percentage * 10.0).roundToInt() / 10.0
            )
            repository.updateSubmission(updated)
            _userMessage.value = "Gabarito do aluno atualizado!"
        }
    }

    /**
     * Executes optical reading & grading of captured student answer sheet bitmap.
     */
    fun processAnswerSheetImage(bitmap: Bitmap, exam: Exam, fallbackStudentName: String = "Aluno(a)") {
        viewModelScope.launch {
            _scanUiState.value = ScanUiState.Processing
            try {
                val result = OmrScannerEngine.scanAndEvaluate(bitmap, exam, fallbackStudentName)
                _scanUiState.value = ScanUiState.Success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                _scanUiState.value = ScanUiState.Error("Erro na leitura óptica: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

    /**
     * Saves the scanned result into Room database.
     */
    fun saveScanResult(
        result: ScanEvaluationResult,
        exam: Exam,
        customName: String?,
        customNumber: String?,
        notes: String = "",
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val finalName = customName?.trim()?.takeIf { it.isNotEmpty() } ?: result.studentName
            val finalNumber = customNumber?.trim() ?: result.studentNumber

            val submission = ExamSubmission(
                examId = exam.id,
                classId = exam.classId,
                studentName = finalName,
                studentNumber = finalNumber,
                scannedAnswers = result.answers.joinToString(","),
                correctCount = result.correctCount,
                totalQuestions = result.totalQuestions,
                finalScore = result.finalScore,
                percentage = result.percentage,
                notes = notes
            )
            repository.saveSubmission(submission)
            _userMessage.value = "Gabarito de $finalName salvo! (Nota: ${result.finalScore})"
            onSaved()
        }
    }

    /**
     * Exports the exam results to a CSV / Excel spreadsheet and opens share dialog.
     */
    fun exportSpreadsheet(
        context: Context,
        exam: Exam,
        schoolClass: SchoolClass,
        submissions: List<ExamSubmission>
    ): Uri? {
        val stats = repository.calculateStatistics(exam, submissions)
        val uri = SpreadsheetExporter.exportAndShareSpreadsheet(
            context = context,
            schoolClass = schoolClass,
            exam = exam,
            submissions = submissions,
            stats = stats
        )
        if (uri != null) {
            _userMessage.value = "Planilha exportada com sucesso!"
        }
        return uri
    }

    fun calculateStats(exam: Exam, submissions: List<ExamSubmission>): ExamStatistics {
        return repository.calculateStatistics(exam, submissions)
    }

    /**
     * Saves custom answer sheet PDF directly to the device Downloads / Gabaritos storage.
     */
    fun saveAnswerSheetToStorage(context: Context, config: com.example.engine.AnswerSheetConfig): Boolean {
        val (success, message) = com.example.engine.AnswerSheetPdfGenerator.savePdfToDeviceStorage(context, config)
        _userMessage.value = message
        return success
    }

    /**
     * Sends the custom answer sheet directly to Android native print system / Save as PDF.
     */
    fun printAnswerSheet(context: Context, config: com.example.engine.AnswerSheetConfig) {
        com.example.engine.AnswerSheetPdfGenerator.printAnswerSheet(context, config)
    }

    /**
     * Shares the generated PDF file to WhatsApp, Drive, Email, etc.
     */
    fun shareAnswerSheetPdf(context: Context, config: com.example.engine.AnswerSheetConfig) {
        com.example.engine.AnswerSheetPdfGenerator.sharePdf(context, config)
    }

    /**
     * Opens the generated PDF file in the device's default PDF viewer.
     */
    fun openAnswerSheetPdf(context: Context, config: com.example.engine.AnswerSheetConfig) {
        com.example.engine.AnswerSheetPdfGenerator.openPdf(context, config)
    }
}
