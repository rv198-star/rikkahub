package me.rerere.rikkahub.ui.pages.brainypal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildApi
import me.rerere.rikkahub.brainypal.BrainyPalChildHomeState
import me.rerere.rikkahub.data.datastore.Settings

suspend fun Flow<Settings>.loadBrainyPalChildHomeState(
    apiFactory: (rootUrl: String, apiKey: String) -> BrainyPalChildApi,
    chatScreen: Screen,
): BrainyPalChildHomeState {
    val settings = filter { !it.init }.first()
    return BrainyPalChildHomeState.from(
        connection = settings.brainyPalChildConnection,
        apiFactory = apiFactory,
        chatScreen = chatScreen,
    )
}
