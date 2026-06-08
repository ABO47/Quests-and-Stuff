package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public final class ConnectionRenderer {
    private ConnectionRenderer() {
    }

    public static String edgeKey(String sourceQuestId, String targetQuestId) {
        return QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.connectionColor(state, group, sourceQuestId, targetQuestId);
    }

    public static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.connectionColor(state, group, sourceQuestId, targetQuestId, target);
    }

    public static void setConnectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, int color) {
        ConnectionStateMutations.setConnectionColor(state, group, sourceQuestId, targetQuestId, color);
    }

    public static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.isConnectionHidden(state, group, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.isConnectionHidden(state, group, sourceQuestId, targetQuestId, target);
    }

    public static void setConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, boolean hidden) {
        ConnectionStateMutations.setConnectionHidden(state, group, sourceQuestId, targetQuestId, hidden);
    }

    public static void toggleConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.toggleConnectionHidden(state, group, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.isConnectionDirect(state, group, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.isConnectionDirect(state, group, sourceQuestId, targetQuestId, target);
    }

    public static void toggleConnectionMode(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.toggleConnectionMode(state, group, sourceQuestId, targetQuestId);
    }

    public static QuestConnectionMetadata connectionMetadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.metadata(state, group, sourceQuestId, targetQuestId);
    }

    public static QuestConnectionMetadata connectionMetadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.metadata(state, group, sourceQuestId, targetQuestId, target);
    }

    public static void renderPrerequisiteConnections(
            WidgetGroup canvasViewport,
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = ConnectionLayout.prerequisiteAndPendingConnectionLines(state, cards, byQuestId, viewportW, viewportH);
        ConnectionPainter.renderConnectionLines(canvasViewport, state, lines);
    }

    public static List<ConnectionLine> prerequisiteConnectionLines(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        return ConnectionLayout.prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH);
    }

    public static List<String> prerequisiteConnectionLayerKeys(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        return ConnectionLayout.prerequisiteConnectionLayerKeys(state, cards, byQuestId, viewportW, viewportH);
    }

    public static void renderConnectionLayer(WidgetGroup canvasViewport, TabletUiState state, ConnectionLine line) {
        ConnectionPainter.renderConnectionLines(canvasViewport, state, line == null ? List.of() : List.of(line));
    }

    public static void renderPendingConnections(
            WidgetGroup canvasViewport,
            TabletUiState state,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        ConnectionPainter.renderConnectionLines(canvasViewport, state, ConnectionLayout.pendingConnectionLines(state, byQuestId, viewportW, viewportH));
    }

    public static List<CanvasPoint> connectionPath(TabletUiState state, int originX, int originY, int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        return ConnectionPainter.connectionPath(state, originX, originY, sourceX, sourceY, targetX, targetY, direct);
    }

    public static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ConnectionPainter.drawStaticChevrons(graphics, path, color, alpha, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    public static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ConnectionPainter.drawStaticChevrons(graphics, path, color, alpha, scale, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }
}
