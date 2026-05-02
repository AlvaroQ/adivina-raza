package com.alvaroquintana.adivinaperro.application

import android.app.Activity
import android.app.Application
import com.alvaroquintana.adivinaperro.BuildConfig
import com.alvaroquintana.adivinaperro.managers.ActivityHolder
import com.alvaroquintana.adivinaperro.managers.AndroidBreedClassifier
import com.alvaroquintana.adivinaperro.managers.AndroidConsentGate
import com.alvaroquintana.adivinaperro.managers.AndroidIntentLauncher
import com.alvaroquintana.adivinaperro.managers.AndroidLabelBreedMapper
import com.alvaroquintana.adivinaperro.managers.AndroidSettings
import com.alvaroquintana.adivinaperro.managers.AndroidSoundPlayer
import com.alvaroquintana.adivinaperro.managers.BreedClassifier
import com.alvaroquintana.adivinaperro.managers.ConsentGate
import com.alvaroquintana.adivinaperro.managers.ConsentManager
import com.alvaroquintana.adivinaperro.managers.IntentLauncher
import com.alvaroquintana.adivinaperro.managers.LabelBreedMapper
import com.alvaroquintana.adivinaperro.managers.Settings
import com.alvaroquintana.adivinaperro.managers.SoundPlayer
import com.alvaroquintana.data.db.DriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun Application.initDI() {
    startKoin {
        if (BuildConfig.DEBUG) {
            androidLogger()
        }
        androidContext(this@initDI)
        modules(androidPlatformModule, sharedModule)
    }
}

private fun requireActivity(): Activity = ActivityHolder.current
    ?: error("No Activity bound. Did MainActivity register in onResume()?")

private val androidPlatformModule = module {
    single { DriverFactory(androidContext()) }
    single<Settings> { AndroidSettings(androidContext()) }
    single<SoundPlayer> { AndroidSoundPlayer(androidContext()) }
    single<IntentLauncher> { AndroidIntentLauncher { requireActivity() } }
    single<LabelBreedMapper> { AndroidLabelBreedMapper(get(), get()) }
    single<BreedClassifier> { AndroidBreedClassifier(androidContext(), get()) }
    single<ConsentGate> {
        AndroidConsentGate(
            activityProvider = { requireActivity() },
            consent = ConsentManager.getInstance(androidContext())
        )
    }
}
