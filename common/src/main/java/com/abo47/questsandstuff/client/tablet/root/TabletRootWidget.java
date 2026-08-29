package com.abo47.questsandstuff.client.tablet.root;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditRenderer;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

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
    private WidgetGroup contextMenuRoot;
    private int contextMenuRootX;
    private int contextMenuRootY;
    private int contextMenuRootW;
    private int contextMenuRootH;

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

    public CanvasViewport getCanvasViewport() {
        return canvasViewport;
    }

    public void setHomeButton(ButtonWidget btn) {
        this.homeBtn = btn;
    }

    public ButtonWidget getHomeButton() {
        return homeBtn;
    }

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
        if (frontWindowLayer != null) {
            frontWindowLayer.setGui(gui);
        }
        if (state != null && state.root.editorPopup != null) {
            state.root.editorPopup.setGui(gui);
        }
        if (contextMenuRoot != null) {
            contextMenuRoot.setGui(gui);
        }
    }

    public boolean isContextMenuOpen() {
        return contextMenuRoot != null;
    }

    public boolean isContextMenuAt(int x, int y) {
        if (contextMenuRoot == null) return false;
        if (contextMenuRoot.isMouseOverElement(x, y)) return true;
        return isInContextMenuTree(contextMenuRoot, x, y);
    }

    private static boolean isInContextMenuTree(WidgetGroup group, int x, int y) {
        for (Widget child : group.widgets) {
            if (!child.isVisible() || !child.isActive()) continue;
            if (child.isMouseOverElement(x, y)) return true;
            if (child instanceof WidgetGroup wg && isInContextMenuTree(wg, x, y)) {
                return true;
            }
        }
        return false;
    }

    public void clickContextMenu(int mouseX, int mouseY, int button) {
        if (contextMenuRoot != null) {
            contextMenuRoot.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void closeContextMenu() {
        contextMenuRoot = null;
    }

    public void setContextMenu(WidgetGroup menu, int x, int y, int w, int h) {
        contextMenuRoot = menu;
        if (contextMenuRoot != null && getGui() != null) {
            contextMenuRoot.setGui(getGui());
        }
        contextMenuRootX = x;
        contextMenuRootY = y;
        contextMenuRootW = w;
        contextMenuRootH = h;
    }

    public WidgetGroup getContextMenuRoot() {
        return contextMenuRoot;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hasOverride = hasRootOverride();
        if (hasOverride) {
            IGuiTexture saved = getBackgroundTexture();
            setBackground(IGuiTexture.EMPTY);
            drawRootFill(graphics);
            TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), true, graphics, mouseX, mouseY, partialTicks,
                    TabletRootDrawRouter.LayerDraw.BACKGROUND, (g, x, y, t) -> super.drawInBackground(g, x, y, t));
            setBackground(saved);
        } else {
            drawRootFill(graphics);
            TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), true, graphics, mouseX, mouseY, partialTicks,
                    TabletRootDrawRouter.LayerDraw.BACKGROUND, (g, x, y, t) -> super.drawInBackground(g, x, y, t));
        }
        if (homeBtn != null) {
            homeBtn.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
        if (state != null && state.root.skinEditMode) {
            SkinEditRenderer.draw(graphics, this, state, mouseX, mouseY);
        }
        if (state != null && state.root.editorPopup != null) {
            state.root.editorPopup.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), !frontWindowHoverSuppressed(), graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.FOREGROUND, (g, x, y, t) -> super.drawInForeground(g, x, y, t));
        if (homeBtn != null) {
            homeBtn.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
        if (state != null && state.root.skinEditMode && gui != null && gui.getModularUIGui() != null) {
            gui.getModularUIGui().tooltipTexts = null;
        }
    }

    private boolean frontWindowHoverSuppressed() {
        if (isAnyModalOpen()) {
            return true;
        }
        return state != null && (state.questDetails.questDetailsContextOpen
                || state.questDetails.questDetailsPickerSession.active()
                || state.questDetails.questDetailsCommandRewardEditorOpen
                || state.questDetails.questDetailsTaskRenameOpen);
    }

    @Override
    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletRootDrawRouter.draw(modalLayer, frontWindowLayer, isAnyModalOpen(), isFrontWindowOpen(), true, graphics, mouseX, mouseY, partialTicks,
                TabletRootDrawRouter.LayerDraw.OVERLAY, (g, x, y, t) -> super.drawOverlay(g, x, y, t));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (state != null && state.root.editorPopup != null) {
            if (state.root.editorPopup.getGui() == null && getGui() != null) {
                state.root.editorPopup.setGui(getGui());
            }
            if (state.root.editorPopup.isMouseOverElement((int) mouseX, (int) mouseY)) {
                if (state.root.editorPopup.getGui() == null) return true;
                try {
                    state.root.editorPopup.mouseClicked(mouseX, mouseY, button);
                } catch (NullPointerException ignored) {
                    return true;
                }
            }
            return true;
        }
        if (state != null && state.root.skinEditMode) {
            if (SkinEditManager.handleClick(state, this, refresher, (int) mouseX, (int) mouseY, button)) return true;
        }
        if (homeBtn != null && homeBtn.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return TabletRootPointerRouter.mouseClicked(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b) -> super.mouseClicked(x, y, b), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state != null && state.root.editorPopup != null) {
            if (state.root.editorPopup.getGui() == null && getGui() != null) {
                state.root.editorPopup.setGui(getGui());
            }
            if (state.root.editorPopup.isMouseOverElement((int) mouseX, (int) mouseY)) {
                if (state.root.editorPopup.getGui() == null) return true;
                try {
                    state.root.editorPopup.mouseDragged(mouseX, mouseY, button, dragX, dragY);
                } catch (NullPointerException ignored) {
                    return true;
                }
                return true;
            }
        }
        if (state != null && state.root.skinEditMode && isContextMenuOpen() && !ModalStateQueries.anyOpen(state)) return true;
        return TabletRootPointerRouter.mouseDragged(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b, dx, dy) -> super.mouseDragged(x, y, b, dx, dy), mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (state != null && state.root.editorPopup != null) {
            if (state.root.editorPopup.getGui() == null && getGui() != null) {
                state.root.editorPopup.setGui(getGui());
            }
            if (state.root.editorPopup.isMouseOverElement((int) mouseX, (int) mouseY)) {
                if (state.root.editorPopup.getGui() == null) return true;
                try {
                    state.root.editorPopup.mouseReleased(mouseX, mouseY, button);
                } catch (NullPointerException ignored) {
                    return true;
                }
                return true;
            }
        }
        if (state != null && state.root.skinEditMode && isContextMenuOpen() && !ModalStateQueries.anyOpen(state)) return true;
        return TabletRootPointerRouter.mouseReleased(this, state, modalLayer, frontWindowLayer, refresher, (x, y, b) -> super.mouseReleased(x, y, b), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (state != null && state.root.skinEditMode && isContextMenuOpen() && !ModalStateQueries.anyOpen(state)) return true;
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

    private void drawRootFill(GuiGraphics graphics) {
        if (state == null) return;
        int rx = getPositionX(), ry = getPositionY(), rw = getSizeWidth(), rh = getSizeHeight();
        String raw = rootFillRaw(state);
        SkinFillOverride override = SkinFillOverride.parse(raw);
        if (override != null) {
            IGuiTexture tex = override.createTexture();
            if (tex != null) {
                tex.draw(graphics, 0, 0, rx, ry, rw, rh);
                return;
            }
        }
        SurfaceFactory.fill(TabletColors.SURFACE_BASE).draw(graphics, 0, 0, rx, ry, rw, rh);
    }

    private boolean hasRootOverride() {
        if (state == null) return false;
        String raw = rootFillRaw(state);
        SkinFillOverride override = SkinFillOverride.parse(raw);
        return override != null && override.createTexture() != null;
    }

    private static String rootFillRaw(TabletUiState state) {
        if (state.root.skinFillOverrides == null || state.root.skinFillOverrides.isEmpty()) return null;
        String rootKey = SkinOverrideKey.resolveTargetKey(state, "root");
        String qualified = SkinOverrideKey.overrideKey(state, "root");
        String raw = state.root.skinFillOverrides.get(qualified);
        if (raw == null && !qualified.equals(rootKey)) {
            raw = state.root.skinFillOverrides.get(rootKey);
        }
        return raw;
    }

    public static IGuiTexture resolveRootFill(TabletUiState state) {
        if (state == null) return null;
        String raw = rootFillRaw(state);
        if (raw == null) return null;
        SkinFillOverride override = SkinFillOverride.parse(raw);
        if (override == null) return null;
        IGuiTexture tex = override.createTexture();
        if (tex instanceof com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture) return null;
        return tex;
    }

    public static boolean hasActiveRootOverride(TabletUiState state) {
        if (state == null) return false;
        String raw = rootFillRaw(state);
        if (raw == null) return false;
        SkinFillOverride override = SkinFillOverride.parse(raw);
        if (override == null) return false;
        return override.createTexture() != null;
    }

    public static void refreshRootBackground(TabletRootWidget root, TabletUiState state) {
        boolean hasOverride = hasActiveRootOverride(state);
        root.setBackground(state != null && (state.root.fullScreenMode || hasOverride)
                ? SurfaceFactory.transparent()
                : SurfaceFactory.transparentBorder(TabletColors.BORDER_BASE));
    }
}
