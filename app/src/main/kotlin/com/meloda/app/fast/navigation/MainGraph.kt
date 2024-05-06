package com.meloda.app.fast.navigation

import com.ramcosta.composedestinations.annotation.ExternalDestination
import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.generated.auth.navgraphs.AuthNavGraph
import com.ramcosta.composedestinations.generated.conversations.destinations.ConversationsDestination

@NavHostGraph
annotation class MainGraph {

    @ExternalNavGraph<AuthNavGraph>
    @ExternalDestination<ConversationsDestination>
    companion object Includes
}
