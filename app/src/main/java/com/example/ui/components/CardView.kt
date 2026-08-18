package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.Suit
import com.example.ui.theme.CardClubGreen
import com.example.ui.theme.CardDiamondBlue
import com.example.ui.theme.CardHeartRed
import com.example.ui.theme.CardSpadeBlack
import com.example.ui.theme.PokerGold

@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    width: Dp = 48.dp,
    height: Dp = 68.dp,
    isSelected: Boolean = false,
    useFourColorDeck: Boolean = true,
    emptyPlaceholderText: String = "+",
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(6.dp)

    val suitColor = if (card != null) {
        if (useFourColorDeck) {
            when (card.suit) {
                Suit.SPADES -> CardSpadeBlack
                Suit.HEARTS -> CardHeartRed
                Suit.DIAMONDS -> CardDiamondBlue
                Suit.CLUBS -> CardClubGreen
            }
        } else {
            if (card.suit.isRed) CardHeartRed else CardSpadeBlack
        }
    } else Color.Transparent

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(if (card != null) 4.dp else 0.dp, shape)
            .clip(shape)
            .background(
                if (card != null) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B2A23), Color(0xFF13201B))
                    )
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PokerGold else if (card != null) Color(0xFFCBD5E1) else Color(0xFF2E4338),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable { onClick() }
                        .testTag("card_${card?.shortNotation ?: "empty"}")
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.size(width, height / 2.5f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.rank.symbol,
                        color = suitColor,
                        fontWeight = FontWeight.Black,
                        fontSize = (width.value * 0.35f).sp,
                        lineHeight = (width.value * 0.35f).sp
                    )
                    Text(
                        text = card.suit.symbol,
                        color = suitColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (width.value * 0.30f).sp,
                        modifier = Modifier.padding(start = 1.dp)
                    )
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.suit.symbol,
                        color = suitColor.copy(alpha = 0.85f),
                        fontSize = (width.value * 0.5f).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        } else {
            Text(
                text = emptyPlaceholderText,
                color = Color(0xFF6B8779),
                fontWeight = FontWeight.SemiBold,
                fontSize = (width.value * 0.4f).sp
            )
        }
    }
}
