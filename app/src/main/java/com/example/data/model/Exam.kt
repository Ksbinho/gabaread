package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId")]
)
data class Exam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val title: String, // e.g. "Avaliação Bimestral 1", "Simulado Geral"
    val subject: String = "",
    val totalQuestions: Int = 10, // 5, 10, 15, 20, 30, etc.
    val optionsPerQuestion: Int = 5, // 4 (A-D) or 5 (A-E)
    val maxScore: Double = 10.0, // Nota máxima (10.0, 100.0)
    val answerKey: String, // Comma-separated correct answers, e.g. "A,B,C,D,E,A,B,C,D,E"
    val examDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getAnswerKeyList(): List<String> {
        return answerKey.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
    }
}
