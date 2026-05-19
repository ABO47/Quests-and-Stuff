package com.abo47.questsandstuff.client.tablet.modal.panel;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.TabletAssetPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletBiomePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletColorPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletEntityVariantModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletIconPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletLootTablePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletSettingsModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletThemePickerModal;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class ModalPanelRouter {
    private ModalPanelRouter() {
    }

    public static void rebuildChapterModal(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!ModalStateQueries.anyOpen(state)) {
            return;
        }
        TextFieldWidget iconSearchField = null;
        TextFieldWidget assetSearchField = null;
        TextFieldWidget biomeSearchField = null;
        TextFieldWidget lootTableSearchField = null;
        TextFieldWidget entityVariantSearchField = null;
        int w = Math.min(432, overlay.getSize().width - 32);
        int h = Math.min(260, overlay.getSize().height - 32);
        int mx = (overlay.getSize().width - w) / 2;
        int my = (overlay.getSize().height - h) / 2;
        WidgetGroup dim = new WidgetGroup(0, 0, overlay.getSize().width, overlay.getSize().height);
        dim.setBackground(Surfaces.fill(TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 140)));
        overlay.addWidget(dim);
        WidgetGroup modal = TabletUiFactory.panel(mx, my, w, h, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 252), ModColors.BORDER_ACCENT);
        if (state.iconPickerOpen) {
            iconSearchField = TabletIconPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.assetPickerOpen) {
            assetSearchField = TabletAssetPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.biomePickerOpen) {
            biomeSearchField = TabletBiomePickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.lootTablePickerOpen) {
            lootTableSearchField = TabletLootTablePickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.colorPickerOpen) {
            TabletColorPickerModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.themePickerOpen) {
            TabletThemePickerModal.rebuild(modal, state, refresh, w, h);
        } else if (state.entityVariantPickerOpen) {
            entityVariantSearchField = TabletEntityVariantModal.rebuild(modal, state, player, refresh, w, h);
        } else if (state.settingsPanelOpen) {
            TabletSettingsModal.rebuild(modal, state, refresh, w, h);
        }
        overlay.addWidget(modal);
        restoreSearchFocus(state, iconSearchField, assetSearchField, biomeSearchField, lootTableSearchField, entityVariantSearchField);
    }

    private static void restoreSearchFocus(
            TabletUiState state,
            TextFieldWidget iconSearchField,
            TextFieldWidget assetSearchField,
            TextFieldWidget biomeSearchField,
            TextFieldWidget lootTableSearchField,
            TextFieldWidget entityVariantSearchField
    ) {
        if (iconSearchField != null && state.iconSearchFocused) {
            iconSearchField.setFocus(true);
        }
        if (assetSearchField != null && state.assetSearchFocused) {
            assetSearchField.setFocus(true);
        }
        if (biomeSearchField != null && state.biomeSearchFocused) {
            biomeSearchField.setFocus(true);
        }
        if (lootTableSearchField != null && state.lootTableSearchFocused) {
            lootTableSearchField.setFocus(true);
        }
        if (entityVariantSearchField != null && state.entityVariantSearchFocused) {
            entityVariantSearchField.setFocus(true);
        }
    }
}
