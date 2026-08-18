package com.example.model

data class Card(
    val rank: Rank,
    val suit: Suit
) : Comparable<Card> {
    val shortNotation: String
        get() = "${rank.symbol}${suit.charCode}"

    val displayString: String
        get() = "${rank.symbol}${suit.symbol}"

    override fun compareTo(other: Card): Int {
        return this.rank.value.compareTo(other.rank.value)
    }

    companion object {
        fun fromString(str: String): Card? {
            val trimmed = str.trim()
            if (trimmed.length < 2) return null
            val rankPart = if (trimmed.startsWith("10")) "10" else trimmed.take(1)
            val suitChar = trimmed.last()
            val rank = Rank.fromSymbol(rankPart)
            val suit = when (suitChar.lowercaseChar()) {
                's', '♠' -> Suit.SPADES
                'h', '♥' -> Suit.HEARTS
                'd', '♦' -> Suit.DIAMONDS
                'c', '♣' -> Suit.CLUBS
                else -> Suit.SPADES
            }
            return Card(rank, suit)
        }

        val FULL_DECK: List<Card> by lazy {
            val list = mutableListOf<Card>()
            for (suit in Suit.entries) {
                for (rank in Rank.entries) {
                    list.add(Card(rank, suit))
                }
            }
            list
        }
    }
}
