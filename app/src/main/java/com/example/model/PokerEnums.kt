package com.example.model

enum class Suit(val symbol: String, val charCode: Char, val isRed: Boolean) {
    SPADES("♠", 's', false),
    HEARTS("♥", 'h', true),
    DIAMONDS("♦", 'd', true),
    CLUBS("♣", 'c', false);

    companion object {
        fun fromChar(char: Char): Suit = when (char.lowercaseChar()) {
            's' -> SPADES
            'h' -> HEARTS
            'd' -> DIAMONDS
            'c' -> CLUBS
            else -> SPADES
        }
    }
}

enum class Rank(val symbol: String, val value: Int) {
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("T", 10),
    JACK("J", 11),
    QUEEN("Q", 12),
    KING("K", 13),
    ACE("A", 14);

    companion object {
        fun fromSymbol(s: String): Rank = when (s.uppercase()) {
            "2" -> TWO
            "3" -> THREE
            "4" -> FOUR
            "5" -> FIVE
            "6" -> SIX
            "7" -> SEVEN
            "8" -> EIGHT
            "9" -> NINE
            "T", "10" -> TEN
            "J" -> JACK
            "Q" -> QUEEN
            "K" -> KING
            "A", "1" -> ACE
            else -> ACE
        }
    }
}

enum class Position(val displayName: String, val isEarly: Boolean, val isLate: Boolean, val isBlinds: Boolean) {
    UTG("UTG", true, false, false),
    UTG_PLUS_1("UTG+1", true, false, false),
    MP("MP (LJ)", false, false, false),
    HJ("HJ", false, false, false),
    CO("CO", false, true, false),
    BTN("BTN", false, true, false),
    SB("SB", false, false, true),
    BB("BB", false, false, true);

    companion object {
        fun fromString(str: String): Position {
            val upper = str.uppercase().trim()
            return when {
                upper.contains("UTG+1") -> UTG_PLUS_1
                upper.contains("UTG") -> UTG
                upper.contains("HJ") || upper.contains("HIJACK") -> HJ
                upper.contains("CO") || upper.contains("CUTOFF") -> CO
                upper.contains("BTN") || upper.contains("BUTTON") || upper.contains("BU") -> BTN
                upper.contains("SB") || upper.contains("SMALL") -> SB
                upper.contains("BB") || upper.contains("BIG") -> BB
                else -> MP
            }
        }
    }
}

enum class Street(val displayName: String) {
    PREFLOP("Preflop"),
    FLOP("Flop"),
    TURN("Turn"),
    RIVER("River")
}

enum class ActionType(val label: String, val isAggressive: Boolean) {
    FOLD("Fold", false),
    CHECK("Check", false),
    CALL("Call", false),
    BET("Bet", true),
    RAISE("Raise", true),
    ALL_IN("All-in", true)
}

enum class HandCategory(val displayName: String, val scoreMultiplier: Int) {
    HIGH_CARD("High Card", 1),
    ONE_PAIR("One Pair", 2),
    TWO_PAIR("Two Pair", 3),
    THREE_OF_A_KIND("Three of a Kind", 4),
    STRAIGHT("Straight", 5),
    FLUSH("Flush", 6),
    FULL_HOUSE("Full House", 7),
    FOUR_OF_A_KIND("Four of a Kind", 8),
    STRAIGHT_FLUSH("Straight Flush", 9),
    ROYAL_FLUSH("Royal Flush", 10)
}
