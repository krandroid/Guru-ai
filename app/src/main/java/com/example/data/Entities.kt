package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answer_keys")
data class AnswerKey(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "grading_history")
data class GradingHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val subject: String,
    val title: String,
    val studentAnswer: String,
    val score: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
