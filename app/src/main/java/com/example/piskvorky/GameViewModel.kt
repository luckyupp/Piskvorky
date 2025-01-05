package com.example.piskvorky

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(private val gameHistoryManager: GameHistoryManager) : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val gameLogic = GameLogic()

    /**
     * Processes a player’s move at the specified row and column.
     * Handles turn switching, result checking, and optionally triggers AI move if in AI mode.
     */
    fun makeMove(row: Int, col: Int) {
        val currentState = _gameState.value

        // Ensure the cell is empty and game is ongoing
        if (currentState.board[row][col] == CellState.EMPTY &&
            currentState.result == GameResult.ONGOING) {

            // Player makes their move
            val newBoard = currentState.board.map { it.toMutableList() }
            newBoard[row][col] = currentState.currentPlayer

            val result = gameLogic.checkWin(newBoard, row, col)

            // Update the state after player's move
            _gameState.update { state ->
                state.copy(
                    board = newBoard,
                    currentPlayer = if (state.currentPlayer == CellState.X) CellState.O else CellState.X,
                    result = result
                )
            }

            // Handle game result and add to history if game ends
            if (result != GameResult.ONGOING) {
                addGameToHistory(result)
            }

            // If the game is still ongoing and it's a PC's turn
            val isPlayerVsAI = currentState.gameMode != GameMode.PLAYER_VS_PLAYER
            if (result == GameResult.ONGOING && isPlayerVsAI) {
                val updatedState = _gameState.value // Get the latest state
                if (updatedState.currentPlayer == CellState.O) { // Confirm it's AI's turn
                    viewModelScope.launch {
                        val (pcRow, pcCol) = gameLogic.makeComputerMove(updatedState.board, updatedState.gameMode)
                        processAIMove(pcRow, pcCol)
                    }
                }
            }
        }
    }

    /**
     * Processes the AI's move based on the given row and column.
     */
    private fun processAIMove(row: Int, col: Int) {
        val currentState = _gameState.value

        // Ensure the cell is empty and game is ongoing
        if (currentState.board[row][col] == CellState.EMPTY &&
            currentState.result == GameResult.ONGOING) {

            val newBoard = currentState.board.map { it.toMutableList() }
            newBoard[row][col] = currentState.currentPlayer

            val result = gameLogic.checkWin(newBoard, row, col)

            _gameState.update { state ->
                state.copy(
                    board = newBoard,
                    currentPlayer = if (state.currentPlayer == CellState.X) CellState.O else CellState.X,
                    result = result
                )
            }

            // Handle game result and add to history if game ends
            if (result != GameResult.ONGOING) {
                addGameToHistory(result)
            }
        }
    }

    /**
     * Sets the game mode and resets the board to the initial state.
     */
    fun setGameMode(mode: GameMode) {
        _gameState.update { state ->
            state.copy(
                board = List(15) { List(15) { CellState.EMPTY } },
                currentPlayer = CellState.X,
                gameMode = mode,
                result = GameResult.ONGOING
            )
        }
    }

    /**
     * Resets the game to its initial state.
     */
    fun resetGame() {
        _gameState.update { state ->
            state.copy(
                board = List(15) { List(15) { CellState.EMPTY } },
                currentPlayer = CellState.X,
                result = GameResult.ONGOING
            )
        }
    }

    /**
     * Adds the completed game to the history.
     */
    private fun addGameToHistory(result: GameResult) {
        val currentState = _gameState.value
        gameHistoryManager.addGame(
            playerX = "Player X", // Replace with actual player names if available
            playerO = "Player O",
            winner = when (result) {
                GameResult.X_WINS -> "X"
                GameResult.O_WINS -> "O"
                else -> "Draw"
            },
            movesCount = calculateMovesCount(),
            gameMode = currentState.gameMode
        )
    }

    /**
     * Calculates the total number of moves played in the current game.
     */
    private fun calculateMovesCount(): Int {
        return _gameState.value.board.sumOf { row ->
            row.count { it != CellState.EMPTY }
        }
    }

    /**
     * Returns the history of games.
     */
    fun getGameHistory(): List<GameHistoryEntry> = gameHistoryManager.getHistory()


}

class GameViewModelFactory(private val gameHistoryManager: GameHistoryManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(gameHistoryManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
