package io.homeassistant.companion.android.common.vehicle

import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.car.app.CarAppService
import androidx.car.app.validation.HostValidator
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.common.R
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.common.data.servers.ServerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableMap
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
open class HaCarAppService : CarAppService() {

    companion object {
        private const val TAG = "HaCarAppService"
    }

    @Inject
    lateinit var serverManager: ServerManager

    protected val serverId = MutableStateFlow(0)
    protected val allEntities = MutableStateFlow<Map<String, Entity<*>>>(emptyMap())
    private var allEntitiesJob: Job? = null

    override fun createHostValidator(): HostValidator {
        return if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(R.array.hosts_allowlist)
                .build()
        }
    }

    protected fun loadEntities(scope: CoroutineScope, id: Int) {
        allEntitiesJob?.cancel()
        allEntitiesJob = scope.launch {
            allEntities.emit(emptyMap())
            serverId.value = id
            val entities: MutableMap<String, Entity<*>>? =
                if (serverManager.getServer(id) != null) {
                    serverManager.integrationRepository(id).getEntities()
                        ?.associate { it.entityId to it }
                        ?.toMutableMap()
                } else {
                    null
                }
            if (entities != null) {
                allEntities.emit(entities.toImmutableMap())
                serverManager.integrationRepository(id).getEntityUpdates()?.collect { entity ->
                    entities[entity.entityId] = entity
                    allEntities.emit(entities.toImmutableMap())
                }
            } else {
                Log.w(TAG, "No entities found?")
                allEntities.emit(emptyMap())
            }
        }
    }
}
