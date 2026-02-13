package com.simplegolfgps.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplegolfgps.settings.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    dashboardState: DashboardState,
    shotAnalysisState: ShotAnalysisState,
    filterState: FilterState,
    selectedTab: AnalyticsTab,
    settings: SettingsState,
    courseNames: List<String>,
    onSelectTab: (AnalyticsTab) -> Unit,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
    onClearFilters: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Tab selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                AnalyticsTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, AnalyticsTab.entries.size),
                        selected = selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                    ) {
                        Text(tab.label)
                    }
                }
            }

            // Tab content
            when (selectedTab) {
                AnalyticsTab.Dashboard -> DashboardTab(
                    state = dashboardState,
                    useImperial = settings.useImperial,
                )
                AnalyticsTab.ShotAnalysis -> ShotAnalysisTab(
                    state = shotAnalysisState,
                    filterState = filterState,
                    settings = settings,
                    courseNames = courseNames,
                    onUpdateFilter = onUpdateFilter,
                    onClearFilters = onClearFilters,
                )
            }
        }
    }
}
