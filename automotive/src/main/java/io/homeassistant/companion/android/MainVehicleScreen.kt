package io.homeassistant.companion.android

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import io.homeassistant.companion.android.common.R
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.common.data.servers.ServerManager
import io.homeassistant.companion.android.common.vehicle.MainVehicleScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

open class MainVehicleScreen(
    carContext: CarContext,
    override val serverManager: ServerManager,
    serverId: StateFlow<Int>,
    allEntities: Flow<Map<String, Entity<*>>>,
    onChangeServer: (Int) -> Unit
) : MainVehicleScreen(carContext, serverManager, serverId, allEntities, onChangeServer) {

    companion object {
        private const val TAG = "MainVehicleScreen"
    }

    override fun onGetTemplate(): Template {
        if (isLoggedIn == false) {
            return MessageTemplate.Builder(carContext.getString(R.string.aa_app_not_logged_in))
                .setTitle(carContext.getString(R.string.app_name))
                .setHeaderAction(Action.APP_ICON)
                .addAction(
                    Action.Builder()
                        .setTitle(carContext.getString(R.string.login))
                        .setOnClickListener(
                            ParkedOnlyOnClickListener.create {
                                Log.i(TAG, "Starting login activity")
                                screenManager.push(LoginScreen(carContext))
                            }
                        )
                        .build()
                )
                .build()
        }

        return super.onGetTemplate()
    }

}