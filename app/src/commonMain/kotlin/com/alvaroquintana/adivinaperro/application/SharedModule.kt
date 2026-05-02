package com.alvaroquintana.adivinaperro.application

import com.alvaroquintana.adivinaperro.ui.recognition.RecognitionViewModel
import com.alvaroquintana.adivinaperro.ui.game.BiggerSmallerViewModel
import com.alvaroquintana.adivinaperro.ui.game.DescriptionViewModel
import com.alvaroquintana.adivinaperro.ui.game.FciTriviaViewModel
import com.alvaroquintana.adivinaperro.ui.game.GameViewModel
import com.alvaroquintana.adivinaperro.ui.info.InfoViewModel
import com.alvaroquintana.adivinaperro.ui.result.ResultViewModel
import com.alvaroquintana.adivinaperro.ui.select.SelectViewModel
import com.alvaroquintana.data.datasource.BreedEsDataBaseSourceImpl
import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.data.db.createDatabase
import com.alvaroquintana.data.repository.BreedByIdRepository
import com.alvaroquintana.usecases.GetBreedById
import com.alvaroquintana.usecases.GetBreedList
import com.alvaroquintana.usecases.GetRandomBreedsWithDescription
import com.alvaroquintana.usecases.GetRandomBreedsWithFciGroup
import com.alvaroquintana.usecases.GetRandomBreedsWithWeight
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Multiplatform Koin module shared between Android and iOS. Each
 * platform registers its own DriverFactory + Settings / SoundPlayer /
 * IntentLauncher / ConsentGate before pulling this module in.
 */
val sharedModule = module {
    single { createDatabase(get()) }

    // Wrap the gitlive accessors so a missing FirebaseApp.configure()
    // does not crash the app at boot time on iOS — DataBaseSource handles
    // null gracefully (returns empty data, no remote sync).
    single<dev.gitlive.firebase.firestore.FirebaseFirestore?> {
        runCatching { Firebase.firestore }.getOrNull()
    }
    single<dev.gitlive.firebase.crashlytics.FirebaseCrashlytics?> {
        runCatching { Firebase.crashlytics }.getOrNull()
    }
    single<DataBaseSource> { BreedEsDataBaseSourceImpl(get(), get(), get()) }

    factory { BreedByIdRepository(get()) }
    factory { GetBreedById(get()) }
    factory { GetBreedList(get()) }
    factory { GetRandomBreedsWithWeight(get()) }
    factory { GetRandomBreedsWithDescription(get()) }
    factory { GetRandomBreedsWithFciGroup(get()) }

    viewModelOf(::SelectViewModel)
    viewModelOf(::GameViewModel)
    viewModelOf(::BiggerSmallerViewModel)
    viewModelOf(::DescriptionViewModel)
    viewModelOf(::FciTriviaViewModel)
    viewModelOf(::ResultViewModel)
    viewModelOf(::InfoViewModel)
    viewModelOf(::RecognitionViewModel)
}
