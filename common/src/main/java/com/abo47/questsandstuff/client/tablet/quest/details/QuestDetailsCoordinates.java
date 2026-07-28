package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class QuestDetailsCoordinates {
    private QuestDetailsCoordinates() {
    }

    public static int localCoord(double mouse, int ownerPos, int size) {
        int fromAbsolute = (int) Math.round(mouse - ownerPos);
        if (fromAbsolute >= -2 && fromAbsolute <= size + 2) {
            return fromAbsolute;
        }
        return (int) Math.round(mouse);
    }

    public static int localX(TabletUiState state, double mouseX, int ownerX, int size) {
        return localAxis(mouseX, ownerX, screenX(state, ownerX), ownerLooksScreenX(state, ownerX), size);
    }

    public static int localY(TabletUiState state, double mouseY, int ownerY, int size) {
        return localAxis(mouseY, ownerY, screenY(state, ownerY), ownerLooksScreenY(state, ownerY), size);
    }

    public static int screenX(TabletUiState state, int ownerX) {
        if (state == null || ownerLooksScreenX(state, ownerX)) {
            return ownerX;
        }
        return state.questDetails.questDetailsScreenX + ownerX - state.questDetails.questDetailsX;
    }

    public static int screenY(TabletUiState state, int ownerY) {
        if (state == null || ownerLooksScreenY(state, ownerY)) {
            return ownerY;
        }
        return state.questDetails.questDetailsScreenY + ownerY - state.questDetails.questDetailsY;
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
        QuestDetailsTransientManager.openContext(
                state,
                kind,
                id,
                modalX(state, mouseX, ownerX, localX),
                modalY(state, mouseY, ownerY, localY)
        );
        EntityMotionEditor.close(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details context open kind={} id={} pos={},{} owner={},{} pointer={},{}",
                kind,
                state.questDetails.questDetailsContextId.isBlank() ? "<none>" : state.questDetails.questDetailsContextId,
                state.questDetails.questDetailsContextX,
                state.questDetails.questDetailsContextY,
                ownerX,
                ownerY,
                Math.round(mouseX),
                Math.round(mouseY));
    }

    private static int modalX(TabletUiState state, double mouseX, int ownerX, int localX) {
        int fromPointer = (int) Math.round(mouseX) - modalScreenX(state);
        if (fromPointer >= -2 && fromPointer <= state.questDetails.questDetailsW + 2) {
            return fromPointer;
        }
        return screenX(state, ownerX) - modalScreenX(state) + localX;
    }

    private static int modalY(TabletUiState state, double mouseY, int ownerY, int localY) {
        int fromPointer = (int) Math.round(mouseY) - modalScreenY(state);
        if (fromPointer >= -2 && fromPointer <= state.questDetails.questDetailsH + 2) {
            return fromPointer;
        }
        return screenY(state, ownerY) - modalScreenY(state) + localY;
    }

    private static int modalScreenX(TabletUiState state) {
        return state == null ? 0 : state.questDetails.questDetailsScreenX;
    }

    private static int modalScreenY(TabletUiState state) {
        return state == null ? 0 : state.questDetails.questDetailsScreenY;
    }

    private static int localAxis(double mouse, int ownerPos, int projectedScreenPos, boolean ownerLooksScreen, int size) {
        int direct = (int) Math.round(mouse - ownerPos);
        if (insideLoose(direct, size)) {
            return direct;
        }
        int projected = (int) Math.round(mouse - projectedScreenPos);
        if (insideLoose(projected, size)) {
            return projected;
        }
        return ownerLooksScreen ? direct : projected;
    }

    private static boolean insideLoose(int value, int size) {
        return value >= -2 && value <= size + 2;
    }

    private static boolean ownerLooksScreenX(TabletUiState state, int ownerX) {
        if (state == null) {
            return true;
        }
        return ownerX >= state.questDetails.questDetailsScreenX - 2
                && ownerX <= state.questDetails.questDetailsScreenX + Math.max(1, state.questDetails.questDetailsW) + 2;
    }

    private static boolean ownerLooksScreenY(TabletUiState state, int ownerY) {
        if (state == null) {
            return true;
        }
        return ownerY >= state.questDetails.questDetailsScreenY - 2
                && ownerY <= state.questDetails.questDetailsScreenY + Math.max(1, state.questDetails.questDetailsH) + 2;
    }
}
