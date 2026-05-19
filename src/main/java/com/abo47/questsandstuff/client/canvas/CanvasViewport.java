package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
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
import java.util.List;
import java.util.Map;

public final class CanvasViewport extends WidgetGroup {
    private final TabletUiState state;
    private final Player player;
    private Runnable refresher = () -> {};
    private Runnable canvasRefresher = () -> {};
    private List<QuestCardLayout> cards = List.of();
    private Map<String, QuestCardLayout> byQuestId = Map.of();
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
        this.cards = List.copyOf(cards);
        this.byQuestId = Map.copyOf(byQuestId);
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
        canvasRefresher.run();
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
        if (animationFinished) {
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
