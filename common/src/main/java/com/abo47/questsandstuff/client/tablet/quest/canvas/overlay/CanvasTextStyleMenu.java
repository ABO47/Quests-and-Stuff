package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasTextStyleMenu {
    private CanvasTextStyleMenu() {
    }

    static void render(CanvasViewport canvasViewport, TabletUiState state, Runnable refresh) {
        if (!state.canvas.canvasTextMenuOpen || state.canvas.canvasTextMenuTarget.isBlank()) {
            return;
        }
        String chapter = selectedChapterName(state);
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, state.canvas.canvasTextMenuTarget);
        if (text == null) {
            TextStyleSession.closeMainCanvas(state);
            return;
        }
        int toolCount = 8;
        int[] bounds = CanvasRenderer.canvasTextMenuBounds(state, text, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), toolCount);
        int x = bounds[0];
        int y = bounds[1];
        int menuW = bounds[2];
        int menuH = bounds[3];
        int columns = bounds[5];

        renderShared(canvasViewport, state, text, x, y, menuW, menuH, columns, "canvas", refresh, next -> CanvasLayerMutations.putCanvasText(state, group, fitCanvasText(state, next)), () -> {
            ModalOpenActions.openColorPicker(state, ModalTargets.canvasText(group, text.id()), CanvasRenderer.activeTextColor(state, text));
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text color open picker chapter={} id={}", group, text.id());
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
        CanvasTextLayer menuText = CanvasLayerMutations.effectiveQuestDetailsText(state, text);
        int[] bounds = menuBoundsForGeometry(state, menuText, viewportW, viewportH, scroll, state.questDetails.questDetailsGridSnapLocked, toolCount);
        int x = viewportX + bounds[0];
        int y = viewportY + bounds[1];
        int hitX = state.questDetails.questDetailsViewportOriginX + bounds[0];
        int hitY = state.questDetails.questDetailsViewportOriginY + bounds[1];
        TextStyleSession.setQuestDetailsBounds(state, hitX, hitY, bounds[2], bounds[3]);
        renderShared(parent, state, text, x, y, bounds[2], bounds[3], bounds[5], "quest details", refresh, updateText, openColorPicker);
    }

    private static int alignButtonBase(String currentAlign, String option) {
        return option.equals(currentAlign) ? TabletColors.INTERACTIVE : TabletColors.SURFACE_PANEL_ALT;
    }

    private static int toggleButtonBase(boolean enabled) {
        return enabled ? TabletColors.INTERACTIVE : TabletColors.SURFACE_PANEL_ALT;
    }

    private static void renderShared(
            WidgetGroup parent,
            TabletUiState state,
            CanvasTextLayer text,
            int x,
            int y,
            int menuW,
            int menuH,
            int columns,
            String logScope,
            Runnable refresh,
            Consumer<CanvasTextLayer> updateText,
            Runnable openColorPicker
    ) {
        WidgetGroup floating = TextStyleButtons.shell(x, y, menuW, menuH, click -> {
            markStyleInteraction(state, logScope);
            QuestsAndStuffMod.debugLog("[QnS:UI] {} text menu internal click target={}", logScope, text.id());
            refresh.run();
        });

        String align = text.align();
        boolean bold = CanvasRenderer.isTextStyleFlagActive(state, text, "bold");
        boolean italic = CanvasRenderer.isTextStyleFlagActive(state, text, "italic");
        addTextStyleButton(floating, 0, menuW, columns, "style_align_left", alignButtonBase(align, "left"), click -> updateStyle(state, logScope, text, text.withAlign("left"), updateText, refresh));
        addTextStyleButton(floating, 1, menuW, columns, "style_align_center", alignButtonBase(align, "center"), click -> updateStyle(state, logScope, text, text.withAlign("center"), updateText, refresh));
        addTextStyleButton(floating, 2, menuW, columns, "style_align_right", alignButtonBase(align, "right"), click -> updateStyle(state, logScope, text, text.withAlign("right"), updateText, refresh));
        addTextStyleButton(floating, 3, menuW, columns, "style_color", TabletColors.SURFACE_PANEL_ALT, CanvasRenderer.activeTextColor(state, text), click -> {
            markStyleInteraction(state, logScope);
            openColorPicker.run();
        });
        addTextStyleButton(floating, 4, menuW, columns, "context_style", toggleButtonBase(!bold && !italic), click -> updateStyle(state, logScope, text, CanvasRenderer.applyTextStyleSelection(state, text, "normal"), updateText, refresh));
        addTextStyleButton(floating, 5, menuW, columns, "style_bold", toggleButtonBase(bold), click -> updateStyle(state, logScope, text, CanvasRenderer.toggleTextStyleSelection(state, text, "bold"), updateText, refresh));
        addTextStyleButton(floating, 6, menuW, columns, "style_italic", toggleButtonBase(italic), click -> updateStyle(state, logScope, text, CanvasRenderer.toggleTextStyleSelection(state, text, "italic"), updateText, refresh));
        addFontSizeControl(floating, state, logScope, text, menuW, columns, refresh, updateText);
        parent.addWidget(floating);
    }

    private static void updateStyle(TabletUiState state, String logScope, CanvasTextLayer oldText, CanvasTextLayer next, Consumer<CanvasTextLayer> updateText, Runnable refresh) {
        markStyleInteraction(state, logScope);
        updateText.accept(CanvasTextRenderer.fitTextHeight(next));
        keepQuestDetailsStyleMenuOpen(state, logScope, next.id());
        QuestsAndStuffMod.debugLog("[QnS:UI] {} text style id={}", logScope, oldText.id());
        refresh.run();
    }

    private static CanvasTextLayer fitCanvasText(TabletUiState state, CanvasTextLayer text) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private static void addTextStyleButton(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addTextStyleButton(parent, index, menuWidth, columns, iconName, baseColor, null, null, callback);
    }

    private static void addTextStyleButton(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Integer iconTint, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addTextStyleButton(parent, index, menuWidth, columns, iconName, baseColor, iconTint, null, callback);
    }

    private static void addTextStyleButton(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Integer iconTint, Component[] tooltips, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        TextStyleButtons.addTool(parent, index, menuWidth, columns, iconName, baseColor, iconTint, tooltips, callback);
    }

    private static void addFontSizeControl(WidgetGroup parent, TabletUiState state, String logScope, CanvasTextLayer text, int menuWidth, int columns, Runnable refresh, Consumer<CanvasTextLayer> updateText) {
        int fontSize = text.fontSize();
        boolean open = text.id().equals(textFontSizeFieldTarget(state, logScope));
        if (open) {
            TextStyleButtons.addFontSizeField(parent, 7, menuWidth, columns, fontSize, value -> {
                markStyleInteraction(state, logScope);
                updateText.accept(CanvasTextRenderer.fitTextHeight(text.withFontSize(value)));
                keepQuestDetailsStyleMenuOpen(state, logScope, text.id());
                QuestsAndStuffMod.debugLog("[QnS:UI] {} text font-size id={} size={}", logScope, text.id(), value);
            }, () -> closeFontSizeField(state, logScope, refresh), () -> closeFontSizeField(state, logScope, refresh), () -> closeFontSizeField(state, logScope, refresh));
            return;
        }
        int baseColor = open || fontSize != CanvasTextLayer.DEFAULT_FONT_SIZE ? TabletColors.INTERACTIVE : TabletColors.SURFACE_PANEL_ALT;
        addTextStyleButton(parent, 7, menuWidth, columns, "size", baseColor, null, new Component[]{
                Component.literal("Font size: " + fontSize)
        }, click -> {
            markStyleInteraction(state, logScope);
            setTextFontSizeFieldTarget(state, logScope, text.id());
            QuestsAndStuffMod.debugLog("[QnS:UI] {} text font-size field id={} open=true", logScope, text.id());
            refresh.run();
        });
    }

    private static void closeFontSizeField(TabletUiState state, String logScope, Runnable refresh) {
        setTextFontSizeFieldTarget(state, logScope, "");
        refresh.run();
    }

    private static void markStyleInteraction(TabletUiState state, String logScope) {
        if (isQuestDetailsScope(logScope)) {
            TextStyleSession.markQuestDetailsInteraction(state);
        }
    }

    private static void keepQuestDetailsStyleMenuOpen(TabletUiState state, String logScope, String textId) {
        if (isQuestDetailsScope(logScope)) {
            TextStyleSession.openQuestDetails(state, textId);
        }
    }

    private static boolean isQuestDetailsScope(String logScope) {
        return "quest details".equals(logScope);
    }

    private static String textFontSizeFieldTarget(TabletUiState state, String logScope) {
        return TextStyleSession.fontSizeTarget(state, surface(logScope));
    }

    private static void setTextFontSizeFieldTarget(TabletUiState state, String logScope, String target) {
        TextStyleSession.setFontSizeTarget(state, surface(logScope), target);
    }

    private static TextStyleSession.Surface surface(String logScope) {
        return isQuestDetailsScope(logScope) ? TextStyleSession.Surface.QUEST_DETAILS : TextStyleSession.Surface.MAIN_CANVAS;
    }

    private static int[] menuBoundsForGeometry(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int scroll, boolean gridSnapLocked, int toolCount) {
        int oldContentX = state.canvas.canvasContentX;
        int oldContentY = state.canvas.canvasContentY;
        int oldContentW = state.canvas.canvasContentW;
        int oldContentH = state.canvas.canvasContentH;
        int oldOffsetX = state.canvas.canvasOffsetX;
        int oldOffsetY = state.canvas.canvasOffsetY;
        float oldZoom = state.canvas.canvasZoom;
        boolean oldGridSnap = state.canvas.gridSnapLocked;
        state.canvas.canvasContentX = 0;
        state.canvas.canvasContentY = -scroll;
        state.canvas.canvasContentW = viewportW;
        state.canvas.canvasContentH = viewportH;
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;
        state.canvas.gridSnapLocked = gridSnapLocked;
        try {
            return CanvasRenderer.canvasTextMenuBounds(state, text, viewportW, viewportH, toolCount);
        } finally {
            state.canvas.canvasContentX = oldContentX;
            state.canvas.canvasContentY = oldContentY;
            state.canvas.canvasContentW = oldContentW;
            state.canvas.canvasContentH = oldContentH;
            state.canvas.canvasOffsetX = oldOffsetX;
            state.canvas.canvasOffsetY = oldOffsetY;
            state.canvas.canvasZoom = oldZoom;
            state.canvas.gridSnapLocked = oldGridSnap;
        }
    }
}
