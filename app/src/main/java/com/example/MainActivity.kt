package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.EquityCalculatorScreen
import com.example.ui.screens.HandHistoryScreen
import com.example.ui.screens.RangeChartScreen
import com.example.ui.screens.ScreenAdvisorScreen
import com.example.ui.screens.TableSolverScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PokerBorder
import com.example.ui.theme.PokerFeltDark
import com.example.ui.theme.PokerGold
import com.example.ui.theme.PokerSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PokerViewModel

enum class AppDestination(val title: String, val icon: ImageVector, val tag: String) {
    SCREEN_ADVISOR("Vision", Icons.Default.Visibility, "nav_vision"),
    TABLE_SOLVER("Solver", Icons.Default.Casino, "nav_solver"),
    RANGES("Ranges", Icons.Default.GridOn, "nav_ranges"),
    EQUITY("Equity", Icons.Default.Calculate, "nav_equity"),
    HISTORY("History", Icons.Default.History, "nav_history")
}

class MainActivity : ComponentActivity() {
    private val pokerViewModel: PokerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainPokerApp(viewModel = pokerViewModel)
            }
        }
    }
}

@Composable
fun MainPokerApp(viewModel: PokerViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.SCREEN_ADVISOR) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PokerFeltDark,
        bottomBar = {
            NavigationBar(
                containerColor = PokerSurface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PokerGold,
                            indicatorColor = PokerGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(destination.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.SCREEN_ADVISOR -> ScreenAdvisorScreen(viewModel = viewModel)
                AppDestination.TABLE_SOLVER -> TableSolverScreen(viewModel = viewModel)
                AppDestination.RANGES -> RangeChartScreen(viewModel = viewModel)
                AppDestination.EQUITY -> EquityCalculatorScreen(viewModel = viewModel)
                AppDestination.HISTORY -> HandHistoryScreen(viewModel = viewModel)
            }
        }
    }
}
