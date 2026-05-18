package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TabletModalState {
    private TabletModalState() {
    }

    static void applyModalFlags(TabletUiState state, ModalWindowManager.ModalFlags flags) {
        state.iconPickerOpen = flags.iconOpen();
        state.assetPickerOpen = flags.assetOpen();
        state.biomePickerOpen = flags.biomeOpen();
        state.lootTablePickerOpen = flags.lootTableOpen();
        state.colorPickerOpen = flags.colorOpen();
        state.themePickerOpen = flags.themeOpen();
        state.entityVariantPickerOpen = flags.entityVariantOpen();
    }

    public static void openModal(TabletUiState state, ModalWindowManager.ModalType type) {
        applyModalFlags(state, ModalWindowManager.open(type));
    }

    public static void closeAllModals(TabletUiState state) {
        boolean closingSoundPicker = state.modalQuestCompletionSoundTarget != null && !state.modalQuestCompletionSoundTarget.isBlank();
        applyModalFlags(state, ModalWindowManager.closeAll());
        state.modalQuestTarget = "";
        state.modalCanvasBackgroundTarget = "";
        state.modalCanvasImageTarget = "";
        state.modalCanvasEntityTarget = "";
        state.entityVariantTarget = "";
        state.entityVariantSelected = "";
        state.entityVariantFolder = "";
        state.entityVariantSearch = "";
        state.entityVariantSearchFocused = false;
        state.entityVariantScroll = 0;
        state.entityVariantScrollDragging = false;
        state.modalQuestCompletionSoundTarget = "";
        if (closingSoundPicker) {
            state.assetBrowseDir = "";
            state.assetSelected = "";
        }
        state.questDetailsPickTarget = "";
        state.questDetailsAssetPickTarget = "";
        state.assetContextOpen = false;
        state.assetRenameOpen = false;
        state.assetSearchFocused = false;
        state.assetGridScrollDragging = false;
        state.iconScrollDragging = false;
        state.iconSearchFocused = false;
        state.iconEntityMode = false;
        state.biomeSearchFocused = false;
        state.biomeScrollDragging = false;
        state.lootTableSearchFocused = false;
        state.lootTableScrollDragging = false;
        state.pickerLastClickKey = "";
        state.pickerLastClickAtMs = 0L;
        state.colorPaletteContextOpen = false;
        state.colorPaletteContextValue = Integer.MIN_VALUE;
        state.colorPaletteScrollDragging = false;
        state.themeScrollDragging = false;
    }
}
