package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.QuestIconProvider;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTENT_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runGroupAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runQuestIconAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletIconPickerModal {
    private TabletIconPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        int sidePad = 8;
        String detailsTarget = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget.trim();
        String canvasEntityTarget = state.modalCanvasEntityTarget == null ? "" : state.modalCanvasEntityTarget.trim();
        boolean entityPicker = !canvasEntityTarget.isBlank() || isEntityPickerTarget(detailsTarget);
        boolean supportsEntityIcons = supportsEntityIconSelection(detailsTarget, state.modalQuestTarget, state.modalChapterTarget);
        if (entityPicker) {
            state.iconTagMode = false;
            state.iconEntityMode = true;
        } else if (!supportsEntityIcons) {
            state.iconEntityMode = false;
        }
        int headY = 22;
        int headH = 18;
        int modeW = entityPicker ? 0 : headH;
        int gap = 4;
        int gridX = sidePad;
        int gridW = w - sidePad * 2;
        int searchX = gridX + modeW + (entityPicker ? 0 : gap);
        int searchW = gridW - modeW - (entityPicker ? 0 : gap);
        int gridY = headY + headH + 4;
        int gridH = h - gridY - 8;
        int slot = 18;

        TabletModalPanel.addModalClose(modal, gridX + gridW - headH, 4, headH, state, refresh);
        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, state.iconSearch, 80, value -> {
            state.iconSearch = SearchFilter.normalizeUserInput(value);
            state.iconScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] icon search mode={} query='{}'", iconModeName(state, entityPicker), state.iconSearch);
            refresh.run();
        }, focused -> state.iconSearchFocused = focused);

        if (!entityPicker) {
            TabletModalPanel.addModeToggleIconButton(modal, gridX, headY, modeW, headH, iconModeIcon(state), click -> {
                cycleIconMode(state, supportsEntityIcons);
                state.iconScroll = 0;
                QuestsAndStuffMod.debugLog("[QnS:UI] icon picker mode={}", iconModeName(state, false));
                refresh.run();
            });
        }

        boolean pickingEntityIcons = entityPicker || state.iconEntityMode;
        List<String> entries = pickingEntityIcons
                ? EntityPreviewRenderer.searchableSpawnEggEntries(state.iconSearch)
                : QuestIconProvider.searchableEntries(state.iconSearch, state.iconTagMode);
        String chapterTarget = state.modalChapterTarget == null || state.modalChapterTarget.isBlank() ? selectedGroupName(state) : state.modalChapterTarget;
        String questTarget = state.modalQuestTarget == null ? "" : state.modalQuestTarget.trim();
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                slot,
                slot,
                0,
                6,
                6,
                entries,
                TabletModalPanel.tr("ui.questsandstuff.common.none_short"),
                ScrollState.bind(
                        () -> state.iconScroll,
                        value -> state.iconScroll = value,
                        () -> state.iconScrollDragging,
                        dragging -> state.iconScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, entry, index, x, y, tileW, tileH, layout) -> {
            String pickedIcon = pickingEntityIcons ? pickedEntityIcon(entry) : entry;
            String previewIcon = pickedIcon.isBlank() ? entry : pickedIcon;
            surface.addWidget(new ImageWidget(x, y, 18, 18, SlotWidget.ITEM_SLOT_TEXTURE));
            surface.addWidget(new DisplayIconWidget(x + 1, y + 1, CONTENT_ICON_SIZE, CONTENT_ICON_SIZE, previewIcon));
            ButtonWidget hit = flatHitButton(x + 1, y + 1, CONTENT_ICON_SIZE, CONTENT_ICON_SIZE, click -> {
                boolean doubleClick = click.button == 0
                        && TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("icon", chapterTarget, questTarget, previewIcon));
                if (!canvasEntityTarget.isBlank()) {
                    if (TabletModalPanel.runCanvasEntityAction(player, state, canvasEntityTarget, entry)) {
                        closeAll(state);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity picked group={} item={}", canvasEntityTarget, entry);
                } else if (!detailsTarget.isBlank()) {
                    String detailsPick = entityPicker ? entry : (pickingEntityIcons && !pickedIcon.isBlank() ? pickedIcon : entry);
                    QuestDetailsWindow.applyIconPick(player, state, detailsPick);
                    closeAll(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details icon picked target={} icon={}", detailsTarget, previewIcon);
                } else if (!questTarget.isBlank()) {
                    runQuestIconAction(player, questTarget, pickingEntityIcons && !pickedIcon.isBlank() ? pickedIcon : entry);
                    closeAll(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] icon picked target={} quest={} icon={}", chapterTarget, questTarget, previewIcon);
                } else {
                    runGroupAction(player, state, "set_icon", chapterTarget, pickingEntityIcons && !pickedIcon.isBlank() ? pickedIcon : entry, 0);
                    if (doubleClick) {
                        closeAll(state);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] icon picked target={} quest={} icon={}", chapterTarget, questTarget, previewIcon);
                }
                refresh.run();
            });
            hit.setHoverTooltips(TabletModalPanel.iconTooltip(previewIcon));
            hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
            hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
            surface.addWidget(hit);
                });
        return search;
    }

    private static boolean isEntityPickerTarget(String target) {
        return ModalTargetParser.parse(target).isEntityIconPickerTarget();
    }

    private static boolean supportsEntityIconSelection(String detailsTarget, String questTarget, String chapterTarget) {
        ModalTargetParser.Target details = ModalTargetParser.parse(detailsTarget);
        if (!details.kind().isBlank()) {
            return details.supportsEntityIconSelection();
        }
        return (questTarget != null && !questTarget.isBlank()) || (chapterTarget != null && !chapterTarget.isBlank());
    }

    private static void cycleIconMode(TabletUiState state, boolean supportsEntityIcons) {
        if (state.iconEntityMode) {
            state.iconEntityMode = false;
            state.iconTagMode = false;
        } else if (state.iconTagMode && supportsEntityIcons) {
            state.iconTagMode = false;
            state.iconEntityMode = true;
        } else {
            state.iconTagMode = !state.iconTagMode;
            state.iconEntityMode = false;
        }
    }

    private static String iconModeIcon(TabletUiState state) {
        if (state.iconEntityMode) {
            return "entity";
        }
        return state.iconTagMode ? "mode_tags" : "mode_items";
    }

    private static String iconModeName(TabletUiState state, boolean entityPicker) {
        if (entityPicker || state.iconEntityMode) {
            return "entities";
        }
        return state.iconTagMode ? "tags" : "items";
    }

    private static String pickedEntityIcon(String entry) {
        String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(entry);
        return entityId.isBlank() ? "" : EntityPreviewRenderer.entityAsset(entityId);
    }
}
