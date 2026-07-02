package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

public record ChapterMetadataSnapshot(
        List<String> chapterOrder,
        Map<String, String> chapterIcons,
        Map<String, String> chapterBackgrounds,
        Map<String, String> chapterCanvasBackgrounds,
        Map<String, String> chapterTextAlign,
        Map<String, Integer> chapterTextColor,
        Map<String, String> chapterTextStyle,
        Map<String, Integer> chapterTextSize,
        Map<String, Boolean> chapterLockUntilUnlocked,
        Map<String, Boolean> chapterHideUntilUnlocked,
        Map<String, List<CanvasExclusiveChoice>> canvasExclusiveChoicesByChapter,
        Map<String, List<CanvasImageLayer>> canvasImagesByChapter,
        Map<String, List<CanvasTextLayer>> canvasTextsByChapter,
        Map<String, List<String>> canvasLayerOrderByChapter
) {
}
