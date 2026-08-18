package com.example.ui.components

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
import com.example.model.Card
import com.example.model.Position
import com.example.model.Street
import com.example.ui.theme.PokerBorder
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerFeltGreen
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerRuby
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun PokerTableView(
    heroCards: List<Card>,
    boardCards: List<Card>,
    heroPosition: Position,
    potSize: Float,
    currentBet: Float,
    street: Street,
    heroStack: Float,
    onHeroCardClick: (Int) -> Unit,
    onBoardCardClick: (Int) -> Unit,
    onPositionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Felt Table (Oval Shaped Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .shadow(12.dp, RoundedCornerShape(100.dp))
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1B4E38),
                            PokerFeltGreen,
                            PokerFeltDark
                        )
                    )
                )
                .border(
                    width = 4.dp,
                    color = Color(0xFF2C3E33),
                    shape = RoundedCornerShape(100.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Table Elements Container
            Column(
                modifier = Modifier.matchParentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Position Badge and Street status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF12221A))
                            .border(1.dp, PokerGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { onPositionClick() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("position_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "POS: ",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = heroPosition.displayName,
                                color = PokerGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Street Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF12221A))
                            .border(1.dp, PokerBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = street.displayName.uppercase(),
                            color = PokerCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pot & Bet Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PokerGold.copy(alpha = 0.15f))
                            .border(1.dp, PokerGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "POT: $${potSize.roundToInt()}",
                            color = PokerGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Center: Community Board Cards (5 cards)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COMMUNITY BOARD",
                        color = Color(0xFF86A795),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 5) {
                            val card = boardCards.getOrNull(i)
                            CardView(
                                card = card,
                                width = 42.dp,
                                height = 60.dp,
                                emptyPlaceholderText = when (i) {
                                    in 0..2 -> "F"
                                    3 -> "T"
                                    else -> "R"
                                },
                                onClick = { onBoardCardClick(i) }
                            )
                        }
                    }
                }

                // Bottom: Hero Hole Cards & Stack
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 2) {
                                val card = heroCards.getOrNull(i)
                                CardView(
                                    card = card,
                                    width = 46.dp,
                                    height = 66.dp,
                                    isSelected = true,
                                    emptyPlaceholderText = if (i == 0) "C1" else "C2",
                                    onClick = { onHeroCardClick(i) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
