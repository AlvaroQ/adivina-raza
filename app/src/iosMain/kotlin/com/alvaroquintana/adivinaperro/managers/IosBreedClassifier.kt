package com.alvaroquintana.adivinaperro.managers

import com.alvaroquintana.domain.BreedPrediction

class IosBreedClassifier : BreedClassifier {
    override fun isAvailable(): Boolean = false
    override suspend fun classify(imageBytes: ByteArray): List<BreedPrediction> = emptyList()
}
