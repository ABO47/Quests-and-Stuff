package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.modal.IconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.modal.RecipePickerMode;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TabletModalState {
    private static final int POINTER_SOURCE_SIZE = 16;
    private static final long POINTER_SOURCE_MAX_AGE_MS = 800L;

    private TabletModalState() {
    }

    static void applyModalType(TabletUiState state, ModalWindowManager.ModalType type) {
        ModalWindowManager.ModalType safeType = type == null ? ModalWindowManager.ModalType.NONE : type;
        state.modalSession = ModalSession.capture(safeType, state);
    }

    public static void openModal(TabletUiState state, ModalWindowManager.ModalType type) {
        if (state == null) {
            return;
        }
        state.modalWindowClosing = false;
        applyModalType(state, type);
        startOpenAnimation(state, type);
    }

    public static void closeAllModals(TabletUiState state) {
        if (state == null || state.modalWindowClosing) {
            return;
        }
        ModalWindowManager.ModalType type = ModalStateQueries.activeType(state);
        if (type == ModalWindowManager.ModalType.NONE) {
            finishCloseAllModals(state);
            return;
        }
        clearModalInteractionState(state);
        if (!QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            finishCloseAllModals(state);
            return;
        }
        state.modalWindowClosing = true;
        state.modalWindowAnimationStartMs = System.currentTimeMillis();
        QuestsAndStuffMod.debugLog("[QnS:UI] modal close start type={}", type);
    }

    public static void closeAllModalsImmediately(TabletUiState state) {
        if (state == null) {
            return;
        }
        finishCloseAllModals(state);
    }

    public static boolean finishClosingIfDone(TabletUiState state) {
        if (state == null || !state.modalWindowClosing) {
            return false;
        }
        if (QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                && SourceOriginRevealWidget.windowRunning(state.modalWindowAnimationStartMs)) {
            return false;
        }
        ModalWindowManager.ModalType type = ModalStateQueries.activeType(state);
        finishCloseAllModals(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] modal close finish type={}", type);
        return true;
    }

    public static void rememberPointerSource(TabletUiState state, int x, int y) {
        if (state == null) {
            return;
        }
        state.modalWindowLastPointerX = x;
        state.modalWindowLastPointerY = y;
        state.modalWindowLastPointerAtMs = System.currentTimeMillis();
    }

    private static void startOpenAnimation(TabletUiState state, ModalWindowManager.ModalType type) {
        if (!QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            clearAnimationState(state);
            return;
        }
        long now = System.currentTimeMillis();
        state.modalWindowAnimationStartMs = now;
        capturePointerSource(state, now);
        QuestsAndStuffMod.debugLog("[QnS:UI] modal open type={} source={} x={} y={} w={} h={}",
                type,
                state.modalWindowAnimationHasSource,
                state.modalWindowAnimationSourceX,
                state.modalWindowAnimationSourceY,
                state.modalWindowAnimationSourceW,
                state.modalWindowAnimationSourceH);
    }

    private static void capturePointerSource(TabletUiState state, long now) {
        boolean recentPointer = state.modalWindowLastPointerAtMs > 0L
                && now - state.modalWindowLastPointerAtMs <= POINTER_SOURCE_MAX_AGE_MS;
        boolean insideRoot = state.modalWindowLastPointerX >= 0
                && state.modalWindowLastPointerY >= 0
                && state.modalWindowLastPointerX <= TabletUiFactory.rootWidth(state)
                && state.modalWindowLastPointerY <= TabletUiFactory.rootHeight(state);
        if (!recentPointer || !insideRoot) {
            state.modalWindowAnimationHasSource = false;
            state.modalWindowAnimationSourceX = 0;
            state.modalWindowAnimationSourceY = 0;
            state.modalWindowAnimationSourceW = 0;
            state.modalWindowAnimationSourceH = 0;
            return;
        }
        int half = POINTER_SOURCE_SIZE / 2;
        int rootW = TabletUiFactory.rootWidth(state);
        int rootH = TabletUiFactory.rootHeight(state);
        state.modalWindowAnimationHasSource = true;
        state.modalWindowAnimationSourceX = Math.max(0, Math.min(rootW - POINTER_SOURCE_SIZE, state.modalWindowLastPointerX - half));
        state.modalWindowAnimationSourceY = Math.max(0, Math.min(rootH - POINTER_SOURCE_SIZE, state.modalWindowLastPointerY - half));
        state.modalWindowAnimationSourceW = POINTER_SOURCE_SIZE;
        state.modalWindowAnimationSourceH = POINTER_SOURCE_SIZE;
    }

    private static void finishCloseAllModals(TabletUiState state) {
        boolean closingSoundPicker = state.modalQuestCompletionSoundTarget != null && !state.modalQuestCompletionSoundTarget.isBlank()
                || !state.modalQuestCompletionSoundTargets.isEmpty();
        applyModalType(state, ModalWindowManager.ModalType.NONE);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
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
        state.entityVariantTarget = "";
        state.entityVariantSelected = "";
        state.entityVariantFolder = "";
        state.entityVariantSearch = "";
        state.entityVariantSearchFocused = false;
        state.entityVariantScroll = 0;
        state.entityVariantScrollDragging = false;
        state.modalQuestCompletionSoundTarget = "";
        state.modalQuestCompletionSoundTargets.clear();
        if (closingSoundPicker) {
            state.assetBrowseDir = "";
            state.assetSelected = "";
        }
        state.questDetailsPickTarget = "";
        state.questDetailsAssetPickTarget = "";
        state.assetContextOpen = false;
        state.assetRenameOpen = false;
        clearBlueprintCodeState(state);
        state.assetSearchFocused = false;
        state.assetGridScrollDragging = false;
        clearPrerequisitesManagerState(state);
        state.modalHudBackgroundOpacityDragging = false;
        state.iconScrollDragging = false;
        state.iconSearchFocused = false;
        IconPickerMode.reset(state);
        state.biomeSearchFocused = false;
        state.biomeScrollDragging = false;
        state.advancementSearchFocused = false;
        state.advancementScrollDragging = false;
        state.recipeSearchFocused = false;
        RecipePickerMode.reset(state);
        state.recipeScrollDragging = false;
        state.structureSearchFocused = false;
        state.structureScrollDragging = false;
        state.blockSearchFocused = false;
        state.blockTagMode = false;
        state.blockScrollDragging = false;
        state.statSearchFocused = false;
        state.statScrollDragging = false;
        state.dimensionSearchFocused = false;
        state.dimensionScrollDragging = false;
        state.lootTableSearchFocused = false;
        state.lootTableScrollDragging = false;
        state.itemInventorySearchFocused = false;
        state.itemInventoryScrollDragging = false;
        state.soundSearchFocused = false;
        state.soundScrollDragging = false;
        state.soundSelected = "";
        state.pickerLastClickKey = "";
        state.pickerLastClickAtMs = 0L;
        state.colorPaletteContextOpen = false;
        state.colorPaletteContextValue = Integer.MIN_VALUE;
        state.colorPaletteScrollDragging = false;
        state.themeScrollDragging = false;
        state.settingsScrollDragging = false;
        clearAnimationState(state);
    }

    private static void clearModalInteractionState(TabletUiState state) {
        state.assetContextOpen = false;
        state.assetRenameOpen = false;
        clearBlueprintCodeState(state);
        state.assetSearchFocused = false;
        state.assetGridScrollDragging = false;
        state.prerequisitesManagerScrollDragging = false;
        state.prerequisitesManagerContextOpen = false;
        state.prerequisitesManagerSearchFocused = false;
        state.prerequisitesManagerHoveredConnectionKey = "";
        state.modalHudBackgroundOpacityDragging = false;
        state.iconScrollDragging = false;
        state.iconSearchFocused = false;
        IconPickerMode.reset(state);
        state.biomeSearchFocused = false;
        state.biomeScrollDragging = false;
        state.advancementSearchFocused = false;
        state.advancementScrollDragging = false;
        state.recipeSearchFocused = false;
        RecipePickerMode.reset(state);
        state.recipeScrollDragging = false;
        state.structureSearchFocused = false;
        state.structureScrollDragging = false;
        state.blockSearchFocused = false;
        state.blockTagMode = false;
        state.blockScrollDragging = false;
        state.statSearchFocused = false;
        state.statScrollDragging = false;
        state.dimensionSearchFocused = false;
        state.dimensionScrollDragging = false;
        state.lootTableSearchFocused = false;
        state.lootTableScrollDragging = false;
        state.itemInventorySearchFocused = false;
        state.itemInventoryScrollDragging = false;
        state.soundSearchFocused = false;
        state.soundScrollDragging = false;
        state.entityVariantSearchFocused = false;
        state.entityVariantScrollDragging = false;
        state.pickerLastClickKey = "";
        state.pickerLastClickAtMs = 0L;
        state.colorPaletteContextOpen = false;
        state.colorPaletteContextValue = Integer.MIN_VALUE;
        state.colorPaletteScrollDragging = false;
        state.themeScrollDragging = false;
        state.settingsScrollDragging = false;
    }

    private static void clearPrerequisitesManagerState(TabletUiState state) {
        state.prerequisitesManagerQuestId = "";
        state.prerequisitesManagerSearch = "";
        state.prerequisitesManagerSearchFocused = false;
        state.prerequisitesManagerExternalMode = false;
        state.prerequisitesManagerScroll = 0;
        state.prerequisitesManagerScrollDragging = false;
        state.prerequisitesManagerContextOpen = false;
        state.prerequisitesManagerContextPrerequisiteId = "";
        state.prerequisitesManagerSelectedConnectionKey = "";
        state.prerequisitesManagerHoveredConnectionKey = "";
        state.prerequisitesManagerContextX = 0;
        state.prerequisitesManagerContextY = 0;
        state.prerequisitesManagerContextMenuX = 0;
        state.prerequisitesManagerContextMenuY = 0;
        state.prerequisitesManagerContextMenuW = 0;
        state.prerequisitesManagerContextMenuH = 0;
    }

    private static void clearBlueprintCodeState(TabletUiState state) {
        state.blueprintCodeOpen = false;
        state.blueprintCodeImportMode = false;
        state.blueprintCodeAnimationStartMs = 0L;
        state.blueprintCodeTarget = "";
        state.blueprintCodeDraft = "";
        state.blueprintCodeMessage = "";
    }

    private static void clearAnimationState(TabletUiState state) {
        state.modalWindowClosing = false;
        state.modalWindowAnimationStartMs = 0L;
        state.modalWindowAnimationHasSource = false;
        state.modalWindowAnimationSourceX = 0;
        state.modalWindowAnimationSourceY = 0;
        state.modalWindowAnimationSourceW = 0;
        state.modalWindowAnimationSourceH = 0;
    }
}
