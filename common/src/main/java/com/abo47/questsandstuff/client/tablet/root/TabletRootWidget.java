package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.SkinEditOverlay;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
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
    private ButtonWidget homeBtn;

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

    public void setHomeButton(ButtonWidget btn) {
        this.homeBtn = btn;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawFullscreenBackdrop(graphics);
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.BACKGROUND, (g, x, y, t) -> super.drawInBackground(g, x, y, t));
        if (homeBtn != null) {
            homeBtn.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
        if (state != null && state.root.skinEditMode) {
            SkinEditOverlay.draw(graphics, this, mouseX, mouseY);
        }
    }

    @Override
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.FOREGROUND, (g, x, y, t) -> super.drawInForeground(g, x, y, t));
        if (homeBtn != null) {
            homeBtn.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.OVERLAY, (g, x, y, t) -> super.drawOverlay(g, x, y, t));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (state != null && state.root.skinEditMode) return true;
        if (homeBtn != null && homeBtn.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return TabletRootPointerRouter.mouseClicked(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b) -> super.mouseClicked(x, y, b), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state != null && state.root.skinEditMode) return true;
        return TabletRootPointerRouter.mouseDragged(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b, dx, dy) -> super.mouseDragged(x, y, b, dx, dy), mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (state != null && state.root.skinEditMode) return true;
        return TabletRootPointerRouter.mouseReleased(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b) -> super.mouseReleased(x, y, b), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (state != null && state.root.skinEditMode) return true;
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

    private void drawFullscreenBackdrop(GuiGraphics graphics) {
        if (state == null || !state.root.fullScreenMode) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        if (screenW <= 0 || screenH <= 0) {
            return;
        }
        int rootX = getPosition().x;
        int rootY = getPosition().y;
        int rootW = getSize().width;
        int rootH = getSize().height;
        int fill = ModColors.SURFACE_BASE;
        Surfaces.fill(fill).draw(graphics, 0, 0, 0, 0, rootX, screenH);
        Surfaces.fill(fill).draw(graphics, 0, 0, rootX + rootW, 0, screenW - (rootX + rootW), screenH);
        Surfaces.fill(fill).draw(graphics, 0, 0, rootX, 0, rootW, rootY);
        Surfaces.fill(fill).draw(graphics, 0, 0, rootX, rootY + rootH, rootW, screenH - (rootY + rootH));
    }
}
