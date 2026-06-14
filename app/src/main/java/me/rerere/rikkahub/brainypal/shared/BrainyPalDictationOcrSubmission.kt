package me.rerere.rikkahub.brainypal.shared

object BrainyPalDictationOcrSubmission {
    fun request(
        detail: BrainyPalChildPracticeTaskDetail,
        imageRef: String,
        rawOcrText: String,
    ): BrainyPalSubmitDictationOcrEvidenceRequest {
        val lines = recognizedLines(rawOcrText)
        val items = detail.items
            .zip(lines)
            .map { (item, line) ->
                BrainyPalSubmitDictationOcrEvidenceItemRequest(
                    itemId = item.itemId,
                    recognizedText = line,
                )
            }
        return BrainyPalSubmitDictationOcrEvidenceRequest(
            imageRef = imageRef,
            recognizedText = lines.joinToString("\n"),
            items = items,
        )
    }

    fun recognizedLines(rawOcrText: String): List<String> {
        return rawOcrText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("<") && it.endsWith(">") }
            .filterNot { it.startsWith("* The image_file_ocr tag") }
            .toList()
    }
}
