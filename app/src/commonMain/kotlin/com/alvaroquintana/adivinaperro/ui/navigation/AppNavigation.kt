package com.alvaroquintana.adivinaperro.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Splash

@Serializable
data object Select

@Serializable
data object Game

@Serializable
data object BiggerSmaller

@Serializable
data object Description

@Serializable
data object FciTrivia


@Serializable
data class Result(val points: Int)

@Serializable
data object Info

@Serializable
data object Recognition

@Serializable
data object Settings
