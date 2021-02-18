package ru.melod1n.project.vkm.base.mvp

import android.app.Application
import android.os.Handler

object MvpBase {

    lateinit var handler: Handler

    fun init(application: Application) {
        handler = Handler(application.mainLooper)
    }

    fun init(appHandler: Handler) {
        handler = appHandler
    }

    fun post(runnable: Runnable) {
        handler.post(runnable)
    }
}