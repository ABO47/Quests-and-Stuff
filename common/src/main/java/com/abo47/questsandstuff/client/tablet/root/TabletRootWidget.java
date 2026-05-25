package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class TabletRootWidget extends WidgetGroup {
    private final TabletUiState state;
    private WidgetGroup modalLayer;
    private WidgetGroup frontWindowLayer;
    private CanvasViewport canvasViewport;
    private Runnable refresher = () -> {
    };
    private Runnable undoAction = () -> {
    };
    private Runnable redoAction = () -> {
    };

    public TabletRootWidget(int x, int y, int width, int height, TabletUiState state) {
        super(x, y, width, height);
        this.state = state;
    }

    public void setRefresher(Runnable refresher) {
        this.refresher = refresher == null ? () -> {
        } : refresher;
    }

    public void setUndoRedoActions(Runnable undoAction, Runnable redoAction) {
        this.undoAction = undoAction == null ? () -> {
        } : undoAction;
        this.redoAction = redoAction == null ? () -> {
        } : redoAction;
    }

    public void setModalLayer(WidgetGroup modalLayer) {
        this.modalLayer = modalLayer;
    }

    public void setFrontWindowLayer(WidgetGroup frontWindowLayer) {
        this.frontWindowLayer = frontWindowLayer;
    }

    public void setCanvasViewport(CanvasViewport canvasViewport) {
        this.canvasViewport = canvasViewport;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.BACKGROUND, (g, x, y, t) -> super.drawInBackground(g, x, y, t));
    }

    @Override
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.FOREGROUND, (g, x, y, t) -> super.drawInForeground(g, x, y, t));
    }

    @Override
    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.OVERLAY, (g, x, y, t) -> super.drawOverlay(g, x, y, t));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return TabletRootPointerRouter.mouseClicked(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b) -> super.mouseClicked(x, y, b), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return TabletRootPointerRouter.mouseDragged(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b, dx, dy) -> super.mouseDragged(x, y, b, dx, dy), mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return TabletRootPointerRouter.mouseReleased(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b) -> super.mouseReleased(x, y, b), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return TabletRootPointerRouter.mouseWheelMove(this, state, modalLayer, frontWindowLayer, (x, y, d) -> super.mouseWheelMove(x, y, d), mouseX, mouseY, wheelDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return TabletRootKeyboardRouter.keyPressed(this, state, modalLayer, frontWindowLayer, canvasViewport, refresher, undoAction, redoAction,
                (key, scan, mod) -> super.keyPressed(key, scan, mod), keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return TabletRootKeyboardRouter.keyReleased(this, state, refresher, (key, scan, mod) -> super.keyReleased(key, scan, mod), keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return TabletRootKeyboardRouter.charTyped(this, state, modalLayer, frontWindowLayer, refresher, (typed, mod) -> super.charTyped(typed, mod), c, modifiers);
    }

    Player resolvePlayer() {
        return Minecraft.getInstance().player;
    }

    boolean isAnyModalOpen() {
        return ModalStateQueries.anyOpen(state);
    }

    boolean isFrontWindowOpen() {
        return QuestDetailsWindow.isVisible(state) && frontWindowLayer != null;
    }
}
