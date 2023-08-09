package com.meloda.fast.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.meloda.fast.screens.main.activity.MainActivity

class LongPollQSTileService : TileService() {

    override fun onTileAdded() {
        Log.d("LongPollQSTileService", "onTileAdded")
        super.onTileAdded()
    }

    override fun onStartListening() {
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.updateTile()
        Log.d("LongPollQSTileService", "onStartListening")
        super.onStartListening()
    }


    override fun onStopListening() {
        Log.d("LongPollQSTileService", "onStopListening")
        super.onStopListening()
    }

    override fun onClick() {
        Log.d("LongPollQSTileService", "onClick")

        startActivityAndCollapse(Intent(this, MainActivity::class.java).apply {
            putExtra("data", "open_settings")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        super.onClick()
    }

    override fun onTileRemoved() {
        Log.d("LongPollQSTileService", "onTileRemoved")
        super.onTileRemoved()
    }
}
