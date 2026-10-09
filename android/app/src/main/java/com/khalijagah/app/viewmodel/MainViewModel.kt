package com.khalijagah.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.khalijagah.app.data.model.*
import com.khalijagah.app.data.repository.KhaliJagahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "KhaliJagah"

data class AppUiState(
    val stations: List<Station> = DEFAULT_STATIONS,
    val stationsLoading: Boolean = false,
    val stationsError: String? = null,

    val sourceStation: Station? = null,
    val destStation: Station? = null,

    val trains: List<TrainResult> = emptyList(),
    val trainsLoading: Boolean = false,
    val trainsError: String? = null,

    val selectedTrain: TrainResult? = null,

    val crowdData: CrowdResponse? = null,
    val crowdLoading: Boolean = false,
    val crowdError: String? = null,

    val analysisResult: AnalyzeResponse? = null,
    val analysisLoading: Boolean = false,
    val analysisError: String? = null
)

class MainViewModel : ViewModel() {
    private val repository = KhaliJagahRepository()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        loadStations()
    }

    fun loadStations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(stationsLoading = true, stationsError = null)
            repository.getStations()
                .onSuccess { stations ->
                    Log.d(TAG, "Stations loaded successfully: count=${stations.size}")
                    _uiState.value = _uiState.value.copy(
                        stations = if (stations.isNotEmpty()) stations else DEFAULT_STATIONS,
                        stationsLoading = false
                    )
                }
                .onFailure { e ->
                    Log.w(TAG, "Failed to load stations from backend: ${e.message}. Preserving default stations.")
                    _uiState.value = _uiState.value.copy(
                        stations = if (_uiState.value.stations.isEmpty()) DEFAULT_STATIONS else _uiState.value.stations,
                        stationsLoading = false,
                        stationsError = e.message ?: "Failed to load stations"
                    )
                }
        }
    }

    fun selectSource(station: Station) {
        Log.d(TAG, "selectSource: ${station.shortName} (${station.id})")
        _uiState.value = _uiState.value.copy(
            sourceStation = station,
            trains = emptyList(),
            selectedTrain = null,
            crowdData = null,
            analysisResult = null
        )
    }

    fun selectDestination(station: Station) {
        Log.d(TAG, "selectDestination: ${station.shortName} (${station.id})")
        _uiState.value = _uiState.value.copy(
            destStation = station,
            trains = emptyList(),
            selectedTrain = null,
            crowdData = null,
            analysisResult = null
        )
    }

    fun swapStations() {
        val current = _uiState.value
        _uiState.value = current.copy(
            sourceStation = current.destStation,
            destStation = current.sourceStation,
            trains = emptyList(),
            selectedTrain = null,
            crowdData = null,
            analysisResult = null
        )
    }

    fun searchTrains() {
        val source = _uiState.value.sourceStation ?: return
        val dest = _uiState.value.destStation ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                trainsLoading = true,
                trainsError = null,
                trains = emptyList()
            )
            repository.getTrains(source.id, dest.id)
                .onSuccess { trains ->
                    _uiState.value = _uiState.value.copy(
                        trains = trains,
                        trainsLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        trainsLoading = false,
                        trainsError = e.message ?: "Failed to load trains"
                    )
                }
        }
    }

    fun selectTrain(train: TrainResult) {
        _uiState.value = _uiState.value.copy(
            selectedTrain = train,
            crowdData = null,
            crowdError = null,
            analysisResult = null,
            analysisError = null
        )
        loadCrowdData(train.id)
    }

    private fun loadCrowdData(trainId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(crowdLoading = true, crowdError = null)
            repository.getCrowdData(trainId)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        crowdData = data,
                        crowdLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        crowdLoading = false,
                        crowdError = e.message ?: "Failed to load crowd data"
                    )
                }
        }
    }

    fun analyzeCrowd() {
        val train = _uiState.value.selectedTrain ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                analysisLoading = true,
                analysisError = null
            )
            repository.analyzeCrowd(train.id)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        analysisResult = result,
                        analysisLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        analysisLoading = false,
                        analysisError = e.message ?: "Analysis failed"
                    )
                }
        }
    }

    fun clearAnalysis() {
        _uiState.value = _uiState.value.copy(
            analysisResult = null,
            analysisError = null
        )
    }

    fun resetJourney() {
        _uiState.value = AppUiState(
            stations = _uiState.value.stations
        )
    }
}
