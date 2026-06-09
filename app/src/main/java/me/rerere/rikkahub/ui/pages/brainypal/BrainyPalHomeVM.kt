package me.rerere.rikkahub.ui.pages.brainypal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildApiFactory
import me.rerere.rikkahub.brainypal.BrainyPalChildHomeState
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.utils.UiState
import kotlin.uuid.Uuid

class BrainyPalHomeVM(
    private val settingsStore: SettingsStore,
    private val apiFactory: BrainyPalChildApiFactory,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<BrainyPalChildHomeState>>(UiState.Loading)
    val state: StateFlow<UiState<BrainyPalChildHomeState>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = UiState.Success(
                settingsStore.settingsFlow.loadBrainyPalChildHomeState(
                    apiFactory = apiFactory::create,
                    chatScreen = Screen.Chat(
                        id = Uuid.random().toString(),
                    ),
                )
            )
        }
    }
}
