package com.meloda.fast.fragment.ui.presenter

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.textfield.TextInputEditText
import com.meloda.fast.R
import com.meloda.fast.activity.MainActivityDeprecated
import com.meloda.fast.api.UserConfig
import com.meloda.fast.extensions.FragmentExtensions.runOnUiThread
import com.meloda.fast.fragment.FragmentConversationsDeprecated
import com.meloda.fast.fragment.LoginFragment
import com.meloda.fast.fragment.ValidationFragment
import com.meloda.fast.fragment.ui.repository.LoginRepository
import com.meloda.fast.fragment.ui.view.LoginView
import com.meloda.mvp.MvpOnLoadListener
import com.meloda.mvp.MvpPresenter
import com.squareup.picasso.Picasso
import org.json.JSONObject


class LoginPresenter(
    viewState: LoginView
) : MvpPresenter<Any, LoginRepository, LoginView>(
    viewState,
    LoginRepository::class.java.name
) {

    private var lastEmail: String = ""
    private var lastPassword: String = ""

    private lateinit var fragment: LoginFragment

    fun onCreate(context: Context, fragment: LoginFragment, bundle: Bundle?) {
        super.onCreate(context, bundle)
        this.fragment = fragment
    }

    override fun onViewCreated(bundle: Bundle?) {
        viewState.initViews()
        viewState.prepareViews()
    }

    fun login(
        email: String,
        password: String,
        captcha: String = "",
        onLoadListener: MvpOnLoadListener<Any?>? = null
    ) {
        lastEmail = email
        lastPassword = password

        repository.login(requireContext(), email, password, captcha,
            object : MvpOnLoadListener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    checkResponse(response, onLoadListener)
                }

                override fun onError(t: Throwable) {
                    onLoadListener?.onError(t)
                }
            })
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private fun checkResponse(
        response: JSONObject,
        onLoadListener: MvpOnLoadListener<Any?>? = null
    ) {
        if (response.has("error")) {
            val errorString = response.optString("error")
            when (errorString) {
                "need_validation" -> {
                    val redirectUrl = response.optString("redirect_uri")

                    val bundle = Bundle()
                    bundle.putString("url", redirectUrl)

                    fragment.runOnUiThread {
                        fragment.setFragmentResultListener("validation") { _, bundle ->
                            val userId = bundle.getInt("userId")
                            val token = bundle.getString("token") ?: ""
                            saveUserData(userId, token)

                            openMainScreen()
                        }
                    }

                    fragment.parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            ValidationFragment().apply { arguments = bundle })
                        .addToBackStack("")
                        .commit()

                }
                "need_captcha" -> {
                    val captchaImage = response.optString("captcha_img")
                    val captchaSid = response.optString("captcha_sid")
                    showCaptchaDialog(captchaImage, captchaSid)
                }
            }
        } else {
            val userId = response.optInt("user_id", -1)
            val token = response.optString("access_token")
            saveUserData(userId, token)

            openMainScreen()

            onLoadListener?.onResponse(null)
        }
    }

    private fun openMainScreen() {
        fragment.runOnUiThread {
            (fragment.requireActivity() as MainActivityDeprecated).bottomBar.isVisible = true

            fragment.parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    FragmentConversationsDeprecated()
                ).commit()
        }
    }

    private fun saveUserData(userId: Int, token: String) {
        UserConfig.userId = userId
        UserConfig.token = token
        UserConfig.save()
    }

    private fun showCaptchaDialog(captchaImage: String, captchaSid: String) {
        val resources = fragment.resources
        val metrics = resources.displayMetrics

        fragment.runOnUiThread {
            val image = ImageView(requireContext())
            image.layoutParams = ViewGroup.LayoutParams(
                (metrics.widthPixels / 3.5).toInt(), metrics.heightPixels / 7
            )

            Picasso.get().load(captchaImage).priority(Picasso.Priority.HIGH).into(image)

            val captchaCodeEditText = TextInputEditText(requireContext())
            captchaCodeEditText.setHint(R.string.captcha_hint)

            captchaCodeEditText.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

            val builder = AlertDialog.Builder(requireContext())

            val layout = LinearLayout(requireContext())

            layout.orientation = LinearLayout.VERTICAL
            layout.gravity = Gravity.CENTER
            layout.addView(image)
            layout.addView(captchaCodeEditText)

            builder.setView(layout)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                val captchaCode = captchaCodeEditText.text.toString().trim()

                login(
                    lastEmail,
                    lastPassword,
                    "&captcha_sid=$captchaSid&captcha_key=$captchaCode"
                )
            }

            builder.setTitle(R.string.input_captcha)
            builder.show()
        }
    }


}