package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.QuestIconProvider;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
        String canvasModelTarget = state.modalCanvasModelTarget == null ? "" : state.modalCanvasModelTarget.trim();
        ModalTargetParser.Target details = ModalTargetParser.parse(detailsTarget);
        ModalTargetParser.Target canvasModel = ModalTargetParser.parse(canvasModelTarget);
        boolean entityPicker = !canvasEntityTarget.isBlank() || details.isEntityIconPickerTarget();
        boolean itemModelPicker = canvasModel.isItemModelPickerTarget() || details.isItemModelPickerTarget();
        boolean useItemPicker = isUseItemPickerTarget(details);
        String chapterTarget = state.modalChapterTarget == null || state.modalChapterTarget.isBlank() ? selectedGroupName(state) : state.modalChapterTarget;
        String questTarget = state.modalQuestTarget == null ? "" : state.modalQuestTarget.trim();
        boolean supportsEntityIcons = supportsEntityIconSelection(detailsTarget, questTarget, chapterTarget);
        boolean supportsInventoryIcons = supportsInventoryIconSelection(detailsTarget, questTarget, chapterTarget, canvasEntityTarget, canvasModelTarget);
        if (entityPicker) {
            state.iconTagMode = false;
            state.iconAllItemsMode = false;
            state.iconEntityMode = true;
            state.iconInventoryMode = false;
        } else if (itemModelPicker) {
            state.iconAllItemsMode = false;
            state.iconEntityMode = false;
            state.iconInventoryMode = false;
        } else if (useItemPicker) {
            state.iconEntityMode = false;
        } else if (!supportsEntityIcons) {
            state.iconAllItemsMode = false;
            state.iconEntityMode = false;
        }
        if (!supportsInventoryIcons) {
            state.iconInventoryMode = false;
        }
        if (state.iconInventoryMode) {
            state.iconTagMode = false;
            state.iconAllItemsMode = false;
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
            QuestsAndStuffMod.debugLog("[QnS:UI] icon search mode={} query='{}'", iconModeName(state, entityPicker, useItemPicker), state.iconSearch);
            refresh.run();
        }, focused -> state.iconSearchFocused = focused);

        if (itemModelPicker) {
            TabletModalPanel.addModeToggleIconButton(modal, gridX, headY, modeW, headH, state.iconTagMode ? "mode_tags" : "mode_items", click -> {
                int direction = iconCycleDirection(click.button);
                cycleModelItemMode(state, direction);
                state.iconScroll = 0;
                QuestsAndStuffMod.debugLog("[QnS:UI] icon picker mode={} direction={}", iconModeName(state, false, useItemPicker), cycleDirectionName(direction));
                refresh.run();
            });
        } else if (!entityPicker) {
            TabletModalPanel.addModeToggleIconButton(modal, gridX, headY, modeW, headH, iconModeIcon(state, useItemPicker), click -> {
                int direction = iconCycleDirection(click.button);
                cycleIconMode(state, supportsEntityIcons, supportsInventoryIcons, useItemPicker, direction);
                state.iconScroll = 0;
                QuestsAndStuffMod.debugLog("[QnS:UI] icon picker mode={} direction={}", iconModeName(state, false, useItemPicker), cycleDirectionName(direction));
                refresh.run();
            });
        }

        boolean pickingEntityIcons = entityPicker || state.iconEntityMode;
        boolean pickingInventoryIcons = supportsInventoryIcons && state.iconInventoryMode && !entityPicker && !itemModelPicker;
        if (pickingInventoryIcons) {
            String inventoryTarget = inventoryIconTarget(detailsTarget, questTarget, chapterTarget);
            List<ItemStack> entries = TabletItemInventoryPickerModal.inventoryEntries(player, state.iconSearch);
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
                    QuestVocabulary.text(QuestVocabulary.NO_INVENTORY_ITEMS),
                    ScrollState.bind(
                            () -> state.iconScroll,
                            value -> state.iconScroll = value,
                            () -> state.iconScrollDragging,
                            dragging -> state.iconScrollDragging = dragging
                    ),
                    null,
                    refresh,
                    (surface, stack, index, x, y, tileW, tileH, layout) -> TabletItemInventoryPickerModal.renderStackTile(surface, stack, x, y, picked -> applyInventoryIconPick(player, state, picked, inventoryTarget, refresh))
            );
        } else {
            List<String> entries = itemModelPicker
                    ? searchableModelItemEntries(state.iconSearch, state.iconTagMode)
                    : pickingEntityIcons
                    ? EntityPreviewRenderer.searchableSpawnEggEntries(state.iconSearch)
                    : searchableIconEntries(state, useItemPicker);
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
                    if (!canvasModelTarget.isBlank()) {
                        if (TabletModalPanel.runCanvasModelAction(state, canvasModelTarget, entry)) {
                            closeAll(state);
                        }
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas model picked target={} item={}", canvasModelTarget, entry);
                    } else if (!canvasEntityTarget.isBlank()) {
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
        }
        return search;
    }

    private static List<String> searchableIconEntries(TabletUiState state, boolean useItemPicker) {
        if (useItemPicker && !state.iconAllItemsMode && !state.iconTagMode && !state.iconInventoryMode) {
            return QuestIconProvider.searchableUsableItemEntries(state.iconSearch);
        }
        return QuestIconProvider.searchableEntries(state.iconSearch, state.iconTagMode);
    }

    private static List<String> searchableModelItemEntries(String search, boolean tagMode) {
        return QuestIconProvider.searchableEntries(search, tagMode).stream()
                .filter(entry -> !CanvasModelPreviewRenderer.itemAssetForPick(entry).isBlank())
                .toList();
    }

    private static boolean isUseItemPickerTarget(ModalTargetParser.Target target) {
        return target.isTaskSimpleIcon() && "item_use".equals(typePath(target.type()));
    }

    private static String typePath(String type) {
        String value = type == null ? "" : type.trim();
        int namespaceSeparator = value.indexOf(':');
        return namespaceSeparator >= 0 ? value.substring(namespaceSeparator + 1) : value;
    }

    private static boolean supportsEntityIconSelection(String detailsTarget, String questTarget, String chapterTarget) {
        ModalTargetParser.Target details = ModalTargetParser.parse(detailsTarget);
        if (!details.kind().isBlank()) {
            return details.supportsEntityIconSelection();
        }
        return (questTarget != null && !questTarget.isBlank()) || (chapterTarget != null && !chapterTarget.isBlank());
    }

    private static boolean supportsInventoryIconSelection(String detailsTarget, String questTarget, String chapterTarget, String canvasEntityTarget, String canvasModelTarget) {
        if ((canvasEntityTarget != null && !canvasEntityTarget.isBlank()) || (canvasModelTarget != null && !canvasModelTarget.isBlank())) {
            return false;
        }
        ModalTargetParser.Target details = ModalTargetParser.parse(detailsTarget);
        if (!details.kind().isBlank()) {
            return details.supportsInventoryIconSelection();
        }
        return (questTarget != null && !questTarget.isBlank()) || (chapterTarget != null && !chapterTarget.isBlank());
    }

    private static String inventoryIconTarget(String detailsTarget, String questTarget, String chapterTarget) {
        String details = detailsTarget == null ? "" : detailsTarget.trim();
        if (!details.isBlank()) {
            return details;
        }
        String quest = questTarget == null ? "" : questTarget.trim();
        if (!quest.isBlank()) {
            return ModalTargets.questIcon(quest);
        }
        String chapter = chapterTarget == null ? "" : chapterTarget.trim();
        return chapter.isBlank() ? "" : ModalTargets.chapterIcon(chapter);
    }

    private static void applyInventoryIconPick(Player player, TabletUiState state, ItemStack stack, String target, Runnable refresh) {
        if (target == null || target.isBlank() || stack == null || stack.isEmpty()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] icon inventory pick ignored target={}", target == null ? "" : target);
            refresh.run();
            return;
        }
        state.questDetailsPickTarget = target;
        QuestDetailsWindow.applyInventoryItemPick(player, state, stack);
        closeAll(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] icon inventory pick applied target={} hasNbt={}", target, stack.hasTag());
        refresh.run();
    }

    private static int iconCycleDirection(int button) {
        return button == 1 ? -1 : 1;
    }

    private static String cycleDirectionName(int direction) {
        return direction < 0 ? "backward" : "forward";
    }

    private static void cycleModelItemMode(TabletUiState state, int direction) {
        int next = Math.floorMod((state.iconTagMode ? 1 : 0) + direction, 2);
        state.iconAllItemsMode = false;
        state.iconEntityMode = false;
        state.iconInventoryMode = false;
        state.iconTagMode = next == 1;
    }

    private static void cycleIconMode(TabletUiState state, boolean supportsEntityIcons, boolean supportsInventoryIcons, boolean useItemPicker, int direction) {
        if (useItemPicker) {
            cycleUseItemIconMode(state, supportsInventoryIcons, direction);
            return;
        }
        cycleGeneralIconMode(state, supportsEntityIcons, supportsInventoryIcons, direction);
    }

    private static void cycleUseItemIconMode(TabletUiState state, boolean supportsInventoryIcons, int direction) {
        int modeCount = 3 + (supportsInventoryIcons ? 1 : 0);
        int current = state.iconInventoryMode && supportsInventoryIcons ? 3 : state.iconTagMode ? 2 : state.iconAllItemsMode ? 1 : 0;
        int next = Math.floorMod(current + direction, modeCount);
        clearIconModeFlags(state);
        state.iconAllItemsMode = next == 1;
        state.iconTagMode = next == 2;
        state.iconInventoryMode = supportsInventoryIcons && next == 3;
    }

    private static void cycleGeneralIconMode(TabletUiState state, boolean supportsEntityIcons, boolean supportsInventoryIcons, int direction) {
        int modeCount = 2 + (supportsEntityIcons ? 1 : 0) + (supportsInventoryIcons ? 1 : 0);
        int inventoryMode = supportsInventoryIcons ? modeCount - 1 : -1;
        int current = state.iconInventoryMode && supportsInventoryIcons ? inventoryMode : state.iconEntityMode && supportsEntityIcons ? 2 : state.iconTagMode ? 1 : 0;
        int next = Math.floorMod(current + direction, modeCount);
        clearIconModeFlags(state);
        if (next == inventoryMode) {
            state.iconInventoryMode = true;
            return;
        }
        state.iconTagMode = next == 1;
        state.iconEntityMode = supportsEntityIcons && next == 2;
    }

    private static void clearIconModeFlags(TabletUiState state) {
        state.iconAllItemsMode = false;
        state.iconTagMode = false;
        state.iconEntityMode = false;
        state.iconInventoryMode = false;
    }

    private static String iconModeIcon(TabletUiState state, boolean useItemPicker) {
        if (state.iconInventoryMode) {
            return "mode_inventory";
        }
        if (state.iconEntityMode) {
            return "entity";
        }
        if (state.iconTagMode) {
            return "mode_tags";
        }
        return useItemPicker && !state.iconAllItemsMode ? "send-horizontal" : "mode_items";
    }

    private static String iconModeName(TabletUiState state, boolean entityPicker, boolean useItemPicker) {
        if (state.iconInventoryMode) {
            return "inventory";
        }
        if (entityPicker || state.iconEntityMode) {
            return "entities";
        }
        if (state.iconTagMode) {
            return "tags";
        }
        return useItemPicker && !state.iconAllItemsMode ? "usable_items" : "items";
    }

    private static String pickedEntityIcon(String entry) {
        String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(entry);
        return entityId.isBlank() ? "" : EntityPreviewRenderer.entityAsset(entityId);
    }
}
