package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

public record ChapterMetadataSnapshot(
        List<String> groupOrder,
        Map<String, String> groupIcons,
        Map<String, String> groupBackgrounds,
        Map<String, String> groupCanvasBackgrounds,
        Map<String, String> groupTextAlign,
        Map<String, Integer> groupTextColor,
        Map<String, String> groupTextStyle,
        Map<String, Integer> groupTextSize,
        Map<String, Boolean> groupLockUntilUnlocked,
        Map<String, Boolean> groupHideUntilUnlocked,
        Map<String, List<CanvasImageLayer>> canvasImagesByGroup,
        Map<String, List<CanvasTextLayer>> canvasTextsByGroup,
        Map<String, List<String>> canvasLayerOrderByGroup
) {
}
