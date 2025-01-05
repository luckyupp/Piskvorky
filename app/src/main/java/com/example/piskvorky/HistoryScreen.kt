package com.example.piskvorky

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val history: List<GameHistoryEntry>,
    private val textColor: Color
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val winnerView: TextView = itemView.findViewById(android.R.id.text1)
        private val detailsView: TextView = itemView.findViewById(android.R.id.text2)

        private fun GameMode.toDisplayName(): String {
            return when (this) {
                GameMode.PLAYER_VS_PLAYER -> "Player O"
                GameMode.PLAYER_VS_PC_EASY -> "Easy PC"
                GameMode.PLAYER_VS_PC_MEDIUM -> "Medium PC"
                GameMode.PLAYER_VS_PC_HARD -> "Hard PC"
            }
        }

        private fun formatTime(rawTime: String): String {
            return try {
                val inputFormatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
                val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                val parsedDateTime = java.time.LocalDateTime.parse(rawTime, inputFormatter)
                parsedDateTime.format(outputFormatter)
            } catch (e: Exception) {
                rawTime // Pokud formátování selže, vrátíme původní řetězec
            }
        }



        fun bind(entry: GameHistoryEntry) {
            val winner = when (entry.winner) {
                "X" -> "👑 Player X"
                "O" -> "👑 ${if (entry.gameMode != GameMode.PLAYER_VS_PLAYER) entry.gameMode.toDisplayName() else "Player O"}"
                else -> "Draw"
            }

            val loser = when (entry.winner) {
                "X" -> "⚫ ${if (entry.gameMode != GameMode.PLAYER_VS_PLAYER) entry.gameMode.toDisplayName() else "Player O"}"
                "O" -> "⚫ Player X"
                else -> "⚫ Player X, ⚫ ${if (entry.gameMode != GameMode.PLAYER_VS_PLAYER) entry.gameMode.toDisplayName() else "Player O"}"
            }

            val movesText = "Moves: ${entry.movesCount}"
            val timeText = "End: ${formatTime(entry.endTime)}"
            /*val winRate = if (entry.gameMode != GameMode.PLAYER_VS_PLAYER) {
                "WinRate: ${"%.2f".format(statistics.winRateAgainstPC)}%"
            } else {
                "WinRate: N/A"
            }*/

            val winnerText = "$winner vs $loser".let { text ->
                if (text.length > 45) text.take(45) + "..." else text
            }
            val detailsText = "$movesText | $timeText".let { text ->
                if (text.length > 80) text.take(80) + "..." else text
            }

            winnerView.text = winnerText
            detailsView.text = detailsText
            winnerView.setTextColor(textColor.toArgb())
            detailsView.setTextColor(textColor.toArgb())
        }
    }




        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            //setPadding(16, 8, 16, 8) // Odsazení kolem textu

            // Hlavní řádek (hráči)
            addView(TextView(context).apply {
                id = android.R.id.text1
                textSize = 18f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            // Detailní řádek (tahy, čas, winrate)
            addView(TextView(context).apply {
                id = android.R.id.text2
                textSize = 14f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
        }
        return HistoryViewHolder(itemView)
    }







    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount(): Int = history.size
}

@Composable
fun HistoryScreen(
    history: List<GameHistoryEntry>
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current

    AndroidView(factory = { ctx ->
        RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = HistoryAdapter(history, textColor)
        }
    })
}
