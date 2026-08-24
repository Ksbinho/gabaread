package com.example.engine

import com.example.data.model.Exam
import com.example.data.model.SchoolClass

object AnswerSheetGenerator {

    /**
     * Generates a clean text/markdown representation and sharing payload of the official answer sheet format.
     */
    fun getPrintableInstructions(schoolClass: SchoolClass, exam: Exam): String {
        val sb = StringBuilder()
        sb.append("=====================================================\n")
        sb.append("         FOLHA DE RESPOSTAS - GABARITO OFICIAL       \n")
        sb.append("=====================================================\n")
        sb.append("ESCOLA / INSTITUIÇÃO: _______________________________\n")
        sb.append("TURMA: ${schoolClass.name.padEnd(20)} ANO: ${schoolClass.schoolYear}\n")
        sb.append("DISCIPLINA: ${(schoolClass.subject.ifEmpty { exam.subject }).padEnd(20)}\n")
        sb.append("AVALIAÇÃO: ${exam.title}\n")
        sb.append("NOME DO ALUNO: __________________________ Nº: ______\n")
        sb.append("-----------------------------------------------------\n")
        sb.append("INSTRUÇÕES DE PREENCHIMENTO:\n")
        sb.append("1. Use caneta esferográfica preta ou azul.\n")
        sb.append("2. Preencha completamente o círculo da resposta escolhida.\n")
        sb.append("3. Não rasure nem dobre esta folha.\n")
        sb.append("-----------------------------------------------------\n\n")

        val options = listOf("A", "B", "C", "D", "E").take(exam.optionsPerQuestion)

        for (q in 1..exam.totalQuestions) {
            val qStr = q.toString().padStart(2, '0')
            val bubbles = options.joinToString("   ") { "( $it )" }
            sb.append("  Questão $qStr:   $bubbles\n")
        }

        sb.append("\n=====================================================\n")
        sb.append("Código da Prova: [EXAM-${exam.id}-${exam.totalQuestions}Q]\n")
        return sb.toString()
    }
}
