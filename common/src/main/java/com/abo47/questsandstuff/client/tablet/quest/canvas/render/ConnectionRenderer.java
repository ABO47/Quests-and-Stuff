package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public final class ConnectionRenderer {
    private ConnectionRenderer() {
    }

    public static String connectionKey(String sourceQuestId, String targetQuestId) {
        return QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.connectionColor(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static void setConnectionColor(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, int color) {
        ConnectionStateMutations.setConnectionColor(state, chapter, sourceQuestId, targetQuestId, color);
    }

    public static boolean isConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.isConnectionHidden(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static boolean isConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.isConnectionHidden(state, chapter, sourceQuestId, targetQuestId, target);
    }

    public static void setConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, boolean hidden) {
        ConnectionStateMutations.setConnectionHidden(state, chapter, sourceQuestId, targetQuestId, hidden);
    }

    public static void toggleConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.toggleConnectionHidden(state, chapter, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.isConnectionDirect(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static boolean isConnectionDirect(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.isConnectionDirect(state, chapter, sourceQuestId, targetQuestId, target);
    }

    public static void toggleConnectionMode(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.toggleConnectionMode(state, chapter, sourceQuestId, targetQuestId);
    }

    public static QuestConnectionMetadata connectionMetadata(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.metadata(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
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

    public static void drawTexturedChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, String textureStr, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ConnectionPainter.drawTexturedChevrons(graphics, path, color, alpha, scale, textureStr, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    public static CanvasExclusiveChoice findEc(TabletUiState state, String chapter, String id) {
        return ConnectionStyleResolver.findEc(state, chapter, id);
    }

    public static boolean isEcId(TabletUiState state, String chapter, String id) {
        return ConnectionStyleResolver.isEcId(state, chapter, id);
    }

    public static int ecConnectionColor(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.ecConnectionColor(state, chapter, sourceQuestId, targetQuestId);
    }

    public static boolean ecIsConnectionDirect(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.ecIsConnectionDirect(state, chapter, sourceQuestId, targetQuestId);
    }

    public static void setEcConnectionColor(TabletUiState state, String chapter, String ecId, String questId, int color) {
        ConnectionStateMutations.setEcConnectionColor(state, chapter, ecId, questId, color);
    }

    public static void setEcConnectionTexture(TabletUiState state, String chapter, String ecId, String questId, String texture) {
        ConnectionStateMutations.setEcConnectionTexture(state, chapter, ecId, questId, texture);
    }

    public static void setEcConnectionTextureSpacing(TabletUiState state, String chapter, String ecId, String questId, int spacing) {
        ConnectionStateMutations.setEcConnectionTextureSpacing(state, chapter, ecId, questId, spacing);
    }

    public static void setEcConnectionMode(TabletUiState state, String chapter, String ecId, String questId, boolean direct) {
        ConnectionStateMutations.setEcConnectionMode(state, chapter, ecId, questId, direct);
    }

    public static void setEcConnectionHidden(TabletUiState state, String chapter, String ecId, String questId, boolean hidden) {
        ConnectionStateMutations.setEcConnectionHidden(state, chapter, ecId, questId, hidden);
    }

    public static void setConnectionTexture(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, String texture) {
        ConnectionStateMutations.setConnectionTexture(state, chapter, sourceQuestId, targetQuestId, texture);
    }

    public static void setConnectionTextureSpacing(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, int spacing) {
        ConnectionStateMutations.setConnectionTextureSpacing(state, chapter, sourceQuestId, targetQuestId, spacing);
    }

    public static String connectionTexture(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.connectionTexture(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static String ecConnectionTexture(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.ecConnectionTexture(state, chapter, sourceQuestId, targetQuestId);
    }

    public static int ecConnectionTextureSpacing(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.ecConnectionTextureSpacing(state, chapter, sourceQuestId, targetQuestId);
    }

    public static void removeConnectionTransientState(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.removeConnectionTransientState(state, chapter, sourceQuestId, targetQuestId);
    }
}
