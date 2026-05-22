package com.abo47.questsandstuff.quest.editor.canvas;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;

import java.util.Map;

public final class EditorPlacementService {
    private EditorPlacementService() {
    }

    public static int[] findNearestFreePosition(Map<String, QuestDefinition> quests, String group, int x, int y, int step) {
        int grid = Math.max(1, step);
        int baseX = Math.round((float) x / (float) grid) * grid;
        int baseY = Math.round((float) y / (float) grid) * grid;
        if (!isOccupiedInGroup(quests, group, baseX, baseY)) {
            return new int[]{baseX, baseY};
        }
        for (int i = 1; i <= 128; i++) {
            int right = baseX + i * grid;
            if (!isOccupiedInGroup(quests, group, right, baseY)) {
                return new int[]{right, baseY};
            }
            int down = baseY + i * grid;
            if (!isOccupiedInGroup(quests, group, baseX, down)) {
                return new int[]{baseX, down};
            }
            int left = baseX - i * grid;
            if (!isOccupiedInGroup(quests, group, left, baseY)) {
                return new int[]{left, baseY};
            }
            int up = baseY - i * grid;
            if (!isOccupiedInGroup(quests, group, baseX, up)) {
                return new int[]{baseX, up};
            }
        }
        return new int[]{baseX, baseY};
    }

    private static boolean isOccupiedInGroup(Map<String, QuestDefinition> quests, String group, int x, int y) {
        for (QuestDefinition definition : quests.values()) {
            ChapterDefinition view = definition.display().groups().get(group);
            if (view != null && view.x() == x && view.y() == y) {
                return true;
            }
        }
        return false;
    }
}
