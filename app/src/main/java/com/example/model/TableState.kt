package com.example.model

data class TableState(
    val heroCards: List<Card> = listOf(
        Card(Rank.ACE, Suit.SPADES),
        Card(Rank.KING, Suit.HEARTS)
    ),
    val boardCards: List<Card> = listOf(
        Card(Rank.ACE, Suit.DIAMONDS),
        Card(Rank.TEN, Suit.CLUBS),
        Card(Rank.FOUR, Suit.SPADES)
    ),
    val heroPosition: Position = Position.BTN,
    val opponentPosition: Position = Position.BB,
    val potSize: Float = 120.0f,
    val currentBetToCall: Float = 40.0f,
    val heroStack: Float = 500.0f,
    val villainStack: Float = 500.0f,
    val bigBlindSize: Float = 2.0f,
    val street: Street = Street.FLOP,
    val isFacingBet: Boolean = true,
    val isHeroInitiator: Boolean = true,
    val stackDepthBb: Int = 100
) {
    val potOddsRatio: Float
        get() = if (currentBetToCall > 0f) currentBetToCall / (potSize + currentBetToCall) * 100f else 0f

    val spr: Float
        get() = if (potSize > 0f) minOf(heroStack, villainStack) / potSize else 0f

    val effectiveStackBb: Float
        get() = if (bigBlindSize > 0f) minOf(heroStack, villainStack) / bigBlindSize else 100f
}
