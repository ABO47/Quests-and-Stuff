package com.abo47.questsandstuff.client.canvas.overlay;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.controls.FontSizeSliderWidget;
import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.FONT_SIZE_SLIDER_POPOVER_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.FONT_SIZE_SLIDER_POPOVER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class CanvasTextStyleMenu {
    private CanvasTextStyleMenu() {
    }

    static void render(CanvasViewport canvasViewport, TabletUiState state, Runnable refresh) {
        if (!state.canvasTextMenuOpen || state.canvasTextMenuTarget.isBlank()) {
            return;
        }
        String group = selectedGroupName(state);
        CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, state.canvasTextMenuTarget);
        if (text == null) {
            state.canvasTextMenuOpen = false;
            state.canvasTextMenuTarget = "";
            state.canvasTextFontSizeSliderTarget = "";
            return;
        }
        int toolCount = 8;
        int[] bounds = CanvasRenderer.canvasTextMenuBounds(state, text, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), toolCount);
        int x = bounds[0];
        int y = bounds[1];
        int menuW = bounds[2];
        int menuH = bounds[3];
        int btnW = bounds[4];
        int columns = bounds[5];

        renderShared(canvasViewport, state, text, x, y, menuW, menuH, btnW, columns, "canvas", refresh, next -> CanvasRenderer.putCanvasText(state, group, fitCanvasText(state, next)), () -> {
            ModalOpenActions.openColorPicker(state, ModalTargets.canvasText(group, text.id()), text.color());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text color open picker group={} id={}", group, text.id());
            refresh.run();
        });
    }

    public static void renderQuestDetails(
            WidgetGroup parent,
            TabletUiState state,
            CanvasTextLayer text,
            int viewportX,
            int viewportY,
            int viewportW,
            int viewportH,
            int scroll,
            Consumer<CanvasTextLayer> updateText,
            Runnable openColorPicker,
            Runnable refresh
    ) {
        int toolCount = 8;
        int[] bounds = menuBoundsForGeometry(state, text, viewportW, viewportH, scroll, state.questDetailsGridSnapLocked, toolCount);
        int x = viewportX + bounds[0];
        int y = viewportY + bounds[1];
        int hitH = bounds[3];
        if (text.id().equals(state.questDetailsTextFontSizeSliderTarget)) {
            int sliderBottom = toolY(7, bounds[5]) + 16 + FONT_SIZE_SLIDER_POPOVER_GAP + FONT_SIZE_SLIDER_POPOVER_H;
            hitH = Math.max(hitH, sliderBottom);
        }
        state.questDetailsTextStyleMenuX = x;
        state.questDetailsTextStyleMenuY = y;
        state.questDetailsTextStyleMenuW = bounds[2];
        state.questDetailsTextStyleMenuH = hitH;
        renderShared(parent, state, text, x, y, bounds[2], bounds[3], bounds[4], bounds[5], "quest details", refresh, updateText, openColorPicker);
    }

    private static int toolX(int index, int columns, int buttonWidth) {
        return 2 + (index % Math.max(1, columns)) * buttonWidth;
    }

    private static int toolY(int index, int columns) {
        return 2 + (index / Math.max(1, columns)) * 16;
    }

    private static int alignButtonBase(String currentAlign, String option) {
        return option.equals(currentAlign) ? ModColors.INTERACTIVE : ModColors.SURFACE_PANEL_ALT;
    }

    private static int toggleButtonBase(boolean enabled) {
        return enabled ? ModColors.INTERACTIVE : ModColors.SURFACE_PANEL_ALT;
    }

    private static void renderShared(
            WidgetGroup parent,
            TabletUiState state,
            CanvasTextLayer text,
            int x,
            int y,
            int menuW,
            int menuH,
            int btnW,
            int columns,
            String logScope,
            Runnable refresh,
            Consumer<CanvasTextLayer> updateText,
            Runnable openColorPicker
    ) {
        WidgetGroup floating = panel(x, y, menuW, menuH, withAlpha(ModColors.SURFACE_BASE, 244), ModColors.BORDER_ACCENT);
        floating.addWidget(panel(1, 1, menuW - 2, menuH - 2, withAlpha(ModColors.SURFACE_PANEL_ALT, 192), ModColors.BORDER_BASE));
        floating.addWidget(flatHitButton(0, 0, menuW, menuH, click -> {
            markStyleInteraction(state, logScope);
            QuestsAndStuffMod.debugLog("[QnS:UI] {} text menu internal click target={}", logScope, text.id());
            refresh.run();
        }));

        String align = text.align();
        boolean bold = CanvasRenderer.isTextStyleFlagActive(state, text, "bold");
        boolean italic = CanvasRenderer.isTextStyleFlagActive(state, text, "italic");
        addTextStyleButton(floating, toolX(0, columns, btnW), toolY(0, columns), btnW, 16, "style_align_left", alignButtonBase(align, "left"), click -> updateStyle(state, logScope, text, text.withAlign("left"), updateText, refresh));
        addTextStyleButton(floating, toolX(1, columns, btnW), toolY(1, columns), btnW, 16, "style_align_center", alignButtonBase(align, "center"), click -> updateStyle(state, logScope, text, text.withAlign("center"), updateText, refresh));
        addTextStyleButton(floating, toolX(2, columns, btnW), toolY(2, columns), btnW, 16, "style_align_right", alignButtonBase(align, "right"), click -> updateStyle(state, logScope, text, text.withAlign("right"), updateText, refresh));
        addTextStyleButton(floating, toolX(3, columns, btnW), toolY(3, columns), btnW, 16, "style_color", ModColors.SURFACE_PANEL_ALT, text.color(), click -> {
            markStyleInteraction(state, logScope);
            openColorPicker.run();
        });
        addTextStyleButton(floating, toolX(4, columns, btnW), toolY(4, columns), btnW, 16, "context_style", toggleButtonBase(!bold && !italic), click -> updateStyle(state, logScope, text, CanvasRenderer.applyTextStyleSelection(state, text, "normal"), updateText, refresh));
        addTextStyleButton(floating, toolX(5, columns, btnW), toolY(5, columns), btnW, 16, "style_bold", toggleButtonBase(bold), click -> updateStyle(state, logScope, text, CanvasRenderer.toggleTextStyleSelection(state, text, "bold"), updateText, refresh));
        addTextStyleButton(floating, toolX(6, columns, btnW), toolY(6, columns), btnW, 16, "style_italic", toggleButtonBase(italic), click -> updateStyle(state, logScope, text, CanvasRenderer.toggleTextStyleSelection(state, text, "italic"), updateText, refresh));
        int sizeX = toolX(7, columns, btnW);
        int sizeY = toolY(7, columns);
        addFontSizeControl(floating, state, logScope, text, sizeX, sizeY, btnW, 16, refresh);
        parent.addWidget(floating);
        if (text.id().equals(textFontSizeSliderTarget(state, logScope))) {
            addFontSizePopover(parent, state, logScope, text, x + sizeX, y + sizeY + 16 + FONT_SIZE_SLIDER_POPOVER_GAP, btnW, value -> updateText.accept(CanvasTextRenderer.fitTextHeight(text.withFontSize(value))), refresh);
        }
    }

    private static void updateStyle(TabletUiState state, String logScope, CanvasTextLayer oldText, CanvasTextLayer next, Consumer<CanvasTextLayer> updateText, Runnable refresh) {
        markStyleInteraction(state, logScope);
        updateText.accept(CanvasTextRenderer.fitTextHeight(next));
        keepQuestDetailsStyleMenuOpen(state, logScope, next.id());
        QuestsAndStuffMod.debugLog("[QnS:UI] {} text style id={}", logScope, oldText.id());
        refresh.run();
    }

    private static CanvasTextLayer fitCanvasText(TabletUiState state, CanvasTextLayer text) {
        return state.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private static void addTextStyleButton(WidgetGroup parent, int x, int y, int w, int h, String iconName, int baseColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addTextStyleButton(parent, x, y, w, h, iconName, baseColor, null, null, callback);
    }

    private static void addTextStyleButton(WidgetGroup parent, int x, int y, int w, int h, String iconName, int baseColor, Integer iconTint, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addTextStyleButton(parent, x, y, w, h, iconName, baseColor, iconTint, null, callback);
    }

    private static void addTextStyleButton(WidgetGroup parent, int x, int y, int w, int h, String iconName, int baseColor, Integer iconTint, Component[] tooltips, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        int fill = baseColor == ModColors.SURFACE_PANEL_ALT ? withAlpha(ModColors.SURFACE_BASE, 40) : withAlpha(baseColor, 190);
        WidgetGroup bg = new WidgetGroup(x, y, w, h);
        bg.setBackground(Surfaces.fill(fill));
        parent.addWidget(bg);
        var texture = iconTint == null ? UiIconAtlas.iconTexture(iconName) : tintedIconTexture(iconName, iconTint);
        if (texture != null) {
            int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, Math.min(w - 4, h - 4)));
            parent.addWidget(new ImageWidget(x + (w - iconSize) / 2, y + (h - iconSize) / 2, iconSize, iconSize, texture));
        }
        ButtonWidget hit = flatHitButton(x, y, w, h, callback);
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 70)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 100)));
        if (tooltips != null) {
            hit.setHoverTooltips(tooltips);
        }
        parent.addWidget(hit);
    }

    private static void addFontSizeControl(WidgetGroup parent, TabletUiState state, String logScope, CanvasTextLayer text, int x, int y, int w, int h, Runnable refresh) {
        int fontSize = text.fontSize();
        boolean open = text.id().equals(textFontSizeSliderTarget(state, logScope));
        int baseColor = open || fontSize != CanvasTextLayer.DEFAULT_FONT_SIZE ? ModColors.INTERACTIVE : ModColors.SURFACE_PANEL_ALT;
        addTextStyleButton(parent, x, y, w, h, "size", baseColor, null, new Component[]{
                Component.literal("Font size: " + fontSize)
        }, click -> {
            markStyleInteraction(state, logScope);
            setTextFontSizeSliderTarget(state, logScope, open && !"quest details".equals(logScope) ? "" : text.id());
            QuestsAndStuffMod.debugLog("[QnS:UI] {} text font-size slider id={} open={}", logScope, text.id(), !open || "quest details".equals(logScope));
            refresh.run();
        });
    }

    private static void addFontSizePopover(WidgetGroup parent, TabletUiState state, String logScope, CanvasTextLayer text, int x, int y, int width, Consumer<Integer> updateFontSize, Runnable refresh) {
        int popoverW = Math.max(1, width);
        WidgetGroup popover = panel(
                x,
                y,
                popoverW,
                FONT_SIZE_SLIDER_POPOVER_H,
                withAlpha(ModColors.SURFACE_BASE, 246),
                ModColors.BORDER_ACCENT
        );
        popover.addWidget(new FontSizeSliderWidget(
                0,
                0,
                popoverW,
                FONT_SIZE_SLIDER_POPOVER_H,
                CanvasTextLayer.MIN_FONT_SIZE,
                18,
                text.fontSize(),
                value -> {
                    markStyleInteraction(state, logScope);
                    updateFontSize.accept(value);
                    QuestsAndStuffMod.debugLog("[QnS:UI] {} text font-size id={} size={}", logScope, text.id(), value);
                },
                refresh,
                () -> textFontSizeSliderDragging(state, logScope) && text.id().equals(textFontSizeSliderDragTarget(state, logScope)),
                dragging -> {
                    markStyleInteraction(state, logScope);
                    setTextFontSizeSliderDragging(state, logScope, dragging, text.id());
                }
        ));
        parent.addWidget(popover);
    }

    private static void markStyleInteraction(TabletUiState state, String logScope) {
        if (isQuestDetailsScope(logScope)) {
            state.questDetailsTextStyleInteractionAtMs = System.currentTimeMillis();
        }
    }

    private static void keepQuestDetailsStyleMenuOpen(TabletUiState state, String logScope, String textId) {
        if (isQuestDetailsScope(logScope)) {
            state.questDetailsTextStyleOpen = true;
            state.questDetailsTextStyleTarget = textId == null ? "" : textId;
        }
    }

    private static boolean isQuestDetailsScope(String logScope) {
        return "quest details".equals(logScope);
    }

    private static String textFontSizeSliderTarget(TabletUiState state, String logScope) {
        return isQuestDetailsScope(logScope) ? state.questDetailsTextFontSizeSliderTarget : state.canvasTextFontSizeSliderTarget;
    }

    private static void setTextFontSizeSliderTarget(TabletUiState state, String logScope, String target) {
        if (isQuestDetailsScope(logScope)) {
            state.questDetailsTextFontSizeSliderTarget = target == null ? "" : target;
        } else {
            state.canvasTextFontSizeSliderTarget = target == null ? "" : target;
        }
    }

    private static boolean textFontSizeSliderDragging(TabletUiState state, String logScope) {
        return isQuestDetailsScope(logScope) ? state.questDetailsTextFontSizeSliderDragging : state.canvasTextFontSizeSliderDragging;
    }

    private static String textFontSizeSliderDragTarget(TabletUiState state, String logScope) {
        return isQuestDetailsScope(logScope) ? state.questDetailsTextFontSizeSliderDragTarget : state.canvasTextFontSizeSliderDragTarget;
    }

    private static void setTextFontSizeSliderDragging(TabletUiState state, String logScope, boolean dragging, String textId) {
        if (isQuestDetailsScope(logScope)) {
            state.questDetailsTextFontSizeSliderDragging = dragging;
            state.questDetailsTextFontSizeSliderDragTarget = dragging ? textId : "";
        } else {
            state.canvasTextFontSizeSliderDragging = dragging;
            state.canvasTextFontSizeSliderDragTarget = dragging ? textId : "";
        }
    }

    private static int[] menuBoundsForGeometry(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int scroll, boolean gridSnapLocked, int toolCount) {
        int oldContentX = state.canvasContentX;
        int oldContentY = state.canvasContentY;
        int oldContentW = state.canvasContentW;
        int oldContentH = state.canvasContentH;
        int oldOffsetX = state.canvasOffsetX;
        int oldOffsetY = state.canvasOffsetY;
        float oldZoom = state.canvasZoom;
        boolean oldGridSnap = state.gridSnapLocked;
        state.canvasContentX = 0;
        state.canvasContentY = -scroll;
        state.canvasContentW = viewportW;
        state.canvasContentH = viewportH;
        state.canvasOffsetX = 0;
        state.canvasOffsetY = 0;
        state.canvasZoom = 1.0f;
        state.gridSnapLocked = gridSnapLocked;
        try {
            return CanvasRenderer.canvasTextMenuBounds(state, text, viewportW, viewportH, toolCount);
        } finally {
            state.canvasContentX = oldContentX;
            state.canvasContentY = oldContentY;
            state.canvasContentW = oldContentW;
            state.canvasContentH = oldContentH;
            state.canvasOffsetX = oldOffsetX;
            state.canvasOffsetY = oldOffsetY;
            state.canvasZoom = oldZoom;
            state.gridSnapLocked = oldGridSnap;
        }
    }

    private static ResourceTexture tintedIconTexture(String iconName, int argbColor) {
        ResourceLocation id = UiIconAtlas.icon(iconName);
        return id == null ? null : new SmoothResourceTexture(id).setColor(argbColor);
    }
}
