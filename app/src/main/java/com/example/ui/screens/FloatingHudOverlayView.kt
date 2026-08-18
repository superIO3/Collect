package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.GtoRecommendation
import com.example.model.TableState
import com.example.ui.components.ActionFrequencyBar
import com.example.ui.components.CardView
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerRuby
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun FloatingHudOverlayView(
    tableState: TableState,
    recommendation: GtoRecommendation,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionColor = when (recommendation.primaryAction) {
        ActionType.RAISE, ActionType.BET -> PokerGold
        ActionType.CALL -> PokerEmerald
        ActionType.CHECK -> PokerCyan
        ActionType.FOLD -> PokerRuby
        ActionType.ALL_IN -> Color(0xFFC084FC)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, actionColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
        color = Color(0xEE0B1410),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PokerEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE SCREEN GTO HUD",
                        color = PokerGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("close_floating_hud")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close HUD",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cards & Context Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hero cards mini
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tableState.heroCards.forEach { card ->
                        CardView(card = card, width = 32.dp, height = 46.dp)
                    }
                    if (tableState.boardCards.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color(0xFF2C3E33))
                                .padding(horizontal = 4.dp)
                        )
                        tableState.boardCards.forEach { card ->
                            CardView(card = card, width = 28.dp, height = 40.dp)
                        }
                    }
                }

                // Pos & Pot chip
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${tableState.heroPosition.displayName} • ${tableState.street.displayName}",
                        color = PokerCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pot: $${tableState.potSize.roundToInt()}" + if (tableState.currentBetToCall > 0) " (Call $${tableState.currentBetToCall.roundToInt()})" else "",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Primary Recommendation Callout Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                actionColor.copy(alpha = 0.25f),
                                Color(0xFF13221B)
                            )
                        )
                    )
                    .border(1.dp, actionColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WHAT TO DO NOW",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recommendation.primaryAction.label.uppercase()} ${recommendation.primarySizing}",
                            color = actionColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EQUITY",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", recommendation.heroEquity)}%",
                            color = if (recommendation.heroEquity >= 50f) PokerEmerald else PokerRuby,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Frequency Bar
            ActionFrequencyBar(frequencies = recommendation.frequencies)

            Spacer(modifier = Modifier.height(6.dp))

            // Quick one-line explanation
            Text(
                text = recommendation.explanation,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}
