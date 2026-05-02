package com.alvaroquintana.adivinaperro.ui.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinaperro.managers.BreedClassifier
import com.alvaroquintana.domain.BreedPrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecognitionViewModel(
    private val breedClassifier: BreedClassifier
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecognitionUiState>(RecognitionUiState.Idle)
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

    fun classify(imageBytes: ByteArray) {
        _uiState.value = RecognitionUiState.Loading
        viewModelScope.launch {
            try {
                val predictions = breedClassifier.classify(imageBytes)
                val bestConfidence = predictions.maxOfOrNull { it.confidence } ?: 0f
                _uiState.value = if (bestConfidence < CONFIDENCE_THRESHOLD) {
                    RecognitionUiState.NotRecognized
                } else {
                    RecognitionUiState.Results(predictions)
                }
            } catch (e: Exception) {
                _uiState.value = RecognitionUiState.Error(e.message ?: "Classification failed")
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = RecognitionUiState.Idle
    }

    fun isAvailable(): Boolean = breedClassifier.isAvailable()

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.4f
    }
}

sealed interface RecognitionUiState {
    data object Idle : RecognitionUiState
    data object Loading : RecognitionUiState
    data class Results(val predictions: List<BreedPrediction>) : RecognitionUiState
    data object NotRecognized : RecognitionUiState
    data class Error(val message: String) : RecognitionUiState
}
