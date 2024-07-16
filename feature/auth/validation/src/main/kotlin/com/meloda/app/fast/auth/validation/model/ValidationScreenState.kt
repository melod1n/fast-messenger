package dev.meloda.fast.auth.validation.model

data class ValidationScreenState(
    val code: String?,
    val codeError: Boolean,
    val isSmsButtonVisible: Boolean,
    val delayTime: Int,
    val phoneMask: String
) {

    companion object {
        val EMPTY = ValidationScreenState(
            code = null,
            codeError = false,
            isSmsButtonVisible = false,
            delayTime = 0,
            phoneMask = ""
        )
    }
}
