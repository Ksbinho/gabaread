package com.example.data.repository

import com.example.data.local.GabaritoDao
import com.example.data.model.Exam
import com.example.data.model.ExamStatistics
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class GabaritoRepository(private val dao: GabaritoDao) {

    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val allExams: Flow<List<Exam>> = dao.getAllExams()
    val allSubmissions: Flow<List<ExamSubmission>> = dao.getAllSubmissions()

    fun getExamsForClass(classId: Long): Flow<List<Exam>> = dao.getExamsByClass(classId)

    fun getSubmissionsForExam(examId: Long): Flow<List<ExamSubmission>> = dao.getSubmissionsByExam(examId)

    suspend fun getClassById(id: Long): SchoolClass? = dao.getClassById(id)

    suspend fun getExamById(id: Long): Exam? = dao.getExamById(id)

    suspend fun getSubmissionById(id: Long): ExamSubmission? = dao.getSubmissionById(id)

    suspend fun createClass(name: String, subject: String, schoolYear: String): Long {
        return dao.insertClass(
            SchoolClass(
                name = name,
                subject = subject,
                schoolYear = schoolYear
            )
        )
    }

    suspend fun updateClass(schoolClass: SchoolClass) = dao.updateClass(schoolClass)

    suspend fun deleteClass(schoolClass: SchoolClass) = dao.deleteClass(schoolClass)

    suspend fun createExam(
        classId: Long,
        title: String,
        subject: String,
        totalQuestions: Int,
        optionsPerQuestion: Int,
        maxScore: Double,
        answerKey: String,
        examDate: Long = System.currentTimeMillis()
    ): Long {
        return dao.insertExam(
            Exam(
                classId = classId,
                title = title,
                subject = subject,
                totalQuestions = totalQuestions,
                optionsPerQuestion = optionsPerQuestion,
                maxScore = maxScore,
                answerKey = answerKey,
                examDate = examDate
            )
        )
    }

    suspend fun updateExam(exam: Exam) = dao.updateExam(exam)

    suspend fun deleteExam(exam: Exam) = dao.deleteExam(exam)

    suspend fun saveSubmission(submission: ExamSubmission): Long {
        return dao.insertSubmission(submission)
    }

    suspend fun updateSubmission(submission: ExamSubmission) {
        dao.updateSubmission(submission)
    }

    suspend fun deleteSubmission(submission: ExamSubmission) {
        dao.deleteSubmission(submission)
    }

    /**
     * Computes statistics for a given exam based on submissions
     */
    fun calculateStatistics(exam: Exam, submissions: List<ExamSubmission>): ExamStatistics {
        if (submissions.isEmpty()) {
            return ExamStatistics()
        }

        val total = submissions.size
        val scores = submissions.map { it.finalScore }
        val avg = scores.average()
        val highest = scores.maxOrNull() ?: 0.0
        val lowest = scores.minOrNull() ?: 0.0
        val passingCount = submissions.count { it.percentage >= 60.0 }
        val passRate = (passingCount.toDouble() / total) * 100.0

        val answerKeyList = exam.getAnswerKeyList()
        val questionAccuracy = mutableMapOf<Int, Double>()
        val mostCommonMistakes = mutableMapOf<Int, String>()

        for (q in 1..exam.totalQuestions) {
            val correctOpt = answerKeyList.getOrNull(q - 1) ?: ""
            var correctInQ = 0
            val wrongCounts = mutableMapOf<String, Int>()

            for (sub in submissions) {
                val ans = sub.getAnswersList().getOrNull(q - 1) ?: "-"
                if (ans == correctOpt) {
                    correctInQ++
                } else if (ans != "-") {
                    wrongCounts[ans] = (wrongCounts[ans] ?: 0) + 1
                }
            }

            val acc = (correctInQ.toDouble() / total) * 100.0
            questionAccuracy[q] = (acc * 10.0).roundToInt() / 10.0

            val mostCommon = wrongCounts.maxByOrNull { it.value }?.key ?: "-"
            mostCommonMistakes[q] = mostCommon
        }

        return ExamStatistics(
            totalSubmissions = total,
            averageScore = (avg * 10.0).roundToInt() / 10.0,
            highestScore = (highest * 10.0).roundToInt() / 10.0,
            lowestScore = (lowest * 10.0).roundToInt() / 10.0,
            passRatePercentage = (passRate * 10.0).roundToInt() / 10.0,
            questionAccuracy = questionAccuracy,
            questionMostWrongAnswers = mostCommonMistakes
        )
    }

    /**
     * Seeds initial demo data if database is fresh.
     */
    suspend fun seedInitialDataIfEmpty() {
        val existingClasses = dao.getAllClasses().first()
        if (existingClasses.isNotEmpty()) return

        // 1. Create Class 9º Ano A
        val classId1 = dao.insertClass(
            SchoolClass(
                name = "9º Ano A",
                subject = "Matemática",
                schoolYear = "2026",
                studentCount = 28
            )
        )

        // 2. Create Class 3º Ano EM
        val classId2 = dao.insertClass(
            SchoolClass(
                name = "3º Ano B (Ensino Médio)",
                subject = "Física & Química",
                schoolYear = "2026",
                studentCount = 32
            )
        )

        // 3. Create Sample Exam for 9º Ano A (10 questions, A-E, 10.0 points)
        val key10Q = "A,C,B,D,E,A,B,C,E,D"
        val examId1 = dao.insertExam(
            Exam(
                classId = classId1,
                title = "Avaliação Bimestral - Geometria e Álgebra",
                subject = "Matemática",
                totalQuestions = 10,
                optionsPerQuestion = 5,
                maxScore = 10.0,
                answerKey = key10Q,
                examDate = System.currentTimeMillis() - 86400000L
            )
        )

        // Add 5 realistic sample student scanned submissions
        val sampleStudents = listOf(
            Triple("Lucas Gabriel Silva", "01", "A,C,B,D,E,A,B,C,E,D"), // 10/10
            Triple("Beatriz Souza Lima", "04", "A,C,B,D,E,A,A,C,E,D"), // 9/10
            Triple("Carlos Eduardo Santos", "07", "A,C,A,D,E,A,B,C,B,D"), // 8/10
            Triple("Mariana Rocha Costa", "18", "A,C,B,B,E,B,B,C,E,D"), // 7/10
            Triple("Rafael Henrique Alves", "22", "B,C,A,D,C,A,B,A,E,D") // 6/10
        )

        val keysList = key10Q.split(",")
        for ((name, num, answers) in sampleStudents) {
            val ansList = answers.split(",")
            var correct = 0
            for (i in ansList.indices) {
                if (ansList[i] == keysList.getOrNull(i)) correct++
            }
            val percentage = (correct.toDouble() / 10.0) * 100.0
            val score = (correct.toDouble() / 10.0) * 10.0

            dao.insertSubmission(
                ExamSubmission(
                    examId = examId1,
                    classId = classId1,
                    studentName = name,
                    studentNumber = num,
                    scannedAnswers = answers,
                    correctCount = correct,
                    totalQuestions = 10,
                    finalScore = score,
                    percentage = percentage,
                    scannedAt = System.currentTimeMillis() - (1000L * 60 * (10..120).random())
                )
            )
        }

        // 4. Create Sample Exam for 3º Ano EM (15 questions)
        val key15Q = "A,B,C,D,E,A,B,C,D,E,A,B,C,D,E"
        dao.insertExam(
            Exam(
                classId = classId2,
                title = "Simulado ENEM - Ciências da Natureza",
                subject = "Física & Química",
                totalQuestions = 15,
                optionsPerQuestion = 5,
                maxScore = 10.0,
                answerKey = key15Q,
                examDate = System.currentTimeMillis()
            )
        )
    }
}
