package com.alvaroquintana.adivinaperro

import adivinaraza.app.generated.resources.Res
import adivinaraza.app.generated.resources.app_name
import adivinaraza.app.generated.resources.choose_one
import adivinaraza.app.generated.resources.info_title
import adivinaraza.app.generated.resources.mode_bigger_smaller
import adivinaraza.app.generated.resources.mode_classic_title
import adivinaraza.app.generated.resources.recognition_title
import adivinaraza.app.generated.resources.mode_description
import adivinaraza.app.generated.resources.mode_fci_trivia
import adivinaraza.app.generated.resources.resultado_screen_title
import adivinaraza.app.generated.resources.settings
import adivinaraza.app.generated.resources.settings_version
import adivinaraza.app.generated.resources.share_message
import adivinaraza.app.generated.resources.share_message_general
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.alvaroquintana.adivinaperro.managers.AdMobConfig
import com.alvaroquintana.adivinaperro.managers.Analytics
import com.alvaroquintana.adivinaperro.managers.ConsentGate
import com.alvaroquintana.adivinaperro.managers.ErrorTracker
import com.alvaroquintana.adivinaperro.managers.IntentLauncher
import com.alvaroquintana.adivinaperro.managers.Settings
import com.alvaroquintana.adivinaperro.managers.SoundPlayer
import com.alvaroquintana.adivinaperro.ui.animation.NavTransitions
import com.alvaroquintana.adivinaperro.ui.composables.AdBannerView
import com.alvaroquintana.adivinaperro.ui.composables.GameAppBar
import com.alvaroquintana.adivinaperro.ui.composables.rememberRewardedAdState
import com.alvaroquintana.adivinaperro.ui.game.BiggerSmallerScreenContent
import com.alvaroquintana.adivinaperro.ui.game.BiggerSmallerViewModel
import com.alvaroquintana.adivinaperro.ui.game.DescriptionScreenContent
import com.alvaroquintana.adivinaperro.ui.game.DescriptionViewModel
import com.alvaroquintana.adivinaperro.ui.game.FciTriviaScreenContent
import com.alvaroquintana.adivinaperro.ui.game.FciTriviaViewModel
import com.alvaroquintana.adivinaperro.ui.game.GameScreen
import com.alvaroquintana.adivinaperro.ui.game.GameViewModel
import com.alvaroquintana.adivinaperro.managers.BreedClassifier
import com.alvaroquintana.adivinaperro.ui.info.InfoScreen
import com.alvaroquintana.adivinaperro.ui.info.InfoViewModel
import com.alvaroquintana.adivinaperro.ui.navigation.BiggerSmaller
import com.alvaroquintana.adivinaperro.ui.navigation.Description
import com.alvaroquintana.adivinaperro.ui.navigation.FciTrivia
import com.alvaroquintana.adivinaperro.ui.navigation.Game
import com.alvaroquintana.adivinaperro.ui.navigation.Info
import com.alvaroquintana.adivinaperro.ui.navigation.Recognition
import com.alvaroquintana.adivinaperro.ui.navigation.Result
import com.alvaroquintana.adivinaperro.ui.navigation.Select
import com.alvaroquintana.adivinaperro.ui.navigation.Settings as SettingsRoute
import com.alvaroquintana.adivinaperro.ui.navigation.Splash
import com.alvaroquintana.adivinaperro.ui.recognition.RecognitionScreen
import com.alvaroquintana.adivinaperro.ui.recognition.RecognitionViewModel
import com.alvaroquintana.adivinaperro.ui.result.ResultScreen
import com.alvaroquintana.adivinaperro.ui.result.ResultViewModel
import com.alvaroquintana.adivinaperro.ui.select.SelectScreen
import com.alvaroquintana.adivinaperro.ui.settings.SettingsScreen
import com.alvaroquintana.adivinaperro.ui.splash.SplashScreen
import com.alvaroquintana.adivinaperro.ui.theme.AdivinaPerroTheme
import com.alvaroquintana.adivinaperro.ui.theme.ThemeMode
import com.alvaroquintana.adivinaperro.ui.theme.rememberAppWindowSizeClass
import com.alvaroquintana.adivinaperro.utils.Constants.TOTAL_BREED
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val PREFS_THEME_MODE = "theme_mode"
private const val PREFS_SOUND = "sound"
private const val STAGE_DELAY_MS = 1_000L
private const val REWARDED_AD_INTERVAL = 6

private fun ThemeMode.persistedName(): String = name

private fun parseThemeMode(value: String?): ThemeMode = try {
    if (value == null) ThemeMode.SYSTEM else ThemeMode.valueOf(value)
} catch (_: Exception) {
    ThemeMode.SYSTEM
}

private fun Settings.getString(key: String, default: String): String {
    // Settings interface only exposes int/boolean; we encode theme as int ordinal.
    val ordinal = getInt(key.intKey(), -1)
    return ThemeMode.entries.getOrNull(ordinal)?.persistedName() ?: default
}

private fun Settings.putString(key: String, value: String) {
    val ordinal = ThemeMode.entries.indexOfFirst { it.name == value }.coerceAtLeast(0)
    putInt(key.intKey(), ordinal)
}

private fun String.intKey(): String = "${this}_int"

@Composable
fun App(
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
    adMobConfig: AdMobConfig = AdMobConfig.EMPTY,
    onThemeModePersist: (ThemeMode) -> Unit = {}
) {
    var themeMode by remember { mutableStateOf(initialThemeMode) }
    val windowSizeClass = rememberAppWindowSizeClass()

    AdivinaPerroTheme(themeMode = themeMode, windowSizeClass = windowSizeClass) {
        val navController = rememberNavController()
        AppNavHost(
            navController = navController,
            adMobConfig = adMobConfig,
            currentThemeMode = themeMode,
            onThemeModeChanged = { mode ->
                themeMode = mode
                onThemeModePersist(mode)
            }
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    adMobConfig: AdMobConfig,
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Splash,
        enterTransition = { NavTransitions.enterTransition },
        exitTransition = { NavTransitions.exitTransition },
        popEnterTransition = { NavTransitions.popEnterTransition },
        popExitTransition = { NavTransitions.popExitTransition }
    ) {
        composable<Splash>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition }
        ) {
            SplashScreen(
                onNavigateToSelect = {
                    navController.navigate(Select) {
                        popUpTo<Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Select> {
            val breedClassifier: BreedClassifier = koinInject()
            SelectScreen(
                showRecognition = breedClassifier.isAvailable(),
                onNavigateToGame = {
                    Analytics.analyticsGameModeSelected(Analytics.MODE_CLASSIC)
                    navController.navigate(Game)
                },
                onNavigateToBiggerSmaller = {
                    Analytics.analyticsGameModeSelected(Analytics.MODE_BIGGER_SMALLER)
                    navController.navigate(BiggerSmaller)
                },
                onNavigateToDescription = {
                    Analytics.analyticsGameModeSelected(Analytics.MODE_DESCRIPTION)
                    navController.navigate(Description)
                },
                onNavigateToFciTrivia = {
                    Analytics.analyticsGameModeSelected(Analytics.MODE_FCI_TRIVIA)
                    navController.navigate(FciTrivia)
                },
                onNavigateToLearn = {
                    Analytics.analyticsClicked(Analytics.BTN_LEARN)
                    navController.navigate(Info)
                },
                onNavigateToRecognition = {
                    navController.navigate(Recognition)
                },
                onNavigateToSettings = {
                    Analytics.analyticsClicked(Analytics.BTN_SETTINGS)
                    navController.navigate(SettingsRoute)
                }
            )
        }

        composable<Game> { GameRoute(navController, adMobConfig) }
        composable<BiggerSmaller> { BiggerSmallerRoute(navController, adMobConfig) }
        composable<Description> { DescriptionRoute(navController, adMobConfig) }
        composable<FciTrivia> { FciTriviaRoute(navController, adMobConfig) }

        composable<Recognition>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { NavTransitions.fadeExitTransition }
        ) {
            RecognitionRoute(navController)
        }

        composable<Result>(
            enterTransition = { NavTransitions.resultEnterTransition },
            exitTransition = { NavTransitions.resultExitTransition },
            popEnterTransition = { NavTransitions.resultEnterTransition },
            popExitTransition = { NavTransitions.resultExitTransition }
        ) { backStackEntry ->
            val result: Result = backStackEntry.toRoute()
            ResultRoute(navController, result.points)
        }

        composable<Info>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { NavTransitions.fadeExitTransition }
        ) {
            InfoRoute(navController, adMobConfig)
        }

        composable<SettingsRoute>(
            enterTransition = { NavTransitions.fadeEnterTransition },
            exitTransition = { NavTransitions.fadeExitTransition },
            popEnterTransition = { NavTransitions.fadeEnterTransition },
            popExitTransition = { NavTransitions.fadeExitTransition }
        ) {
            SettingsRouteScreen(
                navController = navController,
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged
            )
        }
    }
}

@Composable
private fun GameScreenLayout(
    title: String,
    onBackClick: () -> Unit,
    showLives: Boolean,
    lives: Int = 0,
    showBanner: Boolean = false,
    bannerAdUnitId: String = "",
    bannerAdLocation: String = Analytics.AD_LOC_GAME,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        GameAppBar(title = title, onBackClick = onBackClick, showLives = showLives, lives = lives)

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = 680.dp)) { content() }
        }

        if (showBanner && bannerAdUnitId.isNotEmpty()) {
            AdBannerView(
                adUnitId = bannerAdUnitId,
                modifier = Modifier.fillMaxWidth(),
                adLocation = bannerAdLocation
            )
        }
    }
}

@Composable
private fun GameRoute(navController: NavHostController, adMobConfig: AdMobConfig) {
    val viewModel: GameViewModel = koinViewModel()
    val soundPlayer: SoundPlayer = koinInject()

    var life by rememberSaveable { mutableIntStateOf(3) }
    var stage by rememberSaveable { mutableIntStateOf(1) }
    var points by rememberSaveable { mutableIntStateOf(0) }
    var showBanner by remember { mutableStateOf(false) }

    val rewardedAdState = rememberRewardedAdState(
        adUnitId = adMobConfig.bonificadoGame,
        adLocation = Analytics.AD_LOC_GAME
    )

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { navigation ->
            when (navigation) {
                is GameViewModel.Navigation.Result -> {
                    navController.navigate(Result(points)) { popUpTo<Select>() }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showingAds.collect { model ->
            when (model) {
                is GameViewModel.UiModel.ShowBannerAd -> showBanner = model.show
                is GameViewModel.UiModel.ShowReewardAd -> rewardedAdState.show()
                else -> {}
            }
        }
    }

    GameScreenLayout(
        title = stringResource(Res.string.mode_classic_title),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        lives = life,
        showBanner = showBanner,
        bannerAdUnitId = adMobConfig.bannerGame,
        bannerAdLocation = Analytics.AD_LOC_GAME
    ) {
        GameScreen(
            viewModel = viewModel,
            stage = stage,
            lives = life,
            points = points,
            onAnswerSelected = { selectedIndex, correctName, options ->
                val selectedText = options[selectedIndex]
                val isCorrect = selectedText == correctName
                Analytics.analyticsGameAnswer(isCorrect, stage, Analytics.MODE_CLASSIC)
                ErrorTracker.setCustomKey("game_mode", Analytics.MODE_CLASSIC)
                ErrorTracker.setCustomKey("current_stage", stage)
                ErrorTracker.setCustomKey("current_score", points)
                ErrorTracker.setCustomKey("lives_remaining", life)
                if (isCorrect) {
                    soundPlayer.playSuccess()
                    points += 1
                } else {
                    soundPlayer.playFail()
                    life--
                }
                stage += 1
            }
        )
    }

    LaunchedEffect(stage) {
        if (stage > 1) {
            delay(STAGE_DELAY_MS)
            if (stage > TOTAL_BREED || life < 1) {
                viewModel.navigateToResult(points.toString())
            } else {
                viewModel.generateNewStage()
                if (stage != 0 && stage % REWARDED_AD_INTERVAL == 0) viewModel.showRewardedAd()
            }
        }
    }
}

@Composable
private fun BiggerSmallerRoute(navController: NavHostController, adMobConfig: AdMobConfig) {
    val viewModel: BiggerSmallerViewModel = koinViewModel()
    val soundPlayer: SoundPlayer = koinInject()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBanner by remember { mutableStateOf(false) }

    val rewardedAdState = rememberRewardedAdState(
        adUnitId = adMobConfig.bonificadoGame,
        adLocation = Analytics.AD_LOC_GAME
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BiggerSmallerViewModel.Event.NavigateToResult -> {
                    navController.navigate(Result(event.points)) { popUpTo<Select>() }
                }
                is BiggerSmallerViewModel.Event.ShowBannerAd -> showBanner = event.show
                is BiggerSmallerViewModel.Event.ShowRewardedAd -> rewardedAdState.show()
            }
        }
    }

    GameScreenLayout(
        title = stringResource(Res.string.mode_bigger_smaller),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        lives = state.lives,
        showBanner = showBanner,
        bannerAdUnitId = adMobConfig.bannerGame,
        bannerAdLocation = Analytics.AD_LOC_GAME
    ) {
        BiggerSmallerScreenContent(viewModel = viewModel, soundPlayer = soundPlayer)
    }
}

@Composable
private fun DescriptionRoute(navController: NavHostController, adMobConfig: AdMobConfig) {
    val viewModel: DescriptionViewModel = koinViewModel()
    val soundPlayer: SoundPlayer = koinInject()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBanner by remember { mutableStateOf(false) }

    val rewardedAdState = rememberRewardedAdState(
        adUnitId = adMobConfig.bonificadoGame,
        adLocation = Analytics.AD_LOC_GAME
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DescriptionViewModel.Event.NavigateToResult -> {
                    navController.navigate(Result(event.points)) { popUpTo<Select>() }
                }
                is DescriptionViewModel.Event.ShowBannerAd -> showBanner = event.show
                is DescriptionViewModel.Event.ShowRewardedAd -> rewardedAdState.show()
            }
        }
    }

    GameScreenLayout(
        title = stringResource(Res.string.mode_description),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        lives = state.lives,
        showBanner = showBanner,
        bannerAdUnitId = adMobConfig.bannerGame,
        bannerAdLocation = Analytics.AD_LOC_GAME
    ) {
        DescriptionScreenContent(viewModel = viewModel, soundPlayer = soundPlayer)
    }
}

@Composable
private fun FciTriviaRoute(navController: NavHostController, adMobConfig: AdMobConfig) {
    val viewModel: FciTriviaViewModel = koinViewModel()
    val soundPlayer: SoundPlayer = koinInject()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showBanner by remember { mutableStateOf(false) }

    val rewardedAdState = rememberRewardedAdState(
        adUnitId = adMobConfig.bonificadoGame,
        adLocation = Analytics.AD_LOC_GAME
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FciTriviaViewModel.Event.NavigateToResult -> {
                    navController.navigate(Result(event.points)) { popUpTo<Select>() }
                }
                is FciTriviaViewModel.Event.ShowBannerAd -> showBanner = event.show
                is FciTriviaViewModel.Event.ShowRewardedAd -> rewardedAdState.show()
            }
        }
    }

    GameScreenLayout(
        title = stringResource(Res.string.mode_fci_trivia),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        lives = state.lives,
        showBanner = showBanner,
        bannerAdUnitId = adMobConfig.bannerGame,
        bannerAdLocation = Analytics.AD_LOC_GAME
    ) {
        FciTriviaScreenContent(viewModel = viewModel, soundPlayer = soundPlayer)
    }
}

@Composable
private fun ResultRoute(navController: NavHostController, gamePoints: Int) {
    val viewModel: ResultViewModel = koinViewModel()
    val soundPlayer: SoundPlayer = koinInject()
    val intentLauncher: IntentLauncher = koinInject()
    val appName = stringResource(Res.string.app_name)
    val shareMessageWithPoints = stringResource(Res.string.share_message, gamePoints)
    val chooseLabel = stringResource(Res.string.choose_one)

    LaunchedEffect(Unit) {
        soundPlayer.playBark()
        viewModel.getPersonalRecord(gamePoints)
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { navigation ->
            when (navigation) {
                ResultViewModel.Navigation.Select -> {
                    navController.navigate(Select) { popUpTo<Select>() }
                }
                ResultViewModel.Navigation.Rate -> intentLauncher.rateApp()
                is ResultViewModel.Navigation.Share -> intentLauncher.shareApp(
                    appName = appName,
                    shareMessageBody = shareMessageWithPoints,
                    chooseLabel = chooseLabel
                )
            }
        }
    }

    GameScreenLayout(
        title = stringResource(Res.string.resultado_screen_title),
        onBackClick = {
            navController.navigate(Select) { popUpTo<Select> { inclusive = true } }
        },
        showLives = false,
        showBanner = false
    ) {
        ResultScreen(
            viewModel = viewModel,
            gamePoints = gamePoints,
            onPlayAgain = { viewModel.navigateToSelect() },
            onShare = { viewModel.navigateToShare(gamePoints) },
            onRate = { viewModel.navigateToRate() }
        )
    }
}

@Composable
private fun InfoRoute(navController: NavHostController, adMobConfig: AdMobConfig) {
    val viewModel: InfoViewModel = koinViewModel()
    val selectedDog by viewModel.selectedDog.collectAsStateWithLifecycle()
    var currentPage by rememberSaveable { mutableIntStateOf(0) }

    val rewardedAdState = rememberRewardedAdState(
        adUnitId = adMobConfig.bonificadoGame,
        adLocation = Analytics.AD_LOC_INFO
    )

    LaunchedEffect(Unit) {
        viewModel.showingAds.collect { model ->
            when (model) {
                is InfoViewModel.UiModel.ShowReewardAd -> rewardedAdState.show()
                else -> {}
            }
        }
    }

    HandleBackPress(enabled = selectedDog != null) { viewModel.closeDogDetail() }

    GameScreenLayout(
        title = stringResource(Res.string.info_title),
        onBackClick = {
            if (selectedDog != null) viewModel.closeDogDetail() else navController.popBackStack()
        },
        showLives = false,
        showBanner = selectedDog != null,
        bannerAdUnitId = adMobConfig.bannerInfo,
        bannerAdLocation = Analytics.AD_LOC_INFO
    ) {
        InfoScreen(
            viewModel = viewModel,
            currentPage = currentPage,
            onLoadMore = { nextPage ->
                currentPage = nextPage
                viewModel.loadMoreDogList(nextPage)
            }
        )
    }
}

@Composable
private fun SettingsRouteScreen(
    navController: NavHostController,
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    val settings: Settings = koinInject()
    val intentLauncher: IntentLauncher = koinInject()
    val consentGate: ConsentGate = koinInject()

    var isSoundEnabled by remember { mutableStateOf(settings.getBoolean(PREFS_SOUND, true)) }
    var themeMode by remember { mutableStateOf(currentThemeMode) }

    val versionLabel = stringResource(Res.string.settings_version)
    val versionText = "$versionLabel ${appVersionName()} (Build ${appVersionCode()})"
    val settingsAppName = stringResource(Res.string.app_name)
    val settingsShareMessage = stringResource(Res.string.share_message_general)
    val settingsChooseLabel = stringResource(Res.string.choose_one)

    LaunchedEffect(Unit) {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_SETTINGS)
    }

    GameScreenLayout(
        title = stringResource(Res.string.settings),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        showBanner = false
    ) {
        SettingsScreen(
            isSoundEnabled = isSoundEnabled,
            themeMode = themeMode,
            versionText = versionText,
            showPrivacyOptions = consentGate.isPrivacyOptionsRequired,
            onSoundToggle = { enabled ->
                isSoundEnabled = enabled
                settings.putBoolean(PREFS_SOUND, enabled)
            },
            onThemeModeChanged = { mode ->
                themeMode = mode
                onThemeModeChanged(mode)
            },
            onRateApp = { intentLauncher.rateApp() },
            onShare = {
                intentLauncher.shareApp(
                    appName = settingsAppName,
                    shareMessageBody = settingsShareMessage,
                    chooseLabel = settingsChooseLabel
                )
            },
            onPrivacyOptions = { consentGate.showPrivacyOptionsForm() },
            onPrivacyPolicy = { intentLauncher.openPrivacyPolicy() }
        )
    }
}

@Composable
private fun RecognitionRoute(navController: NavHostController) {
    val viewModel: RecognitionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameScreenLayout(
        title = stringResource(Res.string.recognition_title),
        onBackClick = { navController.popBackStack() },
        showLives = false,
        showBanner = false
    ) {
        RecognitionScreen(
            viewModel = viewModel,
            uiState = uiState,
            onBreedClick = { breedId ->
                navController.navigate(Info)
            }
        )
    }
}

expect fun appVersionName(): String
expect fun appVersionCode(): Int

@Composable
expect fun HandleBackPress(enabled: Boolean, onBack: () -> Unit)
