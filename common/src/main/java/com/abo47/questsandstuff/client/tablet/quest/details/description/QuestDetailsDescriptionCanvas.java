package com.abo47.questsandstuff.client.tablet.quest.details.description;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class QuestDetailsDescriptionCanvas extends WidgetGroup {
    private final TabletUiState state;
    private final String questId;
    private final QuestDetailsDescriptionSelection selection;
    private final QuestDetailsDescriptionEventRouter events;

    QuestDetailsDescriptionCanvas(int x, int y, int w, int h, TabletUiState state, Player player, Runnable refresh, String questId) {
        super(x, y, w, h);
        this.state = state;
        this.questId = questId;
        QuestDetailsDescriptionTransform transforms = new QuestDetailsDescriptionTransform(state, this::contentX, this::contentY, this::contentW, this::contentH);
        QuestDetailsDescriptionTextEdit textEdit = new QuestDetailsDescriptionTextEdit(state, refresh, questId, this::contentW, this::contentH);
        this.selection = new QuestDetailsDescriptionSelection(state, this::contentX, this::contentY, this::contentW, this::contentH);
        QuestDetailsDescriptionHitTest hitTest = new QuestDetailsDescriptionHitTest(state, selection, this::contentW, this::contentH);
        this.events = new QuestDetailsDescriptionEventRouter(
                state,
                player,
                refresh,
                questId,
                transforms,
                textEdit,
                selection,
                hitTest,
                new QuestDetailsDescriptionEventRouter.Surface() {
                    @Override
                    public boolean isMouseOverElement(double mouseX, double mouseY) {
                        return QuestDetailsDescriptionCanvas.this.isMouseOverElement(mouseX, mouseY);
                    }

                    @Override
                    public boolean mouseWheelMoveFallback(double mouseX, double mouseY, double wheelDelta) {
                        return QuestDetailsDescriptionCanvas.super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                    }

                    @Override
                    public boolean mouseClickedFallback(double mouseX, double mouseY, int button) {
                        return QuestDetailsDescriptionCanvas.super.mouseClicked(mouseX, mouseY, button);
                    }

                    @Override
                    public boolean mouseDraggedFallback(double mouseX, double mouseY, int button, double dragX, double dragY) {
                        return QuestDetailsDescriptionCanvas.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                    }

                    @Override
                    public boolean mouseReleasedFallback(double mouseX, double mouseY, int button) {
                        return QuestDetailsDescriptionCanvas.super.mouseReleased(mouseX, mouseY, button);
                    }

                    @Override
                    public boolean keyPressedFallback(int keyCode, int scanCode, int modifiers) {
                        return QuestDetailsDescriptionCanvas.super.keyPressed(keyCode, scanCode, modifiers);
                    }

                    @Override
                    public boolean charTypedFallback(char codePoint, int modifiers) {
                        return QuestDetailsDescriptionCanvas.super.charTyped(codePoint, modifiers);
                    }

                    @Override
                    public void focus(boolean focus) {
                        QuestDetailsDescriptionCanvas.this.setFocus(focus);
                    }

                    @Override
                    public int contentX() {
                        return QuestDetailsDescriptionCanvas.this.contentX();
                    }

                    @Override
                    public int contentY() {
                        return QuestDetailsDescriptionCanvas.this.contentY();
                    }

                    @Override
                    public int contentW() {
                        return QuestDetailsDescriptionCanvas.this.contentW();
                    }

                    @Override
                    public int contentH() {
                        return QuestDetailsDescriptionCanvas.this.contentH();
                    }
                }
        );
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
        withScissor(graphics, () -> {
            QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
            QuestDetailsDescriptionCanvasRenderer.drawContent(graphics, mouseX, mouseY, state, model, contentX(), contentY(), contentW(), contentH());
            selection.drawMultiSelectionBounds(graphics, model);
            selection.drawBoxSelection(graphics);
            drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
        });
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return events.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return events.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return events.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return events.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return events.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return events.mouseReleased(mouseX, mouseY, button);
    }

    private void withScissor(GuiGraphics graphics, Runnable draw) {
        CanvasViewportScissor.draw(graphics, contentX(), contentY(), contentW() + 1, contentH() + 1, draw);
    }

    private int contentX() {
        return getPositionX();
    }

    private int contentY() {
        return getPositionY();
    }

    private int contentW() {
        return Math.max(1, getSizeWidth() - 1);
    }

    private int contentH() {
        return Math.max(1, getSizeHeight() - 1);
    }
}
