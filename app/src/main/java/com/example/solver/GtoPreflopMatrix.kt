package com.example.solver

import com.example.model.ActionType
import com.example.model.Position
import com.example.model.Rank

enum class MatrixAction {
    RAISE,
    CALL,
    FOLD,
    MIXED_RAISE_CALL,
    MIXED_RAISE_FOLD
}

data class RangeCell(
    val comboName: String, // e.g. "AKs", "AA", "76s", "T9o"
    val rank1: Rank,
    val rank2: Rank,
    val isSuited: Boolean,
    val isPair: Boolean,
    val defaultAction: MatrixAction = MatrixAction.FOLD,
    val raiseFrequency: Float = 0f,
    val callFrequency: Float = 0f,
    val foldFrequency: Float = 100f,
    val evBb: Float = 0.0f
)

object GtoPreflopMatrix {

    private val RANKS = listOf(
        Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK, Rank.TEN,
        Rank.NINE, Rank.EIGHT, Rank.SEVEN, Rank.SIX, Rank.FIVE,
        Rank.FOUR, Rank.THREE, Rank.TWO
    )

    fun generateEmptyGrid(): List<List<RangeCell>> {
        val grid = mutableListOf<List<RangeCell>>()
        for (row in 0 until 13) {
            val rowList = mutableListOf<RangeCell>()
            for (col in 0 until 13) {
                val r1 = RANKS[row]
                val r2 = RANKS[col]
                val isPair = row == col
                val isSuited = col > row // Above diagonal
                val isOffsuit = row > col // Below diagonal

                val name = when {
                    isPair -> "${r1.symbol}${r2.symbol}"
                    isSuited -> "${r1.symbol}${r2.symbol}s"
                    else -> "${r2.symbol}${r1.symbol}o"
                }

                rowList.add(
                    RangeCell(
                        comboName = name,
                        rank1 = if (r1.value >= r2.value) r1 else r2,
                        rank2 = if (r1.value >= r2.value) r2 else r1,
                        isSuited = isSuited,
                        isPair = isPair
                    )
                )
            }
            grid.add(rowList)
        }
        return grid
    }

    fun getRangeForScenario(position: Position, stackDepthBb: Int = 100, isFacingRaise: Boolean = false): List<List<RangeCell>> {
        val baseGrid = generateEmptyGrid()
        return baseGrid.map { row ->
            row.map { cell ->
                evaluateComboStrategy(cell, position, stackDepthBb, isFacingRaise)
            }
        }
    }

    private fun evaluateComboStrategy(
        cell: RangeCell,
        position: Position,
        stackDepthBb: Int,
        isFacingRaise: Boolean
    ): RangeCell {
        val r1 = cell.rank1.value
        val r2 = cell.rank2.value

        if (!isFacingRaise) {
            // Open-Raise (RFI) GTO Nash Ranges
            when (position) {
                Position.UTG, Position.UTG_PLUS_1 -> {
                    // ~15% range
                    if (cell.isPair && r1 >= 7) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 2.4f)
                    if (cell.isPair && r1 >= 5) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 60f, foldFrequency = 40f, evBb = 0.5f)
                    if (cell.isSuited) {
                        if (r1 == 14 && r2 >= 10) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 2.1f)
                        if (r1 == 14 && r2 in 2..5) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 50f, foldFrequency = 50f, evBb = 0.3f)
                        if (r1 == 13 && r2 >= 11) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.6f)
                        if (r1 == 12 && r2 == 11) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.2f)
                        if (r1 in 9..11 && r2 == r1 - 1) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 45f, foldFrequency = 55f, evBb = 0.2f)
                    } else if (!cell.isPair) {
                        if (r1 == 14 && r2 >= 12) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.8f)
                        if (r1 == 14 && r2 == 11) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 50f, foldFrequency = 50f, evBb = 0.4f)
                    }
                }
                Position.MP, Position.HJ -> {
                    // ~21% range
                    if (cell.isPair && r1 >= 4) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.9f)
                    if (cell.isPair && r1 in 2..3) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 70f, foldFrequency = 30f, evBb = 0.4f)
                    if (cell.isSuited) {
                        if (r1 == 14 && r2 >= 8) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.8f)
                        if (r1 == 14 && r2 in 2..5) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 80f, foldFrequency = 20f, evBb = 0.6f)
                        if (r1 == 13 && r2 >= 9) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.4f)
                        if (r1 == 12 && r2 >= 9) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 90f, foldFrequency = 10f, evBb = 1.1f)
                        if (r1 in 7..11 && r2 == r1 - 1) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 0.9f)
                    } else if (!cell.isPair) {
                        if (r1 == 14 && r2 >= 11) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.7f)
                        if (r1 == 14 && r2 == 10) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 70f, foldFrequency = 30f, evBb = 0.5f)
                        if (r1 == 13 && r2 >= 11) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 80f, foldFrequency = 20f, evBb = 0.8f)
                    }
                }
                Position.CO -> {
                    // ~28% range
                    if (cell.isPair) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 2.2f)
                    if (cell.isSuited) {
                        if (r1 == 14 || (r1 == 13 && r2 >= 5) || (r1 == 12 && r2 >= 7) || (r1 == 11 && r2 >= 7)) {
                            return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.5f)
                        }
                        if (r1 - r2 in 1..2 && r2 >= 4) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 0.8f)
                    } else if (!cell.isPair) {
                        if (r1 == 14 && r2 >= 9) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.4f)
                        if (r1 == 13 && r2 >= 10) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.1f)
                        if (r1 == 12 && r2 >= 10) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 90f, foldFrequency = 10f, evBb = 0.7f)
                    }
                }
                Position.BTN -> {
                    // ~48% range
                    if (cell.isPair) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 2.5f)
                    if (cell.isSuited) {
                        if (r1 >= 10 || r2 >= 6 || (r1 - r2 <= 3)) {
                            return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.6f)
                        }
                        return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 70f, foldFrequency = 30f, evBb = 0.4f)
                    } else if (!cell.isPair) {
                        if (r1 == 14 || (r1 == 13 && r2 >= 6) || (r1 == 12 && r2 >= 8) || (r1 == 11 && r2 >= 8) || (r1 == 10 && r2 >= 8)) {
                            return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.2f)
                        }
                    }
                }
                Position.SB -> {
                    // ~45% range (Raise or Limp/Fold)
                    if (cell.isPair && r1 >= 6) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 2.0f)
                    if (cell.isPair) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_CALL, raiseFrequency = 50f, callFrequency = 50f, evBb = 0.8f)
                    if (cell.isSuited && (r1 >= 10 || r1 == 14)) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.5f)
                    if (cell.isSuited) return cell.copy(defaultAction = MatrixAction.CALL, callFrequency = 100f, evBb = 0.6f)
                    if (r1 == 14 && r2 >= 8) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 1.3f)
                }
                Position.BB -> {
                    // Walk / Check option
                    return cell.copy(defaultAction = MatrixAction.CALL, callFrequency = 100f, foldFrequency = 0f)
                }
            }
        } else {
            // Facing Raise (3-bet or Defend)
            if (cell.isPair && r1 >= 11) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 3.5f) // QQ+, KK, AA 3-bet pure
            if (cell.isPair && r1 in 7..10) return cell.copy(defaultAction = MatrixAction.CALL, callFrequency = 90f, raiseFrequency = 10f, evBb = 1.4f)
            if (cell.isPair && r1 in 2..6) return cell.copy(defaultAction = MatrixAction.CALL, callFrequency = 70f, foldFrequency = 30f, evBb = 0.5f)
            if (cell.isSuited) {
                if (r1 == 14 && r2 >= 13) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 3.2f) // AKs
                if (r1 == 14 && r2 in 11..12) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_CALL, raiseFrequency = 60f, callFrequency = 40f, evBb = 1.8f)
                if (r1 == 14 && r2 in 2..5) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_FOLD, raiseFrequency = 65f, foldFrequency = 35f, evBb = 0.6f) // A2s-A5s 3-bet bluff blockers
                if (r1 in 9..13 && r2 == r1 - 1) return cell.copy(defaultAction = MatrixAction.CALL, callFrequency = 85f, foldFrequency = 15f, evBb = 0.9f)
            } else if (!cell.isPair) {
                if (r1 == 14 && r2 == 13) return cell.copy(defaultAction = MatrixAction.RAISE, raiseFrequency = 100f, foldFrequency = 0f, evBb = 2.8f) // AKo
                if (r1 == 14 && r2 == 12) return cell.copy(defaultAction = MatrixAction.MIXED_RAISE_CALL, raiseFrequency = 30f, callFrequency = 70f, evBb = 1.1f)
            }
        }

        return cell.copy(defaultAction = MatrixAction.FOLD, foldFrequency = 100f, raiseFrequency = 0f, callFrequency = 0f, evBb = 0.0f)
    }

    fun getComboName(c1: com.example.model.Card, c2: com.example.model.Card): String {
        val r1 = if (c1.rank.value >= c2.rank.value) c1.rank else c2.rank
        val r2 = if (c1.rank.value >= c2.rank.value) c2.rank else c1.rank
        val isPair = r1 == r2
        val isSuited = c1.suit == c2.suit
        return when {
            isPair -> "${r1.symbol}${r2.symbol}"
            isSuited -> "${r1.symbol}${r2.symbol}s"
            else -> "${r1.symbol}${r2.symbol}o"
        }
    }
}
