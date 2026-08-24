package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "9º Ano A", "3º Ano EM"
    val subject: String = "", // e.g. "Matemática", "História"
    val schoolYear: String = "2026",
    val studentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
