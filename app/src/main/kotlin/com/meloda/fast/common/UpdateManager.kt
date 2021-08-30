package com.meloda.fast.common

import android.util.Log
import androidx.collection.arrayMapOf
import com.meloda.fast.concurrent.TaskManager
import com.meloda.fast.BuildConfig
import com.meloda.fast.model.NewUpdateInfo
import com.meloda.fast.net.HttpRequest
import org.json.JSONArray
import org.json.JSONObject

object UpdateManager {

    interface OnUpdateListener {
        fun onNewUpdate(updateInfo: NewUpdateInfo)

        fun onNoUpdates()
    }

    private const val checkLink = "https://melodev.procsec.top/vkm/project_vkm_ota.json"

    private const val PRODUCT_NAME = "project_vkm"
    private const val BRANCH = "alpha"
    private const val OFFSET = 0

    private const val TAG = "UpdateManager"

    fun checkUpdates(onUpdateListener: OnUpdateListener) {
        TaskManager.execute {
            val newLink = "https://temply.procsec.top/prop/deploy/api/method/getOTA"

            val params = arrayMapOf<String, String>()
            params["product"] = PRODUCT_NAME
            params["branch"] = BRANCH
            params["offset"] = OFFSET.toString()
            params["code"] = AppGlobal.versionCode.toString()

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Request started")
            }

            HttpRequest[newLink, params].asString().let {
                AppGlobal.post {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "response: $it")
                    }

                    val response: Any = if (it == "[]") JSONArray(it) else JSONObject(it)

                    val newUpdateInfo: NewUpdateInfo? =
                        if (response is JSONArray) null else NewUpdateInfo(response as JSONObject)

                    if (response is JSONArray || newUpdateInfo?.version?.isEmpty() == true || newUpdateInfo?.version == AppGlobal.versionName) {
                        onUpdateListener.onNoUpdates()
                        return@post
                    } else {
                        newUpdateInfo?.let { onUpdateListener.onNewUpdate(it) }
                    }
                }
            }

//            HttpRequest[checkLink].asString().let {
//                val response = JSONObject(it)
//
//                val updateInfo = UpdateInfo(response)
//
//                AppGlobal.handler.post {
//                    if (updateInfo.version.isEmpty() || updateInfo.version == AppGlobal.versionName) {
//                        onUpdateListener.onNoUpdates()
//                        return@post
//                    }
//
//                    if (AppGlobal.versionName != updateInfo.version) {
//                        onUpdateListener.onNewUpdate(updateInfo)
//                    }
//                }
//            }
        }
    }

}