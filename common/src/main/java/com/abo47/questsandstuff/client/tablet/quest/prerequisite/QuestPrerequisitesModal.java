package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.modal.ModalContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalPreviewLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintMiniRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasElementStore;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.ChromeFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

public final class QuestPrerequisitesModal {
    private static final String CONTEXT_ANIMATION_KEY = "prerequisites_manager";
    private static final int HEADER_BUTTON_SIZE = 18;
    private static final int HEADER_CLOSE_ANCHOR_RIGHT_PAD = 26;
    private static final int HEADER_CLOSE_RENDER_X_OFFSET = 1;
    private static final int HEADER_CLOSE_ANCHOR_Y = 4;
    private static final int HEADER_BUTTON_RENDER_Y = 1;
    private static final int HEADER_SEARCH_TO_BUTTON_GAP = 3;
    private static final int HEADER_BUTTON_TO_CLOSE_GAP = 5;

    private QuestPrerequisitesModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        addHeader(modal, state, refresh, w);
        ModalPreviewLayout.Metrics layout = ModalPreviewLayout.calculate(w, h);

        String chapter = TabletStateQueries.selectedChapterName(state);
        PrerequisiteConnectionModel model;
        if (state.modal.prerequisitesManagerEcMode) {
            CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, state.modal.prerequisitesManagerQuestId);
            model = PrerequisiteConnectionModel.buildForEc(ec, chapter, state.modal.prerequisitesManagerSearch);
        } else {
            String questId = safe(state.modal.prerequisitesManagerQuestId);
            CompoundTag questTag = ClientQuestStateFacade.quest(questId);
            model = PrerequisiteConnectionModel.build(questId, questTag, chapter, state.modal.prerequisitesManagerSearch, state.modal.prerequisitesManagerExternalMode);
        }
        TextFieldWidget search = addSearch(modal, state, refresh, layout, w);

        PrerequisiteRowsPanel.add(modal, state, refresh, layout, w, h, model.questId(), model.rows());
        addPreview(modal, state, model, layout, chapter, state.modal.prerequisitesManagerEcMode ? false : state.modal.prerequisitesManagerExternalMode);

        if (state.modal.prerequisitesManagerContextOpen && !state.modal.prerequisitesManagerContextPrerequisiteId.isBlank()) {
            addContextDismissLayer(modal, state, refresh, w, h);
            addConnectionContext(modal, state, player, refresh, w, h, model);
        }
        return search;
    }

    private static void addHeader(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        modal.addWidget(label(8, 6, TabletTranslationKeys.text(QuestTranslationKeys.MODAL_CONNECTIONS_MANAGER), TabletColors.TEXT_PRIMARY));
        int closeAnchorX = w - HEADER_CLOSE_ANCHOR_RIGHT_PAD;
        if (!state.modal.prerequisitesManagerEcMode) {
            int modeX = headerModeButtonX(w);
            String labelKey = state.modal.prerequisitesManagerExternalMode ? QuestTranslationKeys.CONNECTIONS_MODE_EXTERNAL : QuestTranslationKeys.CONNECTIONS_MODE_LOCAL;
            ButtonWidget mode = ChromeFactory.iconButton(modeX, HEADER_BUTTON_RENDER_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "open", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
                state.modal.prerequisitesManagerExternalMode = !state.modal.prerequisitesManagerExternalMode;
                state.modal.prerequisitesManagerScroll = 0;
                state.modal.prerequisitesManagerContextOpen = false;
                state.modal.prerequisitesManagerContextPrerequisiteId = "";
                state.modal.prerequisitesManagerSelectedConnectionKey = "";
                state.modal.prerequisitesManagerHoveredConnectionKey = "";
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] connections manager mode external={}", state.modal.prerequisitesManagerExternalMode);
                refresh.run();
            });
            mode.setHoverTooltips(new Component[]{Component.translatable(labelKey)});
            modal.addWidget(mode);
        }
        TabletModalPanel.addModalClose(modal, closeAnchorX, HEADER_CLOSE_ANCHOR_Y, HEADER_BUTTON_SIZE, state, refresh);
    }

    private static TextFieldWidget addSearch(WidgetGroup modal, TabletUiState state, Runnable refresh, ModalPreviewLayout.Metrics layout, int modalW) {
        int modeX = headerModeButtonX(modalW);
        int searchW = Math.max(40, modeX - layout.rightX() - HEADER_SEARCH_TO_BUTTON_GAP);
        return ModalShell.addSearchField(modal, layout.rightX(), 2, searchW, 16, state.modal.prerequisitesManagerSearch, 80, value -> {
            state.modal.prerequisitesManagerSearch = SearchFilter.normalizeUserInput(value);
            state.modal.prerequisitesManagerScroll = 0;
            state.modal.prerequisitesManagerHoveredConnectionKey = "";
            refresh.run();
        }, focused -> state.modal.prerequisitesManagerSearchFocused = focused);
    }

    private static int headerModeButtonX(int modalW) {
        int closeRenderX = modalW - HEADER_CLOSE_ANCHOR_RIGHT_PAD + HEADER_CLOSE_RENDER_X_OFFSET;
        return closeRenderX - HEADER_BUTTON_SIZE - HEADER_BUTTON_TO_CLOSE_GAP;
    }

    private static void addPreview(WidgetGroup modal, TabletUiState state, PrerequisiteConnectionModel model, ModalPreviewLayout.Metrics layout, String chapter, boolean externalMode) {
        WidgetGroup preview = ModalPreviewLayout.previewPanel(layout);
        preview.addWidget(label(8, 8, crop(model.targetTitle(), 22), TabletColors.TEXT_SECONDARY));
        String countKey = externalMode ? QuestTranslationKeys.CONNECTIONS_EXTERNAL_COUNT : QuestTranslationKeys.CONNECTIONS_LOCAL_COUNT;
        preview.addWidget(label(8, 20, TabletTranslationKeys.text(countKey, model.modeRows().size()), TabletColors.TEXT_MUTED));
        if (!state.modal.prerequisitesManagerSearch.isBlank()) {
            preview.addWidget(label(8, 32, TabletTranslationKeys.text(QuestTranslationKeys.PREREQUISITES_CONNECTION_COUNT, model.rows().size()), TabletColors.TEXT_MUTED));
        }

        CanvasBlueprint blueprint = PrerequisitePreviewBuilder.build(chapter, model, externalMode);
        int previewY = state.modal.prerequisitesManagerSearch.isBlank() ? 42 : 54;
        preview.addWidget(CanvasBlueprintMiniRenderer.previewWidget(
                8,
                previewY,
                layout.leftW() - 16,
                Math.max(24, layout.bodyH() - previewY - 10),
                blueprint,
                () -> model.highlightedQuests(PrerequisiteConnectionModel.highlightedConnections(state.modal.prerequisitesManagerHoveredConnectionKey, state.modal.prerequisitesManagerSelectedConnectionKey)),
                () -> PrerequisiteConnectionModel.highlightedConnections(state.modal.prerequisitesManagerHoveredConnectionKey, state.modal.prerequisitesManagerSelectedConnectionKey)
        ));
        modal.addWidget(preview);
    }

    private static void addContextDismissLayer(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        int rootW = TabletStateQueries.rootWidth(state);
        int rootH = TabletStateQueries.rootHeight(state);
        int modalX = ModalContextMenuPlacement.modalX(state, w);
        int modalY = ModalContextMenuPlacement.modalY(state, h);
        ButtonWidget dismiss = flatHitButton(-modalX, -modalY, rootW, rootH, click -> {
            state.modal.prerequisitesManagerContextOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            refresh.run();
        });
        modal.addWidget(dismiss);
    }

    private static void addConnectionContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h, PrerequisiteConnectionModel model) {
        PrerequisiteConnectionRow row = model.selectedRow(state.modal.prerequisitesManagerSelectedConnectionKey);
        if (row == null) {
            state.modal.prerequisitesManagerContextOpen = false;
            return;
        }
        List<ContextAction> actions = new java.util.ArrayList<>();
        String chapter = TabletStateQueries.selectedChapterName(state);
        String sourceId = row.sourceId();
        String targetId = row.targetId();
        if (!row.exclusiveChoice()) {
            boolean direct = CanvasRenderer.isConnectionDirect(state, chapter, sourceId, targetId);
            actions.add(new ContextAction(
                    direct ? I18n.get("ui.questsandstuff.context.connection_grid") : I18n.get("ui.questsandstuff.context.connection_direct"),
                    "connect", TabletColors.INTERACTIVE, () -> {
                EditorCanvasCommandClient.runConnectionModeAction(player, targetId, sourceId, direct);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager context action=toggle_connection_mode source={} target={}", sourceId, targetId);
                refresh.run();
            }));
        } else {
            boolean direct = ConnectionRenderer.ecIsConnectionDirect(state, chapter, sourceId, targetId);
            actions.add(new ContextAction(
                    direct ? I18n.get("ui.questsandstuff.context.connection_grid") : I18n.get("ui.questsandstuff.context.connection_direct"),
                    "connect", TabletColors.INTERACTIVE, () -> {
                EditorCanvasCommandClient.runEcConnectionModeAction(player, state, sourceId, targetId, !direct);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager context action=toggle_ec_connection_mode source={} target={}", sourceId, targetId);
                refresh.run();
            }));
        }
        int connectionColor = CanvasRenderer.connectionColor(state, chapter, sourceId, targetId);
        actions.add(new ContextAction(
                I18n.get("ui.questsandstuff.context.connection_color"),
                "style_color", TabletColors.INTERACTIVE, () -> {
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(chapter, sourceId, targetId), connectionColor);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager context action=connection_color source={} target={}", sourceId, targetId);
            refresh.run();
        }));
        actions.add(ContextActionFactory.warningDelete(
                state,
                "connection:remove:" + row.key(),
                TabletTranslationKeys.text(QuestTranslationKeys.PREREQUISITES_REMOVE_CONNECTION),
                () -> removeConnection(player, state, row)
        ));
        int ctxW = Math.min(150, Math.max(96, w - 16));
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        ModalContextMenuPlacement.Placement placement = ModalContextMenuPlacement.fitToRootFromModal(state, state.modal.prerequisitesManagerContextX, state.modal.prerequisitesManagerContextY, ctxW, menuH, w, h);
        state.modal.prerequisitesManagerContextMenuX = placement.x();
        state.modal.prerequisitesManagerContextMenuY = placement.y();
        state.modal.prerequisitesManagerContextMenuW = placement.w();
        state.modal.prerequisitesManagerContextMenuH = placement.h();
        modal.addWidget(ContextMenuPanel.build(placement.x(), placement.y(), placement.w(), actions, 0, visibleRows, TabletColors.BORDER_ACCENT, state, action -> {
            if (action.closeAfterClick()) {
                state.modal.prerequisitesManagerContextOpen = false;
                state.modal.prerequisitesManagerContextPrerequisiteId = "";
                ContextMenuController.clearDeleteConfirm(state);
            }
            refresh.run();
        }, CONTEXT_ANIMATION_KEY, w, h));
    }

    private static void removeConnection(Player player, TabletUiState state, PrerequisiteConnectionRow row) {
        if (!PrerequisiteConnectionRemover.canRemove(row)) {
            return;
        }
        String chapter = TabletStateQueries.selectedChapterName(state);
        if (state.modal.prerequisitesManagerEcMode) {
            removeEcConnection(player, state, row);
        } else {
            EditorCanvasCommandClient.runPrerequisiteAction(player, row.targetId(), row.sourceId(), false);
        }
        ConnectionRenderer.removeConnectionTransientState(state, chapter, row.sourceId(), row.targetId());
        PrerequisiteConnectionRemover.clearAfterRemove(state, row);
        QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager action=remove_connection source={} target={}", row.sourceId(), row.targetId());
    }

    private static void removeEcConnection(Player player, TabletUiState state, PrerequisiteConnectionRow row) {
        String chapter = TabletStateQueries.selectedChapterName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, state.modal.prerequisitesManagerQuestId);
        if (ec == null) {
            return;
        }
        String removeId = row.kind() == PrerequisiteConnectionKind.INCOMING ? row.sourceId() : row.targetId();
        CanvasExclusiveChoice updated = ec.removeAllEdgeState(removeId);
        if (!updated.equals(ec)) {
            CanvasElementStore.putCanvasExclusiveChoice(state, chapter, updated, true);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
