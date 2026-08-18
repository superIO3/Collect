package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ActionType
import com.example.ui.components.ActionFrequencyBar
import com.example.ui.components.CardView
import com.example.ui.components.EquityMeter
import com.example.ui.theme.PokerBorder
import com.example.ui.theme.PokerCyan
import com.example.ui.theme.PokerEmerald
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerPurple
import com.example.ui.theme.PokerRuby
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PokerViewModel
import kotlin.math.roundToInt

@Composable
fun ScreenAdvisorScreen(
    viewModel: PokerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tableState by viewModel.tableState.collectAsState()
    val recommendation by viewModel.recommendation.collectAsState()
    val isAnalyzing by viewModel.isVisionAnalyzing.collectAsState()
    val visionDetection by viewModel.visionDetection.collectAsState()
    val visionError by viewModel.visionError.collectAsState()
    val isFloatingHudActive by viewModel.isFloatingHudActive.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                capturedBitmap = bitmap
                viewModel.analyzeTableBitmap(bitmap)
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            viewModel.analyzeTableBitmap(bitmap)
        }
    }

    val actionColor = when (recommendation.primaryAction) {
        ActionType.RAISE, ActionType.BET -> PokerGold
        ActionType.CALL -> PokerEmerald
        ActionType.CHECK -> PokerCyan
        ActionType.FOLD -> PokerRuby
        ActionType.ALL_IN -> Color(0xFFC084FC)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PokerFeltDark)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header: Live Vision Title & Floating HUD toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAnalyzing) PokerGold else PokerEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SCREEN VISION ADVISOR",
                            color = PokerGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Real-Time GTO Strategy",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // HUD Overlay Toggle Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.toggleFloatingHud() }
                        .testTag("toggle_hud_button"),
                    color = if (isFloatingHudActive) PokerEmerald.copy(alpha = 0.2f) else PokerSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFloatingHudActive) PokerEmerald else PokerBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "HUD Overlay",
                            tint = if (isFloatingHudActive) PokerEmerald else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFloatingHudActive) "HUD ON" else "HUD SIM",
                            color = if (isFloatingHudActive) PokerEmerald else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Screen Capture / Photo Input Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("upload_screenshot_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PokerSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PokerBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload Table Screenshot",
                        tint = PokerGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Screenshot",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("camera_capture_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PokerSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PokerBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Table Photo",
                        tint = PokerCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Camera",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.analyzeSampleTable() },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(46.dp)
                        .testTag("sample_table_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PokerGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Analyze Sample Table",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sample Table",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Test Benches (Fast Scenario Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Flop C-Bet" to 1,
                    "Bluff Catch" to 2,
                    "3-Bet Pot" to 3,
                    "Turn Draw" to 4
                ).forEach { (label, index) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF14201A))
                            .border(1.dp, PokerBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.applyPresetScenario(index) }
                            .padding(vertical = 6.dp)
                            .testTag("preset_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Analyzing State or Error Alert
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PokerSurface)
                        .border(1.dp, PokerGold, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = PokerGold,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gemini Vision analyzing table cards & pot sizes...",
                            color = PokerGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            } else if (visionError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2D1616))
                        .border(1.dp, PokerRuby, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Vision Notice: $visionError (Switched to built-in instant GTO Solver).",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // PRIMARY HERO DECISION BANNER (The Core "What to Do" Answer)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, actionColor, RoundedCornerShape(16.dp))
                    .testTag("gto_decision_card"),
                color = PokerSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "GTO Decision",
                                tint = actionColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GTO OPTIMAL ACTION",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.saveCurrentHandManually() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("save_hand_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save Hand to History",
                                tint = PokerGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Big Action Callout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = recommendation.primaryAction.label.uppercase(),
                                color = actionColor,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (recommendation.primarySizing.isNotBlank()) {
                                Text(
                                    text = recommendation.primarySizing,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Confidence Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(actionColor.copy(alpha = 0.2f))
                                .border(1.dp, actionColor, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(recommendation.confidence * 100).roundToInt()}% GTO Confidence",
                                color = actionColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Frequency distribution bar
                    Text(
                        text = "SOLVER FREQUENCY MIX",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ActionFrequencyBar(frequencies = recommendation.frequencies)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strategic Rationale Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF101B15))
                            .border(1.dp, Color(0xFF23362C), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = recommendation.strategicConcept,
                                    color = PokerCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = recommendation.explanation,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detected Table Situation Overview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, PokerBorder, RoundedCornerShape(14.dp)),
                color = PokerSurface,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DETECTED TABLE STATE",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cards row: Hero + Board
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HERO HAND",
                                color = PokerGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tableState.heroCards.forEach { card ->
                                    CardView(card = card, width = 38.dp, height = 54.dp, isSelected = true)
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "COMMUNITY BOARD (${tableState.street.displayName.uppercase()})",
                                color = PokerCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (tableState.boardCards.isEmpty()) {
                                    Text(
                                        text = "No board (Preflop)",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
                                } else {
                                    tableState.boardCards.forEach { card ->
                                        CardView(card = card, width = 34.dp, height = 48.dp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics row: Pot, Current Bet, Position, SPR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "POT SIZE", color = TextSecondary, fontSize = 9.sp)
                            Text(text = "$${tableState.potSize.roundToInt()}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "TO CALL", color = TextSecondary, fontSize = 9.sp)
                            Text(
                                text = if (tableState.currentBetToCall > 0) "$${tableState.currentBetToCall.roundToInt()}" else "Checked",
                                color = if (tableState.currentBetToCall > 0) PokerRuby else PokerEmerald,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "HERO POS", color = TextSecondary, fontSize = 9.sp)
                            Text(text = tableState.heroPosition.displayName, color = PokerGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "SPR", color = TextSecondary, fontSize = 9.sp)
                            Text(text = String.format("%.1f", tableState.spr), color = PokerCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Equity & Pot Odds Breakdown
            EquityMeter(
                heroEquity = recommendation.heroEquity,
                villainEquity = recommendation.villainEquity,
                potOdds = recommendation.potOddsPercent,
                spr = recommendation.spr
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Floating HUD Simulated Preview
        AnimatedVisibility(
            visible = isFloatingHudActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            FloatingHudOverlayView(
                tableState = tableState,
                recommendation = recommendation,
                onClose = { viewModel.toggleFloatingHud() }
            )
        }
    }
}
