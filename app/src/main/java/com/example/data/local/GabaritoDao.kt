package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Exam
import com.example.data.model.ExamSubmission
import com.example.data.model.SchoolClass
import kotlinx.coroutines.flow.Flow

@Dao
interface GabaritoDao {
    // Classes
    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassById(id: Long): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // Exams
    @Query("SELECT * FROM exams ORDER BY createdAt DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE classId = :classId ORDER BY createdAt DESC")
    fun getExamsByClass(classId: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: Long): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    // Submissions
    @Query("SELECT * FROM submissions WHERE examId = :examId ORDER BY scannedAt DESC")
    fun getSubmissionsByExam(examId: Long): Flow<List<ExamSubmission>>

    @Query("SELECT * FROM submissions WHERE id = :id")
    suspend fun getSubmissionById(id: Long): ExamSubmission?

    @Query("SELECT * FROM submissions ORDER BY scannedAt DESC")
    fun getAllSubmissions(): Flow<List<ExamSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: ExamSubmission): Long

    @Update
    suspend fun updateSubmission(submission: ExamSubmission)

    @Delete
    suspend fun deleteSubmission(submission: ExamSubmission)

    @Query("DELETE FROM submissions WHERE examId = :examId")
    suspend fun deleteSubmissionsByExam(examId: Long)
}
