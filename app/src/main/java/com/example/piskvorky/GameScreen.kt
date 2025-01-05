package com.example.piskvorky

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.piskvorky.*
import com.example.piskvorky.CellState
import com.example.piskvorky.GameViewModel
import com.example.piskvorky.GameMode
import com.example.piskvorky.GameResult
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Main game screen for the Tic-Tac-Toe application.
 * Displays the game board, game status, current player, and history toggle.
 * Allows selecting a cell and tracks the currently selected position.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    var showHistory by remember { mutableStateOf(false) } // Toggle between game board and history view
    var selectedPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) } // Track selected cell position

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tic-Tac-Toe (Piskvorky)",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (gameState.result) {
                            GameResult.X_WINS -> "X Wins!"
                            GameResult.O_WINS -> "O Wins!"
                            GameResult.DRAW -> "It's a Draw!"
                            GameResult.ONGOING -> "Current Player: ${if (gameState.currentPlayer == CellState.X) "X" else "O"}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Selected Position
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val positionText = selectedPosition?.let { (row, col) ->
                        "Selected Position: Row ${row + 1}, Column ${col + 1}"
                    } ?: "No position selected"
                    Text(
                        text = positionText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Game Mode Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = 2
            ) {
                arrayOf(
                    GameMode.PLAYER_VS_PLAYER to "Player vs Player",
                    GameMode.PLAYER_VS_PC_EASY to "Easy PC",
                    GameMode.PLAYER_VS_PC_MEDIUM to "Medium PC",
                    GameMode.PLAYER_VS_PC_HARD to "Hard PC"
                ).forEach { (mode, label) ->
                    Button(
                        onClick = { viewModel.setGameMode(mode) },
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f, true),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gameState.gameMode == mode)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Board or History
            Box(modifier = Modifier.weight(1f)) {
                if (!showHistory) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(15),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        items(gameState.board.flatten().size) { index ->
                            val row = index / 15
                            val col = index % 15
                            val cellState = gameState.board[row][col]

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable(enabled = cellState == CellState.EMPTY) {
                                        viewModel.makeMove(row, col)
                                        selectedPosition = row to col // Update selected position
                                    }
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (cellState) {
                                        CellState.X -> "X"
                                        CellState.O -> "O"
                                        CellState.EMPTY -> ""
                                    },
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = when (cellState) {
                                        CellState.X -> Color.Blue
                                        CellState.O -> Color.Red
                                        CellState.EMPTY -> Color.Unspecified
                                    }
                                )
                            }
                        }
                    }
                } else {
                    val history = viewModel.getGameHistory()

                    HistoryScreen(
                        history = history,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle and Reset Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showHistory = !showHistory },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(if (showHistory) "Back to Game" else "View History")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.resetGame() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restart")
                }
            }
        }
    }
}
