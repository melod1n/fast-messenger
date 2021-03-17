package com.meloda.fast.activity

import android.content.Intent
import android.os.Bundle
import com.meloda.fast.UserConfig
import com.meloda.fast.base.BaseActivity

class DropUserDataActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserConfig.clear()

//        TaskManager.execute { AppGlobal.database.clearAllTables() }

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

}