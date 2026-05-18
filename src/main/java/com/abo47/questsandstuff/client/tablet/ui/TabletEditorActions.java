package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

final class TabletEditorActions {
    private TabletEditorActions() {
    }

    static String selectedGroupName(TabletUiState state) {
        return EditorCommandClient.selectedGroupName(state);
    }

    static String uniqueGroupName(String preferred, String excludeCurrent) {
        return EditorCommandClient.uniqueGroupName(preferred, excludeCurrent);
    }

    static String sanitizeGroupName(String value) {
        return EditorCommandClient.sanitizeGroupName(value);
    }

    static void runGroupAction(Player player, TabletUiState state, String action, String group, String value, int offset) {
        EditorCommandClient.runGroupAction(player, state, action, group, value, offset);
    }

    static void runCanvasMoveAction(Player player, TabletUiState state, Map<String, CanvasPoint> positions) {
        EditorCommandClient.runCanvasMoveAction(player, state, positions);
    }

    static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        EditorCommandClient.runPrerequisiteAction(player, questId, prerequisiteId, add);
    }

    static void runQuestIconAction(Player player, String questId, String icon) {
        EditorCommandClient.runQuestIconAction(player, questId, icon);
    }

    static void runRemoveQuestAction(Player player, String questId) {
        EditorCommandClient.runRemoveQuestAction(player, questId);
    }

    static void addQuestAt(Player player, TabletUiState state, int logicalX, int logicalY, String title) {
        EditorCommandClient.addQuestAt(player, state, logicalX, logicalY, title);
    }

    static int snapToGrid(TabletUiState state, int value) {
        return CanvasRenderer.snapToGrid(state, value);
    }

    static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        return CanvasRenderer.hitTestCard(cards, x, y);
    }

    static EdgeHit hitTestEdge(TabletUiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        return CanvasRenderer.hitTestEdge(state, cards, byQuestId, x, y);
    }

    static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasRenderer.isContextMenuHit(state, x, y);
    }
}
