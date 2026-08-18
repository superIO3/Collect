package com.example

import com.example.model.ActionType
import com.example.model.Card
import com.example.model.HandCategory
import com.example.model.Position
import com.example.model.Rank
import com.example.model.Street
import com.example.model.Suit
import com.example.model.TableState
import com.example.solver.GtoEngine
import com.example.solver.PokerEvaluator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testRoyalFlushEvaluation() {
    val cards = listOf(
      Card(Rank.ACE, Suit.SPADES),
      Card(Rank.KING, Suit.SPADES),
      Card(Rank.QUEEN, Suit.SPADES),
      Card(Rank.JACK, Suit.SPADES),
      Card(Rank.TEN, Suit.SPADES),
      Card(Rank.TWO, Suit.HEARTS),
      Card(Rank.THREE, Suit.CLUBS)
    )
    val score = PokerEvaluator.evaluate7Cards(cards)
    assertEquals(HandCategory.ROYAL_FLUSH, score.category)
    assertEquals(14, score.primaryValue)
  }

  @Test
  fun testFullHouseEvaluation() {
    val cards = listOf(
      Card(Rank.ACE, Suit.SPADES),
      Card(Rank.ACE, Suit.HEARTS),
      Card(Rank.ACE, Suit.DIAMONDS),
      Card(Rank.KING, Suit.SPADES),
      Card(Rank.KING, Suit.CLUBS),
      Card(Rank.FOUR, Suit.HEARTS),
      Card(Rank.TWO, Suit.DIAMONDS)
    )
    val score = PokerEvaluator.evaluate7Cards(cards)
    assertEquals(HandCategory.FULL_HOUSE, score.category)
    assertEquals(14, score.primaryValue)
    assertEquals(13, score.tieBreakers.firstOrNull())
  }

  @Test
  fun testEquitySimulation() {
    val heroCards = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.HEARTS))
    val board = listOf(Card(Rank.SEVEN, Suit.CLUBS), Card(Rank.TWO, Suit.DIAMONDS), Card(Rank.NINE, Suit.SPADES))
    val (heroEquity, villainEquity) = PokerEvaluator.simulateEquity(heroCards, board, trials = 300)
    assertTrue("Pocket Aces should have >70% equity on dry board", heroEquity > 70f)
  }

  @Test
  fun testGtoEngineRecommendation() {
    val table = TableState(
      heroCards = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
      boardCards = emptyList(),
      heroPosition = Position.BTN,
      potSize = 35f,
      currentBetToCall = 0f,
      street = Street.PREFLOP
    )
    val rec = GtoEngine.solve(table)
    assertEquals(ActionType.RAISE, rec.primaryAction)
    assertTrue(rec.heroEquity > 55f)
  }
}

