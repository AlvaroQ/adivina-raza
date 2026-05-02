package com.alvaroquintana.data.time

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
actual fun currentTimeMillis(): Long = time(null) * 1000L
