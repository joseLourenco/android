package io.homeassistant.companion.android

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn

class HaCarAppService : io.homeassistant.companion.android.common.vehicle.HaCarAppService() {

    override fun onCreateSession(sessionInfo: SessionInfo): Session {
        return object : Session() {
            init {
                serverManager.getServer()?.let {
                    loadEntities(lifecycleScope, it.id)
                }
            }

            val serverIdFlow = serverId.asStateFlow()
            val entityFlow = allEntities.shareIn(
                lifecycleScope,
                SharingStarted.WhileSubscribed(10_000),
                1
            )

            override fun onCreateScreen(intent: Intent): Screen {
                return MainVehicleScreen(
                    carContext,
                    serverManager,
                    serverIdFlow,
                    entityFlow
                ) { loadEntities(lifecycleScope, it) }
            }
        }
    }

}