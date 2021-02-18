package ru.melod1n.project.vkm.activity

import android.content.Intent
import android.os.Bundle
import ru.melod1n.project.vkm.base.BaseActivity
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.common.TaskManager
import ru.melod1n.project.vkm.api.UserConfig

class DropUserDataActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserConfig.clear()

        TaskManager.execute { AppGlobal.database.clearAllTables() }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

}