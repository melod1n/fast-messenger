package com.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.old.VKLongPollServer
import com.meloda.fast.util.AndroidUtils
import org.json.JSONArray
import org.json.JSONObject

// TODO: 8/31/2021 rewrite, use job
@Deprecated("Absolutely obsolete")
class LongPollService : Service() {
    private var thread: Thread? = null
    private var running = false

    override fun onCreate() {
        super.onCreate()
        running = false

//        thread = LowThread(Updater())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (flags and START_FLAG_RETRY == 0) {
            Log.w(TAG, "Retry launch!")
        } else {
            Log.d(TAG, "Simple launch")
        }
        if (running) return START_STICKY
        running = true

        try {
            thread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        running = false

        thread?.interrupt()
    }

    private inner class Updater : Runnable {
        override fun run() {

            var server: VKLongPollServer? = null

            while (running && UserConfig.isLoggedIn()) {
                if (!AndroidUtils.hasConnection()) {
                    try {
                        Thread.sleep(5000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    continue
                }
                try {
                    if (server == null) {
                        server = null
//                        server = VKApi.messages().getLongPollServer()
//                            .execute(VKLongPollServer::class.java)!![0]
                    }

                    val response = getResponse(server)
                    if (response.has("failed")) {
                        Log.w(TAG, "Failed get response")
                        Thread.sleep(1000)
                        server = null
                        continue
                    }

                    val tsResponse = response.optLong("ts")
                    val updates = response.getJSONArray("updates")

                    Log.i(TAG, "updates: $updates")

                    server?.ts = tsResponse

                    if (updates.length() != 0) {
                        process(updates)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        Thread.sleep(5000)
                        server = null
                    } catch (e1: InterruptedException) {
                        e1.printStackTrace()
                    }
                }
            }
        }

        @Throws(Exception::class)
        private fun getResponse(server: VKLongPollServer?): JSONObject {
            return JSONObject("")
//            val params = arrayMapOf<String, String>()
//            params["act"] = "a_check"
//            params["key"] = server.key
//            params["ts"] = server.ts.toString()
//            params["wait"] = "10"
//            params["mode"] = "490"
//            params["version"] = "9"
//
//            val buffer = HttpRequest["https://" + server.server, params].asString()
//
//            return JSONObject(buffer)
        }

        @WorkerThread
        private fun process(updates: JSONArray) {
        }
    }

    companion object {
        private const val TAG = "LongPollService"
    }
}