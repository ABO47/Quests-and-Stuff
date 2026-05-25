package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasChapterSwitchAnimation;
import com.abo47.questsandstuff.client.canvas.render.CanvasConnectionAnimation;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CanvasViewport extends WidgetGroup {
    private final TabletUiState state;
    private final Player player;
    private Runnable refresher = () -> {};
    private Runnable canvasRefresher = () -> {};
    private List<QuestCardLayout> cards = List.of();
    private Map<String, QuestCardLayout> byQuestId = Map.of();
    private WidgetGroup canvasContentLayer;
    private final Map<String, WidgetGroup> questCardLayers = new HashMap<>();
    private final Map<String, LayerPosition> selectionQuestLayerBases = new HashMap<>();
    private int canvasContentLayerBaseX;
    private int canvasContentLayerBaseY;
    private int livePanX;
    private int livePanY;
    private boolean canvasRefreshQueued;
    private final CanvasInlineTextEditor textEditor;
    private final CanvasElementTransformController elementTransforms;
    private final CanvasSelectionTransformController selectionTransforms;

    public CanvasViewport(int x, int y, int width, int height, TabletUiState state, Player player) {
        super(x, y, width, height);
        this.state = state;
        this.player = player;
        this.textEditor = new CanvasInlineTextEditor(this, state, this::refresh);
        this.elementTransforms = new CanvasElementTransformController(state);
        this.selectionTransforms = new CanvasSelectionTransformController(state, elementTransforms);
    }

    public void setRefresher(Runnable refresher) {
        this.refresher = refresher == null ? () -> {} : refresher;
    }

    public void setCanvasRefresher(Runnable canvasRefresher) {
        this.canvasRefresher = canvasRefresher == null ? () -> {} : canvasRefresher;
    }

    public void updateCardCache(List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId) {
        this.cards = shiftedCards(cards, livePanX, livePanY);
        this.byQuestId = shiftedLookup(byQuestId, livePanX, livePanY);
    }

    public List<QuestCardLayout> cardCache() {
        return cards;
    }

    public Map<String, QuestCardLayout> cardLookup() {
        return byQuestId;
    }

    public Player player() {
        return player;
    }

    public void refresh() {
        refresher.run();
    }

    public void refreshCanvas() {
        canvasRefreshQueued = false;
        canvasRefresher.run();
    }

    void queueCanvasRefresh() {
        canvasRefreshQueued = true;
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
        canvasContentLayer = null;
        questCardLayers.clear();
        selectionQuestLayerBases.clear();
        canvasContentLayerBaseX = 0;
        canvasContentLayerBaseY = 0;
    }

    void registerQuestCardLayer(String questId, WidgetGroup layer) {
        if (questId == null || questId.isBlank() || layer == null) {
            return;
        }
        questCardLayers.put(questId, layer);
    }

    void setCanvasContentLayer(WidgetGroup canvasContentLayer) {
        this.canvasContentLayer = canvasContentLayer;
        if (canvasContentLayer == null) {
            canvasContentLayerBaseX = 0;
            canvasContentLayerBaseY = 0;
            return;
        }
        canvasContentLayerBaseX = canvasContentLayer.getSelfPositionX();
        canvasContentLayerBaseY = canvasContentLayer.getSelfPositionY();
        applyLivePanToLayer();
    }

    void beginCanvasPan() {
        if (livePanX != 0 || livePanY != 0) {
            shiftCardCache(-livePanX, -livePanY);
        }
        livePanX = 0;
        livePanY = 0;
        state.canvasLivePanX = 0;
        state.canvasLivePanY = 0;
        applyLivePanToLayer();
    }

    boolean previewCanvasPan(int dx, int dy) {
        if (dx == 0 && dy == 0) {
            return true;
        }
        if (canvasContentLayer == null) {
            return false;
        }
        livePanX += dx;
        livePanY += dy;
        state.canvasLivePanX = livePanX;
        state.canvasLivePanY = livePanY;
        applyLivePanToLayer();
        shiftCardCache(dx, dy);
        return true;
    }

    boolean panPreviewNeedsRefresh() {
        int thresholdX = Math.max(1, (CanvasLayoutService.panRenderOverscanX(getSizeWidth()) * 3) / 4);
        int thresholdY = Math.max(1, (CanvasLayoutService.panRenderOverscanY(getSizeHeight()) * 3) / 4);
        return Math.abs(livePanX) >= thresholdX || Math.abs(livePanY) >= thresholdY;
    }

    void beginSelectionDragPreview() {
        selectionQuestLayerBases.clear();
    }

    boolean selectionDragPreviewSupported() {
        return state.selectedCanvasImageId.isBlank()
                && state.selectedCanvasTextId.isBlank()
                && state.selectedCanvasImageIds.isEmpty()
                && state.selectedCanvasTextIds.isEmpty();
    }

    boolean previewSelectionDrag() {
        if (!selectionDragPreviewSupported() || state.selectedQuestIds.isEmpty()) {
            return false;
        }
        ensureSelectionLayerBases();
        if (selectionQuestLayerBases.isEmpty()) {
            return false;
        }
        int dx = selectionDragScreenX();
        int dy = selectionDragScreenY();
        for (Map.Entry<String, LayerPosition> entry : selectionQuestLayerBases.entrySet()) {
            WidgetGroup layer = questCardLayers.get(entry.getKey());
            if (layer != null) {
                LayerPosition base = entry.getValue();
                layer.setSelfPosition(base.x() + dx, base.y() + dy);
            }
        }
        previewSelectionBounds(dx, dy);
        return true;
    }

    void endSelectionDragPreview() {
        selectionQuestLayerBases.clear();
    }

    private void ensureSelectionLayerBases() {
        if (!selectionQuestLayerBases.isEmpty()) {
            return;
        }
        for (String questId : state.selectedQuestIds) {
            WidgetGroup layer = questCardLayers.get(questId);
            if (layer != null) {
                selectionQuestLayerBases.put(questId, new LayerPosition(layer.getSelfPositionX(), layer.getSelfPositionY()));
            }
        }
    }

    private void previewSelectionBounds(int dx, int dy) {
        if (state.dragStartSelectionRight <= state.dragStartSelectionLeft
                || state.dragStartSelectionBottom <= state.dragStartSelectionTop) {
            return;
        }
        state.selectionBoundsVisible = true;
        state.selectionBoundsLeft = state.dragStartSelectionLeft + dx;
        state.selectionBoundsTop = state.dragStartSelectionTop + dy;
        state.selectionBoundsRight = state.dragStartSelectionRight + dx;
        state.selectionBoundsBottom = state.dragStartSelectionBottom + dy;
    }

    private int selectionDragScreenX() {
        return CanvasGeometry.screenX(state, state.dragStartBoundsLeft + state.dragSelectionDeltaX)
                - CanvasGeometry.screenX(state, state.dragStartBoundsLeft);
    }

    private int selectionDragScreenY() {
        return CanvasGeometry.screenY(state, state.dragStartBoundsTop + state.dragSelectionDeltaY)
                - CanvasGeometry.screenY(state, state.dragStartBoundsTop);
    }

    void commitCanvasPan() {
        if (livePanX == 0 && livePanY == 0) {
            return;
        }
        state.canvasOffsetX += livePanX;
        state.canvasOffsetY += livePanY;
        livePanX = 0;
        livePanY = 0;
        state.canvasLivePanX = 0;
        state.canvasLivePanY = 0;
        applyLivePanToLayer();
    }

    private void applyLivePanToLayer() {
        if (canvasContentLayer == null) {
            return;
        }
        canvasContentLayer.setSelfPosition(canvasContentLayerBaseX + livePanX, canvasContentLayerBaseY + livePanY);
    }

    private void shiftCardCache(int dx, int dy) {
        if (cards.isEmpty()) {
            return;
        }
        List<QuestCardLayout> shifted = new ArrayList<>(cards.size());
        Map<String, QuestCardLayout> shiftedByQuestId = new HashMap<>();
        for (QuestCardLayout card : cards) {
            QuestCardLayout shiftedCard = shiftCard(card, dx, dy);
            shifted.add(shiftedCard);
            shiftedByQuestId.put(shiftedCard.questId(), shiftedCard);
        }
        cards = List.copyOf(shifted);
        byQuestId = Map.copyOf(shiftedByQuestId);
    }

    private static List<QuestCardLayout> shiftedCards(List<QuestCardLayout> cards, int dx, int dy) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }
        if (dx == 0 && dy == 0) {
            return List.copyOf(cards);
        }
        List<QuestCardLayout> shifted = new ArrayList<>(cards.size());
        for (QuestCardLayout card : cards) {
            shifted.add(shiftCard(card, dx, dy));
        }
        return List.copyOf(shifted);
    }

    private static Map<String, QuestCardLayout> shiftedLookup(Map<String, QuestCardLayout> byQuestId, int dx, int dy) {
        if (byQuestId == null || byQuestId.isEmpty()) {
            return Map.of();
        }
        if (dx == 0 && dy == 0) {
            return Map.copyOf(byQuestId);
        }
        Map<String, QuestCardLayout> shifted = new HashMap<>();
        for (Map.Entry<String, QuestCardLayout> entry : byQuestId.entrySet()) {
            shifted.put(entry.getKey(), shiftCard(entry.getValue(), dx, dy));
        }
        return Map.copyOf(shifted);
    }

    private static QuestCardLayout shiftCard(QuestCardLayout card, int dx, int dy) {
        return new QuestCardLayout(
                card.questId(),
                card.tag(),
                card.logicalX(),
                card.logicalY(),
                card.logicalWidth(),
                card.logicalHeight(),
                card.slotLogicalWidth(),
                card.slotLogicalHeight(),
                card.visualLogicalX(),
                card.visualLogicalY(),
                card.scale(),
                card.x() + dx,
                card.y() + dy,
                card.width(),
                card.height()
        );
    }

    private record LayerPosition(int x, int y) {
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
        CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        boolean animationFinished = CanvasMinimapController.finishAnimationIfDone(state);
        animationFinished |= CanvasConnectionAnimation.finishIfDone(state);
        animationFinished |= CanvasChapterSwitchAnimation.finishIfDone(state);
        if (animationFinished || canvasRefreshQueued) {
            canvasRefreshQueued = false;
            refreshCanvas();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return CanvasViewportClickController.mouseClicked(this, state, player, refresher, cards, byQuestId, textEditor, elementTransforms, selectionTransforms, mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (EntityMotionEditor.isMainCanvasOpen(state) && super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (textEditor.handleKeyPressed(keyCode)) {
            return true;
        }
        if (state.canEdit && isCtrlDown() && keyCode == GLFW.GLFW_KEY_C) {
            if (CanvasClipboardController.copySelectionToClipboard(this, state)) {
                refresher.run();
            }
            return true;
        }
        if (state.canEdit && isCtrlDown() && keyCode == GLFW.GLFW_KEY_V) {
            if (CanvasClipboardController.pasteNearSelectionOrViewportCenter(player, state, this)) {
                refresher.run();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (EntityMotionEditor.isMainCanvasOpen(state) && super.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (textEditor.handleCharTyped(codePoint)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return CanvasViewportInputController.mouseDragged(this, state, refresher, cards, byQuestId, textEditor, elementTransforms, selectionTransforms, mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return CanvasViewportInputController.mouseReleased(this, state, player, refresher, cards, textEditor, selectionTransforms, mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return CanvasViewportInputController.mouseWheelMove(this, state, refresher, textEditor, mouseX, mouseY, wheelDelta);
    }

    boolean callSuperMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    boolean callSuperMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    boolean callSuperMouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    boolean callSuperMouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    boolean shiftDown() {
        return isShiftDown();
    }

    boolean ctrlDown() {
        return isCtrlDown();
    }
}
