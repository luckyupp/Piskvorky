package com.example.piskvorky

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Data class for storing individual game entries
data class GameHistoryEntry(
    val playerX: String,
    val playerO: String,
    val winner: String, // "X", "O", or "Draw"
    val movesCount: Int,
    val endTime: String, // ISO 8601 format
    val gameMode: GameMode
)

// Class for managing history and statistics
class GameHistoryManager(private val context: Context) {

    private val fileName = "game_history.json"
    private val gson = Gson()

    // Cached history data
    private var history: MutableList<GameHistoryEntry> = mutableListOf()

    init {
        loadHistory()
    }

    // Add a new game entry
    fun addGame(playerX: String, playerO: String, winner: String, movesCount: Int, gameMode: GameMode) {
        val entry = GameHistoryEntry(
            playerX = playerX,
            playerO = playerO,
            winner = winner,
            movesCount = movesCount,
            endTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            gameMode = gameMode
        )
        history.add(entry)
        saveHistory()
    }

    // Get all game history
    fun getHistory(): List<GameHistoryEntry> = history

    // Calculate statistics from history
    fun getStatistics(): GameStatistics {
        val totalGames = history.size
        val winsAgainstPC = history.count { it.gameMode != GameMode.PLAYER_VS_PLAYER && it.winner == "X" }
        val lossesAgainstPC = history.count { it.gameMode != GameMode.PLAYER_VS_PLAYER && it.winner == "O" }
        val winRateAgainstPC = if (winsAgainstPC + lossesAgainstPC > 0) {
            (winsAgainstPC.toDouble() / (winsAgainstPC + lossesAgainstPC)) * 100
        } else 0.0

        return GameStatistics(
            totalGames = totalGames,
            winsAgainstPC = winsAgainstPC,
            lossesAgainstPC = lossesAgainstPC,
            winRateAgainstPC = winRateAgainstPC
        )
    }

    // Load history from file
    private fun loadHistory() {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<GameHistoryEntry>>() {}.type
            history = gson.fromJson(json, type) ?: mutableListOf()
        }
    }

    // Save history to file
    private fun saveHistory() {
        val file = File(context.filesDir, fileName)
        val json = gson.toJson(history)
        file.writeText(json)
    }
}

// Data class for game statistics
data class GameStatistics(
    val totalGames: Int,
    val winsAgainstPC: Int,
    val lossesAgainstPC: Int,
    val winRateAgainstPC: Double
)
