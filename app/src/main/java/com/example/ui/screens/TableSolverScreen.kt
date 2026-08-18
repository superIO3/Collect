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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.ActionType
import com.example.model.Card
import com.example.model.Position
import com.example.model.Street
import com.example.ui.components.ActionFrequencyBar
import com.example.ui.components.CardPickerSheet
import com.example.ui.components.EquityMeter
import com.example.ui.components.PokerTableView
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
fun TableSolverScreen(
    viewModel: PokerViewModel,
    modifier: Modifier = Modifier
) {
    val tableState by viewModel.tableState.collectAsState()
    val recommendation by viewModel.recommendation.collectAsState()

    // Card Picker Bottom Sheet State
    var showCardPicker by remember { mutableStateOf(false) }
    var pickingHeroCardIndex by remember { mutableStateOf<Int?>(null) }
    var pickingBoardCardIndex by remember { mutableStateOf<Int?>(null) }

    var potInput by remember { mutableStateOf(tableState.potSize) }
    var betInput by remember { mutableStateOf(tableState.currentBetToCall) }

    val allUsedCards = tableState.heroCards + tableState.boardCards

    val actionColor = when (recommendation.primaryAction) {
        ActionType.RAISE, ActionType.BET -> PokerGold
        ActionType.CALL -> PokerEmerald
        ActionType.CHECK -> PokerCyan
        ActionType.FOLD -> PokerRuby
        ActionType.ALL_IN -> Color(0xFFC084FC)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PokerFeltDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MANUAL TABLE SOLVER",
                    color = PokerGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Interactive GTO Sandbox",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.applyPresetScenario(1) },
                modifier = Modifier.testTag("reset_table_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Table",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Street Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Street.entries.forEach { street ->
                val isSelected = tableState.street == street
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PokerGold else PokerSurface)
                        .border(
                            1.dp,
                            if (isSelected) PokerGold else PokerBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setStreet(street) }
                        .padding(vertical = 8.dp)
                        .testTag("street_tab_${street.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = street.displayName,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Table Graphic
        PokerTableView(
            heroCards = tableState.heroCards,
            boardCards = tableState.boardCards,
            heroPosition = tableState.heroPosition,
            potSize = tableState.potSize,
            currentBet = tableState.currentBetToCall,
            street = tableState.street,
            heroStack = tableState.heroStack,
            onHeroCardClick = { index ->
                pickingHeroCardIndex = index
                pickingBoardCardIndex = null
                showCardPicker = true
            },
            onBoardCardClick = { index ->
                pickingBoardCardIndex = index
                pickingHeroCardIndex = null
                showCardPicker = true
            },
            onPositionClick = {
                // Cycle position
                val nextPos = Position.entries[(tableState.heroPosition.ordinal + 1) % Position.entries.size]
                viewModel.setHeroPosition(nextPos)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Position Selectors Row
        Text(
            text = "HERO POSITION",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Position.entries.forEach { pos ->
                val isSelected = tableState.heroPosition == pos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) PokerGold else PokerSurface)
                        .border(
                            1.dp,
                            if (isSelected) PokerGold else PokerBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.setHeroPosition(pos) }
                        .padding(vertical = 6.dp)
                        .testTag("pos_btn_${pos.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pos.displayName.take(3),
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pot & Bet Sliders / Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, PokerBorder, RoundedCornerShape(12.dp)),
            color = PokerSurface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Pot Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pot Size", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "$${tableState.potSize.roundToInt()}", color = PokerGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = tableState.potSize,
                    onValueChange = { newPot ->
                        viewModel.setPotAndBet(newPot, tableState.currentBetToCall)
                    },
                    valueRange = 10f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = PokerGold,
                        activeTrackColor = PokerGold,
                        inactiveTrackColor = Color(0xFF26382E)
                    ),
                    modifier = Modifier.testTag("pot_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bet to Call Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Facing Bet to Call", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = if (tableState.currentBetToCall > 0) "$${tableState.currentBetToCall.roundToInt()}" else "$0 (Checked)",
                        color = if (tableState.currentBetToCall > 0) PokerRuby else PokerEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = tableState.currentBetToCall,
                    onValueChange = { newBet ->
                        viewModel.setPotAndBet(tableState.potSize, newBet)
                    },
                    valueRange = 0f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (tableState.currentBetToCall > 0) PokerRuby else PokerEmerald,
                        activeTrackColor = if (tableState.currentBetToCall > 0) PokerRuby else PokerEmerald,
                        inactiveTrackColor = Color(0xFF26382E)
                    ),
                    modifier = Modifier.testTag("bet_slider")
                )

                // Quick Bet Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Check ($0)" to 0f,
                        "1/3 Pot" to (tableState.potSize * 0.33f),
                        "1/2 Pot" to (tableState.potSize * 0.50f),
                        "2/3 Pot" to (tableState.potSize * 0.67f),
                        "Pot Bet" to tableState.potSize
                    ).forEach { (label, amount) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF16251E))
                                .border(1.dp, PokerBorder, RoundedCornerShape(6.dp))
                                .clickable { viewModel.setPotAndBet(tableState.potSize, amount) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GTO Solution Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, actionColor, RoundedCornerShape(14.dp)),
            color = PokerSurface,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOLVER RECOMMENDATION",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = { viewModel.saveCurrentHandManually() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Save Hand",
                            tint = PokerGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${recommendation.primaryAction.label.uppercase()} ${recommendation.primarySizing}",
                        color = actionColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${recommendation.handStrengthName}",
                        color = PokerCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ActionFrequencyBar(frequencies = recommendation.frequencies)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = recommendation.explanation,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Equity Meter
        EquityMeter(
            heroEquity = recommendation.heroEquity,
            villainEquity = recommendation.villainEquity,
            potOdds = recommendation.potOddsPercent,
            spr = recommendation.spr
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Card Picker Modal Sheet
    if (showCardPicker) {
        CardPickerSheet(
            title = if (pickingHeroCardIndex != null) "Select Hero Hole Card ${pickingHeroCardIndex!! + 1}" else "Select Community Board Card",
            usedCards = allUsedCards,
            onCardSelected = { card ->
                if (pickingHeroCardIndex != null) {
                    viewModel.updateHeroCard(pickingHeroCardIndex!!, card)
                } else if (pickingBoardCardIndex != null) {
                    viewModel.updateBoardCard(pickingBoardCardIndex!!, card)
                }
            },
            onDismiss = {
                showCardPicker = false
                pickingHeroCardIndex = null
                pickingBoardCardIndex = null
            }
        )
    }
}
