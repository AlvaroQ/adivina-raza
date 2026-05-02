package com.alvaroquintana.adivinaperro.managers

import com.alvaroquintana.domain.BreedPrediction

interface BreedClassifier {
    suspend fun classify(imageBytes: ByteArray): List<BreedPrediction>
    fun isAvailable(): Boolean
}
