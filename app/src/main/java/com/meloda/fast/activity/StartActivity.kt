package com.meloda.fast.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.button.MaterialButton
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.BaseActivity

@SuppressLint("InflateParams")
class StartActivity : BaseActivity() {

    private lateinit var startEnter: MaterialButton
    private lateinit var startLoginSettings: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        initViews()

        prepareEnterButton()
    }

    private fun initViews() {
        startEnter = findViewById(R.id.startEnter)
        startLoginSettings = findViewById(R.id.startLoginSettings)
    }

    private fun prepareEnterButton() {
        startEnter.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        startEnter.setOnLongClickListener {
            showUserIdTokenDialog()
            true
        }

        startLoginSettings.setOnClickListener {
            Toast.makeText(this, R.string.in_progress_placeholder, Toast.LENGTH_LONG).show()
        }
    }

    private fun showUserIdTokenDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.custom_data)

            val view = LayoutInflater.from(this@StartActivity)
                .inflate(R.layout.activity_login_custom_data, null, false) as View
            setView(view)

            val userId = view.findViewById<AppCompatEditText>(R.id.customDataUserId)
            val token = view.findViewById<AppCompatEditText>(R.id.customDataToken)

            setPositiveButton(android.R.string.ok) { _, _ ->
                if (userId.text.toString().isEmpty() || token.text.toString().isEmpty())
                    return@setPositiveButton
                val id = userId.text.toString().toInt()
                val accessToken = token.text.toString()

                if (id < 1) return@setPositiveButton

                UserConfig.userId = id
                UserConfig.token = accessToken
                UserConfig.save()

                finish()
                startActivity(Intent(this@StartActivity, MainActivity::class.java))
            }

            setCancelable(false)
            setNegativeButton(android.R.string.cancel, null)
        }.show()
    }

}