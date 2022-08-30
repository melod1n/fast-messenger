package com.meloda.fast.api.network.ota

import android.os.Parcelable
import com.meloda.fast.model.UpdateItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class OtaGetLatestReleaseResponse(val release: UpdateItem?) : Parcelable