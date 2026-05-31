package dev.meloda.fast.common

import dev.meloda.fast.common.model.NetworkState
import dev.meloda.fast.common.model.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkStateListener {

    private val networkStatus = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    val networkStatusFlow = networkStatus.asStateFlow()

    private val networkState = MutableStateFlow(NetworkState.DISCONNECTED)
    val networkStateFlow = networkState.asStateFlow()

    fun updateNetworkState(state: NetworkState) {
        networkState.value = state
        networkStatus.value = when (state) {
            NetworkState.CONNECTED -> NetworkStatus.AVAILABLE
            NetworkState.DISCONNECTED -> NetworkStatus.UNAVAILABLE
        }
    }
}
