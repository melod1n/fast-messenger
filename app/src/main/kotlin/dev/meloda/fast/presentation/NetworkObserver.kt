package dev.meloda.fast.presentation

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dev.meloda.fast.common.NetworkStateListener
import dev.meloda.fast.common.model.NetworkState
import dev.meloda.fast.common.model.NetworkStatus
import dev.meloda.fast.common.model.NetworkType
import dev.meloda.fast.logger.FastLogger
import java.util.concurrent.ConcurrentHashMap

internal class NetworkObserver(
    private val connectivityManager: ConnectivityManager,
    private val logger: FastLogger,
    private val networkStateListener: NetworkStateListener
) {

    private val networks = ConcurrentHashMap<Network, NetworkModel>()

    private var clearCallbacks: (() -> Unit)? = null

    fun start() {
        startListener()
    }

    private fun syncNetworkState() {
        val state = if (networks.values.any { it.isInternetAvailable() }) {
            NetworkState.CONNECTED
        } else {
            NetworkState.DISCONNECTED
        }

        networkStateListener.updateNetworkState(state)
        log("STATE: $state")
    }

    private fun startListener() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                log("onAvailable(): network: $network")

                networks[network] = mapNetworkModel(
                    network = network,
                    capabilities = connectivityManager.getNetworkCapabilities(network),
                    properties = connectivityManager.getLinkProperties(network),
                    status = NetworkStatus.AVAILABLE,
                    assumeInternet = true
                )

                syncNetworkState()
            }

            override fun onUnavailable() {
                log("onUnavailable()")

                networks.clear()
                syncNetworkState()
            }

            override fun onLost(network: Network) {
                log("onLost() network: $network")

                networks.remove(network)
                syncNetworkState()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                log("onCapabilitiesChanged(): network: $network; caps: $networkCapabilities")

                val current = networks[network]
                networks[network] = mapNetworkModel(
                    network = network,
                    capabilities = networkCapabilities,
                    from = current,
                    status = current?.status ?: NetworkStatus.AVAILABLE
                )

                syncNetworkState()
            }

            override fun onBlockedStatusChanged(
                network: Network,
                blocked: Boolean
            ) {
                log("onBlockedStatusChanged(): network: $network; blocked: $blocked")

                networks[network] = mapNetworkModel(
                    network = network,
                    from = networks[network],
                    status = if (blocked) NetworkStatus.BLOCKED else NetworkStatus.UNBLOCKED
                )

                syncNetworkState()
            }

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties
            ) {
                log("onLinkPropertiesChanged(): network: $network; props: $linkProperties")

                val current = networks[network]
                networks[network] = mapNetworkModel(
                    network = network,
                    properties = linkProperties,
                    from = current,
                    status = current?.status ?: NetworkStatus.AVAILABLE
                )

                syncNetworkState()
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                log("onLosing(): network: $network; maxMsToLive: $maxMsToLive")

                val current = networks[network]
                networks[network] = mapNetworkModel(
                    network = network,
                    maxMsToLive = maxMsToLive.toLong(),
                    from = current,
                    status = current?.status ?: NetworkStatus.AVAILABLE
                )

                syncNetworkState()
            }

            override fun onReserved(networkCapabilities: NetworkCapabilities) {
                log("onReserved(): caps: $networkCapabilities")
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        clearCallbacks = { connectivityManager.unregisterNetworkCallback(callback) }

        refreshActiveNetwork()
    }

    private fun log(text: String) {
        logger.debug(this::class, text)
    }

    private fun refreshActiveNetwork() {
        val network = connectivityManager.activeNetwork
        if (network == null) {
            networks.clear()
            syncNetworkState()
            return
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
            networks[network] = mapNetworkModel(
                network = network,
                capabilities = capabilities,
                properties = connectivityManager.getLinkProperties(network),
                status = NetworkStatus.AVAILABLE
            )
        }

        syncNetworkState()
    }

    private fun mapNetworkModel(
        network: Network,
        capabilities: NetworkCapabilities? = null,
        properties: LinkProperties? = null,
        status: NetworkStatus? = null,
        maxMsToLive: Long? = null,
        from: NetworkModel? = null,
        assumeInternet: Boolean = false
    ): NetworkModel {
        val caps = capabilities
            ?: from?.networkCapabilities
            ?: connectivityManager.getNetworkCapabilities(network)

        val networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            else -> from?.type ?: NetworkType.UNKNOWN
        }

        val hasInternet = if (caps != null) {
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            from?.hasInternet ?: assumeInternet
        }

        val signalStrength =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                caps?.signalStrength
            } else {
                null
            } ?: from?.signalStrength ?: Int.MAX_VALUE

        return NetworkModel(
            id = network.hashCode(),
            type = networkType,
            original = network,
            hasInternet = hasInternet,
            signalStrength = signalStrength,
            status = status ?: from?.status ?: NetworkStatus.UNAVAILABLE,
            maxMsToLive = maxMsToLive ?: from?.maxMsToLive,
            networkCapabilities = caps,
            linkProperties = properties
                ?: from?.linkProperties
                ?: connectivityManager.getLinkProperties(network)
        )
    }
}


data class NetworkModel(
    val id: Int,
    val type: NetworkType,
    val original: Network,
    val hasInternet: Boolean,
    val signalStrength: Int,
    val status: NetworkStatus,
    val maxMsToLive: Long?,
    val networkCapabilities: NetworkCapabilities?,
    val linkProperties: LinkProperties?
) {
    fun isStatusOk(): Boolean = status.isOk()

    fun isInternetAvailable(): Boolean = hasInternet && isStatusOk()
}
