package com.koinonia.igreja.presentation.features.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor() : ViewModel() {

    private val _attendanceRate = MutableStateFlow(84.5f) // 84.5% de presença geral
    val attendanceRate: StateFlow<Float> = _attendanceRate.asStateFlow()

    private val _totalMembers = MutableStateFlow(142)
    val totalMembers: StateFlow<Int> = _totalMembers.asStateFlow()

    private val _absentCount = MutableStateFlow(22)
    val absentCount: StateFlow<Int> = _absentCount.asStateFlow()

    private val _visitorCountThisMonth = MutableStateFlow(15)
    val visitorCountThisMonth: StateFlow<Int> = _visitorCountThisMonth.asStateFlow()
}
