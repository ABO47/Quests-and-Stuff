package com.abo47.questsandstuff.client.tablet.layout;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;

import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import org.joml.Vector4f;

public final class TabletPanelChrome {
    private TabletPanelChrome() {
    }

    public static void drawWindowShadow(GuiGraphics graphics, WidgetGroup panel) {
        drawWindowShadow(graphics, panel.getPositionX(), panel.getPositionY(), panel.getSizeWidth(), panel.getSizeHeight());
    }

    public static void drawWindowShadow(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int soft = withAlpha(TabletColors.SURFACE_BASE, 82);
        int hard = withAlpha(TabletColors.SURFACE_BASE, 120);
        SurfaceFactory.fill(soft).draw(graphics, 0, 0, x + 4, y + 5, w, h);
        SurfaceFactory.fill(hard).draw(graphics, 0, 0, x + 2, y + 3, w, h);
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelChrome(graphics, panel, state.canvas.canvasViewportX, state.canvas.canvasViewportY, state.canvas.canvasViewportW, state.canvas.canvasViewportH, state);
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH) {
        drawCanvasPanelChrome(graphics, panel, viewportX, viewportY, viewportW, viewportH, null);
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH, @javax.annotation.Nullable TabletUiState state) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        IGuiTexture fill = resolveFill(panel);
        if (hasPanelOverride(panel, state)) {
            fillPanelRect(fill, graphics, x, y, x + w, y + h);
        } else {
            int innerLeft = x + 1;
            int innerTop = y + 1;
            int innerRight = x + Math.max(1, w - 1);
            int innerBottom = y + Math.max(1, h - 1);
            fillPanelRect(fill, graphics, innerLeft, innerTop, innerRight, innerBottom);
        }
    }

    public static void viewportScissor(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH, Runnable draw) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int pvx = x + viewportX;
        int pvy = y + viewportY;
        var trans = graphics.pose().last().pose();
        var p1 = trans.transform(new Vector4f(pvx, pvy, 0, 1));
        var p2 = trans.transform(new Vector4f(pvx + viewportW, pvy + viewportH, 0, 1));
        graphics.enableScissor((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
        try {
            draw.run();
        } finally {
            graphics.disableScissor();
        }
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelOutlines(graphics, panel, state.canvas.canvasViewportX, state.canvas.canvasViewportY, state.canvas.canvasViewportW, state.canvas.canvasViewportH, state.root.canEdit, state.canvas.gridEnabled, state.canvas.gridOpacityPercent, TabletGridControls.defaultGridColor(state), state);
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH, boolean canEdit, boolean gridEnabled, int gridOpacityPercent, int gridColor) {
        drawCanvasPanelOutlines(graphics, panel, viewportX, viewportY, viewportW, viewportH, canEdit, gridEnabled, gridOpacityPercent, gridColor, null);
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH, boolean canEdit, boolean gridEnabled, int gridOpacityPercent, int gridColor, @javax.annotation.Nullable TabletUiState state) {
        if (!isActiveSkinPanel(panel, state)) {
            drawRectOutline(graphics, panel.getPositionX(), panel.getPositionY(), panel.getSize().width, panel.getSize().height, TabletColors.BORDER_BASE);
        }
        if (hasBuiltinCanvasBackground(state)) return;
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        int holeLeft = x + Math.max(1, Math.min(w - 1, viewportX));
        int holeTop = y + Math.max(1, Math.min(h - 1, viewportY));
        int holeRight = x + Math.max(1, Math.min(w - 1, viewportX + viewportW));
        int holeBottom = y + Math.max(1, Math.min(h - 1, viewportY + viewportH));

        if (holeRight > holeLeft && holeBottom > holeTop) {
            if (!hasPanelOverride(panel, state)) {
                drawRectOutline(graphics, holeLeft - 1, holeTop - 1, holeRight - holeLeft + 2, holeBottom - holeTop + 2, TabletColors.BORDER_BASE);
            }
            if (canEdit && gridEnabled) {
                drawRectOutline(graphics, holeLeft, holeTop, holeRight - holeLeft, holeBottom - holeTop, gridLineColor(gridOpacityPercent, gridColor));
            }
        }
    }

    private static boolean isActiveSkinPanel(WidgetGroup panel, @javax.annotation.Nullable TabletUiState state) {
        if (state == null || state.root.skinFillOverrides == null) return false;
        if (state.root.skinFillOverrides.isEmpty()) return false;
        String appPrefix = state.root.currentApp.isBlank() ? "" : state.root.currentApp + ":";
        for (String entryKey : state.root.skinFillOverrides.keySet()) {
            if (entryKey.contains(":") && !entryKey.startsWith(appPrefix)) continue;
            String bareKey = entryKey.contains(":") ? entryKey.substring(entryKey.indexOf(':') + 1) : entryKey;
            if ("root".equals(bareKey)) continue;
            Widget w = SkinAnchorRegistry.findByKey(bareKey);
            if (w == null) continue;
            if (w == panel) return true;
            Widget cur = w.getParent();
            while (cur != null) {
                if (cur == panel) return true;
                cur = cur.getParent();
            }
        }
        
        String panelKey = SkinAnchorRegistry.keyFor(panel);
        if (panelKey != null) {
            String bgKey = SkinOverrideKey.viewportBackgroundKey(panelKey);
            if (bgKey != null) {
                String bgAppKey = appPrefix + bgKey;
                if (state.root.skinFillOverrides.containsKey(bgKey)
                        || state.root.skinFillOverrides.containsKey(bgAppKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasBuiltinCanvasBackground(@javax.annotation.Nullable TabletUiState state) {
        if (state == null) return false;
        if (QuestDetailsWindow.isVisible(state)) {
            String questId = state.questDetails.questDetailsQuestId;
            if (questId != null && !questId.isBlank()) {
                CompoundTag quest = ClientQuestStateFacade.quest(questId);
                if (quest != null) {
                    String bg = QuestDetailsDescriptionModel.decode(quest).canvasBackground();
                    return bg != null && !bg.equals("default") && !bg.isBlank();
                }
            }
        } else {
            String chapter = state.root.selectedChapter;
            if (chapter != null && !chapter.isBlank()) {
                String bg = ClientQuestStateFacade.chapterCanvasBackground(chapter);
                return bg != null && !bg.equals("default") && !bg.isBlank();
            }
        }
        return false;
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, @javax.annotation.Nullable TabletUiState state, int viewportX, int viewportY, int viewportW, int viewportH, boolean canEdit, boolean gridEnabled, int gridOpacityPercent, int gridColor) {
        drawCanvasPanelOutlines(graphics, panel, viewportX, viewportY, viewportW, viewportH, canEdit, gridEnabled, gridOpacityPercent, gridColor, state);
    }

    public static void drawPanelChrome(GuiGraphics graphics, WidgetGroup panel) {
        drawPanelChromeNoShadow(graphics, panel);
    }

    public static void drawPanelChrome(GuiGraphics graphics, WidgetGroup panel, @javax.annotation.Nullable TabletUiState state) {
        drawPanelChromeNoShadow(graphics, panel, state);
    }

    public static void drawRootChromeNoShadow(GuiGraphics graphics, WidgetGroup panel) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        fillRootRect(resolveFill(panel), graphics, x, y, x + Math.max(1, w), y + Math.max(1, h));
    }

    public static void drawPanelChromeNoShadow(GuiGraphics graphics, WidgetGroup panel) {
        drawPanelChromeNoShadow(graphics, panel, null);
    }

    public static void drawPanelChromeNoShadow(GuiGraphics graphics, WidgetGroup panel, @javax.annotation.Nullable TabletUiState state) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        IGuiTexture fill = resolveFill(panel);
        if (hasPanelOverride(panel, state)) {
            fillPanelRect(fill, graphics, x, y, x + w, y + h);
        } else {
            int right = x + Math.max(1, w - 1);
            int bottom = y + Math.max(1, h - 1);
            fillPanelRect(fill, graphics, x + 1, y + 1, right, bottom);
        }
    }

    public static void drawPanelOutline(GuiGraphics graphics, WidgetGroup panel) {
        if (hasSkinOverride(panel)) return;
        drawRectOutline(graphics, panel.getPositionX(), panel.getPositionY(), panel.getSize().width, panel.getSize().height, TabletColors.BORDER_BASE);
    }

    public static void drawPanelOutline(GuiGraphics graphics, WidgetGroup panel, @javax.annotation.Nullable TabletUiState state) {
        if (hasPanelOverride(panel, state)) return;
        if (hasSkinOverride(panel)) return;
        drawRectOutline(graphics, panel.getPositionX(), panel.getPositionY(), panel.getSize().width, panel.getSize().height, TabletColors.BORDER_BASE);
    }

    public static void drawRectOutline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        IGuiTexture fill = SurfaceFactory.fill(color);
        fill.draw(graphics, 0, 0, x, y, w, 1);
        fill.draw(graphics, 0, 0, x, y + h - 1, w, 1);
        fill.draw(graphics, 0, 0, x, y + 1, 1, Math.max(0, h - 2));
        fill.draw(graphics, 0, 0, x + w - 1, y + 1, 1, Math.max(0, h - 2));
    }

    static IGuiTexture resolveFill(WidgetGroup panel) {
        IGuiTexture bg = panel.getBackgroundTexture();
        if (bg == null || bg.equals(IGuiTexture.EMPTY)) {
            return SurfaceFactory.fill(TabletColors.SURFACE_PANEL);
        }
        if (bg instanceof ColorRectTexture crt) {
            return SurfaceFactory.fill(crt.color);
        }
        return bg;
    }

    static boolean hasSkinOverride(WidgetGroup panel) {
        IGuiTexture bg = panel.getBackgroundTexture();
        if (bg == null || bg.equals(IGuiTexture.EMPTY)) return false;
        return !(bg instanceof ColorRectTexture) && !(bg instanceof GuiTextureGroup);
    }

    static boolean hasPanelOverride(WidgetGroup panel, @javax.annotation.Nullable TabletUiState state) {
        if (hasSkinOverride(panel)) return true;
        if (state == null) return false;
        String panelKey = SkinAnchorRegistry.keyFor(panel);
        if (panelKey != null && state.root.activeSkinTargets.contains(panelKey)) return true;
        if (state.root.skinFillOverrides == null || state.root.skinFillOverrides.isEmpty()) return false;
        for (String entryKey : state.root.skinFillOverrides.keySet()) {
            String appPrefix = state.root.currentApp.isBlank() ? "" : state.root.currentApp + ":";
            if (entryKey.contains(":") && !entryKey.startsWith(appPrefix)) continue;
            String bareKey = entryKey.contains(":") ? entryKey.substring(entryKey.indexOf(':') + 1) : entryKey;
            if ("root".equals(bareKey)) continue;
            Widget w = SkinAnchorRegistry.findByKey(bareKey);
            if (w == panel) return true;
        }
        return false;
    }

    private static int gridLineColor(int gridOpacityPercent, int gridColor) {
        int alphaPercent = Math.max(0, Math.min(100, gridOpacityPercent));
        int alpha = Math.max(20, Math.min(220, (255 * alphaPercent) / 100));
        return (alpha << 24) | (gridColor & 0x00FFFFFF);
    }

    private static void fillPanelRect(IGuiTexture fill, GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (right > left && bottom > top) {
            fill.draw(graphics, 0, 0, left, top, right - left, bottom - top);
        }
    }

    private static void fillRootRect(IGuiTexture fill, GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (right > left && bottom > top) {
            fill.draw(graphics, 0, 0, left, top, right - left, bottom - top);
        }
    }
}
