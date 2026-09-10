package com.jyoti.assistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt is wired up here so every module (core + feature)
 * can contribute its own DI bindings without app/ knowing the implementation details.
 * Adding a new feature module later = add its Hilt module, nothing here changes.
 */
@HiltAndroidApp
class JyotiApplication : Application()
