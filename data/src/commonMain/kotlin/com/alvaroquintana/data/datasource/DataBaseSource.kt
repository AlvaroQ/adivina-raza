package com.alvaroquintana.data.datasource

import com.alvaroquintana.domain.App
import com.alvaroquintana.domain.Dog

interface DataBaseSource {
    suspend fun ensureSynced()
    suspend fun getBreedById(id: Int): Dog
    suspend fun getBreedList(currentPage: Int): MutableList<Dog>
    suspend fun getAppsRecommended(): MutableList<App>
    suspend fun getRandomBreedsWithWeight(count: Int): List<Dog>
    suspend fun getRandomBreedsWithDescription(count: Int): List<Dog>
    suspend fun getRandomBreedsWithFciGroup(count: Int): List<Dog>
    suspend fun getRandomBreedsWithCare(count: Int): List<Dog>
}