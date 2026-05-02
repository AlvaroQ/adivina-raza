package com.alvaroquintana.adivinaperro.application

import com.alvaroquintana.adivinaperro.managers.ConsentGate
import com.alvaroquintana.adivinaperro.managers.IntentLauncher
import com.alvaroquintana.adivinaperro.managers.BreedClassifier
import com.alvaroquintana.adivinaperro.managers.IosBreedClassifier
import com.alvaroquintana.adivinaperro.managers.IosConsentGate
import com.alvaroquintana.adivinaperro.managers.IosIntentLauncher
import com.alvaroquintana.adivinaperro.managers.IosSettings
import com.alvaroquintana.adivinaperro.managers.IosSoundPlayer
import com.alvaroquintana.adivinaperro.managers.Settings
import com.alvaroquintana.adivinaperro.managers.SoundPlayer
import com.alvaroquintana.data.db.DriverFactory
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

private val iosPlatformModule = module {
    single { DriverFactory() }
    single<Settings> { IosSettings() }
    single<SoundPlayer> { IosSoundPlayer() }
    single<IntentLauncher> { IosIntentLauncher() }
    single<ConsentGate> { IosConsentGate() }
    single<BreedClassifier> { IosBreedClassifier() }
}

fun initKoinIos() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        modules(iosPlatformModule, sharedModule)
    }
}
