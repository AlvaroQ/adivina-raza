package com.alvaroquintana.adivinaperro.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinaperro.managers.Analytics
import com.alvaroquintana.domain.Dog
import com.alvaroquintana.usecases.GetRandomBreedsWithDescription
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val getRandomBreedsWithDescription: GetRandomBreedsWithDescription) : ViewModel() {
    private lateinit var dog: Dog

    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question.asStateFlow()

    private val _responseOptions = MutableSharedFlow<MutableList<String>>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val responseOptions: SharedFlow<MutableList<String>> = _responseOptions.asSharedFlow()

    private val _progress = MutableStateFlow<UiModel>(UiModel.Loading(false))
    val progress: StateFlow<UiModel> = _progress.asStateFlow()

    private val _navigation = MutableSharedFlow<Navigation>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val navigation: SharedFlow<Navigation> = _navigation.asSharedFlow()

    private val _showingAds = MutableSharedFlow<UiModel>(replay = 1, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val showingAds: SharedFlow<UiModel> = _showingAds.asSharedFlow()

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_GAME)
        generateNewStage()
        _showingAds.tryEmit(UiModel.ShowBannerAd(true))
    }

    fun generateNewStage() {
        viewModelScope.launch {
            _progress.value = UiModel.Loading(true)

            val dogs = getRandomBreedsWithDescription.invoke(4)
            if (dogs.size < 4) {
                _progress.value = UiModel.Loading(false)
                _navigation.tryEmit(Navigation.Result)
                return@launch
            }

            val correctIndex = (0 until 4).random()
            dog = dogs[correctIndex]

            val optionList = dogs.map { it.name }.toMutableList()
            // Shuffle options but keep correct answer tracking via dog.name
            optionList.shuffle()

            _responseOptions.tryEmit(optionList)
            _question.value = dog.icon
            _progress.value = UiModel.Loading(false)

        }
    }

    fun showRewardedAd() {
        _showingAds.tryEmit(UiModel.ShowReewardAd(true))
    }

    fun navigateToResult(points: String) {
        Analytics.analyticsGameFinished(points, Analytics.MODE_CLASSIC)
        _navigation.tryEmit(Navigation.Result)
    }

    fun getNameBreedCorrect() : String {
        return dog.name
    }

    // No numeric-id based random selection; we rely on repository random sampling.

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowBannerAd(val show: Boolean) : UiModel()
        data class ShowReewardAd(val show: Boolean) : UiModel()
    }

    sealed class Navigation {
        object Result : Navigation()
    }
}