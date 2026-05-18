package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class QuestDetailsMouse {
    private QuestDetailsMouse() {
    }

    public static int localCoord(double mouse, int ownerPos, int size) {
        int fromAbsolute = (int) Math.round(mouse - ownerPos);
        if (fromAbsolute >= -2 && fromAbsolute <= size + 2) {
            return fromAbsolute;
        }
        return (int) Math.round(mouse);
    }

    public static void openContextAtPointer(
            TabletUiState state,
            String kind,
            String id,
            double mouseX,
            double mouseY,
            int ownerX,
            int ownerY,
            int localX,
            int localY
    ) {
        QuestDetailsTransientState.openContext(
                state,
                kind,
                id,
                modalX(state, mouseX, ownerX, localX),
                modalY(state, mouseY, ownerY, localY)
        );
        EntityMotionEditor.close(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details context open kind={} id={} pos={},{} owner={},{} pointer={},{}",
                kind,
                state.questDetailsContextId.isBlank() ? "<none>" : state.questDetailsContextId,
                state.questDetailsContextX,
                state.questDetailsContextY,
                ownerX,
                ownerY,
                Math.round(mouseX),
                Math.round(mouseY));
    }

    private static int modalX(TabletUiState state, double mouseX, int ownerX, int localX) {
        return (int) Math.round(mouseX) - modalScreenX(state);
    }

    private static int modalY(TabletUiState state, double mouseY, int ownerY, int localY) {
        return (int) Math.round(mouseY) - modalScreenY(state);
    }

    private static int modalScreenX(TabletUiState state) {
        if (state.questDetailsScreenX != state.questDetailsX || state.questDetailsScreenY != state.questDetailsY) {
            return state.questDetailsScreenX;
        }
        return state.questDetailsX;
    }

    private static int modalScreenY(TabletUiState state) {
        if (state.questDetailsScreenX != state.questDetailsX || state.questDetailsScreenY != state.questDetailsY) {
            return state.questDetailsScreenY;
        }
        return state.questDetailsY;
    }
}
