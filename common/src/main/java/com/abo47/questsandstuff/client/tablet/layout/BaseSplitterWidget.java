package com.abo47.questsandstuff.client.tablet.layout;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.TabletClickSounds;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;

public abstract class BaseSplitterWidget extends WidgetGroup {
    protected final TabletUiState state;
    protected final Runnable refresh;
    protected final String skinTargetKey;

    public BaseSplitterWidget(int x, int y, int w, int h, TabletUiState state, Runnable refresh) {
        this(x, y, w, h, state, refresh, null);
    }

    public BaseSplitterWidget(int x, int y, int w, int h, TabletUiState state, Runnable refresh, String skinTargetKey) {
        super(x, y, w, h);
        this.state = state;
        this.refresh = refresh;
        this.skinTargetKey = skinTargetKey;
    }

    protected abstract boolean isSplitterLocked();
    protected abstract boolean isSplitterDragging();
    protected abstract void setSplitterDragging(boolean dragging);
    protected abstract int splitterDragStartX();
    protected abstract void setSplitterDragStartX(int x);
    protected abstract int splitterStartWidth();
    protected abstract void setSplitterStartWidth(int width);
    protected abstract int getLeftPanelWidth();
    protected abstract void setLeftPanelWidth(int width);

    protected int dragThresholdPx() {
        return 0;
    }

    protected boolean hasDragMoved() {
        return true;
    }

    protected void setDragMoved(boolean moved) {
    }

    protected int computeDragWidth(int rawWidth) {
        return rawWidth;
    }

    protected boolean canCollapseOnDrag() {
        return false;
    }

    protected int collapseThresholdWidth() {
        return 0;
    }

    protected int collapsedPanelWidth() {
        return 0;
    }

    protected boolean isPanelCollapsed() {
        return false;
    }

    protected void setPanelCollapsed(boolean collapsed) {
    }

    protected void onSplitterClick() {
        TabletClickSounds.playClick();
    }

    protected void onSplitterRelease() {
        persistUiState(state);
    }

    protected boolean cancelDragOnClickLocked() {
        return false;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isSplitterDragging() || isMouseOverElement(mouseX, mouseY);
        boolean resizeHovered = hovered && !isSplitterLocked();
        TabletResizeCursor.update(resizeHovered);
        int left = getPositionX();
        int top = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();

        IGuiTexture skinBg = getBackgroundTexture();
        boolean hasSkinOverride = skinBg != null && !skinBg.equals(IGuiTexture.EMPTY);
        if (hasSkinOverride) {
            skinBg.draw(graphics, mouseX, mouseY, left, top, width, height);
        } else {
            SurfaceFactory.fill(TabletColors.SURFACE_PANEL_ALT).draw(graphics, mouseX, mouseY, left, top, width, height);
            SurfaceFactory.fill(TabletColors.BORDER_BASE).draw(graphics, mouseX, mouseY, left, top, width, 1);
            SurfaceFactory.fill(TabletColors.BORDER_BASE).draw(graphics, mouseX, mouseY, left, top + height - 1, width, 1);
        }
        if (hovered) {
            ResourceLocation mask = resolveSkinMask();
            if (mask != null) {
                GlowShaderHelper.drawGlowMasked(graphics, mouseX, mouseY, left, top, width, height, TabletColors.GLOW, mask);
            } else {
                GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, left, top, width, height);
            }
        }

        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (cancelDragOnClickLocked() && isSplitterLocked()) {
            setSplitterDragging(false);
            TabletResizeCursor.update(false);
            return true;
        }
        setSplitterDragging(true);
        setDragMoved(false);
        setSplitterDragStartX((int) Math.round(mouseX));
        setSplitterStartWidth(getLeftPanelWidth());
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isSplitterLocked()) {
            TabletResizeCursor.update(false);
            return true;
        }
        if (!isSplitterDragging()) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int dx = (int) Math.round(mouseX) - splitterDragStartX();
        int threshold = dragThresholdPx();
        if (threshold > 0 && !hasDragMoved() && Math.abs(dx) <= threshold) {
            return true;
        }
        setDragMoved(true);
        int rawWidth = splitterStartWidth() + dx;
        int nextWidth = resolveDragWidth(rawWidth);
        setLeftPanelWidth(nextWidth);
        refresh.run();
        return true;
    }

    private int resolveDragWidth(int rawWidth) {
        if (canCollapseOnDrag() && rawWidth <= collapseThresholdWidth()) {
            setPanelCollapsed(true);
            return collapsedPanelWidth();
        }
        setPanelCollapsed(false);
        return computeDragWidth(rawWidth);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isSplitterDragging()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        setSplitterDragging(false);
        boolean moved = hasDragMoved();
        setDragMoved(false);
        if (!moved) {
            onSplitterClick();
            return true;
        }
        setLeftPanelWidth(getLeftPanelWidth());
        onSplitterRelease();
        TabletResizeCursor.update(false);
        refresh.run();
        return true;
    }

    private ResourceLocation resolveSkinMask() {
        if (skinTargetKey == null || skinTargetKey.isBlank() || state == null) return null;
        String raw = SkinOverrideKey.resolveOverride(state, skinTargetKey);
        if (raw == null) return null;
        SkinFillOverride override = SkinFillOverride.parse(raw);
        if (override == null) return null;
        String path = override.path();
        if (path == null || path.isBlank()) return null;
        ResourceLocation id = AssetLibrary.staticTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) {
            id = AssetLibrary.tileTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        }
        return id;
    }

}
