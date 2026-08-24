package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Exam
import com.example.data.model.ExamStatistics
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SpreadsheetExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    /**
     * Generates a formatted CSV spreadsheet string with UTF-8 BOM for Microsoft Excel & Google Sheets compatibility.
     */
    fun generateCsvContent(
        schoolClass: SchoolClass,
        exam: Exam,
        submissions: List<ExamSubmission>,
        stats: ExamStatistics
    ): String {
        val sb = StringBuilder()
        // Add UTF-8 BOM so Excel opens accents correctly
        sb.append("\uFEFF")

        // Exam Header Summary
        sb.append("RELATÓRIO DE DESEMPENHO E CORREÇÃO DE GABARITOS\n")
        sb.append("Turma;\"${escapeCsv(schoolClass.name)}\";Ano Letivo;\"${escapeCsv(schoolClass.schoolYear)}\"\n")
        sb.append("Disciplina;\"${escapeCsv(schoolClass.subject.ifEmpty { exam.subject })}\";Prova;\"${escapeCsv(exam.title)}\"\n")
        sb.append("Data da Prova;\"${dateFormat.format(Date(exam.examDate))}\";Nota Máxima;${exam.maxScore}\n")
        sb.append("Total de Alunos Corrigidos;${submissions.size};Média da Turma;\"${String.format(Locale("pt", "BR"), "%.2f", stats.averageScore)}\"\n")
        sb.append("Maior Nota;\"${String.format(Locale("pt", "BR"), "%.2f", stats.highestScore)}\";Menor Nota;\"${String.format(Locale("pt", "BR"), "%.2f", stats.lowestScore)}\";Taxa de Aprovação;\"${String.format(Locale("pt", "BR"), "%.1f%%", stats.passRatePercentage)}\"\n\n")

        // Official Answer Key Row
        val answerKeyList = exam.getAnswerKeyList()
        sb.append("GABARITO OFICIAL;;;;;;;")
        for (i in 1..exam.totalQuestions) {
            val key = answerKeyList.getOrNull(i - 1) ?: "-"
            sb.append(";\"Q$i: $key\"")
        }
        sb.append("\n\n")

        // Student Data Table Headers
        sb.append("Nº;Nome do Aluno;Matrícula/Chamada;Acertos;Total Questões;Nota Final;Percentual (%);Situação")
        for (i in 1..exam.totalQuestions) {
            sb.append(";Q$i")
        }
        sb.append(";Data da Correção\n")

        // Student Rows
        val sortedSubmissions = submissions.sortedBy { it.studentName.lowercase() }
        sortedSubmissions.forEachIndexed { index, sub ->
            val num = index + 1
            val status = if (sub.percentage >= 60.0) "Aprovado" else "Recuperação"
            val answersList = sub.getAnswersList()

            sb.append("$num;")
            sb.append("\"${escapeCsv(sub.studentName)}\";")
            sb.append("\"${escapeCsv(sub.studentNumber)}\";")
            sb.append("${sub.correctCount};")
            sb.append("${sub.totalQuestions};")
            sb.append("\"${String.format(Locale("pt", "BR"), "%.1f", sub.finalScore)}\";")
            sb.append("\"${String.format(Locale("pt", "BR"), "%.1f%%", sub.percentage)}\";")
            sb.append("\"$status\"")

            // Question answers with indicator if correct or wrong
            for (q in 1..exam.totalQuestions) {
                val ans = answersList.getOrNull(q - 1) ?: "-"
                val correctAns = answerKeyList.getOrNull(q - 1) ?: ""
                val mark = if (ans == correctAns) "$ans (✓)" else if (ans == "-") "- (branco)" else "$ans (✗)"
                sb.append(";\"$mark\"")
            }

            sb.append(";\"${dateFormat.format(Date(sub.scannedAt))}\"\n")
        }

        // Question Difficulty Analysis Footer
        sb.append("\nESTATÍSTICA DE ACERTOS POR QUESTÃO\n")
        sb.append("Questão;Gabarito;Total Acertos;Taxa de Acerto (%)\n")
        for (q in 1..exam.totalQuestions) {
            val correctAns = answerKeyList.getOrNull(q - 1) ?: "-"
            val accuracy = stats.questionAccuracy[q] ?: 0.0
            val totalHits = submissions.count { it.getAnswersList().getOrNull(q - 1) == correctAns }
            sb.append("Q$q;\"$correctAns\";$totalHits;\"${String.format(Locale("pt", "BR"), "%.1f%%", accuracy)}\"\n")
        }

        return sb.toString()
    }

    /**
     * Exports CSV to file in cache and launches share intent.
     */
    fun exportAndShareSpreadsheet(
        context: Context,
        schoolClass: SchoolClass,
        exam: Exam,
        submissions: List<ExamSubmission>,
        stats: ExamStatistics
    ): Uri? {
        return try {
            val csvContent = generateCsvContent(schoolClass, exam, submissions, stats)
            val cleanTitle = exam.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(25)
            val cleanClass = schoolClass.name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(15)
            val fileName = "Notas_${cleanClass}_${cleanTitle}_${fileDateFormat.format(Date())}.csv"

            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Planilha de Notas - ${exam.title} (${schoolClass.name})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Segue em anexo a planilha com as notas e gabaritos corrigidos da prova '${exam.title}' - Turma: ${schoolClass.name}."
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Planilha de Notas")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
