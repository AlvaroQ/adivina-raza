package com.alvaroquintana.domain

data class BreedPrediction(
    val label: String,
    val breedName: String?,
    val breedId: Long?,
    val confidence: Float,
    val imageUrl: String? = null
)
