package dev.meloda.fast.data.api.stickers

import dev.meloda.fast.network.service.stickers.StickersService

class StickersRepository(
    private val stickersService: StickersService
) {

    suspend fun getProducts(
        type: String = "stickers",
        filters: String = "purchased,active",
        extended: Boolean = true,
        count: Int = 100
    ) = stickersService.getProducts(
        mapOf(
            "type" to type,
            "filters" to filters,
            "extended" to if (extended) "1" else "0",
            "count" to count.toString()
        )
    )
}
