package me.rerere.rikkahub.brainypal.shared

data class BrainyPalDictationOcrReviewRow(
    val itemId: String,
    val title: String,
    val imageRef: String,
    val previewImageRef: String,
    val previewBoundingBox: BrainyPalDictationOcrBoundingBox?,
    val previewButtonLabel: String,
    val recognizedText: String,
    val resultLabel: String,
    val sourceRegionLabel: String,
    val confidenceLabel: String?,
    val guidanceLabel: String?,
    val requiresManualConfirmation: Boolean,
    val confirmationActions: List<BrainyPalDictationOcrConfirmationAction>,
) {
    val hasSourceRegionOverlay: Boolean
        get() = previewBoundingBox != null
}

data class BrainyPalDictationOcrConfirmationAction(
    val confirmation: String,
    val label: String,
)

object BrainyPalDictationOcrReview {
    private val confirmationActions = listOf(
        BrainyPalDictationOcrConfirmationAction(
            confirmation = "child_wrote_wrong",
            label = "孩子写错了",
        ),
        BrainyPalDictationOcrConfirmationAction(
            confirmation = "ocr_recognized_wrong",
            label = "OCR 识别错了",
        ),
        BrainyPalDictationOcrConfirmationAction(
            confirmation = "image_unclear",
            label = "照片不清楚",
        ),
        BrainyPalDictationOcrConfirmationAction(
            confirmation = "correct",
            label = "其实是对的",
        ),
    )

    fun rows(detail: BrainyPalChildPracticeTaskDetail): List<BrainyPalDictationOcrReviewRow> {
        if (detail.taskType != "dictation") return emptyList()
        return detail.items.mapIndexedNotNull { index, item ->
            val evidence = item.ocrEvidence ?: return@mapIndexedNotNull null
            BrainyPalDictationOcrReviewRow(
                itemId = item.itemId,
                title = "第 ${index + 1} 条",
                imageRef = evidence.imageRef,
                previewImageRef = previewImageRef(evidence),
                previewBoundingBox = previewBoundingBox(evidence),
                previewButtonLabel = previewButtonLabel(evidence),
                recognizedText = evidence.recognizedText.ifBlank { item.childAnswer.orEmpty() },
                resultLabel = resultLabel(item, evidence),
                sourceRegionLabel = sourceRegionLabel(evidence),
                confidenceLabel = evidence.confidence?.let { "置信度 ${(it * 100).toInt()}%" },
                guidanceLabel = guidanceLabel(item, evidence),
                requiresManualConfirmation = requiresManualConfirmation(item, evidence),
                confirmationActions = confirmationActions,
            )
        }
    }

    private fun resultLabel(
        item: BrainyPalChildPracticeTaskItem,
        evidence: BrainyPalDictationOcrEvidence,
    ): String {
        if (evidence.confirmationStatus == "confirmed") {
            return when (evidence.errorAttribution) {
                "correct" -> "已确认正确"
                "child_wrote_wrong" -> "已确认写错"
                "ocr_recognized_wrong" -> "OCR 识别错了"
                "image_unclear" -> "照片不清楚"
                else -> if (item.result == "correct") "已确认正确" else "已确认"
            }
        }
        return when (item.result) {
            "correct" -> "识别为正确"
            "incorrect" -> "已确认写错"
            "partial" -> "部分正确"
            "blank_or_low_effort" -> "空白或证据不足"
            "needs_manual_review" -> "需要确认"
            else -> "待批改"
        }
    }

    private fun sourceRegionLabel(evidence: BrainyPalDictationOcrEvidence): String {
        return when {
            evidence.cropRef != null -> "裁剪图"
            evidence.boundingBox != null -> "照片区域"
            else -> "原图"
        }
    }

    private fun previewImageRef(evidence: BrainyPalDictationOcrEvidence): String {
        return evidence.cropRef ?: evidence.imageRef
    }

    private fun previewBoundingBox(evidence: BrainyPalDictationOcrEvidence): BrainyPalDictationOcrBoundingBox? {
        return if (evidence.cropRef == null) evidence.boundingBox else null
    }

    private fun previewButtonLabel(evidence: BrainyPalDictationOcrEvidence): String {
        return when {
            evidence.cropRef != null -> "查看裁剪区域"
            evidence.boundingBox != null -> "查看照片区域"
            else -> "查看原图"
        }
    }

    private fun guidanceLabel(
        item: BrainyPalChildPracticeTaskItem,
        evidence: BrainyPalDictationOcrEvidence,
    ): String? {
        if (evidence.confirmationStatus == "confirmed") {
            return null
        }
        if (evidence.errorAttribution == "image_unclear") {
            return "照片可能不清楚，可以重拍或标记照片不清。"
        }
        if ((evidence.confidence ?: 1f) < 0.8f || item.result == "needs_manual_review") {
            return "识别不太确定，先看照片再确认。"
        }
        return null
    }

    private fun requiresManualConfirmation(
        item: BrainyPalChildPracticeTaskItem,
        evidence: BrainyPalDictationOcrEvidence,
    ): Boolean {
        return evidence.confirmationStatus != "confirmed"
    }
}
