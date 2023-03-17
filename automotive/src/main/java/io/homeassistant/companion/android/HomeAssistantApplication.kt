package io.homeassistant.companion.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.homeassistant.companion.android.common.data.keychain.KeyChainRepository
import io.homeassistant.companion.android.common.data.prefs.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltAndroidApp
open class HomeAssistantAutomotiveApplication : Application() {

    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())

    @Inject
    lateinit var prefsRepository: PrefsRepository

    @Inject
    lateinit var keyChainRepository: KeyChainRepository

    override fun onCreate() {
        super.onCreate()
    }

}