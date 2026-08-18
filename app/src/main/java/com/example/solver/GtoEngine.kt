package com.example.solver

import com.example.model.ActionFrequency
import com.example.model.ActionType
import com.example.model.GtoRecommendation
import com.example.model.HandCategory
import com.example.model.Street
import com.example.model.TableState
import kotlin.math.roundToInt

object GtoEngine {

    fun solve(table: TableState): GtoRecommendation {
        val (heroEquity, villainEquity) = PokerEvaluator.simulateEquity(table.heroCards, table.boardCards, trials = 1000)
        val potOdds = table.potOddsRatio
        val spr = table.spr

        val currentScore = if (table.heroCards.size >= 2 && table.boardCards.isNotEmpty()) {
            PokerEvaluator.evaluate7Cards(table.heroCards + table.boardCards)
        } else null

        val handStrengthName = currentScore?.description ?: if (table.heroCards.size == 2) {
            val c1 = table.heroCards[0]
            val c2 = table.heroCards[1]
            if (c1.rank == c2.rank) "Pocket ${c1.rank.symbol}'s"
            else "${c1.rank.symbol}${c2.rank.symbol} ${if (c1.suit == c2.suit) "Suited" else "Offsuit"}"
        } else "Unknown Hand"

        return when (table.street) {
            Street.PREFLOP -> solvePreflop(table, heroEquity, villainEquity, potOdds, spr, handStrengthName)
            Street.FLOP -> solveFlop(table, heroEquity, villainEquity, potOdds, spr, currentScore, handStrengthName)
            Street.TURN -> solveTurn(table, heroEquity, villainEquity, potOdds, spr, currentScore, handStrengthName)
            Street.RIVER -> solveRiver(table, heroEquity, villainEquity, potOdds, spr, currentScore, handStrengthName)
        }
    }

    private fun solvePreflop(
        table: TableState,
        heroEquity: Float,
        villainEquity: Float,
        potOdds: Float,
        spr: Float,
        handStrength: String
    ): GtoRecommendation {
        if (table.heroCards.size < 2) {
            return defaultFoldRecommendation(heroEquity, villainEquity, potOdds, spr, handStrength)
        }

        val comboName = GtoPreflopMatrix.getComboName(table.heroCards[0], table.heroCards[1])
        val range = GtoPreflopMatrix.getRangeForScenario(table.heroPosition, table.stackDepthBb, table.isFacingBet)
        val flatCells = range.flatten()
        val cell = flatCells.find { it.comboName == comboName }

        val raiseFreq = cell?.raiseFrequency ?: 0f
        val callFreq = cell?.callFrequency ?: 0f
        val foldFreq = cell?.foldFrequency ?: 100f

        val (primaryAction, sizing) = when {
            raiseFreq >= 70f -> Pair(ActionType.RAISE, if (table.isFacingBet) "3-Bet to ${(table.currentBetToCall * 3).roundToInt()} BB" else "Open ${(table.bigBlindSize * 2.5).roundToInt()} BB")
            callFreq >= 60f -> Pair(ActionType.CALL, "Call ${table.currentBetToCall.roundToInt()}")
            raiseFreq > foldFreq && raiseFreq > callFreq -> Pair(ActionType.RAISE, "Mix Raise ${(table.bigBlindSize * 2.5).roundToInt()} BB")
            callFreq > foldFreq -> Pair(ActionType.CALL, "Mix Call")
            else -> Pair(ActionType.FOLD, "Fold 0")
        }

        val frequencies = listOf(
            ActionFrequency(ActionType.RAISE, raiseFreq, suggestedSizing = "2.5x BB", ev = cell?.evBb ?: 0.5f),
            ActionFrequency(ActionType.CALL, callFreq, suggestedSizing = "Match bet", ev = (cell?.evBb ?: 0f) * 0.7f),
            ActionFrequency(ActionType.FOLD, foldFreq, suggestedSizing = "-", ev = 0.0f)
        ).sortedByDescending { it.percentage }

        val concept = when {
            raiseFreq >= 80f -> "Pure GTO Value Open / 3-Bet"
            raiseFreq in 30f..70f -> "Mixed Frequency Exploitative/Balancing"
            callFreq >= 60f -> "Standard GTO Range Defense"
            else -> "Out of Range Fold"
        }

        val explanation = when (primaryAction) {
            ActionType.RAISE -> "With $comboName at ${table.heroPosition.displayName}, GTO solver executes an aggressive open/3-bet with ${raiseFreq.roundToInt()}% frequency to capture dead money and push range advantage."
            ActionType.CALL -> "$comboName has sufficient preflop implied odds and equity (${heroEquity.roundToInt()}%) vs villain's range to comfortably defend against the bet."
            else -> "$comboName is slightly below the profitability threshold from ${table.heroPosition.displayName}. Folding preserves stack equity and avoids negative EV reverse implied odds."
        }

        return GtoRecommendation(
            primaryAction = primaryAction,
            primarySizing = sizing,
            confidence = 0.94f,
            frequencies = frequencies,
            heroEquity = heroEquity,
            villainEquity = villainEquity,
            potOddsPercent = potOdds,
            spr = spr,
            evExpectedValue = cell?.evBb ?: 0.0f,
            handStrengthName = handStrength,
            strategicConcept = concept,
            explanation = explanation,
            isPureAction = raiseFreq >= 95f || foldFreq >= 95f || callFreq >= 95f
        )
    }

    private fun solveFlop(
        table: TableState,
        heroEquity: Float,
        villainEquity: Float,
        potOdds: Float,
        spr: Float,
        score: HandScore?,
        handStrength: String
    ): GtoRecommendation {
        val isMonster = score != null && score.category >= HandCategory.THREE_OF_A_KIND
        val isStrongPair = score != null && (score.category == HandCategory.TWO_PAIR || (score.category == HandCategory.ONE_PAIR && score.primaryValue >= 11))
        val isDraw = heroEquity in 38f..55f

        val (action, sizing, raisePct, callPct, foldPct, ev) = when {
            isMonster -> {
                // High equity value bet or trap
                Tuple6(ActionType.BET, "66% Pot (${(table.potSize * 0.66f).roundToInt()})", 85f, 15f, 0f, 18.5f)
            }
            isStrongPair && heroEquity >= 65f -> {
                Tuple6(ActionType.BET, "33% Pot (${(table.potSize * 0.33f).roundToInt()})", 75f, 25f, 0f, 12.0f)
            }
            isDraw && heroEquity >= potOdds -> {
                if (table.isFacingBet) {
                    Tuple6(ActionType.CALL, "Call ${(table.currentBetToCall).roundToInt()}", 25f, 70f, 5f, 6.2f)
                } else {
                    Tuple6(ActionType.BET, "Semi-bluff 45% Pot", 65f, 35f, 0f, 7.8f)
                }
            }
            table.isFacingBet && heroEquity < potOdds -> {
                Tuple6(ActionType.FOLD, "Fold", 5f, 15f, 80f, 0.0f)
            }
            !table.isFacingBet && heroEquity < 35f -> {
                // Range check or polarized small bluff
                Tuple6(ActionType.CHECK, "Check", 30f, 70f, 0f, 1.5f)
            }
            else -> {
                Tuple6(ActionType.CHECK, "Check", 40f, 60f, 0f, 3.0f)
            }
        }

        val frequencies = listOf(
            ActionFrequency(if (table.isFacingBet) ActionType.RAISE else ActionType.BET, raisePct, sizing, ev),
            ActionFrequency(if (table.isFacingBet) ActionType.CALL else ActionType.CHECK, callPct, if (table.isFacingBet) "Call" else "Check", ev * 0.6f),
            ActionFrequency(ActionType.FOLD, foldPct, "-", 0.0f)
        ).filter { it.percentage > 0f }.sortedByDescending { it.percentage }

        val concept = when {
            isMonster -> "Polarized Range Value Betting"
            isStrongPair -> "Geometric C-Bet for Value & Protection"
            isDraw -> "Semi-Bluff with High Backdoor Equity"
            action == ActionType.CHECK -> "Pot Control & Range Protection"
            else -> "MDF (Minimum Defense Frequency) Fold"
        }

        val explanation = "Hero holds $handStrength with ${heroEquity.roundToInt()}% equity. Pot odds require ${potOdds.roundToInt()}%. SPR is ${String.format("%.1f", spr)}. Recommended move: $action ($sizing) to exploit board texture and balance bluff-to-value ratios."

        return GtoRecommendation(
            primaryAction = action,
            primarySizing = sizing,
            confidence = 0.91f,
            frequencies = frequencies,
            heroEquity = heroEquity,
            villainEquity = villainEquity,
            potOddsPercent = potOdds,
            spr = spr,
            evExpectedValue = ev,
            handStrengthName = handStrength,
            strategicConcept = concept,
            explanation = explanation,
            isPureAction = raisePct >= 90f || foldPct >= 90f
        )
    }

    private fun solveTurn(
        table: TableState,
        heroEquity: Float,
        villainEquity: Float,
        potOdds: Float,
        spr: Float,
        score: HandScore?,
        handStrength: String
    ): GtoRecommendation {
        val isVeryStrong = score != null && score.category >= HandCategory.TWO_PAIR
        val (action, sizing, raisePct, callPct, foldPct, ev) = when {
            isVeryStrong -> Tuple6(ActionType.BET, "75% Pot (${(table.potSize * 0.75f).roundToInt()})", 80f, 20f, 0f, 24.0f)
            heroEquity >= 60f -> Tuple6(ActionType.BET, "50% Pot (${(table.potSize * 0.50f).roundToInt()})", 65f, 35f, 0f, 14.5f)
            table.isFacingBet && heroEquity >= potOdds -> Tuple6(ActionType.CALL, "Call ${(table.currentBetToCall).roundToInt()}", 10f, 80f, 10f, 5.0f)
            table.isFacingBet -> Tuple6(ActionType.FOLD, "Fold", 0f, 15f, 85f, 0.0f)
            else -> Tuple6(ActionType.CHECK, "Check", 25f, 75f, 0f, 2.0f)
        }

        val frequencies = listOf(
            ActionFrequency(if (table.isFacingBet) ActionType.RAISE else ActionType.BET, raisePct, sizing, ev),
            ActionFrequency(if (table.isFacingBet) ActionType.CALL else ActionType.CHECK, callPct, if (table.isFacingBet) "Call" else "Check", ev * 0.5f),
            ActionFrequency(ActionType.FOLD, foldPct, "-", 0.0f)
        ).filter { it.percentage > 0f }.sortedByDescending { it.percentage }

        return GtoRecommendation(
            primaryAction = action,
            primarySizing = sizing,
            confidence = 0.92f,
            frequencies = frequencies,
            heroEquity = heroEquity,
            villainEquity = villainEquity,
            potOddsPercent = potOdds,
            spr = spr,
            evExpectedValue = ev,
            handStrengthName = handStrength,
            strategicConcept = if (isVeryStrong) "Double Barrel for Maximum Value" else "Turn Pot Management",
            explanation = "On the turn, SPR narrows to ${String.format("%.1f", spr)}. Hero's ${heroEquity.roundToInt()}% equity dictates $action sizing at $sizing.",
            isPureAction = raisePct >= 90f || foldPct >= 90f
        )
    }

    private fun solveRiver(
        table: TableState,
        heroEquity: Float,
        villainEquity: Float,
        potOdds: Float,
        spr: Float,
        score: HandScore?,
        handStrength: String
    ): GtoRecommendation {
        val isNutted = score != null && score.category >= HandCategory.FLUSH
        val isTopPairPlus = score != null && score.category >= HandCategory.TWO_PAIR

        val (action, sizing, raisePct, callPct, foldPct, ev) = when {
            isNutted -> Tuple6(ActionType.BET, "All-In or Overbet 120%", 90f, 10f, 0f, 45.0f)
            isTopPairPlus -> Tuple6(ActionType.BET, "Block Bet 33% Pot", 60f, 40f, 0f, 18.0f)
            table.isFacingBet && heroEquity >= potOdds + 15f -> Tuple6(ActionType.CALL, "Bluff Catch Call", 5f, 85f, 10f, 8.0f)
            table.isFacingBet -> Tuple6(ActionType.FOLD, "Fold", 0f, 10f, 90f, 0.0f)
            else -> Tuple6(ActionType.CHECK, "Showdown Check", 20f, 80f, 0f, 3.0f)
        }

        val frequencies = listOf(
            ActionFrequency(if (table.isFacingBet) ActionType.RAISE else ActionType.BET, raisePct, sizing, ev),
            ActionFrequency(if (table.isFacingBet) ActionType.CALL else ActionType.CHECK, callPct, if (table.isFacingBet) "Call" else "Check", ev * 0.4f),
            ActionFrequency(ActionType.FOLD, foldPct, "-", 0.0f)
        ).filter { it.percentage > 0f }.sortedByDescending { it.percentage }

        return GtoRecommendation(
            primaryAction = action,
            primarySizing = sizing,
            confidence = 0.95f,
            frequencies = frequencies,
            heroEquity = heroEquity,
            villainEquity = villainEquity,
            potOddsPercent = potOdds,
            spr = spr,
            evExpectedValue = ev,
            handStrengthName = handStrength,
            strategicConcept = if (isNutted) "Nut Advantage Overbet Polarized Shove" else "Showdown Value / Bluff-Catch",
            explanation = "River action is pure polarized equilibrium. With $handStrength, $action achieves +EV vs villain's polarized calling range.",
            isPureAction = raisePct >= 90f || foldPct >= 90f
        )
    }

    private fun defaultFoldRecommendation(
        heroEquity: Float,
        villainEquity: Float,
        potOdds: Float,
        spr: Float,
        handStrength: String
    ): GtoRecommendation {
        return GtoRecommendation(
            primaryAction = ActionType.FOLD,
            primarySizing = "Fold",
            confidence = 0.99f,
            frequencies = listOf(
                ActionFrequency(ActionType.FOLD, 100f, "-", 0.0f),
                ActionFrequency(ActionType.CALL, 0f, "-", 0.0f),
                ActionFrequency(ActionType.RAISE, 0f, "-", 0.0f)
            ),
            heroEquity = heroEquity,
            villainEquity = villainEquity,
            potOddsPercent = potOdds,
            spr = spr,
            evExpectedValue = 0.0f,
            handStrengthName = handStrength,
            strategicConcept = "Default Out-of-Position Fold",
            explanation = "Hero hand does not meet opening or defense standards.",
            isPureAction = true
        )
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
