package com.alvaroquintana.adivinaperro.managers

import com.alvaroquintana.domain.BreedPrediction

interface LabelBreedMapper {
    suspend fun map(labels: List<Pair<String, Float>>): List<BreedPrediction>
}
