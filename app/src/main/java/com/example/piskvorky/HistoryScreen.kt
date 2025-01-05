package com.example.piskvorky.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.piskvorky.GameHistoryEntry
import com.example.piskvorky.GameStatistics

/**
 * Screen for displaying game history and statistics.
 * @param history List of completed games.
 * @param statistics Summary statistics of games played.
 */
@Composable
fun HistoryScreen(
    history: List<GameHistoryEntry>,
    statistics: GameStatistics
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display statistics
        Text(
            text = "Game Statistics",
            style = MaterialTheme.typography.headlineMedium
        )

        BasicText("Total Games: ${statistics.totalGames}")
        BasicText("Wins Against PC: ${statistics.winsAgainstPC}")
        BasicText("Losses Against PC: ${statistics.lossesAgainstPC}")
        BasicText("Win Rate Against PC: ${"%.2f".format(statistics.winRateAgainstPC)}%")

        // Spacer
        Text("")

        // Display game history
        Text(
            text = "Game History",
            style = MaterialTheme.typography.headlineMedium
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(history.size) { index ->
                val entry = history[index]
                Text(
                    text = "${entry.playerX} vs ${entry.playerO} - Winner: ${entry.winner} - Moves: ${entry.movesCount} - ${entry.endTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
