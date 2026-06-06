package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KoreksiDao {
    // --- AnswerKey Queries ---
    @Query("SELECT * FROM answer_keys ORDER BY timestamp DESC")
    fun getAllAnswerKeys(): Flow<List<AnswerKey>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerKey(answerKey: AnswerKey)

    @Update
    suspend fun updateAnswerKey(answerKey: AnswerKey)

    @Delete
    suspend fun deleteAnswerKey(answerKey: AnswerKey)

    @Query("DELETE FROM answer_keys WHERE id = :id")
    suspend fun deleteAnswerKeyById(id: Int)


    // --- GradingHistory Queries ---
    @Query("SELECT * FROM grading_history ORDER BY timestamp DESC")
    fun getAllGradingHistory(): Flow<List<GradingHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGradingHistory(history: GradingHistory)

    @Delete
    suspend fun deleteGradingHistory(history: GradingHistory)

    @Query("DELETE FROM grading_history WHERE id = :id")
    suspend fun deleteGradingHistoryById(id: Int)
}
