package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.R
import com.example.api.GeminiVisionService
import com.example.data.HandHistoryEntity
import com.example.data.PokerDatabase
import com.example.model.ActionType
import com.example.model.Card
import com.example.model.GtoRecommendation
import com.example.model.Position
import com.example.model.Rank
import com.example.model.Street
import com.example.model.Suit
import com.example.model.TableState
import com.example.model.VisionTableDetection
import com.example.solver.GtoEngine
import com.example.solver.GtoPreflopMatrix
import com.example.solver.PokerEvaluator
import com.example.solver.RangeCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EquityCalcState(
    val heroCards: List<Card> = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
    val villainCards: List<Card> = listOf(Card(Rank.QUEEN, Suit.HEARTS), Card(Rank.QUEEN, Suit.DIAMONDS)),
    val boardCards: List<Card> = listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.SEVEN, Suit.CLUBS), Card(Rank.TWO, Suit.DIAMONDS)),
    val heroWinPercent: Float = 84.5f,
    val villainWinPercent: Float = 15.5f,
    val tiePercent: Float = 0.0f,
    val isCalculating: Boolean = false
)

class PokerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        PokerDatabase::class.java,
        "poker_gto_db"
    ).fallbackToDestructiveMigration().build()

    private val handDao = db.handDao()

    val savedHands: StateFlow<List<HandHistoryEntity>> = handDao.getAllHands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Table State for Solver
    private val _tableState = MutableStateFlow(TableState())
    val tableState: StateFlow<TableState> = _tableState.asStateFlow()

    // Active GTO Recommendation
    private val _recommendation = MutableStateFlow<GtoRecommendation>(GtoEngine.solve(TableState()))
    val recommendation: StateFlow<GtoRecommendation> = _recommendation.asStateFlow()

    // Vision Analysis State
    private val _isVisionAnalyzing = MutableStateFlow(false)
    val isVisionAnalyzing: StateFlow<Boolean> = _isVisionAnalyzing.asStateFlow()

    private val _visionDetection = MutableStateFlow<VisionTableDetection?>(null)
    val visionDetection: StateFlow<VisionTableDetection?> = _visionDetection.asStateFlow()

    private val _visionError = MutableStateFlow<String?>(null)
    val visionError: StateFlow<String?> = _visionError.asStateFlow()

    // Floating HUD Overlay Mode State
    private val _isFloatingHudActive = MutableStateFlow(false)
    val isFloatingHudActive: StateFlow<Boolean> = _isFloatingHudActive.asStateFlow()

    // Range Matrix State
    private val _rangePosition = MutableStateFlow(Position.BTN)
    val rangePosition: StateFlow<Position> = _rangePosition.asStateFlow()

    private val _rangeFacingRaise = MutableStateFlow(false)
    val rangeFacingRaise: StateFlow<Boolean> = _rangeFacingRaise.asStateFlow()

    private val _selectedRangeCell = MutableStateFlow<RangeCell?>(null)
    val selectedRangeCell: StateFlow<RangeCell?> = _selectedRangeCell.asStateFlow()

    // Equity Calculator State
    private val _equityCalcState = MutableStateFlow(EquityCalcState())
    val equityCalcState: StateFlow<EquityCalcState> = _equityCalcState.asStateFlow()

    init {
        recalculateGto()
    }

    fun recalculateGto() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentTable = _tableState.value
            val solved = GtoEngine.solve(currentTable)
            _recommendation.value = solved
        }
    }

    fun updateHeroCard(index: Int, card: Card?) {
        val currentCards = _tableState.value.heroCards.toMutableList()
        if (card != null) {
            if (index < currentCards.size) {
                currentCards[index] = card
            } else {
                currentCards.add(card)
            }
        } else {
            if (index < currentCards.size) {
                currentCards.removeAt(index)
            }
        }
        _tableState.update { it.copy(heroCards = currentCards.take(2)) }
        recalculateGto()
    }

    fun updateBoardCard(index: Int, card: Card?) {
        val currentBoard = _tableState.value.boardCards.toMutableList()
        if (card != null) {
            if (index < currentBoard.size) {
                currentBoard[index] = card
            } else {
                currentBoard.add(card)
            }
        } else {
            if (index < currentBoard.size) {
                currentBoard.removeAt(index)
            }
        }
        val newStreet = when (currentBoard.size) {
            0 -> Street.PREFLOP
            in 1..3 -> Street.FLOP
            4 -> Street.TURN
            5 -> Street.RIVER
            else -> Street.PREFLOP
        }
        _tableState.update { it.copy(boardCards = currentBoard.take(5), street = newStreet) }
        recalculateGto()
    }

    fun setHeroPosition(position: Position) {
        _tableState.update { it.copy(heroPosition = position) }
        recalculateGto()
    }

    fun setPotAndBet(pot: Float, betToCall: Float) {
        _tableState.update {
            it.copy(
                potSize = pot.coerceAtLeast(1f),
                currentBetToCall = betToCall.coerceAtLeast(0f),
                isFacingBet = betToCall > 0f
            )
        }
        recalculateGto()
    }

    fun setStreet(street: Street) {
        _tableState.update { it.copy(street = street) }
        recalculateGto()
    }

    fun toggleFloatingHud() {
        _isFloatingHudActive.update { !it }
    }

    fun analyzeTableBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _isVisionAnalyzing.value = true
            _visionError.value = null

            val result = GeminiVisionService.analyzeTableScreen(bitmap)
            result.onSuccess { (detection, recommendation) ->
                _visionDetection.value = detection
                _recommendation.value = recommendation

                // Synchronize table state with detected values
                _tableState.update {
                    it.copy(
                        heroCards = if (detection.heroCards.isNotEmpty()) detection.heroCards else it.heroCards,
                        boardCards = detection.boardCards,
                        heroPosition = detection.heroPosition,
                        potSize = if (detection.potSize > 0) detection.potSize else it.potSize,
                        currentBetToCall = detection.currentBetToCall,
                        heroStack = if (detection.heroStack > 0) detection.heroStack else it.heroStack,
                        street = detection.street,
                        isFacingBet = detection.currentBetToCall > 0
                    )
                }

                // Automatically save to hand history
                saveHandToDatabase(
                    heroCards = detection.heroCards,
                    boardCards = detection.boardCards,
                    street = detection.street,
                    position = detection.heroPosition,
                    pot = detection.potSize,
                    bet = detection.currentBetToCall,
                    rec = recommendation
                )
            }.onFailure { error ->
                _visionError.value = error.message ?: "Failed to analyze screen table."
                // Fallback to offline solver with current state
                recalculateGto()
            }
            _isVisionAnalyzing.value = false
        }
    }

    fun analyzeSampleTable() {
        try {
            val bitmap = BitmapFactory.decodeResource(
                getApplication<Application>().resources,
                R.drawable.sample_poker_table
            )
            if (bitmap != null) {
                analyzeTableBitmap(bitmap)
            } else {
                // Mock direct detection if bitmap resource issue
                applyPresetScenario(1)
            }
        } catch (e: Exception) {
            applyPresetScenario(1)
        }
    }

    fun applyPresetScenario(presetIndex: Int) {
        when (presetIndex) {
            1 -> {
                // Flop C-Bet with Top Pair
                _tableState.value = TableState(
                    heroCards = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS)),
                    boardCards = listOf(Card(Rank.ACE, Suit.DIAMONDS), Card(Rank.TEN, Suit.CLUBS), Card(Rank.FOUR, Suit.SPADES)),
                    heroPosition = Position.BTN,
                    potSize = 120f,
                    currentBetToCall = 0f,
                    street = Street.FLOP,
                    isFacingBet = false
                )
            }
            2 -> {
                // River Bluff Catch with Pocket Queens
                _tableState.value = TableState(
                    heroCards = listOf(Card(Rank.QUEEN, Suit.SPADES), Card(Rank.QUEEN, Suit.HEARTS)),
                    boardCards = listOf(Card(Rank.JACK, Suit.DIAMONDS), Card(Rank.EIGHT, Suit.CLUBS), Card(Rank.FOUR, Suit.SPADES), Card(Rank.TWO, Suit.HEARTS), Card(Rank.THREE, Suit.CLUBS)),
                    heroPosition = Position.BB,
                    potSize = 340f,
                    currentBetToCall = 180f,
                    street = Street.RIVER,
                    isFacingBet = true
                )
            }
            3 -> {
                // Preflop 3-Bet with AKs vs UTG
                _tableState.value = TableState(
                    heroCards = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
                    boardCards = emptyList(),
                    heroPosition = Position.BTN,
                    opponentPosition = Position.UTG,
                    potSize = 35f,
                    currentBetToCall = 25f,
                    street = Street.PREFLOP,
                    isFacingBet = true
                )
            }
            4 -> {
                // Turn Semi-Bluff Flush + Gutshot Draw
                _tableState.value = TableState(
                    heroCards = listOf(Card(Rank.JACK, Suit.HEARTS), Card(Rank.TEN, Suit.HEARTS)),
                    boardCards = listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.NINE, Suit.HEARTS), Card(Rank.FOUR, Suit.CLUBS), Card(Rank.TWO, Suit.DIAMONDS)),
                    heroPosition = Position.CO,
                    potSize = 210f,
                    currentBetToCall = 70f,
                    street = Street.TURN,
                    isFacingBet = true
                )
            }
        }
        recalculateGto()
    }

    private fun saveHandToDatabase(
        heroCards: List<Card>,
        boardCards: List<Card>,
        street: Street,
        position: Position,
        pot: Float,
        bet: Float,
        rec: GtoRecommendation
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = HandHistoryEntity(
                heroCardsStr = heroCards.joinToString(" ") { it.shortNotation },
                boardCardsStr = boardCards.joinToString(" ") { it.shortNotation },
                street = street.displayName,
                position = position.displayName,
                potSize = pot,
                currentBet = bet,
                recommendedAction = rec.primaryAction.label,
                recommendedSizing = rec.primarySizing,
                heroEquity = rec.heroEquity,
                strategicConcept = rec.strategicConcept,
                explanation = rec.explanation
            )
            handDao.insertHand(entity)
        }
    }

    fun saveCurrentHandManually() {
        val table = _tableState.value
        val rec = _recommendation.value
        saveHandToDatabase(
            heroCards = table.heroCards,
            boardCards = table.boardCards,
            street = table.street,
            position = table.heroPosition,
            pot = table.potSize,
            bet = table.currentBetToCall,
            rec = rec
        )
    }

    fun deleteHandHistory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            handDao.deleteHand(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            handDao.clearAll()
        }
    }

    // Range matrix helpers
    fun setRangePosition(position: Position) {
        _rangePosition.value = position
    }

    fun setRangeFacingRaise(facingRaise: Boolean) {
        _rangeFacingRaise.value = facingRaise
    }

    fun setSelectedRangeCell(cell: RangeCell?) {
        _selectedRangeCell.value = cell
    }

    // Equity Calculator Methods
    fun updateEquityHeroCard(index: Int, card: Card?) {
        val list = _equityCalcState.value.heroCards.toMutableList()
        if (card != null) {
            if (index < list.size) list[index] = card else list.add(card)
        } else {
            if (index < list.size) list.removeAt(index)
        }
        _equityCalcState.update { it.copy(heroCards = list.take(2)) }
        runEquitySimulation()
    }

    fun updateEquityVillainCard(index: Int, card: Card?) {
        val list = _equityCalcState.value.villainCards.toMutableList()
        if (card != null) {
            if (index < list.size) list[index] = card else list.add(card)
        } else {
            if (index < list.size) list.removeAt(index)
        }
        _equityCalcState.update { it.copy(villainCards = list.take(2)) }
        runEquitySimulation()
    }

    fun updateEquityBoardCard(index: Int, card: Card?) {
        val list = _equityCalcState.value.boardCards.toMutableList()
        if (card != null) {
            if (index < list.size) list[index] = card else list.add(card)
        } else {
            if (index < list.size) list.removeAt(index)
        }
        _equityCalcState.update { it.copy(boardCards = list.take(5)) }
        runEquitySimulation()
    }

    fun runEquitySimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = _equityCalcState.value
            if (state.heroCards.size == 2 && state.villainCards.size == 2) {
                _equityCalcState.update { it.copy(isCalculating = true) }
                val (heroEq, villainEq) = PokerEvaluator.simulateEquity(state.heroCards, state.boardCards, trials = 1500)
                _equityCalcState.update {
                    it.copy(
                        heroWinPercent = heroEq,
                        villainWinPercent = villainEq,
                        isCalculating = false
                    )
                }
            }
        }
    }
}
