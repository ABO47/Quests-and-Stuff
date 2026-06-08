package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintMiniRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.modal.ModalLibraryLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

public final class QuestPrerequisitesManagerModal {
    private static final String CONTEXT_ANIMATION_KEY = "prerequisites_manager";
    private static final int HEADER_BUTTON_SIZE = 18;
    private static final int HEADER_CLOSE_ANCHOR_RIGHT_PAD = 26;
    private static final int HEADER_CLOSE_RENDER_X_OFFSET = 1;
    private static final int HEADER_CLOSE_ANCHOR_Y = 4;
    private static final int HEADER_BUTTON_RENDER_Y = 1;
    private static final int HEADER_SEARCH_TO_BUTTON_GAP = 3;
    private static final int HEADER_BUTTON_TO_CLOSE_GAP = 5;

    private QuestPrerequisitesManagerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        addHeader(modal, state, refresh, w);
        ModalLibraryLayout.Metrics layout = ModalLibraryLayout.calculate(w, h);

        String questId = safe(state.prerequisitesManagerQuestId);
        CompoundTag questTag = ClientQuestCache.quest(questId);
        String group = TabletStateQueries.selectedGroupName(state);
        PrerequisiteConnectionModel model = PrerequisiteConnectionModel.build(questId, questTag, group, state.prerequisitesManagerSearch, state.prerequisitesManagerExternalMode);
        TextFieldWidget search = addSearch(modal, state, refresh, layout, w);

        PrerequisiteRowsPanel.add(modal, state, refresh, layout, w, h, model.questId(), model.rows());
        addPreview(modal, state, model, layout, group, state.prerequisitesManagerExternalMode);

        if (state.prerequisitesManagerContextOpen && !state.prerequisitesManagerContextPrerequisiteId.isBlank()) {
            addContextDismissLayer(modal, state, refresh, w, h);
            addConnectionContext(modal, state, player, refresh, w, h, model);
        }
        return search;
    }

    private static void addHeader(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        modal.addWidget(label(8, 6, TabletVocabulary.text(QuestVocabulary.MODAL_CONNECTIONS_MANAGER), ModColors.TEXT_PRIMARY));
        int closeAnchorX = w - HEADER_CLOSE_ANCHOR_RIGHT_PAD;
        int modeX = headerModeButtonX(w);
        String labelKey = state.prerequisitesManagerExternalMode ? QuestVocabulary.CONNECTIONS_MODE_EXTERNAL : QuestVocabulary.CONNECTIONS_MODE_LOCAL;
        ButtonWidget mode = WindowChrome.iconButton(modeX, HEADER_BUTTON_RENDER_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "open", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
            state.prerequisitesManagerExternalMode = !state.prerequisitesManagerExternalMode;
            state.prerequisitesManagerScroll = 0;
            state.prerequisitesManagerContextOpen = false;
            state.prerequisitesManagerContextPrerequisiteId = "";
            state.prerequisitesManagerSelectedConnectionKey = "";
            state.prerequisitesManagerHoveredConnectionKey = "";
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connections manager mode external={}", state.prerequisitesManagerExternalMode);
            refresh.run();
        });
        mode.setHoverTooltips(new Component[]{Component.translatable(labelKey)});
        modal.addWidget(mode);
        TabletModalPanel.addModalClose(modal, closeAnchorX, HEADER_CLOSE_ANCHOR_Y, HEADER_BUTTON_SIZE, state, refresh);
    }

    private static TextFieldWidget addSearch(WidgetGroup modal, TabletUiState state, Runnable refresh, ModalLibraryLayout.Metrics layout, int modalW) {
        int modeX = headerModeButtonX(modalW);
        int searchW = Math.max(40, modeX - layout.rightX() - HEADER_SEARCH_TO_BUTTON_GAP);
        return ModalShell.addSearchField(modal, layout.rightX(), 2, searchW, 16, state.prerequisitesManagerSearch, 80, value -> {
            state.prerequisitesManagerSearch = SearchFilter.normalizeUserInput(value);
            state.prerequisitesManagerScroll = 0;
            state.prerequisitesManagerHoveredConnectionKey = "";
            refresh.run();
        }, focused -> state.prerequisitesManagerSearchFocused = focused);
    }

    private static int headerModeButtonX(int modalW) {
        int closeRenderX = modalW - HEADER_CLOSE_ANCHOR_RIGHT_PAD + HEADER_CLOSE_RENDER_X_OFFSET;
        return closeRenderX - HEADER_BUTTON_SIZE - HEADER_BUTTON_TO_CLOSE_GAP;
    }

    private static void addPreview(WidgetGroup modal, TabletUiState state, PrerequisiteConnectionModel model, ModalLibraryLayout.Metrics layout, String group, boolean externalMode) {
        WidgetGroup preview = ModalLibraryLayout.previewPanel(layout);
        preview.addWidget(label(8, 8, crop(model.targetTitle(), 22), ModColors.TEXT_SECONDARY));
        String countKey = externalMode ? QuestVocabulary.CONNECTIONS_EXTERNAL_COUNT : QuestVocabulary.CONNECTIONS_LOCAL_COUNT;
        preview.addWidget(label(8, 20, TabletVocabulary.text(countKey, model.modeRows().size()), ModColors.TEXT_MUTED));
        if (!state.prerequisitesManagerSearch.isBlank()) {
            preview.addWidget(label(8, 32, TabletVocabulary.text(QuestVocabulary.PREREQUISITES_CONNECTION_COUNT, model.rows().size()), ModColors.TEXT_MUTED));
        }

        CanvasBlueprint blueprint = PrerequisitePreviewBuilder.build(group, model, externalMode);
        int previewY = state.prerequisitesManagerSearch.isBlank() ? 42 : 54;
        preview.addWidget(CanvasBlueprintMiniRenderer.previewWidget(
                8,
                previewY,
                layout.leftW() - 16,
                Math.max(24, layout.bodyH() - previewY - 10),
                blueprint,
                () -> model.highlightedQuests(PrerequisiteConnectionModel.highlightedConnections(state.prerequisitesManagerHoveredConnectionKey, state.prerequisitesManagerSelectedConnectionKey)),
                () -> PrerequisiteConnectionModel.highlightedConnections(state.prerequisitesManagerHoveredConnectionKey, state.prerequisitesManagerSelectedConnectionKey)
        ));
        modal.addWidget(preview);
    }

    private static void addContextDismissLayer(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        int rootW = TabletStateQueries.rootWidth(state);
        int rootH = TabletStateQueries.rootHeight(state);
        int modalX = ModalContextMenuPlacement.modalX(state, w);
        int modalY = ModalContextMenuPlacement.modalY(state, h);
        ButtonWidget dismiss = flatHitButton(-modalX, -modalY, rootW, rootH, click -> {
            state.prerequisitesManagerContextOpen = false;
            state.contextDeleteConfirmKey = "";
            refresh.run();
        });
        modal.addWidget(dismiss);
    }

    private static void addConnectionContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h, PrerequisiteConnectionModel model) {
        PrerequisiteConnectionRow row = model.selectedRow(state.prerequisitesManagerSelectedConnectionKey);
        if (row == null) {
            state.prerequisitesManagerContextOpen = false;
            return;
        }
        List<ContextAction> actions = List.of(ContextActions.warningDelete(
                state,
                "connection:remove:" + row.key(),
                TabletVocabulary.text(QuestVocabulary.PREREQUISITES_REMOVE_CONNECTION),
                () -> removeConnection(player, state, row)
        ));
        int ctxW = Math.min(150, Math.max(96, w - 16));
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        ModalContextMenuPlacement.Placement placement = ModalContextMenuPlacement.fitToRootFromModal(state, state.prerequisitesManagerContextX, state.prerequisitesManagerContextY, ctxW, menuH, w, h);
        state.prerequisitesManagerContextMenuX = placement.x();
        state.prerequisitesManagerContextMenuY = placement.y();
        state.prerequisitesManagerContextMenuW = placement.w();
        state.prerequisitesManagerContextMenuH = placement.h();
        modal.addWidget(ContextMenuPanel.build(placement.x(), placement.y(), placement.w(), actions, 0, visibleRows, ModColors.BORDER_ACCENT, state, action -> {
            if (action.closeAfterClick()) {
                state.prerequisitesManagerContextOpen = false;
                state.prerequisitesManagerContextPrerequisiteId = "";
                state.contextDeleteConfirmKey = "";
            }
            refresh.run();
        }, CONTEXT_ANIMATION_KEY, w, h));
    }

    private static void removeConnection(Player player, TabletUiState state, PrerequisiteConnectionRow row) {
        if (!PrerequisiteConnectionActions.canRemove(row)) {
            return;
        }
        EditorCommandClient.runPrerequisiteAction(player, row.targetId(), row.sourceId(), false);
        PrerequisiteConnectionActions.clearAfterRemove(state, row);
        QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager action=remove_connection source={} target={}", row.sourceId(), row.targetId());
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
