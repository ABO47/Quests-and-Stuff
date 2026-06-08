package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TabletCycleButton;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.model.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
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
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.CANVAS_ENTITY;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.CANVAS_MODEL;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.CHAPTER;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.QUEST;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.QUEST_DETAILS_PICK;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTENT_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runGroupAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runQuestIconAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletIconPickerModal {
    private TabletIconPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        int sidePad = 8;
        String detailsTarget = ModalTargetState.target(state, QUEST_DETAILS_PICK, state.questDetailsPickTarget);
        String canvasEntityTarget = ModalTargetState.target(state, CANVAS_ENTITY, state.modalCanvasEntityTarget);
        String canvasModelTarget = ModalTargetState.target(state, CANVAS_MODEL, state.modalCanvasModelTarget);
        ModalTargetParser.Target details = ModalTargetState.parsedTarget(state, QUEST_DETAILS_PICK, state.questDetailsPickTarget);
        ModalTargetParser.Target canvasModel = ModalTargetState.parsedTarget(state, CANVAS_MODEL, state.modalCanvasModelTarget);
        boolean entityPicker = !canvasEntityTarget.isBlank() || details.isEntityIconPickerTarget();
        boolean itemModelPicker = canvasModel.isItemModelPickerTarget() || details.isItemModelPickerTarget();
        boolean useItemPicker = IconPickerMode.isUseItemPickerTarget(details);
        String resolvedChapterTarget = ModalTargetState.target(state, CHAPTER, state.modalChapterTarget);
        final String chapterTarget = resolvedChapterTarget.isBlank() ? selectedGroupName(state) : resolvedChapterTarget;
        String questTarget = ModalTargetState.target(state, QUEST, state.modalQuestTarget);
        boolean supportsEntityIcons = supportsEntityIconSelection(detailsTarget, questTarget, chapterTarget);
        boolean supportsInventoryIcons = supportsInventoryIconSelection(detailsTarget, questTarget, chapterTarget, canvasEntityTarget, canvasModelTarget);
        IconPickerMode.normalizeForContext(state, entityPicker, itemModelPicker, supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        IconPickerMode mode = IconPickerMode.safe(state.iconMode);
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
            QuestsAndStuffMod.debugLog("[QnS:UI] icon search mode={} query='{}'", IconPickerMode.safe(state.iconMode).logName(), state.iconSearch);
            refresh.run();
        }, focused -> state.iconSearchFocused = focused);

        if (itemModelPicker) {
            IconPickerMode[] cycle = IconPickerMode.modelItemCycle();
            TabletCycleButton.addIconModeButton(
                    modal,
                    gridX,
                    headY,
                    modeW,
                    headH,
                    cycle.length,
                    () -> IconPickerMode.cycleIndex(state.iconMode, cycle),
                    index -> IconPickerMode.iconAt(cycle, index),
                    mode.tooltip(),
                    direction -> {
                        IconPickerMode.cycleModelItems(state, direction);
                        QuestsAndStuffMod.debugLog("[QnS:UI] icon picker mode={} direction={}", IconPickerMode.safe(state.iconMode).logName(), cycleDirectionName(direction));
                        refresh.run();
                    });
        } else if (!entityPicker) {
            IconPickerMode[] cycle = IconPickerMode.cycleForContext(supportsEntityIcons, supportsInventoryIcons, useItemPicker);
            TabletCycleButton.addIconModeButton(
                    modal,
                    gridX,
                    headY,
                    modeW,
                    headH,
                    cycle.length,
                    () -> IconPickerMode.cycleIndex(state.iconMode, cycle),
                    index -> IconPickerMode.iconAt(cycle, index),
                    mode.tooltip(),
                    direction -> {
                        IconPickerMode.cycle(state, supportsEntityIcons, supportsInventoryIcons, useItemPicker, direction);
                        QuestsAndStuffMod.debugLog("[QnS:UI] icon picker mode={} direction={}", IconPickerMode.safe(state.iconMode).logName(), cycleDirectionName(direction));
                        refresh.run();
                    });
        }

        boolean pickingEntityIcons = mode.showingEntities();
        boolean pickingInventoryIcons = supportsInventoryIcons && mode.showingInventory() && !entityPicker && !itemModelPicker;
        boolean pickingFluidIcons = mode.showingFluids() && !entityPicker && !itemModelPicker;
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
                    TabletVocabulary.text(QuestVocabulary.NO_INVENTORY_ITEMS),
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
                    ? searchableModelItemEntries(state.iconSearch, mode.showingTags())
                    : pickingEntityIcons
                    ? EntityPreviewRenderer.searchableSpawnEggEntries(state.iconSearch)
                    : pickingFluidIcons
                    ? DisplayIconProvider.searchableFluidEntries(state.iconSearch)
                    : searchableIconEntries(state);
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

    private static List<String> searchableIconEntries(TabletUiState state) {
        IconPickerMode mode = IconPickerMode.safe(state.iconMode);
        if (mode.showingFluids()) {
            return DisplayIconProvider.searchableFluidEntries(state.iconSearch);
        }
        if (mode == IconPickerMode.USABLE_ITEMS) {
            return DisplayIconProvider.searchableUsableItemEntries(state.iconSearch);
        }
        return DisplayIconProvider.searchableEntries(state.iconSearch, mode.showingTags());
    }

    private static List<String> searchableModelItemEntries(String search, boolean tagMode) {
        return DisplayIconProvider.searchableEntries(search, tagMode).stream()
                .filter(entry -> !ModelAssetPreviewRenderer.itemAssetForPick(entry).isBlank())
                .toList();
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

    private static String cycleDirectionName(int direction) {
        return direction < 0 ? "backward" : "forward";
    }

    private static String pickedEntityIcon(String entry) {
        String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(entry);
        return entityId.isBlank() ? "" : EntityPreviewRenderer.entityAsset(entityId);
    }
}
