package com.meloda.fast.screens.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginArguments(val code: String) : Parcelable {

    companion object {
        val EMPTY: LoginArguments = LoginArguments(code = "")
    }
}
