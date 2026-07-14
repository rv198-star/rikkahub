package me.rerere.rikkahub.ui.brainypal.chat

import android.net.Uri
import androidx.compose.runtime.Composable
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalTheme
import me.rerere.rikkahub.ui.pages.chat.ChatPage
import kotlin.uuid.Uuid

@Composable
fun BrainyPalChatPage(
    id: Uuid,
    text: String?,
    files: List<Uri>,
    nodeId: Uuid? = null,
) {
    BrainyPalTheme {
        ChatPage(
            id = id,
            text = text,
            files = files,
            nodeId = nodeId,
            childMode = true,
        )
    }
}
