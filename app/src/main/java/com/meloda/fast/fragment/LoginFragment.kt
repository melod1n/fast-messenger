package com.meloda.fast.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.fragment.ui.presenter.LoginPresenter
import com.meloda.fast.fragment.ui.view.LoginView
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.KeyboardUtils
import kotlin.math.roundToInt

class LoginFragment : BaseFragment(), LoginView {

    private lateinit var presenter: LoginPresenter

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var authorize: MaterialButton
    private lateinit var card: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = LoginPresenter(this)
        presenter.onCreate(requireContext(), this, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        presenter.onCreateView(savedInstanceState)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presenter.onViewCreated(savedInstanceState)
    }

    override fun initViews() {
        email = requireView().findViewById(R.id.loginEmailEditText)
        password = requireView().findViewById(R.id.loginPasswordEditText)
        authorize = requireView().findViewById(R.id.loginAuthorize)
        card = requireView().findViewById(R.id.loginCard)
    }

    override fun prepareViews() {
        prepareEmailEditText()
        preparePasswordEditText()
        prepareAuthorizeButton()
        prepareCardView()
    }

    private fun prepareEmailEditText() {
        email.addTextChangedListener(onTextChangedListener)
    }

    private fun preparePasswordEditText() {
        password.addTextChangedListener(onTextChangedListener)

        password.setOnEditorActionListener { _, _, event ->
            if (event == null) return@setOnEditorActionListener false
            return@setOnEditorActionListener if (event.action == EditorInfo.IME_ACTION_GO ||
                (event.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER))
            ) {
                KeyboardUtils.hideKeyboardFrom(password)
                authorize.performClick()
                true
            } else false
        }
    }

    private fun prepareAuthorizeButton() {
        authorize.isEnabled = false
        authorize.setOnClickListener {
            val emailString = email.text.toString().trim()
            val passwordString = password.text.toString().trim()

            presenter.login(emailString, passwordString)
        }
    }

    private fun prepareCardView() {
        val width = AndroidUtils.dp(resources.displayMetrics.widthPixels).roundToInt()

        if (width < 380) {
            card.strokeWidth = 0
        }
    }

    override fun showErrorSnackbar(t: Throwable) {
        TODO("Not yet implemented")
    }

    override fun prepareNoItemsView() {
        TODO("Not yet implemented")
    }

    override fun showNoItemsView() {
        TODO("Not yet implemented")
    }

    override fun hideNoItemsView() {
        TODO("Not yet implemented")
    }

    override fun prepareNoInternetView() {
        TODO("Not yet implemented")
    }

    override fun showNoInternetView() {
        TODO("Not yet implemented")
    }

    override fun hideNoInternetView() {
        TODO("Not yet implemented")
    }

    override fun prepareErrorView() {
        TODO("Not yet implemented")
    }

    override fun showErrorView() {
        TODO("Not yet implemented")
    }

    override fun hideErrorView() {
        TODO("Not yet implemented")
    }

    override fun showProgressBar() {
        TODO("Not yet implemented")
    }

    override fun hideProgressBar() {
        TODO("Not yet implemented")
    }

    override fun showRefreshLayout() {
        TODO("Not yet implemented")
    }

    override fun hideRefreshLayout() {
        TODO("Not yet implemented")
    }

    private val onTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            authorize.isEnabled =
                email.text.toString().trim().isNotEmpty() &&
                        password.text.toString().trim().isNotEmpty()
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
}