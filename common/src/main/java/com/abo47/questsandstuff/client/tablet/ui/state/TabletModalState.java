package com.abo47.questsandstuff.client.tablet.ui.state;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.modal.IconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.modal.RecipePickerMode;
import com.abo47.questsandstuff.client.tablet.animation.TabletAnimationTimings;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TabletModalState {
    private static final int POINTER_SOURCE_SIZE = GRID_16;
    private static final long POINTER_SOURCE_MAX_AGE_MS = TabletAnimationTimings.POINTER_SOURCE_MAX_AGE_MS;

    private TabletModalState() {
    }

    public static void applyModalType(TabletUiState state, ModalWindowManager.ModalType type) {
        ModalWindowManager.ModalType safeType = type == null ? ModalWindowManager.ModalType.NONE : type;
        state.modal.modalSession = ModalSession.capture(safeType, state);
    }

    public static void openModal(TabletUiState state, ModalWindowManager.ModalType type) {
        if (state == null) {
            return;
        }
        state.modal.modalWindowClosing = false;
        applyModalType(state, type);
        startOpenAnimation(state, type);
    }

    public static void closeAllModals(TabletUiState state) {
        if (state == null || state.modal.modalWindowClosing) {
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
        state.modal.modalWindowClosing = true;
        state.modal.modalWindowAnimationStartMs = System.currentTimeMillis();
        QuestsAndStuffMod.debugLog("[QnS:UI] modal close start type={}", type);
    }

    public static void closeAllModalsImmediately(TabletUiState state) {
        if (state == null) {
            return;
        }
        finishCloseAllModals(state);
    }

    public static boolean finishClosingIfDone(TabletUiState state) {
        if (state == null || !state.modal.modalWindowClosing) {
            return false;
        }
        if (QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                && SourceOriginRevealWidget.windowRunning(state.modal.modalWindowAnimationStartMs)) {
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
        state.modal.modalWindowLastPointerX = x;
        state.modal.modalWindowLastPointerY = y;
        state.modal.modalWindowLastPointerAtMs = System.currentTimeMillis();
    }

    private static void startOpenAnimation(TabletUiState state, ModalWindowManager.ModalType type) {
        if (!QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            clearAnimationState(state);
            return;
        }
        long now = System.currentTimeMillis();
        state.modal.modalWindowAnimationStartMs = now;
        capturePointerSource(state, now);
        QuestsAndStuffMod.debugLog("[QnS:UI] modal open type={} source={} x={} y={} w={} h={}",
                type,
                state.modal.modalWindowAnimationHasSource,
                state.modal.modalWindowAnimationSourceX,
                state.modal.modalWindowAnimationSourceY,
                state.modal.modalWindowAnimationSourceW,
                state.modal.modalWindowAnimationSourceH);
    }

    private static void capturePointerSource(TabletUiState state, long now) {
        boolean recentPointer = state.modal.modalWindowLastPointerAtMs > 0L
                && now - state.modal.modalWindowLastPointerAtMs <= POINTER_SOURCE_MAX_AGE_MS;
        boolean insideRoot = state.modal.modalWindowLastPointerX >= 0
                && state.modal.modalWindowLastPointerY >= 0
                && state.modal.modalWindowLastPointerX <= TabletStateQueries.rootWidth(state)
                && state.modal.modalWindowLastPointerY <= TabletStateQueries.rootHeight(state);
        if (!recentPointer || !insideRoot) {
            state.modal.modalWindowAnimationHasSource = false;
            state.modal.modalWindowAnimationSourceX = 0;
            state.modal.modalWindowAnimationSourceY = 0;
            state.modal.modalWindowAnimationSourceW = 0;
            state.modal.modalWindowAnimationSourceH = 0;
            return;
        }
        int half = POINTER_SOURCE_SIZE / 2;
        int rootW = TabletStateQueries.rootWidth(state);
        int rootH = TabletStateQueries.rootHeight(state);
        state.modal.modalWindowAnimationHasSource = true;
        state.modal.modalWindowAnimationSourceX = Math.max(0, Math.min(rootW - POINTER_SOURCE_SIZE, state.modal.modalWindowLastPointerX - half));
        state.modal.modalWindowAnimationSourceY = Math.max(0, Math.min(rootH - POINTER_SOURCE_SIZE, state.modal.modalWindowLastPointerY - half));
        state.modal.modalWindowAnimationSourceW = POINTER_SOURCE_SIZE;
        state.modal.modalWindowAnimationSourceH = POINTER_SOURCE_SIZE;
    }

    private static void finishCloseAllModals(TabletUiState state) {
        boolean closingSoundPicker = state.modal.modalQuestCompletionSoundTarget != null && !state.modal.modalQuestCompletionSoundTarget.isBlank()
                || !state.modal.modalQuestCompletionSoundTargets.isEmpty();
        applyModalType(state, ModalWindowManager.ModalType.NONE);
        state.modal.modalQuestTarget = "";
        state.modal.modalChapterTarget = "";
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
        state.modal.modalHudBackgroundTarget = "";
        state.modal.modalHudBackgroundOpacityDragging = false;
        state.pickers.entityVariantTarget = "";
        state.pickers.entityVariantSelected = "";
        state.pickers.entityVariantFolder = "";
        state.pickers.entityVariantSearch = "";
        state.pickers.entityVariantSearchFocused = false;
        state.pickers.entityVariantScroll = 0;
        state.pickers.entityVariantScrollDragging = false;
        state.modal.modalQuestCompletionSoundTarget = "";
        state.modal.modalQuestCompletionSoundTargets.clear();
        state.pickers.saveBrowseDirForMode();
        if (closingSoundPicker) {
            state.pickers.assetBrowseDir = "";
            state.pickers.assetSelected = "";
        }
        state.questDetails.questDetailsPickTarget = "";
        state.questDetails.questDetailsAssetPickTarget = "";
        state.pickers.assetContextOpen = false;
        state.pickers.assetRenameOpen = false;
        clearBlueprintCodeState(state);
        state.pickers.assetSearchFocused = false;
        state.pickers.assetGridScrollDragging = false;
        clearPrerequisitesManagerState(state);
        state.modal.modalHudBackgroundOpacityDragging = false;
        state.pickers.iconScrollDragging = false;
        state.pickers.iconSearchFocused = false;
        IconPickerMode.reset(state);
        state.pickers.biomeSearchFocused = false;
        state.pickers.biomeScrollDragging = false;
        state.pickers.advancementSearchFocused = false;
        state.pickers.advancementScrollDragging = false;
        state.pickers.recipeSearchFocused = false;
        RecipePickerMode.reset(state);
        state.pickers.recipeScrollDragging = false;
        state.pickers.structureSearchFocused = false;
        state.pickers.structureScrollDragging = false;
        state.pickers.blockSearchFocused = false;
        state.pickers.blockTagMode = false;
        state.pickers.blockScrollDragging = false;
        state.pickers.statSearchFocused = false;
        state.pickers.statScrollDragging = false;
        state.pickers.dimensionSearchFocused = false;
        state.pickers.dimensionScrollDragging = false;
        state.pickers.lootTableSearchFocused = false;
        state.pickers.lootTableScrollDragging = false;
        state.pickers.itemInventorySearchFocused = false;
        state.pickers.itemInventoryScrollDragging = false;
        state.pickers.soundSearchFocused = false;
        state.pickers.soundScrollDragging = false;
        state.pickers.soundSelected = "";
        state.pickers.pickerLastClickKey = "";
        state.pickers.pickerLastClickAtMs = 0L;
        state.pickers.colorPaletteContextOpen = false;
        state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
        state.pickers.colorPaletteScrollDragging = false;
        state.modal.themeScrollDragging = false;
        clearAnimationState(state);
    }

    private static void clearModalInteractionState(TabletUiState state) {
        state.pickers.assetContextOpen = false;
        state.pickers.assetRenameOpen = false;
        clearBlueprintCodeState(state);
        state.pickers.assetSearchFocused = false;
        state.pickers.assetGridScrollDragging = false;
        state.modal.prerequisitesManagerScrollDragging = false;
        state.modal.prerequisitesManagerContextOpen = false;
        state.modal.prerequisitesManagerSearchFocused = false;
        state.modal.prerequisitesManagerHoveredConnectionKey = "";
        state.modal.modalHudBackgroundOpacityDragging = false;
        state.pickers.iconScrollDragging = false;
        state.pickers.iconSearchFocused = false;
        IconPickerMode.reset(state);
        state.pickers.biomeSearchFocused = false;
        state.pickers.biomeScrollDragging = false;
        state.pickers.advancementSearchFocused = false;
        state.pickers.advancementScrollDragging = false;
        state.pickers.recipeSearchFocused = false;
        RecipePickerMode.reset(state);
        state.pickers.recipeScrollDragging = false;
        state.pickers.structureSearchFocused = false;
        state.pickers.structureScrollDragging = false;
        state.pickers.blockSearchFocused = false;
        state.pickers.blockTagMode = false;
        state.pickers.blockScrollDragging = false;
        state.pickers.statSearchFocused = false;
        state.pickers.statScrollDragging = false;
        state.pickers.dimensionSearchFocused = false;
        state.pickers.dimensionScrollDragging = false;
        state.pickers.lootTableSearchFocused = false;
        state.pickers.lootTableScrollDragging = false;
        state.pickers.itemInventorySearchFocused = false;
        state.pickers.itemInventoryScrollDragging = false;
        state.pickers.soundSearchFocused = false;
        state.pickers.soundScrollDragging = false;
        state.pickers.entityVariantSearchFocused = false;
        state.pickers.entityVariantScrollDragging = false;
        state.pickers.pickerLastClickKey = "";
        state.pickers.pickerLastClickAtMs = 0L;
        state.pickers.colorPaletteContextOpen = false;
        state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
        state.pickers.colorPaletteScrollDragging = false;
        state.modal.themeScrollDragging = false;
    }

    private static void clearPrerequisitesManagerState(TabletUiState state) {
        state.modal.prerequisitesManagerQuestId = "";
        state.modal.prerequisitesManagerSearch = "";
        state.modal.prerequisitesManagerSearchFocused = false;
        state.modal.prerequisitesManagerExternalMode = false;
        state.modal.prerequisitesManagerScroll = 0;
        state.modal.prerequisitesManagerScrollDragging = false;
        state.modal.prerequisitesManagerContextOpen = false;
        state.modal.prerequisitesManagerContextPrerequisiteId = "";
        state.modal.prerequisitesManagerSelectedConnectionKey = "";
        state.modal.prerequisitesManagerHoveredConnectionKey = "";
        state.modal.prerequisitesManagerContextX = 0;
        state.modal.prerequisitesManagerContextY = 0;
        state.modal.prerequisitesManagerContextMenuX = 0;
        state.modal.prerequisitesManagerContextMenuY = 0;
        state.modal.prerequisitesManagerContextMenuW = 0;
        state.modal.prerequisitesManagerContextMenuH = 0;
    }

    private static void clearBlueprintCodeState(TabletUiState state) {
        state.modal.blueprintCodeOpen = false;
        state.modal.blueprintCodeImportMode = false;
        state.modal.blueprintCodeAnimationStartMs = 0L;
        state.modal.blueprintCodeTarget = "";
        state.modal.blueprintCodeDraft = "";
        state.modal.blueprintCodeMessage = "";
    }

    private static void clearAnimationState(TabletUiState state) {
        state.modal.modalWindowClosing = false;
        state.modal.modalWindowAnimationStartMs = 0L;
        state.modal.modalWindowAnimationHasSource = false;
        state.modal.modalWindowAnimationSourceX = 0;
        state.modal.modalWindowAnimationSourceY = 0;
        state.modal.modalWindowAnimationSourceW = 0;
        state.modal.modalWindowAnimationSourceH = 0;
    }
}
