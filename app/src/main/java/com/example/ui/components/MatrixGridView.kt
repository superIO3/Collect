package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solver.MatrixAction
import com.example.solver.RangeCell
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerRuby

@Composable
fun MatrixGridView(
    grid: List<List<RangeCell>>,
    onCellClick: (RangeCell) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F1714))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        grid.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                row.forEach { cell ->
                    val bgColor = when (cell.defaultAction) {
                        MatrixAction.RAISE -> PokerGold.copy(alpha = 0.85f)
                        MatrixAction.CALL -> PokerEmerald.copy(alpha = 0.85f)
                        MatrixAction.MIXED_RAISE_CALL -> Color(0xFFD97706)
                        MatrixAction.MIXED_RAISE_FOLD -> PokerGold.copy(alpha = 0.45f)
                        MatrixAction.FOLD -> Color(0xFF1E2822)
                    }

                    val textColor = when (cell.defaultAction) {
                        MatrixAction.RAISE, MatrixAction.CALL, MatrixAction.MIXED_RAISE_CALL -> Color.Black
                        MatrixAction.MIXED_RAISE_FOLD -> Color.White
                        MatrixAction.FOLD -> Color(0xFF64748B)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(bgColor)
                            .clickable { onCellClick(cell) }
                            .testTag("cell_${cell.comboName}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cell.comboName,
                            color = textColor,
                            fontSize = 8.5.sp,
                            fontWeight = if (cell.defaultAction != MatrixAction.FOLD) FontWeight.Black else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
