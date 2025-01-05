package com.example.piskvorky

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class GameHistoryEntry(
    val playerX: String,
    val playerO: String,
    val winner: String, // "X", "O", or "Draw"
    val movesCount: Int,
    val endTime: String, // ISO 8601 format
    val gameMode: GameMode
)

class GameHistoryManager(private val context: Context) {

    private val fileName = "game_history.json"
    private val gson = Gson()
    private var history: MutableList<GameHistoryEntry> = mutableListOf()

    init {
        loadHistory()
    }

    fun addGame(playerX: String, playerO: String, winner: String, movesCount: Int, gameMode: GameMode) {
        val opponentName = when (gameMode) {
            GameMode.PLAYER_VS_PC_EASY -> "Easy PC"
            GameMode.PLAYER_VS_PC_MEDIUM -> "Medium PC"
            GameMode.PLAYER_VS_PC_HARD -> "Hard PC"
            else -> playerO
        }

        val formattedEndTime = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("d. MMMM yyyy, HH:mm", Locale.getDefault()))

        val entry = GameHistoryEntry(
            playerX = playerX,
            playerO = opponentName,
            winner = winner,
            movesCount = movesCount,
            endTime = formattedEndTime,
            gameMode = gameMode
        )
        history.add(entry)
        saveHistory()
    }

    fun getHistory(): List<GameHistoryEntry> = history

    private fun loadHistory() {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<GameHistoryEntry>>() {}.type
            history = gson.fromJson(json, type) ?: mutableListOf()
        }
    }

    private fun saveHistory() {
        val file = File(context.filesDir, fileName)
        val json = gson.toJson(history)
        file.writeText(json)
    }
}
