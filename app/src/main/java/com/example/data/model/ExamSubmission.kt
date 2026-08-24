package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "submissions",
    foreignKeys = [
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("examId")]
)
data class ExamSubmission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Long,
    val classId: Long,
    val studentName: String,
    val studentNumber: String = "",
    val scannedAnswers: String, // Comma-separated student answers, e.g. "A,B,C,A,E,A,B,C,D,E"
    val correctCount: Int,
    val totalQuestions: Int,
    val finalScore: Double,
    val percentage: Double,
    val scannedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun getAnswersList(): List<String> {
        return scannedAnswers.split(",").map { it.trim().uppercase() }
    }
}
