package com.example.piskvorky

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val gameLogic = GameLogic()

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


    // New function to handle AI moves separately
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
        }
    }


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

    fun resetGame() {
        _gameState.update { state ->
            state.copy(
                board = List(15) { List(15) { CellState.EMPTY } },
                currentPlayer = CellState.X,
                result = GameResult.ONGOING
            )
        }
    }
}