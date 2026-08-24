package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.quest.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.SearchNormalizer;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState.openModal;

public final class ModalOpenActions {
    private ModalOpenActions() {
    }

    public static void openQuestDetailsIconPicker(TabletUiState state, String target) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = clean(target);
            resetIconPicker(state);
            IconPickerMode.resetForTarget(state, state.questDetails.questDetailsPickTarget);
        });
    }

    public static void openChapterIconPicker(TabletUiState state, String chapter) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalChapterTarget = clean(chapter);
            resetIconPicker(state);
        });
    }

    public static void openQuestIconPicker(TabletUiState state, String questId) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalQuestTarget = clean(questId);
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

    public static void openItemLockPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_LOCK_PICKER, () -> ModalPickerStates.itemLock(state).reset());
    }

    public static void openItemInventoryPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER, () -> ModalPickerStates.itemInventory(state).reset());
    }

    public static void openColorPicker(TabletUiState state, String target, int color) {
        openPickerModal(state, ModalWindowManager.ModalType.COLOR_PICKER, () -> {
            state.pickers.colorPickerTarget = clean(target);
            state.pickers.colorDraft = color;
            state.pickers.colorHexDraft = SearchNormalizer.toHexColor(color);
            state.pickers.colorPaletteContextOpen = false;
            state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            state.pickers.colorPaletteScrollDragging = false;
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    public static void openThemePicker(TabletUiState state) {
        openPickerModal(state, ModalWindowManager.ModalType.THEME_PICKER, () -> {
            state.modal.themeScrollDragging = false;
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    public static void openPrerequisitesManager(TabletUiState state, String questId) {
        openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.modal.prerequisitesManagerQuestId = clean(questId).trim();
            state.modal.prerequisitesManagerEcMode = false;
            ModalPickerStates.prerequisitesManager(state).reset();
            state.modal.prerequisitesManagerExternalMode = false;
            state.modal.prerequisitesManagerContextOpen = false;
            state.modal.prerequisitesManagerContextPrerequisiteId = "";
            state.modal.prerequisitesManagerSelectedConnectionKey = "";
            state.modal.prerequisitesManagerHoveredConnectionKey = "";
            ContextMenuController.clearDeleteConfirm(state);
            ContextMenuController.close(state);
        });
    }

    public static void openPrerequisitesManagerForEc(TabletUiState state, String ecId) {
        openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.modal.prerequisitesManagerQuestId = clean(ecId).trim();
            state.modal.prerequisitesManagerEcMode = true;
            ModalPickerStates.prerequisitesManager(state).reset();
            state.modal.prerequisitesManagerExternalMode = false;
            state.modal.prerequisitesManagerContextOpen = false;
            state.modal.prerequisitesManagerContextPrerequisiteId = "";
            state.modal.prerequisitesManagerSelectedConnectionKey = "";
            state.modal.prerequisitesManagerHoveredConnectionKey = "";
            ContextMenuController.clearDeleteConfirm(state);
            ContextMenuController.close(state);
        });
    }

    public static void openAssetPicker(TabletUiState state, String target) {
        openAssetPicker(state, target, "");
    }

    public static void openAssetPicker(TabletUiState state, String target, String selectedAsset) {
        openAssetPickerSession(state, selectedAsset, () -> state.questDetails.questDetailsAssetPickTarget = clean(target));
    }

    public static void openBlueprintPicker(TabletUiState state, String selectedBlueprint) {
        openAssetPickerSession(state, selectedBlueprint, () -> {
            state.modal.modalBlueprintTarget = "canvas";
            state.pickers.assetBrowseDir = "blueprints";
        });
    }

    public static void openQuestCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        openQuestCustomCompletionSoundPicker(state, questId, currentSound);
    }

    public static void openQuestGameSoundPicker(TabletUiState state, String questId, String currentSound) {
        openSoundPickerSession(state, currentSound, questId, () -> state.modal.modalQuestCompletionSoundTarget = clean(questId));
    }

    public static void openQuestCustomCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        openAssetSoundPickerSession(state, currentSound, questId, () -> state.modal.modalQuestCompletionSoundTarget = clean(questId));
    }

    public static void openBatchQuestGameSoundPicker(TabletUiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = normalizedTargets(questIds);
        openSoundPickerSession(state, currentSound, firstTarget(targets), () -> state.modal.modalQuestCompletionSoundTargets.addAll(targets));
    }

    public static void openBatchQuestCustomCompletionSoundPicker(TabletUiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = normalizedTargets(questIds);
        openAssetSoundPickerSession(state, currentSound, firstTarget(targets), () -> state.modal.modalQuestCompletionSoundTargets.addAll(targets));
    }

    public static void openChapterBackgroundPicker(TabletUiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalChapterTarget = clean(chapter));
    }

    public static void openQuestBackgroundPicker(TabletUiState state, String questId, String currentBackground, boolean grayscale) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalQuestBackgroundTarget = clean(questId);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openEcBackgroundPicker(TabletUiState state, String chapter, String ecId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalEcBackgroundTarget = chapter + ":" + ecId;
        });
    }

    public static void openBatchQuestBackgroundPicker(TabletUiState state, Collection<String> questIds, String currentBackground, boolean grayscale) {
        Set<String> targets = normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalQuestBackgroundTargets.addAll(targets);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudBackgroundPicker(TabletUiState state, String questId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalQuestCompletionHudBackgroundTarget = clean(questId));
    }

    public static void openBatchQuestCompletionHudBackgroundPicker(TabletUiState state, Collection<String> questIds, String currentBackground) {
        Set<String> targets = normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openCanvasBackgroundPicker(TabletUiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalCanvasBackgroundTarget = clean(chapter));
    }

    public static void openHudBackgroundPicker(TabletUiState state, String target, String currentBackground, int currentOpacity) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalHudBackgroundTarget = clean(target).trim();
            state.modal.modalHudBackgroundOpacityDraft = Math.max(0, Math.min(100, currentOpacity));
            state.modal.modalHudBackgroundOpacityDragging = false;
        });
    }

    public static void openCanvasImagePicker(TabletUiState state, String chapter, int logicalX, int logicalY) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalCanvasImageTarget = clean(chapter);
            setCanvasPickPoint(state, logicalX, logicalY);
        });
    }

    public static void openCanvasEntityPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasEntityTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetIconPicker(state);
            IconPickerMode.resetTo(state, IconPickerMode.ENTITIES);
        });
    }

    public static void openCanvasItemPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasModelTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetIconPicker(state);
            IconPickerMode.resetTo(state, IconPickerMode.ITEMS);
        });
    }

    public static void openCanvasBlockPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.BLOCK_PICKER, () -> {
            state.modal.modalCanvasModelTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetBlockPicker(state);
        });
    }

    public static void openCanvasRecipePicker(TabletUiState state, String target, int logicalX, int logicalY) {
        openPickerModal(state, ModalWindowManager.ModalType.RECIPE_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = clean(target);
            setCanvasPickPoint(state, logicalX, logicalY);
            resetRecipePicker(state);
        });
    }

    public static void openConnectionTexturePicker(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.connection(chapter, sourceQuestId, targetQuestId);
        });
    }

    public static void openConnectionTexturePicker(TabletUiState state, String target) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = target;
        });
    }

    public static void openChapterConnectionTexturePicker(TabletUiState state, String chapter, java.util.Collection<String> questIds) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.of(ModalTargets.CONNECTION, chapter, "", "");
            state.modal.modalConnectionTextureChapterTargets.addAll(questIds);
        });
    }

    public static void openEntityVariantPicker(TabletUiState state, String target, String icon) {
        openPickerModal(state, ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER, () -> {
            state.pickers.entityVariantTarget = clean(target);
            state.pickers.entityVariantSelected = EntityPreviewRenderer.entityVariant(clean(icon));
            state.pickers.entityVariantFolder = "";
            ModalPickerStates.entityVariant(state).reset();
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    private static void resetIconPicker(TabletUiState state) {
        ModalPickerStates.icon(state).reset();
        IconPickerMode.reset(state);
    }

    private static void resetAssetPicker(TabletUiState state) {
        state.pickers.assetContextOpen = false;
        state.pickers.assetRenameOpen = false;
        state.modal.blueprintCodeOpen = false;
        state.modal.blueprintCodeImportMode = false;
        state.modal.blueprintCodeTarget = "";
        state.modal.blueprintCodeDraft = "";
        state.modal.blueprintCodeMessage = "";
        state.pickers.assetBrowseDir = "";
        state.pickers.assetPickerSessionFresh = true;
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
        state.pickers.blockTagMode = false;
    }

    private static void openQuestDetailsPicker(TabletUiState state, String target, ModalWindowManager.ModalType type, Runnable reset) {
        openPickerModal(state, type, () -> {
            state.questDetails.questDetailsPickTarget = clean(target);
            reset.run();
        });
    }

    private static void openAssetPickerSession(TabletUiState state, String selectedAsset, Runnable configure) {
        openPickerModal(state, ModalWindowManager.ModalType.ASSET_PICKER, () -> {
            resetAssetPicker(state);
            configure.run();
            state.pickers.assetSelected = clean(selectedAsset);
        });
    }

    private static void openSoundPickerSession(TabletUiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        openPickerModal(state, ModalWindowManager.ModalType.SOUND_PICKER, () -> {
            resetSoundPicker(state);
            configureTargets.run();
            ContextMenuController.clearDeleteConfirm(state);
            state.pickers.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.pickers.soundVolumeDragging = false;
            state.pickers.soundSelected = clean(currentSound).isBlank() || QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound;
        });
    }

    private static void openAssetSoundPickerSession(TabletUiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        openAssetPickerSession(state, clean(currentSound).isBlank() || !QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound, () -> {
            configureTargets.run();
            ContextMenuController.clearDeleteConfirm(state);
            state.pickers.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.pickers.soundVolumeDragging = false;
            state.pickers.assetBrowseDir = "sounds";
        });
    }

    private static void openPickerModal(TabletUiState state, ModalWindowManager.ModalType type, Runnable configure) {
        closeBeforeOpen(state);
        clearModalOpenTargets(state);
        configure.run();
        openModal(state, type);
    }

    private static void clearModalOpenTargets(TabletUiState state) {
        state.modal.modalQuestTarget = "";
        state.modal.modalChapterTarget = "";
        state.questDetails.questDetailsPickTarget = "";
        state.questDetails.questDetailsAssetPickTarget = "";
        state.modal.modalCanvasBackgroundTarget = "";
        state.modal.modalCanvasImageTarget = "";
        state.modal.modalCanvasEntityTarget = "";
        state.modal.modalCanvasModelTarget = "";
        state.modal.modalBlueprintTarget = "";
        state.modal.modalQuestBackgroundTarget = "";
        state.modal.modalQuestBackgroundTargets.clear();
        state.modal.modalQuestBackgroundGrayscale = false;
        state.modal.modalQuestCompletionHudBackgroundTarget = "";
        state.modal.modalQuestCompletionHudBackgroundTargets.clear();
        state.modal.modalEcBackgroundTarget = "";
        state.modal.modalHudBackgroundTarget = "";
        state.modal.modalHudBackgroundOpacityDragging = false;
        state.modal.modalQuestCompletionSoundTarget = "";
        state.modal.modalQuestCompletionSoundTargets.clear();
        state.modal.modalConnectionTextureTarget = "";
        state.modal.modalConnectionTextureChapterTargets.clear();
        state.pickers.entityVariantTarget = "";
        state.pickers.entityVariantSelected = "";
        state.pickers.entityVariantFolder = "";
        state.pickers.colorPickerTarget = "";
        state.modal.prerequisitesManagerQuestId = "";
    }

    private static void setCanvasPickPoint(TabletUiState state, int logicalX, int logicalY) {
        state.canvas.canvasImageLogicalX = logicalX;
        state.canvas.canvasImageLogicalY = logicalY;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static int completionSoundVolume(String questId) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
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
        ContextMenuController.close(state);
        state.questDetails.questDetailsContextOpen = false;
        state.questDetails.questDetailsContextKind = "";
        state.questDetails.questDetailsContextId = "";
        state.questDetails.questDetailsContextScroll = 0;
        state.questDetails.questDetailsContextScrollMax = 0;
        state.modal.prerequisitesManagerContextOpen = false;
        state.pickers.assetContextOpen = false;
        state.pickers.colorPaletteContextOpen = false;
    }
}
