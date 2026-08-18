package com.example.solver

import com.example.model.Card
import com.example.model.HandCategory
import com.example.model.Rank
import com.example.model.Suit
import kotlin.random.Random

data class HandScore(
    val category: HandCategory,
    val primaryValue: Int,
    val tieBreakers: List<Int>,
    val description: String
) : Comparable<HandScore> {
    override fun compareTo(other: HandScore): Int {
        if (this.category.scoreMultiplier != other.category.scoreMultiplier) {
            return this.category.scoreMultiplier.compareTo(other.category.scoreMultiplier)
        }
        if (this.primaryValue != other.primaryValue) {
            return this.primaryValue.compareTo(other.primaryValue)
        }
        for (i in 0 until minOf(this.tieBreakers.size, other.tieBreakers.size)) {
            if (this.tieBreakers[i] != other.tieBreakers[i]) {
                return this.tieBreakers[i].compareTo(other.tieBreakers[i])
            }
        }
        return 0
    }
}

object PokerEvaluator {

    fun evaluate7Cards(cards: List<Card>): HandScore {
        if (cards.size < 5) {
            return HandScore(HandCategory.HIGH_CARD, cards.maxOfOrNull { it.rank.value } ?: 0, emptyList(), "High Card")
        }
        val combinations = get5CardCombinations(cards)
        var bestScore: HandScore? = null
        for (combo in combinations) {
            val score = evaluate5Cards(combo)
            if (bestScore == null || score > bestScore) {
                bestScore = score
            }
        }
        return bestScore ?: evaluate5Cards(cards.take(5))
    }

    private fun get5CardCombinations(cards: List<Card>): List<List<Card>> {
        val result = mutableListOf<List<Card>>()
        val n = cards.size
        for (i in 0 until n - 4) {
            for (j in i + 1 until n - 3) {
                for (k in j + 1 until n - 2) {
                    for (l in k + 1 until n - 1) {
                        for (m in l + 1 until n) {
                            result.add(listOf(cards[i], cards[j], cards[k], cards[l], cards[m]))
                        }
                    }
                }
            }
        }
        return result
    }

    fun evaluate5Cards(cards: List<Card>): HandScore {
        val sorted = cards.sortedByDescending { it.rank.value }
        val ranks = sorted.map { it.rank.value }
        val suits = sorted.map { it.suit }

        val isFlush = suits.distinct().size == 1
        val isStraight = checkStraight(ranks)

        if (isFlush && isStraight) {
            if (ranks.first() == 14 && ranks[1] == 13) {
                return HandScore(HandCategory.ROYAL_FLUSH, 14, emptyList(), "Royal Flush")
            }
            val straightHigh = getStraightHigh(ranks)
            return HandScore(HandCategory.STRAIGHT_FLUSH, straightHigh, emptyList(), "Straight Flush, ${getRankName(straightHigh)} High")
        }

        val rankCounts = ranks.groupingBy { it }.eachCount()
        val countPairs = rankCounts.entries.sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenByDescending { it.key })

        if (countPairs[0].value == 4) {
            val quadRank = countPairs[0].key
            val kicker = countPairs[1].key
            return HandScore(HandCategory.FOUR_OF_A_KIND, quadRank, listOf(kicker), "Four of a Kind, ${getRankName(quadRank)}s")
        }

        if (countPairs[0].value == 3 && countPairs[1].value == 2) {
            val tripRank = countPairs[0].key
            val pairRank = countPairs[1].key
            return HandScore(HandCategory.FULL_HOUSE, tripRank, listOf(pairRank), "Full House, ${getRankName(tripRank)}s full of ${getRankName(pairRank)}s")
        }

        if (isFlush) {
            return HandScore(HandCategory.FLUSH, ranks.first(), ranks.drop(1), "Flush, ${getRankName(ranks.first())} High")
        }

        if (isStraight) {
            val straightHigh = getStraightHigh(ranks)
            return HandScore(HandCategory.STRAIGHT, straightHigh, emptyList(), "Straight, ${getRankName(straightHigh)} High")
        }

        if (countPairs[0].value == 3) {
            val tripRank = countPairs[0].key
            val kickers = countPairs.drop(1).map { it.key }
            return HandScore(HandCategory.THREE_OF_A_KIND, tripRank, kickers, "Three of a Kind, ${getRankName(tripRank)}s")
        }

        if (countPairs[0].value == 2 && countPairs[1].value == 2) {
            val highPair = maxOf(countPairs[0].key, countPairs[1].key)
            val lowPair = minOf(countPairs[0].key, countPairs[1].key)
            val kicker = countPairs[2].key
            return HandScore(HandCategory.TWO_PAIR, highPair, listOf(lowPair, kicker), "Two Pair, ${getRankName(highPair)}s and ${getRankName(lowPair)}s")
        }

        if (countPairs[0].value == 2) {
            val pairRank = countPairs[0].key
            val kickers = countPairs.drop(1).map { it.key }
            return HandScore(HandCategory.ONE_PAIR, pairRank, kickers, "Pair of ${getRankName(pairRank)}s")
        }

        return HandScore(HandCategory.HIGH_CARD, ranks.first(), ranks.drop(1), "High Card ${getRankName(ranks.first())}")
    }

    private fun checkStraight(ranks: List<Int>): Boolean {
        val distinct = ranks.distinct()
        if (distinct.size < 5) return false
        // Normal 5 in a row
        if (distinct[0] - distinct[4] == 4) return true
        // Ace-low straight A-2-3-4-5
        if (distinct.contains(14) && distinct.contains(2) && distinct.contains(3) && distinct.contains(4) && distinct.contains(5)) {
            return true
        }
        return false
    }

    private fun getStraightHigh(ranks: List<Int>): Int {
        val distinct = ranks.distinct()
        if (distinct.contains(14) && distinct.contains(2) && distinct.contains(3) && distinct.contains(4) && distinct.contains(5) && distinct[0] != 5) {
            return 5 // Wheel
        }
        return distinct.first()
    }

    fun getRankName(value: Int): String = when (value) {
        14 -> "Ace"
        13 -> "King"
        12 -> "Queen"
        11 -> "Jack"
        10 -> "Ten"
        else -> value.toString()
    }

    fun simulateEquity(
        heroCards: List<Card>,
        boardCards: List<Card>,
        trials: Int = 1200
    ): Pair<Float, Float> {
        if (heroCards.size < 2) return Pair(50.0f, 50.0f)
        val usedCards = (heroCards + boardCards).toSet()
        val availableDeck = Card.FULL_DECK.filter { it !in usedCards }.toMutableList()

        var heroWins = 0
        var villainWins = 0
        var ties = 0

        val neededBoardCards = 5 - boardCards.size

        for (i in 0 until trials) {
            val deck = ArrayList(availableDeck)
            deck.shuffle()

            // Draw villain 2 cards
            val villain1 = deck.removeAt(deck.lastIndex)
            val villain2 = deck.removeAt(deck.lastIndex)
            val villainHand = listOf(villain1, villain2)

            // Draw remaining community cards
            val simulatedCommunity = boardCards.toMutableList()
            for (b in 0 until neededBoardCards) {
                simulatedCommunity.add(deck.removeAt(deck.lastIndex))
            }

            val heroScore = evaluate7Cards(heroCards + simulatedCommunity)
            val villainScore = evaluate7Cards(villainHand + simulatedCommunity)

            val cmp = heroScore.compareTo(villainScore)
            when {
                cmp > 0 -> heroWins++
                cmp < 0 -> villainWins++
                else -> ties++
            }
        }

        val totalTrials = trials.toFloat()
        val heroEquity = ((heroWins + (ties * 0.5f)) / totalTrials) * 100f
        val villainEquity = 100f - heroEquity
        return Pair(heroEquity, villainEquity)
    }
}
