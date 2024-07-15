package com.meloda.app.fast.ui.basic

import android.os.Build
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.Autofill
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt

fun Modifier.connectNode(handler: AutoFillHandler): Modifier {
    return with(handler) { fillBounds() }
}

fun Modifier.defaultFocusChangeAutoFill(handler: AutoFillHandler): Modifier {
    return this.then(
        Modifier.onFocusChanged {
            if (it.isFocused) {
                handler.request()
            } else {
                handler.cancel()
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun autoFillRequestHandler(
    autofillTypes: List<AutofillType> = listOf(),
    onFill: (String) -> Unit,
): AutoFillHandler {
    val view = LocalView.current
    val context = LocalContext.current
    var isFillRecently = remember { false }
    val autoFillNode = remember {
        AutofillNode(
            autofillTypes = autofillTypes,
            onFill = {
                isFillRecently = true
                onFill(it)
            }
        )
    }
    val autofill = LocalAutofill.current
    LocalAutofillTree.current += autoFillNode

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return EmptyAutoFillHandler

    return remember {
        @RequiresApi(Build.VERSION_CODES.O)
        object : AutoFillHandler {
            val autofillManager = context.getSystemService(AutofillManager::class.java)
            override fun requestManual() {
                autofillManager.requestAutofill(
                    view,
                    autoFillNode.id,
                    autoFillNode.boundingBox?.toAndroidRect() ?: error("BoundingBox is not provided yet")
                )
            }

            override fun requestVerifyManual() {
                if (isFillRecently) {
                    isFillRecently = false
                    requestManual()
                }
            }

            override val autoFill: Autofill?
                get() = autofill

            override val autoFillNode: AutofillNode
                get() = autoFillNode

            override fun request() {
                autofill?.requestAutofillForNode(autofillNode = autoFillNode)
            }

            override fun cancel() {
                autofill?.cancelAutofillForNode(autofillNode = autoFillNode)
            }

            override fun Modifier.fillBounds(): Modifier {
                return this.then(
                    Modifier.onGloballyPositioned {
                        autoFillNode.boundingBox = it.boundsInWindow()
                    })
            }
        }
    }
}

fun Rect.toAndroidRect(): android.graphics.Rect {
    return android.graphics.Rect(
        left.roundToInt(),
        top.roundToInt(),
        right.roundToInt(),
        bottom.roundToInt()
    )
}

@OptIn(ExperimentalComposeUiApi::class)
interface AutoFillHandler {

    val autoFill: Autofill?
    val autoFillNode: AutofillNode?
    fun requestVerifyManual()
    fun requestManual()
    fun request()
    fun cancel()
    fun Modifier.fillBounds(): Modifier
}

@ExperimentalComposeUiApi
data object EmptyAutoFillHandler : AutoFillHandler {
    override val autoFill: Autofill? = null
    override val autoFillNode: AutofillNode? = null
    override fun requestVerifyManual() {}
    override fun requestManual() {}
    override fun request() {}
    override fun cancel() {}
    override fun Modifier.fillBounds(): Modifier = this.then(Modifier)
}
