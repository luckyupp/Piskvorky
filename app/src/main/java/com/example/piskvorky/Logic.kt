package com.example.piskvorky

enum class CellState {
    EMPTY, X, O
}

enum class GameMode {
    PLAYER_VS_PLAYER,
    PLAYER_VS_PC_EASY,
    PLAYER_VS_PC_MEDIUM,
    PLAYER_VS_PC_HARD
}

enum class GameResult {
    ONGOING, X_WINS, O_WINS, DRAW
}

data class GameState(
    val board: List<List<CellState>> = List(15) { List(15) { CellState.EMPTY } },
    val currentPlayer: CellState = CellState.X,
    val gameMode: GameMode = GameMode.PLAYER_VS_PLAYER,
    val result: GameResult = GameResult.ONGOING
)

class GameLogic {
    private val transpositionTable = mutableMapOf<String, Int>()
    fun checkWin(board: List<List<CellState>>, row: Int, col: Int): GameResult {
        val player = board[row][col]

        // Check horizontal
        var count = 0
        for (c in maxOf(0, col - 4)..minOf(14, col + 4)) {
            if (board[row][c] == player) count++
            else count = 0
            if (count == 5) return if (player == CellState.X) GameResult.X_WINS else GameResult.O_WINS
        }

        // Check vertical
        count = 0
        for (r in maxOf(0, row - 4)..minOf(14, row + 4)) {
            if (board[r][col] == player) count++
            else count = 0
            if (count == 5) return if (player == CellState.X) GameResult.X_WINS else GameResult.O_WINS
        }

        // Check diagonal (top-left to bottom-right)
        count = 0
        for (d in -4..4) {
            val r = row + d
            val c = col + d
            if (r in 0..14 && c in 0..14 && board[r][c] == player) count++
            else count = 0
            if (count == 5) return if (player == CellState.X) GameResult.X_WINS else GameResult.O_WINS
        }

        // Check diagonal (top-right to bottom-left)
        count = 0
        for (d in -4..4) {
            val r = row + d
            val c = col - d
            if (r in 0..14 && c in 0..14 && board[r][c] == player) count++
            else count = 0
            if (count == 5) return if (player == CellState.X) GameResult.X_WINS else GameResult.O_WINS
        }

        // Check for draw
        if (board.all { row -> row.none { it == CellState.EMPTY } }) {
            return GameResult.DRAW
        }

        return GameResult.ONGOING
    }

    fun makeComputerMove(board: List<List<CellState>>, mode: GameMode): Pair<Int, Int> {
        return when (mode) {
            GameMode.PLAYER_VS_PC_EASY -> makeRandomMoveNearExisting(board)
            GameMode.PLAYER_VS_PC_MEDIUM -> makeMediumMoveEnhanced(board)
            GameMode.PLAYER_VS_PC_HARD -> makeHardMove(board)
            else -> throw IllegalArgumentException("Invalid game mode")
        }
    }

    private fun makeRandomMoveNearExisting(board: List<List<CellState>>): Pair<Int, Int> {
        val nearMoves = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until 15 && nc in 0 until 15 && board[nr][nc] != CellState.EMPTY) {
                                nearMoves.add(Pair(r, c))
                                break
                            }
                        }
                    }
                }
            }
        }
        return if (nearMoves.isNotEmpty()) nearMoves.random() else makeRandomMoveNearExisting(board)
    }

    private fun makeMediumMoveEnhanced(board: List<List<CellState>>): Pair<Int, Int> {
        val player = CellState.O
        val opponent = CellState.X

        // Check for a winning move
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    val tempBoard = board.map { it.toMutableList() }
                    tempBoard[r][c] = player
                    if (checkWin(tempBoard, r, c) == GameResult.O_WINS) return Pair(r, c)
                }
            }
        }

        // Block opponent's winning move
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    val tempBoard = board.map { it.toMutableList() }
                    tempBoard[r][c] = opponent
                    if (checkWin(tempBoard, r, c) == GameResult.X_WINS) return Pair(r, c)
                }
            }
        }

        // Evaluate moves based on scoring
        var bestMove: Pair<Int, Int>? = null
        var maxScore = Int.MIN_VALUE

        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    val score = evaluatePosition(board, r, c, player, opponent)
                    if (score > maxScore) {
                        maxScore = score
                        bestMove = Pair(r, c)
                    }
                }
            }
        }
        return bestMove ?: makeRandomMoveNearExisting(board)
    }


    private fun evaluatePosition(
        board: List<List<CellState>>,
        row: Int,
        col: Int,
        player: CellState,
        opponent: CellState
    ): Int {
        var score = 0
        score += evaluateLine(board, row, col, 1, 0, player, opponent) // Horizontal
        score += evaluateLine(board, row, col, 0, 1, player, opponent) // Vertical
        score += evaluateLine(board, row, col, 1, 1, player, opponent) // Diagonal \
        score += evaluateLine(board, row, col, 1, -1, player, opponent) // Diagonal /

        return score
    }

    private fun evaluateLine(
        board: List<List<CellState>>,
        row: Int,
        col: Int,
        dr: Int,
        dc: Int,
        player: CellState,
        opponent: CellState
    ): Int {
        var score = 0
        var countPlayer = 0
        var countOpponent = 0

        for (d in -4..4) {
            val nr = row + d * dr
            val nc = col + d * dc
            if (nr in 0..14 && nc in 0..14) {
                when (board[nr][nc]) {
                    player -> countPlayer++
                    opponent -> countOpponent++
                    else -> { }
                }
            }
        }

        // Prioritize extending own lines and blocking opponent
        if (countPlayer > 0 && countOpponent == 0) score += countPlayer * countPlayer
        if (countOpponent > 0 && countPlayer == 0) score += countOpponent * countOpponent

        return score
    }

    private fun makeHardMove(board: List<List<CellState>>): Pair<Int, Int> {
        val player = CellState.O
        val opponent = CellState.X
        var bestMove: Pair<Int, Int>? = null
        var bestValue = Int.MIN_VALUE

        // 1. Block opponent's winning move
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    val tempBoard = board.map { it.toMutableList() }
                    tempBoard[r][c] = opponent
                    if (checkWin(tempBoard, r, c) == GameResult.X_WINS) {
                        return Pair(r, c) // Block opponent's winning move
                    }
                }
            }
        }

        // 2. Try to win by making a winning move
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    val tempBoard = board.map { it.toMutableList() }
                    tempBoard[r][c] = player
                    if (checkWin(tempBoard, r, c) == GameResult.O_WINS) {
                        return Pair(r, c) // Make the winning move
                    }
                }
            }
        }

        // 3. Evaluate all remaining moves based on score
        val candidateMoves = getCandidateMoves(board)
        for ((r, c) in candidateMoves) {
            val tempBoard = board.map { it.toMutableList() }
            tempBoard[r][c] = player
            val moveValue = minimax(tempBoard, depth = 1, isMaximizing = false, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE, player, opponent)
            if (moveValue > bestValue) {
                bestValue = moveValue
                bestMove = Pair(r, c)
            }
        }

        // If no move was found, fallback to a random move
        return bestMove ?: makeRandomMoveNearExisting(board)
    }



    private fun minimax(
        board: List<List<CellState>>,
        depth: Int,
        isMaximizing: Boolean,
        alpha: Int,
        beta: Int,
        player: CellState,
        opponent: CellState
    ): Int {
        val boardHash = board.hashCode().toString() // Convert board state to a string
        transpositionTable[boardHash]?.let {
            return it // Return the previously computed value for this state
        }
        val result = getGameResult(board)
        if (result != GameResult.ONGOING || depth == 0) {
            return evaluateBoard(board, player, opponent)
        }

        var bestValue = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE
        var alphaVar = alpha
        var betaVar = beta

        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) {
                    val tempBoard = board.map { it.toMutableList() }
                    tempBoard[r][c] = if (isMaximizing) player else opponent
                    val value = minimax(tempBoard, depth - 1, !isMaximizing, alphaVar, betaVar, player, opponent)
                    bestValue = if (isMaximizing) {
                        maxOf(bestValue, value)
                    } else {
                        minOf(bestValue, value)
                    }

                    if (isMaximizing) alphaVar = maxOf(alphaVar, bestValue)
                    else betaVar = minOf(betaVar, bestValue)

                    if (betaVar <= alphaVar) break
                }
            }
        }

        return bestValue
    }


    private fun getCandidateMoves(board: List<List<CellState>>): List<Pair<Int, Int>> {
        val candidates = mutableSetOf<Pair<Int, Int>>()
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] != CellState.EMPTY) {
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until 15 && nc in 0 until 15 && board[nr][nc] == CellState.EMPTY) {
                                candidates.add(Pair(nr, nc))
                            }
                        }
                    }
                }
            }
        }
        return candidates.toList()
    }


    private fun getGameResult(board: List<List<CellState>>): GameResult {
        // Iterate through the board to check for a winning condition
        for (row in 0 until 15) {
            for (col in 0 until 15) {
                if (board[row][col] != CellState.EMPTY) {
                    val result = checkWin(board, row, col)
                    if (result != GameResult.ONGOING) return result
                }
            }
        }

        // Check for a draw
        return if (board.all { row -> row.all { it != CellState.EMPTY } }) {
            GameResult.DRAW
        } else {
            GameResult.ONGOING
        }
    }



    private fun evaluateBoard(board: List<List<CellState>>, player: CellState, opponent: CellState): Int {
        var score = 0
        for (r in 0 until 15) {
            for (c in 0 until 15) {
                if (board[r][c] == CellState.EMPTY) continue
                val cellOwner = if (board[r][c] == player) 1 else -1
                score += cellOwner * evaluateLine(board, r, c, 1, 0, player, opponent) // Horizontal
                score += cellOwner * evaluateLine(board, r, c, 0, 1, player, opponent) // Vertical
                score += cellOwner * evaluateLine(board, r, c, 1, 1, player, opponent) // Diagonal \
                score += cellOwner * evaluateLine(board, r, c, 1, -1, player, opponent) // Diagonal /
            }
        }
        return score
    }
}