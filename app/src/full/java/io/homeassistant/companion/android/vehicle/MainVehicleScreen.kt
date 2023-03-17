package io.homeassistant.companion.android.vehicle

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.car.app.CarContext
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import io.homeassistant.companion.android.common.R
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.common.data.servers.ServerManager
import io.homeassistant.companion.android.common.vehicle.MainVehicleScreen
import io.homeassistant.companion.android.launch.LaunchActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@RequiresApi(Build.VERSION_CODES.O)
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
                                carContext.startActivity(
                                    Intent(carContext, LaunchActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                )
                            }
                        )
                        .build()
                )
                .build()
        }

        return super.onGetTemplate()
    }

}