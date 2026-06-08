package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.quest.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.SearchFieldController;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletModalState.openModal;

public final class ModalOpenActions {
    private ModalOpenActions() {
    }

    public static void openQuestDetailsIconPicker(TabletUiState state, String target) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.questDetailsPickTarget = clean(target);
            resetIconPicker(state);
            IconPickerMode.resetForTarget(state, state.questDetailsPickTarget);
        });
    }

    public static void openChapterIconPicker(TabletUiState state, String chapter) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modalChapterTarget = clean(chapter);
            resetIconPicker(state);
        });
    }

    public static void openQuestIconPicker(TabletUiState state, String questId) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modalQuestTarget = clean(questId);
            resetIconPicker(state);
        });
    }

    public static void openBiomePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.BIOME_PICKER, () -> ModalPickerStates.biome(state).reset());
    }

    public static void openAdvancementPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ADVANCEMENT_PICKER, () -> ModalPickerStates.advancement(state).reset());
    }

    public static void openRecipePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.RECIPE_PICKER, () -> resetRecipePicker(state));
    }

    public static void openStructurePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.STRUCTURE_PICKER, () -> ModalPickerStates.structure(state).reset());
    }

    public static void openBlockPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.BLOCK_PICKER, () -> resetBlockPicker(state));
    }

    public static void openStatPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.STAT_PICKER, () -> ModalPickerStates.stat(state).reset());
    }

    public static void openDimensionPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.DIMENSION_PICKER, () -> ModalPickerStates.dimension(state).reset());
    }

    public static void openLootTablePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.LOOT_TABLE_PICKER, () -> ModalPickerStates.lootTable(state).reset());
    }

    public static void openItemInventoryPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER, () -> ModalPickerStates.itemInventory(state).reset());
    }

    public static void openColorPicker(TabletUiState state, String target, int color) {
        openPickerModal(state, ModalWindowManager.ModalType.COLOR_PICKER, () -> {
            state.colorPickerTarget = clean(target);
            state.colorDraft = color;
            state.colorHexDraft = SearchFieldController.toHexColor(color);
            state.colorPaletteContextOpen = false;
            state.colorPaletteContextValue = Integer.MIN_VALUE;
            state.colorPaletteScrollDragging = false;
            ContextMenuState.clearDeleteConfirm(state);
        });
    }

    public static void openThemePicker(TabletUiState state) {
        openPickerModal(state, ModalWindowManager.ModalType.THEME_PICKER, () -> {
            state.themeScrollDragging = false;
            ContextMenuState.clearDeleteConfirm(state);
        });
    }

    public static void openSettingsPanel(TabletUiState state) {
        openPickerModal(state, ModalWindowManager.ModalType.SETTINGS_PANEL, () -> {
            state.settingsTab = 0;
            state.settingsScroll = 0;
            state.settingsScrollDragging = false;
            ContextMenuState.clearDeleteConfirm(state);
        });
    }

    public static void openPrerequisitesManager(TabletUiState state, String questId) {
        openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.prerequisitesManagerQuestId = clean(questId).trim();
            ModalPickerStates.prerequisitesManager(state).reset();
            state.prerequisitesManagerExternalMode = false;
            state.prerequisitesManagerContextOpen = false;
            state.prerequisitesManagerContextPrerequisiteId = "";
            state.prerequisitesManagerSelectedConnectionKey = "";
            state.prerequisitesManagerHoveredConnectionKey = "";
            ContextMenuState.clearDeleteConfirm(state);
            ContextMenuState.close(state);
        });
    }

    public static void openAssetPicker(TabletUiState state, String target) {
        openAssetPicker(state, target, "");
    }

    public static void openAssetPicker(TabletUiState state, String target, String selectedAsset) {
        openAssetPickerSession(state, selectedAsset, () -> state.questDetailsAssetPickTarget = clean(target));
    }

    public static void openBlueprintPicker(TabletUiState state, String selectedBlueprint) {
        openAssetPickerSession(state, selectedBlueprint, () -> {
            state.modalBlueprintTarget = "canvas";
            state.assetBrowseDir = "blueprints";
        });
    }

    public static void openQuestCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        openQuestCustomCompletionSoundPicker(state, questId, currentSound);
    }

    public static void openQuestGameSoundPicker(TabletUiState state, String questId, String currentSound) {
        openSoundPickerSession(state, currentSound, questId, () -> state.modalQuestCompletionSoundTarget = clean(questId));
    }

    public static void openQuestCustomCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        openAssetSoundPickerSession(state, currentSound, questId, () -> state.modalQuestCompletionSoundTarget = clean(questId));
    }

    public static void openBatchQuestGameSoundPicker(TabletUiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = normalizedTargets(questIds);
        openSoundPickerSession(state, currentSound, firstTarget(targets), () -> state.modalQuestCompletionSoundTargets.addAll(targets));
    }

    public static void openBatchQuestCustomCompletionSoundPicker(TabletUiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = normalizedTargets(questIds);
        openAssetSoundPickerSession(state, currentSound, firstTarget(targets), () -> state.modalQuestCompletionSoundTargets.addAll(targets));
    }

    public static void openChapterBackgroundPicker(TabletUiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modalChapterTarget = clean(chapter));
    }

    public static void openQuestBackgroundPicker(TabletUiState state, String questId, String currentBackground, boolean grayscale) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modalQuestBackgroundTarget = clean(questId);
            state.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openBatchQuestBackgroundPicker(TabletUiState state, Collection<String> questIds, String currentBackground, boolean grayscale) {
        Set<String> targets = normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> {
            state.modalQuestBackgroundTargets.addAll(targets);
            state.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudBackgroundPicker(TabletUiState state, String questId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modalQuestCompletionHudBackgroundTarget = clean(questId));
    }

    public static void openBatchQuestCompletionHudBackgroundPicker(TabletUiState state, Collection<String> questIds, String currentBackground) {
        Set<String> targets = normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> state.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openCanvasBackgroundPicker(TabletUiState state, String group, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modalCanvasBackgroundTarget = clean(group));
    }

    public static void openHudBackgroundPicker(TabletUiState state, String target, String currentBackground, int currentOpacity) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modalHudBackgroundTarget = clean(target).trim();
            state.modalHudBackgroundOpacityDraft = Math.max(0, Math.min(100, currentOpacity));
            state.modalHudBackgroundOpacityDragging = false;
        });
    }

    public static void openCanvasImagePicker(TabletUiState state, String group, int logicalX, int logicalY) {
        openAssetPickerSession(state, "", () -> {
            state.modalCanvasImageTarget = clean(group);
            setCanvasPickPoint(state, logicalX, logicalY);
        });
    }

    public static void openCanvasEntityPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modalCanvasEntityTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetIconPicker(state);
            IconPickerMode.resetTo(state, IconPickerMode.ENTITIES);
        });
    }

    public static void openCanvasItemPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modalCanvasModelTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetIconPicker(state);
            IconPickerMode.resetTo(state, IconPickerMode.ITEMS);
        });
    }

    public static void openCanvasBlockPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.BLOCK_PICKER, () -> {
            state.modalCanvasModelTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetBlockPicker(state);
        });
    }

    public static void openCanvasRecipePicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.RECIPE_PICKER, () -> {
            state.questDetailsPickTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetRecipePicker(state);
        });
    }

    public static void openEntityVariantPicker(TabletUiState state, String target, String icon) {
        openPickerModal(state, ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER, () -> {
            state.entityVariantTarget = clean(target);
            state.entityVariantSelected = EntityPreviewRenderer.entityVariant(clean(icon));
            state.entityVariantFolder = "";
            ModalPickerStates.entityVariant(state).reset();
            ContextMenuState.clearDeleteConfirm(state);
        });
    }

    private static void resetIconPicker(TabletUiState state) {
        ModalPickerStates.icon(state).reset();
        IconPickerMode.reset(state);
    }

    private static void resetAssetPicker(TabletUiState state) {
        state.assetContextOpen = false;
        state.assetRenameOpen = false;
        state.blueprintCodeOpen = false;
        state.blueprintCodeImportMode = false;
        state.blueprintCodeTarget = "";
        state.blueprintCodeDraft = "";
        state.blueprintCodeMessage = "";
        state.assetBrowseDir = "";
        ModalPickerStates.asset(state).reset();
    }

    private static void resetSoundPicker(TabletUiState state) {
        ModalPickerStates.sound(state).reset();
    }

    private static void resetRecipePicker(TabletUiState state) {
        ModalPickerStates.recipe(state).reset();
        RecipePickerMode.reset(state);
    }

    private static void resetBlockPicker(TabletUiState state) {
        ModalPickerStates.block(state).reset();
        state.blockTagMode = false;
    }

    private static void openQuestDetailsPicker(TabletUiState state, String target, ModalWindowManager.ModalType type, Runnable reset) {
        openPickerModal(state, type, () -> {
            state.questDetailsPickTarget = clean(target);
            reset.run();
        });
    }

    private static void openAssetPickerSession(TabletUiState state, String selectedAsset, Runnable configure) {
        openPickerModal(state, ModalWindowManager.ModalType.ASSET_PICKER, () -> {
            resetAssetPicker(state);
            configure.run();
            state.assetSelected = clean(selectedAsset);
        });
    }

    private static void openSoundPickerSession(TabletUiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        openPickerModal(state, ModalWindowManager.ModalType.SOUND_PICKER, () -> {
            resetSoundPicker(state);
            configureTargets.run();
            ContextMenuState.clearDeleteConfirm(state);
            state.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.soundVolumeDragging = false;
            state.soundSelected = clean(currentSound).isBlank() || QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound;
        });
    }

    private static void openAssetSoundPickerSession(TabletUiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        openAssetPickerSession(state, clean(currentSound).isBlank() || !QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound, () -> {
            configureTargets.run();
            ContextMenuState.clearDeleteConfirm(state);
            state.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.soundVolumeDragging = false;
            state.assetBrowseDir = "sounds";
        });
    }

    private static void openPickerModal(TabletUiState state, ModalWindowManager.ModalType type, Runnable configure) {
        closeBeforeOpen(state);
        clearModalOpenTargets(state);
        configure.run();
        openModal(state, type);
    }

    private static void clearModalOpenTargets(TabletUiState state) {
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = "";
        state.questDetailsAssetPickTarget = "";
        state.modalCanvasBackgroundTarget = "";
        state.modalCanvasImageTarget = "";
        state.modalCanvasEntityTarget = "";
        state.modalCanvasModelTarget = "";
        state.modalBlueprintTarget = "";
        state.modalQuestBackgroundTarget = "";
        state.modalQuestBackgroundTargets.clear();
        state.modalQuestBackgroundGrayscale = false;
        state.modalQuestCompletionHudBackgroundTarget = "";
        state.modalQuestCompletionHudBackgroundTargets.clear();
        state.modalHudBackgroundTarget = "";
        state.modalHudBackgroundOpacityDragging = false;
        state.modalQuestCompletionSoundTarget = "";
        state.modalQuestCompletionSoundTargets.clear();
        state.entityVariantTarget = "";
        state.entityVariantSelected = "";
        state.entityVariantFolder = "";
        state.colorPickerTarget = "";
        state.prerequisitesManagerQuestId = "";
    }

    private static void setCanvasPickPoint(TabletUiState state, int logicalX, int logicalY) {
        state.canvasImageLogicalX = logicalX;
        state.canvasImageLogicalY = logicalY;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static int completionSoundVolume(String questId) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        if (quest == null || !quest.contains("completion_sound_volume")) {
            return QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME;
        }
        return QuestDisplay.normalizeCompletionSoundVolume(quest.getInt("completion_sound_volume"));
    }

    private static Set<String> normalizedTargets(Collection<String> questIds) {
        Set<String> targets = new LinkedHashSet<>();
        if (questIds == null) {
            return targets;
        }
        for (String questId : questIds) {
            String target = questId == null ? "" : questId.trim();
            if (!target.isBlank()) {
                targets.add(target);
            }
        }
        return targets;
    }

    private static String firstTarget(Set<String> targets) {
        return targets.isEmpty() ? "" : targets.iterator().next();
    }

    private static void closeBeforeOpen(TabletUiState state) {
        ModalCloseActions.closeAllImmediately(state);
        ContextMenuState.close(state);
        state.questDetailsContextOpen = false;
        state.questDetailsContextKind = "";
        state.questDetailsContextId = "";
        state.questDetailsContextScroll = 0;
        state.questDetailsContextScrollMax = 0;
        state.prerequisitesManagerContextOpen = false;
        state.assetContextOpen = false;
        state.colorPaletteContextOpen = false;
    }
}
