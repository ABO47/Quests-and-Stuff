package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasContextMenuController {
    private CanvasContextMenuController() {
    }

    public static void renderCanvasContextMenu(CanvasViewport canvasViewport, TabletUiState state) {
        CanvasContextMenuRenderer.renderCanvasContextMenu(canvasViewport, state);
    }

    public static List<ContextAction> buildContextActions(CanvasViewport canvasViewport, TabletUiState state) {
        List<ContextAction> actions = new ArrayList<>();
        String selectedChapter = selectedChapterName(state);
        CanvasContextCanvasActions.addCanvasEmptyActions(actions, canvasViewport, state, canvasViewport.player(), selectedChapter);
        CanvasContextSelectionActions.addSelectionActions(actions, canvasViewport, state, canvasViewport.player(), selectedChapter);
        CanvasContextQuestActions.addQuestActions(actions, canvasViewport, state, canvasViewport.player(), selectedChapter);
        CanvasContextElementActions.addImageActions(actions, canvasViewport, state, selectedChapter);
        CanvasContextElementActions.addTextActions(actions, canvasViewport, state, selectedChapter);
        CanvasContextElementActions.addExclusiveChoiceActions(actions, canvasViewport, state, selectedChapter);
        CanvasContextConnectionActions.addConnectionActions(actions, canvasViewport, state, canvasViewport.player(), selectedChapter);
        if (!exclusiveSubmenuOpen(state)) {
            CanvasContextGlobalActions.addGlobalActions(actions, canvasViewport, state, canvasViewport.player(), selectedChapter);
        }
        return actions;
    }

    private static boolean exclusiveSubmenuOpen(TabletUiState state) {
        return state.contextMenu.contextQuestCompletionSoundMenuOpen;
    }

    public static List<ConnectionRef> selectedConnections(TabletUiState state, String chapter) {
        return CanvasConnectionSelection.selectedConnections(state, chapter);
    }

    public record ConnectionRef(String prerequisiteId, String questId) {
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasContextMenuSupport.isContextMenuHit(state, x, y);
    }

    public static boolean clickContextMenu(CanvasViewport canvasViewport, TabletUiState state, int x, int y) {
        return CanvasContextMenuSupport.clickContextMenu(canvasViewport, state, x, y);
    }

    public static void scrollContextMenu(TabletUiState state, double wheelDelta) {
        CanvasContextMenuSupport.scrollContextMenu(state, wheelDelta);
    }

    static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
