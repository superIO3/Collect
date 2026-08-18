package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PokerBorder
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerRuby
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun EquityMeter(
    heroEquity: Float,
    villainEquity: Float,
    potOdds: Float,
    spr: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PokerSurface)
            .border(1.dp, PokerBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HERO EQUITY",
                    color = PokerEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "VILLAIN RANGE",
                    color = PokerRuby,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Equity Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${String.format("%.1f", heroEquity)}%",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${String.format("%.1f", villainEquity)}%",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Equity Bar
            val shape = RoundedCornerShape(6.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(shape)
                    .background(Color(0xFF1E2822))
            ) {
                Box(
                    modifier = Modifier
                        .weight(heroEquity.coerceAtLeast(1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(PokerEmerald, Color(0xFF34D399))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(villainEquity.coerceAtLeast(1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(PokerRuby, Color(0xFFF87171))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pot Odds vs Required Equity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "POT ODDS REQUIRED",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val isFavorable = heroEquity >= potOdds
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${String.format("%.1f", potOdds)}%",
                            color = if (isFavorable) PokerGold else PokerRuby,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFavorable) "(+EV Direct Call)" else "(-EV Pure Call)",
                            color = if (isFavorable) PokerEmerald else PokerRuby,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "STACK-TO-POT (SPR)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format("%.2f", spr) + when {
                            spr < 3f -> " (Committed)"
                            spr < 8f -> " (Medium)"
                            else -> " (Deep)"
                        },
                        color = PokerCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
