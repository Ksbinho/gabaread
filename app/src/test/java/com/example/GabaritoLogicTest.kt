package com.example

import com.example.data.model.Exam
import com.example.data.model.ExamStatistics
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass
import com.example.engine.AnswerSheetGenerator
import com.example.engine.SpreadsheetExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GabaritoLogicTest {

    @Test
    fun testAnswerKeyParsing() {
        val exam = Exam(
            classId = 1L,
            title = "Prova de Matemática",
            totalQuestions = 5,
            optionsPerQuestion = 5,
            maxScore = 10.0,
            answerKey = "A,B,C,D,E"
        )

        val keys = exam.getAnswerKeyList()
        assertEquals(5, keys.size)
        assertEquals("A", keys[0])
        assertEquals("B", keys[1])
        assertEquals("C", keys[2])
        assertEquals("D", keys[3])
        assertEquals("E", keys[4])
    }

    @Test
    fun testScoreCalculation() {
        val exam = Exam(
            classId = 1L,
            title = "História",
            totalQuestions = 10,
            optionsPerQuestion = 5,
            maxScore = 10.0,
            answerKey = "A,A,A,A,A,B,B,B,B,B"
        )

        val studentAnswers = listOf("A", "A", "A", "A", "C", "B", "B", "B", "B", "D")
        val correctCount = studentAnswers.filterIndexed { index, ans ->
            ans == exam.getAnswerKeyList()[index]
        }.size

        assertEquals(8, correctCount)
        val percentage = (correctCount.toDouble() / exam.totalQuestions) * 100.0
        val finalScore = (correctCount.toDouble() / exam.totalQuestions) * exam.maxScore

        assertEquals(80.0, percentage, 0.01)
        assertEquals(8.0, finalScore, 0.01)
    }

    @Test
    fun testSpreadsheetCsvContent() {
        val schoolClass = SchoolClass(
            id = 1L,
            name = "9º Ano A",
            subject = "Matemática",
            schoolYear = "2026"
        )

        val exam = Exam(
            id = 10L,
            classId = 1L,
            title = "Simulado 1",
            subject = "Matemática",
            totalQuestions = 3,
            optionsPerQuestion = 5,
            maxScore = 10.0,
            answerKey = "A,B,C"
        )

        val submissions = listOf(
            ExamSubmission(
                id = 100L,
                examId = 10L,
                classId = 1L,
                studentName = "Lucas Souza",
                studentNumber = "12",
                scannedAnswers = "A,B,C",
                correctCount = 3,
                totalQuestions = 3,
                finalScore = 10.0,
                percentage = 100.0
            ),
            ExamSubmission(
                id = 101L,
                examId = 10L,
                classId = 1L,
                studentName = "Mariana Lima",
                studentNumber = "15",
                scannedAnswers = "A,D,C",
                correctCount = 2,
                totalQuestions = 3,
                finalScore = 6.7,
                percentage = 66.7
            )
        )

        val stats = ExamStatistics(
            totalSubmissions = 2,
            averageScore = 8.35,
            highestScore = 10.0,
            lowestScore = 6.7,
            passRatePercentage = 100.0,
            questionAccuracy = mapOf(1 to 100.0, 2 to 50.0, 3 to 100.0)
        )

        val csvContent = SpreadsheetExporter.generateCsvContent(schoolClass, exam, submissions, stats)

        assertNotNull(csvContent)
        assertTrue(csvContent.contains("9º Ano A"))
        assertTrue(csvContent.contains("Simulado 1"))
        assertTrue(csvContent.contains("Lucas Souza"))
        assertTrue(csvContent.contains("Mariana Lima"))
        assertTrue(csvContent.contains("Aprovado"))
        assertTrue(csvContent.contains("ESTATÍSTICA DE ACERTOS POR QUESTÃO"))
    }

    @Test
    fun testPrintableAnswerSheetTemplate() {
        val schoolClass = SchoolClass(name = "3º Ano B", subject = "Física", schoolYear = "2026")
        val exam = Exam(classId = 1L, title = "Prova Bimestral", totalQuestions = 5, optionsPerQuestion = 4, maxScore = 10.0, answerKey = "A,B,C,D,A")

        val template = AnswerSheetGenerator.getPrintableInstructions(schoolClass, exam)

        assertTrue(template.contains("FOLHA DE RESPOSTAS"))
        assertTrue(template.contains("3º Ano B"))
        assertTrue(template.contains("Questão 01:"))
        assertTrue(template.contains("( A )"))
    }
}
