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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Position
import com.example.solver.GtoPreflopMatrix
import com.example.solver.MatrixAction
import com.example.ui.components.MatrixGridView
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
fun RangeChartScreen(
    viewModel: PokerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPosition by viewModel.rangePosition.collectAsState()
    val isFacingRaise by viewModel.rangeFacingRaise.collectAsState()
    val selectedCell by viewModel.selectedRangeCell.collectAsState()

    val currentGrid = remember(selectedPosition, isFacingRaise) {
        GtoPreflopMatrix.getRangeForScenario(selectedPosition, stackDepthBb = 100, isFacingRaise = isFacingRaise)
    }

    val totalRaiseCombos = currentGrid.flatten().filter { it.raiseFrequency > 0f }.size
    val totalCallCombos = currentGrid.flatten().filter { it.callFrequency > 0f }.size
    val totalCombos = currentGrid.flatten().size
    val activePercent = ((totalRaiseCombos + totalCallCombos).toFloat() / totalCombos * 100f).roundToInt()

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
                    text = "GTO PREFLOP CHARTS",
                    color = PokerGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "100BB Nash Equilibrium Ranges",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PokerGold.copy(alpha = 0.15f))
                    .border(1.dp, PokerGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$activePercent% Range",
                    color = PokerGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scenario Selector (Open-Raise vs Facing 3-Bet)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isFacingRaise) PokerGold else PokerSurface)
                    .border(1.dp, if (!isFacingRaise) PokerGold else PokerBorder, RoundedCornerShape(8.dp))
                    .clickable { viewModel.setRangeFacingRaise(false) }
                    .padding(vertical = 8.dp)
                    .testTag("rfi_chart_tab"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open-Raise (RFI)",
                    color = if (!isFacingRaise) Color.Black else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFacingRaise) PokerGold else PokerSurface)
                    .border(1.dp, if (isFacingRaise) PokerGold else PokerBorder, RoundedCornerShape(8.dp))
                    .clickable { viewModel.setRangeFacingRaise(true) }
                    .padding(vertical = 8.dp)
                    .testTag("facing_raise_chart_tab"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Facing Raise (Defend/3-Bet)",
                    color = if (isFacingRaise) Color.Black else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Position Selection Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Position.entries.forEach { pos ->
                val isSelected = selectedPosition == pos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) PokerGold else PokerSurface)
                        .border(1.dp, if (isSelected) PokerGold else PokerBorder, RoundedCornerShape(6.dp))
                        .clickable { viewModel.setRangePosition(pos) }
                        .padding(vertical = 6.dp)
                        .testTag("chart_pos_${pos.name}"),
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

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(PokerGold))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Raise (Pure)", color = TextSecondary, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFD97706)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Mixed", color = TextSecondary, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(PokerEmerald))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Call/Defend", color = TextSecondary, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF1E2822)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Fold", color = TextSecondary, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 13x13 Range Matrix
        MatrixGridView(
            grid = currentGrid,
            onCellClick = { cell ->
                viewModel.setSelectedRangeCell(cell)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Strategy Tip Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, PokerBorder, RoundedCornerShape(12.dp)),
            color = PokerSurface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Strategy Info",
                        tint = PokerCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RANGE INSIGHT: ${selectedPosition.displayName}",
                        color = PokerCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (selectedPosition) {
                        Position.UTG, Position.UTG_PLUS_1 -> "Early position requires tight range discipline (~15%). Suited Aces (A5s-A2s) are included as 3-bet bluff blockers."
                        Position.MP, Position.HJ -> "Middle position expands to ~21%, adding suited broadways and mid pocket pairs."
                        Position.CO -> "Cutoff opens 28-30% of hands, aggressively isolating the button and blinds."
                        Position.BTN -> "Button enjoys absolute postflop position and attacks wide (48-50%), opening almost all suited connectors and pairs."
                        Position.SB -> "Small blind plays out of position and utilizes a polarized raise-or-fold strategy vs BB."
                        Position.BB -> "Big blind gets pot odds discount and defends ~45-55% of hands vs late position opens."
                    },
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Cell Inspector Dialog
    selectedCell?.let { cell ->
        AlertDialog(
            onDismissRequest = { viewModel.setSelectedRangeCell(null) },
            containerColor = PokerSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${cell.comboName} Strategy (${selectedPosition.displayName})",
                        color = PokerGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.setSelectedRangeCell(null) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "Type: ${if (cell.isPair) "Pocket Pair" else if (cell.isSuited) "Suited" else "Offsuit"}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Combinations: ${if (cell.isPair) "6 combos" else if (cell.isSuited) "4 combos" else "12 combos"}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "GTO Action: ${cell.defaultAction.name.replace("_", " ")}",
                        color = when (cell.defaultAction) {
                            MatrixAction.RAISE -> PokerGold
                            MatrixAction.CALL -> PokerEmerald
                            MatrixAction.MIXED_RAISE_CALL -> Color(0xFFD97706)
                            MatrixAction.MIXED_RAISE_FOLD -> PokerGold
                            MatrixAction.FOLD -> PokerRuby
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Raise Frequency: ${cell.raiseFrequency.roundToInt()}%", color = TextPrimary, fontSize = 13.sp)
                    Text(text = "Call Frequency: ${cell.callFrequency.roundToInt()}%", color = TextPrimary, fontSize = 13.sp)
                    Text(text = "Fold Frequency: ${cell.foldFrequency.roundToInt()}%", color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Estimated EV: +${String.format("%.2f", cell.evBb)} BB", color = PokerEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setSelectedRangeCell(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = PokerGold)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
