package com.example.data

import kotlinx.coroutines.flow.Flow

class KoreksiRepository(private val dao: KoreksiDao) {
    val allAnswerKeys: Flow<List<AnswerKey>> = dao.getAllAnswerKeys()
    val allGradingHistory: Flow<List<GradingHistory>> = dao.getAllGradingHistory()

    suspend fun insertAnswerKey(answerKey: AnswerKey) {
        dao.insertAnswerKey(answerKey)
    }

    suspend fun updateAnswerKey(answerKey: AnswerKey) {
        dao.updateAnswerKey(answerKey)
    }

    suspend fun deleteAnswerKey(answerKey: AnswerKey) {
        dao.deleteAnswerKey(answerKey)
    }

    suspend fun deleteAnswerKeyById(id: Int) {
        dao.deleteAnswerKeyById(id)
    }

    suspend fun insertGradingHistory(history: GradingHistory) {
        dao.insertGradingHistory(history)
    }

    suspend fun deleteGradingHistory(history: GradingHistory) {
        dao.deleteGradingHistory(history)
    }

    suspend fun deleteGradingHistoryById(id: Int) {
        dao.deleteGradingHistoryById(id)
    }
}
