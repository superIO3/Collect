package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.Rank
import com.example.model.Suit
import com.example.ui.components.CardPickerSheet
import com.example.ui.components.CardView
import com.example.ui.theme.PokerBorder
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerRuby
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PokerViewModel
import kotlin.math.roundToInt

@Composable
fun EquityCalculatorScreen(
    viewModel: PokerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.equityCalcState.collectAsState()

    var showCardPicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf<String?>(null) } // "hero_0", "villain_1", "board_2"

    val allUsedCards = state.heroCards + state.villainCards + state.boardCards

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PokerFeltDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MONTE CARLO SIMULATOR",
                    color = PokerGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Hand vs Hand Equity",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.runEquitySimulation() },
                modifier = Modifier.testTag("recalculate_equity_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recalculate",
                    tint = PokerGold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero vs Villain Card Rows
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, PokerBorder, RoundedCornerShape(14.dp)),
            color = PokerSurface,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Hero Hand Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "HERO HAND", color = PokerEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0 until 2) {
                                val card = state.heroCards.getOrNull(i)
                                CardView(
                                    card = card,
                                    width = 46.dp,
                                    height = 64.dp,
                                    isSelected = true,
                                    emptyPlaceholderText = "H${i + 1}",
                                    onClick = {
                                        pickerTarget = "hero_$i"
                                        showCardPicker = true
                                    }
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "HERO WIN", color = TextSecondary, fontSize = 10.sp)
                        Text(
                            text = "${String.format("%.1f", state.heroWinPercent)}%",
                            color = PokerEmerald,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Villain Hand Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "VILLAIN HAND", color = PokerRuby, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0 until 2) {
                                val card = state.villainCards.getOrNull(i)
                                CardView(
                                    card = card,
                                    width = 46.dp,
                                    height = 64.dp,
                                    emptyPlaceholderText = "V${i + 1}",
                                    onClick = {
                                        pickerTarget = "villain_$i"
                                        showCardPicker = true
                                    }
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "VILLAIN WIN", color = TextSecondary, fontSize = 10.sp)
                        Text(
                            text = "${String.format("%.1f", state.villainWinPercent)}%",
                            color = PokerRuby,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Community Board Card Selector
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, PokerBorder, RoundedCornerShape(14.dp)),
            color = PokerSurface,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMMUNITY BOARD (FLOP • TURN • RIVER)",
                    color = PokerCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 5) {
                        val card = state.boardCards.getOrNull(i)
                        CardView(
                            card = card,
                            width = 42.dp,
                            height = 60.dp,
                            emptyPlaceholderText = when (i) {
                                in 0..2 -> "Flop"
                                3 -> "Turn"
                                else -> "River"
                            },
                            onClick = {
                                pickerTarget = "board_$i"
                                showCardPicker = true
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Run Simulation Button
        Button(
            onClick = { viewModel.runEquitySimulation() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_simulation_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PokerGold),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = "Run Simulation",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Run 1,500 Trials Simulation",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCardPicker) {
        val target = pickerTarget ?: ""
        val title = when {
            target.startsWith("hero") -> "Select Hero Card"
            target.startsWith("villain") -> "Select Villain Card"
            else -> "Select Board Card"
        }
        CardPickerSheet(
            title = title,
            usedCards = allUsedCards,
            onCardSelected = { card ->
                when {
                    target.startsWith("hero_") -> {
                        val index = target.removePrefix("hero_").toIntOrNull() ?: 0
                        viewModel.updateEquityHeroCard(index, card)
                    }
                    target.startsWith("villain_") -> {
                        val index = target.removePrefix("villain_").toIntOrNull() ?: 0
                        viewModel.updateEquityVillainCard(index, card)
                    }
                    target.startsWith("board_") -> {
                        val index = target.removePrefix("board_").toIntOrNull() ?: 0
                        viewModel.updateEquityBoardCard(index, card)
                    }
                }
            },
            onDismiss = {
                showCardPicker = false
                pickerTarget = null
            }
        )
    }
}
