package com.simplegolfgps.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simplegolfgps.data.*
import kotlinx.coroutines.flow.*

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val shotDao = db.shotDao()
    private val roundDao = db.roundDao()

    private val _selectedTab = MutableStateFlow(AnalyticsTab.Dashboard)
    val selectedTab: StateFlow<AnalyticsTab> = _selectedTab.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val allShots = shotDao.getShotsForAnalytics()
    private val allRounds = roundDao.getAll()

    val courseNames: StateFlow<List<String>> = allRounds
        .map { rounds -> rounds.map { it.courseName }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardState: StateFlow<DashboardState> = combine(allShots, allRounds) { shots, rounds ->
        AnalyticsComputer.computeDashboard(shots, rounds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    val shotAnalysisState: StateFlow<ShotAnalysisState> = combine(
        allShots,
        allRounds,
        _filterState,
    ) { shots, rounds, filters ->
        val filtered = AnalyticsComputer.filterShots(shots, rounds, filters)
        AnalyticsComputer.computeShotAnalysis(shots, filtered, filters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShotAnalysisState())

    fun selectTab(tab: AnalyticsTab) {
        _selectedTab.value = tab
    }

    fun updateFilter(update: FilterState.() -> FilterState) {
        _filterState.update { it.update() }
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }
}
