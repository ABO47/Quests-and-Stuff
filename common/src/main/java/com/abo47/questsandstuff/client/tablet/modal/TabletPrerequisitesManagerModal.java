package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.blueprint.CanvasBlueprintMiniRenderer;
import com.abo47.questsandstuff.client.canvas.blueprint.ClientQuestDefinitionSnapshots;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.TileGridLayout;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletPrerequisitesManagerModal {
    private static final String CONTEXT_ANIMATION_KEY = "prerequisites_manager";
    private static final int ROW_H = 30;
    private static final int ROW_GAP = 4;
    private static final int PAD = 6;
    private static final int HEADER_BUTTON_SIZE = 18;
    private static final int HEADER_CLOSE_ANCHOR_RIGHT_PAD = 26;
    private static final int HEADER_CLOSE_RENDER_X_OFFSET = 1;
    private static final int HEADER_CLOSE_ANCHOR_Y = 4;
    private static final int HEADER_BUTTON_RENDER_Y = 1;
    private static final int HEADER_SEARCH_TO_BUTTON_GAP = 3;
    private static final int HEADER_BUTTON_TO_CLOSE_GAP = 5;
    private static final int COMPACT_X_OFFSET = 128;
    private static final int COMPACT_Y_STEP = 70;

    private TabletPrerequisitesManagerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        addHeader(modal, state, refresh, w);
        ModalLibraryLayout.Metrics layout = ModalLibraryLayout.calculate(w, h);

        String questId = safe(state.prerequisitesManagerQuestId);
        CompoundTag questTag = ClientQuestCache.quest(questId);
        String targetTitle = questTitle(questId, questTag);
        List<ConnectionRow> allRows = connectionRows(questId, questTag, targetTitle);
        String group = TabletUiFactory.selectedGroupName(state);
        List<ConnectionRow> modeRows = rowsForMode(allRows, group, state.prerequisitesManagerExternalMode);
        List<ConnectionRow> rows = filteredRows(modeRows, state.prerequisitesManagerSearch);
        TextFieldWidget search = addSearch(modal, state, refresh, layout, w);

        int rowW = rowWidth(layout, rows.size());
        TileGridLayout rowLayout = TileGridLayout.calculate(layout.rightW(), layout.bodyH(), rowW, ROW_H, ROW_GAP, PAD, PAD, rows.size(), state.prerequisitesManagerScroll);
        state.prerequisitesManagerScroll = rowLayout.scrollStart();
        addConnectionHoverTracker(modal, state, rows, layout, rowLayout);
        addConnectionList(modal, state, refresh, layout, w, h, questId, rows, rowW);
        addPreview(modal, state, questId, questTag, targetTitle, modeRows, rows, layout, state.prerequisitesManagerExternalMode);

        if (state.prerequisitesManagerContextOpen && !state.prerequisitesManagerContextPrerequisiteId.isBlank()) {
            addContextDismissLayer(modal, state, refresh, w, h);
            addConnectionContext(modal, state, player, refresh, w, h, questId);
        }
        return search;
    }

    private static void addHeader(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        modal.addWidget(label(8, 6, TabletModalPanel.tr(QuestVocabulary.MODAL_CONNECTIONS_MANAGER), ModColors.TEXT_PRIMARY));
        int closeAnchorX = w - HEADER_CLOSE_ANCHOR_RIGHT_PAD;
        int modeX = headerModeButtonX(w);
        String labelKey = state.prerequisitesManagerExternalMode ? QuestVocabulary.CONNECTIONS_MODE_EXTERNAL : QuestVocabulary.CONNECTIONS_MODE_LOCAL;
        ButtonWidget mode = WindowChrome.iconButton(modeX, HEADER_BUTTON_RENDER_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "external-link", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
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

    private static int rowWidth(ModalLibraryLayout.Metrics layout, int rowCount) {
        int contentH = Math.max(ROW_H, layout.bodyH() - PAD * 2);
        int visibleRows = Math.max(1, (contentH + ROW_GAP) / (ROW_H + ROW_GAP));
        boolean showScroll = rowCount > visibleRows;
        int scrollReserve = showScroll ? DragScrollBarWidget.RESERVED_WIDTH + ROW_GAP : 0;
        return Math.max(96, layout.rightW() - PAD * 2 - scrollReserve);
    }

    private static void addPreview(WidgetGroup modal, TabletUiState state, String questId, CompoundTag questTag, String targetTitle, List<ConnectionRow> modeRows, List<ConnectionRow> rows, ModalLibraryLayout.Metrics layout, boolean externalMode) {
        WidgetGroup preview = ModalLibraryLayout.previewPanel(layout);
        preview.addWidget(label(8, 8, crop(targetTitle, 22), ModColors.TEXT_SECONDARY));
        String countKey = externalMode ? QuestVocabulary.CONNECTIONS_EXTERNAL_COUNT : QuestVocabulary.CONNECTIONS_LOCAL_COUNT;
        preview.addWidget(label(8, 20, TabletModalPanel.tr(countKey, modeRows.size()), ModColors.TEXT_MUTED));
        if (!state.prerequisitesManagerSearch.isBlank()) {
            preview.addWidget(label(8, 32, TabletModalPanel.tr(QuestVocabulary.PREREQUISITES_CONNECTION_COUNT, rows.size()), ModColors.TEXT_MUTED));
        }

        CanvasBlueprint blueprint = previewBlueprint(state, questId, questTag, rows, externalMode);
        int previewY = state.prerequisitesManagerSearch.isBlank() ? 42 : 54;
        preview.addWidget(CanvasBlueprintMiniRenderer.previewWidget(
                8,
                previewY,
                layout.leftW() - 16,
                Math.max(24, layout.bodyH() - previewY - 10),
                blueprint,
                () -> highlightedQuests(rows, highlightedConnections(state)),
                () -> highlightedConnections(state)
        ));
        modal.addWidget(preview);
    }

    private static void addConnectionList(WidgetGroup modal, TabletUiState state, Runnable refresh, ModalLibraryLayout.Metrics layout, int modalW, int modalH, String questId, List<ConnectionRow> rows, int rowW) {
        TiledPickerPanel.add(
                modal,
                layout.rightX(),
                layout.bodyY(),
                layout.rightW(),
                layout.bodyH(),
                rowW,
                ROW_H,
                ROW_GAP,
                PAD,
                PAD,
                rows,
                TabletModalPanel.tr(QuestVocabulary.PREREQUISITES_NO_CONNECTIONS),
                ScrollState.bind(
                        () -> state.prerequisitesManagerScroll,
                        value -> state.prerequisitesManagerScroll = value,
                        () -> state.prerequisitesManagerScrollDragging,
                        dragging -> state.prerequisitesManagerScrollDragging = dragging
                ),
                () -> {
                    state.prerequisitesManagerContextOpen = false;
                    state.contextDeleteConfirmKey = "";
                },
                refresh,
                (surface, row, index, x, y, cellW, cellH, tileLayout) -> renderConnectionRow(surface, state, refresh, modalW, modalH, questId, row, x, y, cellW, cellH)
        );
    }

    private static void renderConnectionRow(WidgetGroup surface, TabletUiState state, Runnable refresh, int modalW, int modalH, String questId, ConnectionRow row, int x, int y, int cellW, int cellH) {
        boolean selected = row.key().equals(state.prerequisitesManagerSelectedConnectionKey);
        boolean hovered = row.key().equals(state.prerequisitesManagerHoveredConnectionKey);
        WidgetGroup card = new WidgetGroup(x, y, cellW, cellH);
        card.setBackground(Surfaces.bordered(
                selected || hovered ? withAlpha(ModColors.INTERACTIVE, selected ? 64 : 44) : withAlpha(ModColors.SURFACE_PANEL_ALT, 106),
                selected || hovered ? ModColors.BORDER_ACCENT : withAlpha(ModColors.BORDER_BASE, 120)
        ));
        card.addWidget(new DisplayIconWidget(5, 7, 16, 16, row.icon()));
        int textW = Math.max(10, cellW - 34);
        String role = TabletModalPanel.tr(row.kind() == ConnectionKind.INCOMING ? QuestVocabulary.PREREQUISITES_INCOMING : QuestVocabulary.PREREQUISITES_OUTGOING);
        card.addWidget(label(26, 4, crop(role + ": " + row.otherTitle(), Math.max(8, textW / 6)), ModColors.TEXT_SECONDARY));
        card.addWidget(label(26, 17, crop(row.sourceTitle() + " -> " + row.targetTitle(), Math.max(8, textW / 6)), ModColors.TEXT_MUTED));
        surface.addWidget(card);

        ButtonWidget hit = flatHitButton(x, y, cellW, cellH, click -> {
            state.prerequisitesManagerSelectedConnectionKey = row.key();
            if (click.button == 1) {
                openConnectionContextAtPointer(state, modalW, modalH, row);
            } else if (click.button == 0) {
                state.prerequisitesManagerContextOpen = false;
                state.contextDeleteConfirmKey = "";
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager row_click quest={} connection={} button={}", questId, row.key(), click.button);
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 38)));
        surface.addWidget(hit);
    }

    private static void addConnectionHoverTracker(WidgetGroup modal, TabletUiState state, List<ConnectionRow> rows, ModalLibraryLayout.Metrics layout, TileGridLayout rowLayout) {
        WidgetGroup tracker = new WidgetGroup(layout.rightX(), layout.bodyY(), layout.rightW(), layout.bodyH()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                state.prerequisitesManagerHoveredConnectionKey = hoveredConnectionKey(this, rows, rowLayout, mouseX, mouseY);
            }
        };
        modal.addWidget(tracker);
    }

    private static String hoveredConnectionKey(WidgetGroup tracker, List<ConnectionRow> rows, TileGridLayout layout, int mouseX, int mouseY) {
        int localX = mouseX - tracker.getPositionX();
        int localY = mouseY - tracker.getPositionY();
        if (localX < 0 || localY < 0 || localX >= tracker.getSizeWidth() || localY >= tracker.getSizeHeight()) {
            return "";
        }
        for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
            int visibleIndex = i - layout.scrollStart();
            int rowX = layout.tileX(visibleIndex);
            int rowY = layout.tileY(visibleIndex);
            if (localX >= rowX && localX < rowX + layout.tileW() && localY >= rowY && localY < rowY + layout.tileH()) {
                return rows.get(i).key();
            }
        }
        return "";
    }

    private static void openConnectionContextAtPointer(TabletUiState state, int modalW, int modalH, ConnectionRow row) {
        state.prerequisitesManagerContextOpen = true;
        state.prerequisitesManagerContextPrerequisiteId = row.sourceId();
        state.prerequisitesManagerSelectedConnectionKey = row.key();
        state.prerequisitesManagerContextX = ModalContextMenuPlacement.localPointerX(state, modalW);
        state.prerequisitesManagerContextY = ModalContextMenuPlacement.localPointerY(state, modalH);
        state.contextDeleteConfirmKey = "";
        ContextMenuAnimation.start(state, CONTEXT_ANIMATION_KEY);
    }

    private static void addContextDismissLayer(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        int rootW = TabletUiFactory.rootWidth(state);
        int rootH = TabletUiFactory.rootHeight(state);
        int modalX = ModalContextMenuPlacement.modalX(state, w);
        int modalY = ModalContextMenuPlacement.modalY(state, h);
        ButtonWidget dismiss = flatHitButton(-modalX, -modalY, rootW, rootH, click -> {
            state.prerequisitesManagerContextOpen = false;
            state.contextDeleteConfirmKey = "";
            refresh.run();
        });
        modal.addWidget(dismiss);
    }

    private static void addConnectionContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h, String questId) {
        ConnectionRow row = selectedContextRow(state, questId);
        if (row == null) {
            state.prerequisitesManagerContextOpen = false;
            return;
        }
        List<ContextAction> actions = List.of(ContextActions.warningDelete(
                state,
                "connection:remove:" + row.key(),
                TabletModalPanel.tr(QuestVocabulary.PREREQUISITES_REMOVE_CONNECTION),
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

    private static ConnectionRow selectedContextRow(TabletUiState state, String questId) {
        for (ConnectionRow row : connectionRows(questId, ClientQuestCache.quest(questId), questTitle(questId, ClientQuestCache.quest(questId)))) {
            if (row.key().equals(state.prerequisitesManagerSelectedConnectionKey)) {
                return row;
            }
        }
        return null;
    }

    private static void removeConnection(Player player, TabletUiState state, ConnectionRow row) {
        if (row == null || row.sourceId().isBlank() || row.targetId().isBlank()) {
            return;
        }
        EditorCommandClient.runPrerequisiteAction(player, row.targetId(), row.sourceId(), false);
        if (row.key().equals(state.prerequisitesManagerSelectedConnectionKey)) {
            state.prerequisitesManagerSelectedConnectionKey = "";
        }
        if (row.key().equals(state.prerequisitesManagerHoveredConnectionKey)) {
            state.prerequisitesManagerHoveredConnectionKey = "";
        }
        state.prerequisitesManagerContextOpen = false;
        state.prerequisitesManagerContextPrerequisiteId = "";
        state.contextDeleteConfirmKey = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager action=remove_connection source={} target={}", row.sourceId(), row.targetId());
    }

    private static List<ConnectionRow> connectionRows(String questId, CompoundTag questTag, String targetTitle) {
        if (questId.isBlank() || questTag == null || questTag.isEmpty()) {
            return List.of();
        }
        Map<String, ConnectionRow> rows = new LinkedHashMap<>();
        addIncomingRows(rows, questId, questTag, targetTitle);
        addOutgoingRows(rows, questId, targetTitle);
        return List.copyOf(rows.values());
    }

    private static void addIncomingRows(Map<String, ConnectionRow> rows, String questId, CompoundTag questTag, String targetTitle) {
        ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            String sourceId = safe(prerequisites.getString(i));
            if (sourceId.isBlank()) {
                continue;
            }
            CompoundTag sourceTag = ClientQuestCache.quest(sourceId);
            ConnectionRow row = new ConnectionRow(
                    sourceId,
                    questId,
                    questTitle(sourceId, sourceTag),
                    targetTitle,
                    questTitle(sourceId, sourceTag),
                    sourceTag == null ? "" : sourceTag.getString("icon"),
                    ConnectionKind.INCOMING
            );
            rows.putIfAbsent(row.key(), row);
        }
    }

    private static void addOutgoingRows(Map<String, ConnectionRow> rows, String questId, String sourceTitle) {
        for (Map.Entry<String, CompoundTag> entry : ClientQuestCache.questEntries()) {
            String targetId = entry.getKey();
            if (questId.equals(targetId)) {
                continue;
            }
            CompoundTag targetTag = entry.getValue();
            if (!hasPrerequisite(targetTag, questId)) {
                continue;
            }
            ConnectionRow row = new ConnectionRow(
                    questId,
                    targetId,
                    sourceTitle,
                    questTitle(targetId, targetTag),
                    questTitle(targetId, targetTag),
                    targetTag == null ? "" : targetTag.getString("icon"),
                    ConnectionKind.OUTGOING
            );
            rows.putIfAbsent(row.key(), row);
        }
    }

    private static boolean hasPrerequisite(CompoundTag questTag, String prerequisiteId) {
        if (questTag == null || prerequisiteId == null || prerequisiteId.isBlank()) {
            return false;
        }
        ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            if (prerequisiteId.equals(prerequisites.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private static List<ConnectionRow> filteredRows(List<ConnectionRow> rows, String query) {
        if (SearchFilter.normalize(query).isBlank()) {
            return rows;
        }
        List<ConnectionRow> filtered = new ArrayList<>();
        for (ConnectionRow row : rows) {
            String display = row.sourceTitle() + " " + row.targetTitle() + " " + row.otherTitle();
            if (SearchFilter.matches(query, row.sourceId() + " " + row.targetId(), display)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<ConnectionRow> rowsForMode(List<ConnectionRow> rows, String group, boolean externalMode) {
        List<ConnectionRow> filtered = new ArrayList<>();
        for (ConnectionRow row : rows) {
            boolean local = isLocalConnection(row, group);
            if ((externalMode && !local) || (!externalMode && local)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static boolean isLocalConnection(ConnectionRow row, String group) {
        if (group == null || group.isBlank()) {
            return true;
        }
        return questInGroup(row.sourceId(), group) && questInGroup(row.targetId(), group);
    }

    private static boolean questInGroup(String questId, String group) {
        CompoundTag questTag = ClientQuestCache.quest(questId);
        if (questTag == null || questTag.isEmpty() || group == null || group.isBlank()) {
            return false;
        }
        return questTag.getCompound("groups").contains(group, Tag.TAG_COMPOUND);
    }

    private static CanvasBlueprint previewBlueprint(TabletUiState state, String questId, CompoundTag questTag, List<ConnectionRow> rows, boolean externalMode) {
        QuestDefinition focus = ClientQuestDefinitionSnapshots.fromClientTag(questId, questTag);
        if (focus == null) {
            return CanvasBlueprint.empty();
        }

        String group = TabletUiFactory.selectedGroupName(state);
        Map<String, QuestDefinition> definitions = definitionsForPreview(questId, focus, rows);
        Map<String, QuestPlacement> placements = placementsForPreview(group, questId, definitions, rows, externalMode);
        Map<String, Set<String>> prerequisitesByTarget = prerequisitesByTarget(rows);
        List<CanvasBlueprint.QuestEntry> entries = new ArrayList<>();
        List<String> order = new ArrayList<>();
        for (Map.Entry<String, QuestDefinition> entry : definitions.entrySet()) {
            String id = entry.getKey();
            if (questId.equals(id)) {
                continue;
            }
            QuestPlacement placement = placements.get(id);
            if (placement == null) {
                continue;
            }
            entries.add(new CanvasBlueprint.QuestEntry(id, placement.group(), placement.x(), placement.y(), placement.scale(), withPrerequisites(entry.getValue(), prerequisitesByTarget.getOrDefault(id, Set.of()))));
            order.add(CanvasLayerOrdering.questKey(id));
        }
        QuestPlacement focusPlacement = placements.get(questId);
        if (focusPlacement != null) {
            entries.add(new CanvasBlueprint.QuestEntry(questId, focusPlacement.group(), focusPlacement.x(), focusPlacement.y(), focusPlacement.scale(), withPrerequisites(focus, prerequisitesByTarget.getOrDefault(questId, Set.of()))));
            order.add(CanvasLayerOrdering.questKey(questId));
        }
        Origin origin = origin(entries);
        return new CanvasBlueprint(focus.display().title(), origin.x(), origin.y(), entries, List.of(), List.of(), order);
    }

    private static Map<String, QuestDefinition> definitionsForPreview(String questId, QuestDefinition focus, List<ConnectionRow> rows) {
        Map<String, QuestDefinition> definitions = new LinkedHashMap<>();
        definitions.put(questId, focus);
        for (ConnectionRow row : rows) {
            addDefinition(definitions, row.sourceId());
            addDefinition(definitions, row.targetId());
        }
        return definitions;
    }

    private static void addDefinition(Map<String, QuestDefinition> definitions, String questId) {
        if (definitions.containsKey(questId)) {
            return;
        }
        QuestDefinition definition = ClientQuestDefinitionSnapshots.fromClientTag(questId, ClientQuestCache.quest(questId));
        if (definition != null) {
            definitions.put(questId, definition);
        }
    }

    private static Map<String, QuestPlacement> placementsForPreview(String group, String focusId, Map<String, QuestDefinition> definitions, List<ConnectionRow> rows, boolean externalMode) {
        if (externalMode) {
            return compactPlacements(group, focusId, definitions, rows);
        }
        Map<String, QuestPlacement> placements = new LinkedHashMap<>();
        for (Map.Entry<String, QuestDefinition> entry : definitions.entrySet()) {
            placements.put(entry.getKey(), actualPlacement(entry.getValue(), group));
        }
        return placements;
    }

    private static Map<String, QuestPlacement> compactPlacements(String group, String focusId, Map<String, QuestDefinition> definitions, List<ConnectionRow> rows) {
        Map<String, QuestPlacement> placements = new LinkedHashMap<>();
        placements.put(focusId, new QuestPlacement(group, 0, 0, 1.0f));
        List<String> incoming = uniqueOtherIds(rows, focusId, ConnectionKind.INCOMING);
        List<String> outgoing = uniqueOtherIds(rows, focusId, ConnectionKind.OUTGOING);
        addCompactColumn(placements, group, incoming, -COMPACT_X_OFFSET);
        addCompactColumn(placements, group, outgoing, COMPACT_X_OFFSET);
        for (String id : definitions.keySet()) {
            placements.putIfAbsent(id, new QuestPlacement(group, 0, (placements.size() + 1) * COMPACT_Y_STEP, 1.0f));
        }
        return placements;
    }

    private static List<String> uniqueOtherIds(List<ConnectionRow> rows, String focusId, ConnectionKind kind) {
        Set<String> values = new LinkedHashSet<>();
        for (ConnectionRow row : rows) {
            if (row.kind() != kind) {
                continue;
            }
            values.add(focusId.equals(row.sourceId()) ? row.targetId() : row.sourceId());
        }
        return List.copyOf(values);
    }

    private static void addCompactColumn(Map<String, QuestPlacement> placements, String group, List<String> ids, int x) {
        int count = ids.size();
        for (int i = 0; i < ids.size(); i++) {
            int y = Math.round((i - (count - 1) / 2.0f) * COMPACT_Y_STEP);
            placements.put(ids.get(i), new QuestPlacement(group, x, y, 1.0f));
        }
    }

    private static QuestPlacement actualPlacement(QuestDefinition definition, String preferredGroup) {
        ChapterDefinition preferred = definition.display().groups().get(preferredGroup);
        if (preferred != null) {
            return new QuestPlacement(preferredGroup, preferred.x(), preferred.y(), preferred.scale());
        }
        for (Map.Entry<String, ChapterDefinition> entry : definition.display().groups().entrySet()) {
            ChapterDefinition view = entry.getValue();
            return new QuestPlacement(entry.getKey(), view.x(), view.y(), view.scale());
        }
        return new QuestPlacement(preferredGroup, 0, 0, 1.0f);
    }

    private static Map<String, Set<String>> prerequisitesByTarget(List<ConnectionRow> rows) {
        Map<String, Set<String>> prerequisites = new LinkedHashMap<>();
        for (ConnectionRow row : rows) {
            prerequisites.computeIfAbsent(row.targetId(), ignored -> new LinkedHashSet<>()).add(row.sourceId());
        }
        return prerequisites;
    }

    private static QuestDefinition withPrerequisites(QuestDefinition definition, Set<String> prerequisites) {
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    private static Set<String> highlightedConnections(TabletUiState state) {
        String key = !state.prerequisitesManagerHoveredConnectionKey.isBlank()
                ? state.prerequisitesManagerHoveredConnectionKey
                : state.prerequisitesManagerSelectedConnectionKey;
        return key.isBlank() ? Set.of() : Set.of(key);
    }

    private static Set<String> highlightedQuests(List<ConnectionRow> rows, Set<String> highlightedConnections) {
        if (highlightedConnections.isEmpty()) {
            return Set.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        for (ConnectionRow row : rows) {
            if (highlightedConnections.contains(row.key())) {
                ids.add(row.sourceId());
                ids.add(row.targetId());
            }
        }
        return ids;
    }

    private static Origin origin(List<CanvasBlueprint.QuestEntry> entries) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (CanvasBlueprint.QuestEntry entry : entries) {
            minX = Math.min(minX, entry.sourceX());
            minY = Math.min(minY, entry.sourceY());
        }
        return minX == Integer.MAX_VALUE ? new Origin(0, 0) : new Origin(minX, minY);
    }

    private static String questTitle(String questId, CompoundTag questTag) {
        String title = questTag == null ? "" : questTag.getString("title");
        if (title != null && !title.isBlank()) {
            return title;
        }
        return questId == null || questId.isBlank() ? TabletModalPanel.tr(QuestVocabulary.COMMON_UNKNOWN) : questId;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String connectionKey(String sourceId, String targetId) {
        return sourceId + "->" + targetId;
    }

    private enum ConnectionKind {
        INCOMING,
        OUTGOING
    }

    private record ConnectionRow(String sourceId, String targetId, String sourceTitle, String targetTitle, String otherTitle, String icon, ConnectionKind kind) {
        String key() {
            return connectionKey(sourceId, targetId);
        }
    }

    private record QuestPlacement(String group, int x, int y, float scale) {
    }

    private record Origin(int x, int y) {
    }
}
