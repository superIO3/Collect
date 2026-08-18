package com.example.model

data class ActionFrequency(
    val action: ActionType,
    val percentage: Float, // 0.0 to 100.0
    val suggestedSizing: String = "",
    val ev: Float = 0.0f
)

data class GtoRecommendation(
    val primaryAction: ActionType,
    val primarySizing: String,
    val confidence: Float, // 0.0 to 1.0
    val frequencies: List<ActionFrequency>,
    val heroEquity: Float, // e.g. 58.4 (%)
    val villainEquity: Float, // e.g. 41.6 (%)
    val potOddsPercent: Float, // required equity to call, e.g. 25.0 (%)
    val spr: Float, // Stack to Pot Ratio
    val evExpectedValue: Float, // in BB or $
    val handStrengthName: String, // e.g., "Top Pair Top Kicker", "Nut Flush Draw"
    val strategicConcept: String, // e.g., "Polarized C-Bet", "Range Advantage", "Bluff Catch"
    val explanation: String,
    val alternativeLine: String = "",
    val blockers: List<String> = emptyList(),
    val isPureAction: Boolean = false // e.g. 100% fold or 100% raise
)

data class VisionTableDetection(
    val heroCards: List<Card>,
    val boardCards: List<Card>,
    val heroPosition: Position,
    val potSize: Float,
    val currentBetToCall: Float,
    val heroStack: Float,
    val effectiveStack: Float,
    val street: Street,
    val opponentAction: String = "",
    val detectionConfidence: Float = 0.95f,
    val rawTextExtracted: String = ""
)
