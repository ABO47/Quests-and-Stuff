package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintStore;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprintCode;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;
import net.minecraft.client.Minecraft;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class TabletBlueprintCodeModal {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 122;
    private static final int PAD = 8;
    private static final int BTN = 18;
    private static final int GAP = 3;
    private static final int CODE_Y = 32;
    private static final int CODE_H = 44;

    private TabletBlueprintCodeModal() {
    }

    static void openExport(TabletUiState state, String relativePath) {
        CanvasBlueprint blueprint = CanvasBlueprintStore.read(relativePath);
        String code = CanvasBlueprintCode.encode(blueprint);
        state.blueprintCodeOpen = true;
        state.blueprintCodeImportMode = false;
        state.blueprintCodeTarget = relativePath == null ? "" : relativePath;
        state.blueprintCodeDraft = code;
        state.blueprintCodeMessage = code.isBlank()
                ? TabletModalPanel.tr("ui.questsandstuff.blueprints.code_select_export")
                : "";
        state.blueprintCodeAnimationStartMs = System.currentTimeMillis();
        state.assetContextOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] export code open path={} empty={}", state.blueprintCodeTarget, code.isBlank());
    }

    static void openImport(TabletUiState state) {
        state.blueprintCodeOpen = true;
        state.blueprintCodeImportMode = true;
        state.blueprintCodeTarget = "";
        state.blueprintCodeDraft = "";
        state.blueprintCodeMessage = "";
        state.blueprintCodeAnimationStartMs = System.currentTimeMillis();
        state.assetContextOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] import code open");
    }

    static void close(TabletUiState state) {
        state.blueprintCodeOpen = false;
        state.blueprintCodeImportMode = false;
        state.blueprintCodeAnimationStartMs = 0L;
        state.blueprintCodeTarget = "";
        state.blueprintCodeDraft = "";
        state.blueprintCodeMessage = "";
    }

    static void add(WidgetGroup modal, TabletUiState state, Runnable refresh, int modalW, int modalH) {
        modal.addWidget(flatHitButton(0, 0, modalW, modalH, click -> {
            close(state);
            refresh.run();
        }));

        int panelW = Math.min(PANEL_W, Math.max(220, modalW - 32));
        int panelH = PANEL_H;
        int x = Math.max(PAD, (modalW - panelW) / 2);
        int y = Math.max(24, (modalH - panelH) / 2);
        WidgetGroup panel = Surfaces.panel(x, y, panelW, panelH, withAlpha(ModColors.elevatedSurface(), 245), ModColors.BORDER_ACCENT);
        panel.addWidget(label(PAD, 6, TabletModalPanel.tr(state.blueprintCodeImportMode
                ? "ui.questsandstuff.blueprints.import_code"
                : "ui.questsandstuff.blueprints.export_code"), ModColors.TEXT_PRIMARY));

        addHeaderButtons(panel, state, refresh, panelW);
        addCodeField(panel, state, panelW);
        if (!state.blueprintCodeMessage.isBlank()) {
            panel.addWidget(label(PAD, CODE_Y + CODE_H + 8, state.blueprintCodeMessage, ModColors.WARNING));
        }
        modal.addWidget(QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                ? SourceOriginRevealWidget.windowNoShadow(panel, () -> state.blueprintCodeAnimationStartMs, () -> true, () -> null)
                : panel);
    }

    private static void addHeaderButtons(WidgetGroup panel, TabletUiState state, Runnable refresh, int panelW) {
        int closeX = panelW - PAD - BTN;
        panel.addWidget(WindowChrome.closeIconButton(closeX, 4, BTN, BTN, click -> {
            close(state);
            refresh.run();
        }));

        int x = closeX - GAP - BTN;
        if (state.blueprintCodeImportMode) {
            panel.addWidget(WindowChrome.iconButton(x, 4, BTN, BTN, "manual_check", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS), click -> {
                applyImport(state);
                refresh.run();
            }));
            x -= GAP + BTN;
            panel.addWidget(WindowChrome.iconButton(x, 4, BTN, BTN, "paste", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
                state.blueprintCodeDraft = Minecraft.getInstance().keyboardHandler.getClipboard();
                state.blueprintCodeMessage = "";
                refresh.run();
            }));
            return;
        }
        panel.addWidget(WindowChrome.iconButton(x, 4, BTN, BTN, "copy", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(state.blueprintCodeDraft == null ? "" : state.blueprintCodeDraft);
            state.blueprintCodeMessage = TabletModalPanel.tr("ui.questsandstuff.blueprints.code_copied");
            refresh.run();
        }));
    }

    private static void addCodeField(WidgetGroup panel, TabletUiState state, int panelW) {
        CodeEditorWidget editor = new CodeEditorWidget(PAD, CODE_Y, panelW - PAD * 2, CODE_H);
        editor.codeEditor.setLanguageDefinitionUnformatted();
        editor.setBackground(Surfaces.bordered(ModColors.recessedSurface(), ModColors.BORDER_BASE));
        editor.setLines(editorLines(state.blueprintCodeDraft));
        editor.setOnTextChanged(lines -> state.blueprintCodeDraft = rawCode(lines));
        editor.setFocus(true);
        panel.addWidget(editor);
    }

    private static void applyImport(TabletUiState state) {
        CanvasBlueprint blueprint = CanvasBlueprintCode.decode(state.blueprintCodeDraft);
        if (blueprint.isEmpty()) {
            state.blueprintCodeMessage = TabletModalPanel.tr("ui.questsandstuff.blueprints.code_invalid");
            return;
        }
        String saved = CanvasBlueprintStore.save(blueprint, blueprint.name());
        if (saved.isBlank()) {
            state.blueprintCodeMessage = TabletModalPanel.tr("ui.questsandstuff.blueprints.code_save_failed");
            return;
        }
        state.assetBrowseDir = CanvasBlueprintStore.BLUEPRINTS_DIR;
        state.assetSelected = saved;
        state.assetContextFile = saved;
        state.assetGridScroll = 0;
        close(state);
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] imported code saved path={} entries={}", saved, blueprint.contentCount());
    }

    static List<String> editorLines(String value) {
        String raw = value == null ? "" : value;
        if (raw.isEmpty()) {
            return List.of("");
        }
        return List.of(raw.split("\\R", -1));
    }

    static String rawCode(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        return String.join("\n", lines);
    }
}
