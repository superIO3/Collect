package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionFrequency
import com.example.model.ActionType
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerPurple
import com.example.ui.theme.PokerRuby
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ActionFrequencyBar(
    frequencies: List<ActionFrequency>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Multi-segment progress bar
        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(shape)
                .background(Color(0xFF1E2923))
        ) {
            frequencies.forEach { freq ->
                if (freq.percentage > 0.5f) {
                    val color = when (freq.action) {
                        ActionType.RAISE -> PokerGold
                        ActionType.BET -> PokerGold
                        ActionType.CALL -> PokerEmerald
                        ActionType.CHECK -> PokerCyan
                        ActionType.FOLD -> PokerRuby
                        ActionType.ALL_IN -> PokerPurple
                    }
                    Box(
                        modifier = Modifier
                            .weight(freq.percentage.coerceAtLeast(1f))
                            .fillMaxHeight()
                            .background(color)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (freq.percentage >= 14f) {
                            Text(
                                text = "${freq.action.label} ${freq.percentage.roundToInt()}%",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend / labels
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            frequencies.forEach { freq ->
                val color = when (freq.action) {
                    ActionType.RAISE, ActionType.BET -> PokerGold
                    ActionType.CALL -> PokerEmerald
                    ActionType.CHECK -> PokerCyan
                    ActionType.FOLD -> PokerRuby
                    ActionType.ALL_IN -> PokerPurple
                }
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${freq.action.label}: ${freq.percentage.roundToInt()}%" +
                                if (freq.suggestedSizing.isNotBlank() && freq.suggestedSizing != "-") " (${freq.suggestedSizing})" else "",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
