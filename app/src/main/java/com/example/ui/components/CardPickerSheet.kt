package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.Rank
import com.example.model.Suit
import com.example.ui.theme.CardClubGreen
import com.example.ui.theme.CardDiamondBlue
import com.example.ui.theme.CardHeartRed
import com.example.ui.theme.CardSpadeBlack
import com.example.ui.theme.PokerBorder
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardPickerSheet(
    title: String = "Select Card",
    usedCards: List<Card> = emptyList(),
    onCardSelected: (Card?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRank by remember { mutableStateOf<Rank?>(null) }
    var selectedSuit by remember { mutableStateOf<Suit?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PokerSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(
                        onClick = {
                            onCardSelected(null)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("clear_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Card",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_card_picker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "1. CHOOSE RANK",
                color = PokerGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Ranks grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ranks = listOf(
                    Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK, Rank.TEN,
                    Rank.NINE, Rank.EIGHT, Rank.SEVEN, Rank.SIX, Rank.FIVE,
                    Rank.FOUR, Rank.THREE, Rank.TWO
                )

                ranks.forEach { rank ->
                    val isSelected = selectedRank == rank
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PokerGold else PokerFeltDark)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PokerGold else PokerBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedRank = rank
                                if (selectedSuit != null) {
                                    val candidate = Card(rank, selectedSuit!!)
                                    if (candidate !in usedCards) {
                                        onCardSelected(candidate)
                                        onDismiss()
                                    }
                                }
                            }
                            .testTag("rank_${rank.symbol}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.symbol,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. CHOOSE SUIT",
                color = PokerGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Suits row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val suits = listOf(
                    Suit.SPADES to CardSpadeBlack,
                    Suit.HEARTS to CardHeartRed,
                    Suit.DIAMONDS to CardDiamondBlue,
                    Suit.CLUBS to CardClubGreen
                )

                suits.forEach { (suit, color) ->
                    val isSelected = selectedSuit == suit
                    val isAvailable = selectedRank == null || Card(selectedRank!!, suit) !in usedCards

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = isAvailable) {
                                selectedSuit = suit
                                if (selectedRank != null) {
                                    onCardSelected(Card(selectedRank!!, suit))
                                    onDismiss()
                                }
                            }
                            .testTag("suit_${suit.name}"),
                        color = if (isSelected) color.copy(alpha = 0.2f) else PokerFeltDark,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else PokerBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = suit.symbol,
                                color = if (isAvailable) color else TextSecondary.copy(alpha = 0.3f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
