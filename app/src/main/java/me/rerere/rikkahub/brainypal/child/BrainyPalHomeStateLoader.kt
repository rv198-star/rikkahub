package me.rerere.rikkahub.brainypal.child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildApi
import me.rerere.rikkahub.brainypal.child.BrainyPalChildHomeState
import me.rerere.rikkahub.data.datastore.Settings

suspend fun Flow<Settings>.loadBrainyPalChildHomeState(
    apiFactory: (rootUrl: String, apiKey: String) -> BrainyPalChildApi,
    chatScreen: Screen,
): BrainyPalChildHomeState {
    return brainyPalChildHomeStates(
        apiFactory = apiFactory,
        chatScreen = chatScreen,
    ).first()
}

fun Flow<Settings>.brainyPalChildHomeStates(
    apiFactory: (rootUrl: String, apiKey: String) -> BrainyPalChildApi,
    chatScreen: Screen,
): Flow<BrainyPalChildHomeState> {
    return filter { !it.init }
        .map { it.brainyPalChildConnection }
        .distinctUntilChanged()
        .map { connection ->
            BrainyPalChildHomeState.from(
                connection = connection,
                apiFactory = apiFactory,
                chatScreen = chatScreen,
            )
        }
}
