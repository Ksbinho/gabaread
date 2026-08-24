package com.example.data.model

data class ExamStatistics(
    val totalSubmissions: Int = 0,
    val averageScore: Double = 0.0,
    val highestScore: Double = 0.0,
    val lowestScore: Double = 0.0,
    val passRatePercentage: Double = 0.0,
    val questionAccuracy: Map<Int, Double> = emptyMap(), // Question Number -> Accuracy percentage (0.0 to 100.0)
    val questionMostWrongAnswers: Map<Int, String> = emptyMap() // Question Number -> Most common mistake option
)

data class QuestionEvaluation(
    val questionNumber: Int,
    val studentAnswer: String, // "A", "B", "C", "D", "E", or "-"
    val correctAnswer: String, // "A", "B", "C", "D", "E"
    val isCorrect: Boolean
)
